package mx.com.fredkorlabs.registration;

import mx.com.fredkorlabs.registration.adapter.out.persistence.SupplierRegistrationJpaRepository;
import mx.com.fredkorlabs.registration.application.port.in.CleanUpUnassociatedFilesResult;
import mx.com.fredkorlabs.registration.application.port.in.CleanUpUnassociatedFilesUseCase;
import mx.com.fredkorlabs.registration.testsupport.DocumentsVolumeTestConfiguration;
import mx.com.fredkorlabs.registration.testsupport.PostgresTestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static mx.com.fredkorlabs.registration.testsupport.SignatureFixtures.identifyRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** ADR-0010 daily cleanup against PostgreSQL and the temporary documents volume. */
@Import({PostgresTestcontainersConfiguration.class, DocumentsVolumeTestConfiguration.class})
@SpringBootTest(classes = SupplierRegistrationApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class DailyFileCleanupIntegrationTest {

    private static final byte[] PDF = "%PDF-1.7\n%%EOF".getBytes(StandardCharsets.US_ASCII);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CleanUpUnassociatedFilesUseCase cleanUp;

    @Autowired
    private SupplierRegistrationJpaRepository registrations;

    @Autowired
    private JdbcTemplate jdbc;

    private record Session(String token, UUID registrationId) {}

    @AfterEach
    void removeRegistrations() {
        jdbc.update("DELETE FROM registration_fiscal_document");
        jdbc.update("DELETE FROM file_metadata");
        registrations.deleteAll();
    }

    private Session registration(String variant, String rfc) throws Exception {
        String acceptance = JsonMapper.shared().readTree(mockMvc.perform(post("/api/v1/registration-notice-acceptances")
                .contentType(MediaType.APPLICATION_JSON).content("{\"version\":\"1.0\"}"))
            .andReturn().getResponse().getContentAsString()).get("acceptanceId").asString();
        String token = JsonMapper.shared().readTree(mockMvc.perform(post("/api/v1/applicant-identifications")
                .contentType(MediaType.APPLICATION_JSON).content(identifyRequest(acceptance, variant, rfc)))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("registrationToken").asString();
        UUID registrationId = UUID.fromString(JsonMapper.shared().readTree(mockMvc.perform(
                post("/api/v1/supplier-registrations").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asString());
        return new Session(token, registrationId);
    }

    private UUID upload(Session session) throws Exception {
        return UUID.fromString(JsonMapper.shared().readTree(mockMvc.perform(
                multipart("/api/v1/supplier-registrations/" + session.registrationId() + "/fiscal-document")
                    .file(new MockMultipartFile("file", "cedula.pdf", "application/pdf", PDF))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + session.token()))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("id").asString());
    }

    private void dissociate(Session session) throws Exception {
        mockMvc.perform(delete("/api/v1/supplier-registrations/" + session.registrationId() + "/fiscal-document")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + session.token()))
            .andExpect(status().isNoContent());
    }

    private void uploadedAgo(UUID fileId, Duration age) {
        jdbc.update("UPDATE file_metadata SET uploaded_at = ? WHERE id = ?",
            Timestamp.from(Instant.now().minus(age)), fileId);
    }

    private Path content(UUID fileId) {
        return DocumentsVolumeTestConfiguration.ROOT.resolve(jdbc.queryForObject(
            "SELECT relative_path FROM file_metadata WHERE id = ?", String.class, fileId));
    }

    private boolean metadataExists(UUID fileId) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM file_metadata WHERE id = ?", Integer.class, fileId) == 1;
    }

    @Test
    void ADR0010_dailyCleanup_deletesOldDissociatedDocumentsAndKeepsTheRest() throws Exception {
        Session legalEntity = registration("legal-entity", "AAA010101AAA");
        UUID dissociatedOld = upload(legalEntity);
        Path dissociatedOldContent = content(dissociatedOld);
        dissociate(legalEntity);
        UUID associatedOld = upload(legalEntity);
        uploadedAgo(dissociatedOld, Duration.ofHours(25));
        uploadedAgo(associatedOld, Duration.ofHours(25));
        Session individual = registration("individual", "HEGT7610034S2");
        UUID dissociatedRecent = upload(individual);
        dissociate(individual);

        cleanUp.execute();

        assertFalse(metadataExists(dissociatedOld));
        assertFalse(Files.exists(dissociatedOldContent));
        assertTrue(metadataExists(associatedOld));
        assertTrue(Files.exists(content(associatedOld)));
        assertTrue(metadataExists(dissociatedRecent));
        assertTrue(Files.exists(content(dissociatedRecent)));
    }

    @Test
    void ADR0010_stillReferencedFileMarkedNotAssociated_isKeptAndReported() throws Exception {
        Session session = registration("legal-entity", "AAA010101AAA");
        UUID incoherent = upload(session);
        jdbc.update("UPDATE file_metadata SET associated = false WHERE id = ?", incoherent);
        uploadedAgo(incoherent, Duration.ofDays(2));

        CleanUpUnassociatedFilesResult result = cleanUp.execute();

        assertTrue(result.referencedAnomalies().stream().anyMatch(anomaly -> anomaly.fileId().equals(incoherent)));
        assertTrue(metadataExists(incoherent));
        assertTrue(Files.exists(content(incoherent)));
    }

    @Test
    void ADR0010_missingContentAfterAnInterruptedRun_completesTheMetadataRemoval() throws Exception {
        Session session = registration("legal-entity", "AAA010101AAA");
        UUID interrupted = upload(session);
        Files.delete(content(interrupted));
        dissociate(session);
        uploadedAgo(interrupted, Duration.ofDays(2));

        CleanUpUnassociatedFilesResult result = cleanUp.execute();

        assertFalse(metadataExists(interrupted));
        assertTrue(result.failures().stream().noneMatch(failure -> failure.fileId().equals(interrupted)));
    }
}
