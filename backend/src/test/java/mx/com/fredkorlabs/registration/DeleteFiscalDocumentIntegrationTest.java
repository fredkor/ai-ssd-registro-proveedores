package mx.com.fredkorlabs.registration;

import mx.com.fredkorlabs.registration.adapter.out.persistence.SupplierRegistrationJpaRepository;
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
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

import static mx.com.fredkorlabs.registration.testsupport.SignatureFixtures.identifyRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** End to end: logical deletion of the fiscal document in one database transaction (US10, ADR-0010). */
@Import({PostgresTestcontainersConfiguration.class, DocumentsVolumeTestConfiguration.class})
@SpringBootTest(classes = SupplierRegistrationApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class DeleteFiscalDocumentIntegrationTest {

    private static final byte[] PDF = "%PDF-1.7\n1 0 obj\n%%EOF".getBytes(StandardCharsets.US_ASCII);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SupplierRegistrationJpaRepository registrations;

    @Autowired
    private JdbcTemplate jdbc;

    @AfterEach
    void removeRegistrations() {
        jdbc.update("DELETE FROM registration_fiscal_document");
        jdbc.update("DELETE FROM file_metadata");
        registrations.deleteAll();
    }

    private String identify(String variant, String rfc) throws Exception {
        String acceptance = JsonMapper.shared().readTree(mockMvc.perform(post("/api/v1/registration-notice-acceptances")
                .contentType(MediaType.APPLICATION_JSON).content("{\"version\":\"1.0\"}"))
            .andReturn().getResponse().getContentAsString()).get("acceptanceId").asString();
        return JsonMapper.shared().readTree(mockMvc.perform(post("/api/v1/applicant-identifications")
                .contentType(MediaType.APPLICATION_JSON).content(identifyRequest(acceptance, variant, rfc)))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("registrationToken").asString();
    }

    private UUID start(String token) throws Exception {
        return UUID.fromString(JsonMapper.shared().readTree(mockMvc.perform(post("/api/v1/supplier-registrations")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asString());
    }

    private UUID upload(UUID registrationId, String token) throws Exception {
        return UUID.fromString(JsonMapper.shared().readTree(mockMvc.perform(
                multipart("/api/v1/supplier-registrations/" + registrationId + "/fiscal-document")
                    .file(new MockMultipartFile("file", "cedula.pdf", "application/pdf", PDF))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("id").asString());
    }

    private ResultActions remove(Object registrationId, String token) throws Exception {
        return mockMvc.perform(delete("/api/v1/supplier-registrations/" + registrationId + "/fiscal-document")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
    }

    private String progress(UUID registrationId, String section) {
        return jdbc.queryForObject("SELECT status FROM section_progress WHERE registration_id = ? AND section = ?",
            String.class, registrationId, section);
    }

    private void assertProblem(ResultActions result, int status, String code) throws Exception {
        result.andExpect(status().is(status))
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.code").value(code));
    }

    @Test
    void US10_CA006_delete_returns204AndDissociatesWithoutTouchingTheContent() throws Exception {
        String token = identify("legal-entity", "AAA010101AAA");
        UUID registrationId = start(token);
        UUID fileId = upload(registrationId, token);
        String relativePath = jdbc.queryForObject("SELECT relative_path FROM file_metadata WHERE id = ?", String.class, fileId);

        remove(registrationId, token)
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));

        assertEquals(0, jdbc.queryForObject(
            "SELECT COUNT(*) FROM registration_fiscal_document WHERE registration_id = ?", Integer.class, registrationId));
        assertEquals(false, jdbc.queryForObject("SELECT associated FROM file_metadata WHERE id = ?", Boolean.class, fileId));
        assertTrue(Files.exists(DocumentsVolumeTestConfiguration.ROOT.resolve(relativePath)));
    }

    @Test
    void US10_CA011_deletedDocument_isNoLongerShownNorDownloadable() throws Exception {
        String token = identify("legal-entity", "AAA010101AAA");
        UUID registrationId = start(token);
        upload(registrationId, token);
        remove(registrationId, token).andExpect(status().isNoContent());

        assertProblem(mockMvc.perform(get("/api/v1/supplier-registrations/" + registrationId + "/fiscal-document")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)), 404, "FISCAL_DOCUMENT_NOT_FOUND");
        mockMvc.perform(post("/api/v1/supplier-registrations/" + registrationId + "/resume")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + identify("legal-entity", "AAA010101AAA")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fiscalDocument").doesNotExist());
    }

    @Test
    void US10_CA007_delete_leavesGeneralDataPendingAndCucopIntact() throws Exception {
        String token = identify("legal-entity", "AAA010101AAA");
        UUID registrationId = start(token);
        upload(registrationId, token);
        // General Data is not saved through the API yet; set the completed sections directly.
        jdbc.update("UPDATE section_progress SET status = 'COMPLETED' WHERE registration_id = ? "
            + "AND section IN ('GENERAL_DATA', 'CUCOP')", registrationId);

        remove(registrationId, token).andExpect(status().isNoContent());

        assertEquals("PENDING", progress(registrationId, "GENERAL_DATA"));
        assertEquals("COMPLETED", progress(registrationId, "CUCOP"));
    }

    @Test
    void US10_CA007_newUploadAfterDeleting_isAssociatedWithoutCompletingTheSection() throws Exception {
        String token = identify("legal-entity", "AAA010101AAA");
        UUID registrationId = start(token);
        UUID first = upload(registrationId, token);
        remove(registrationId, token).andExpect(status().isNoContent());

        UUID second = upload(registrationId, token);

        assertNotEquals(first, second);
        assertEquals(second, jdbc.queryForObject(
            "SELECT file_id FROM registration_fiscal_document WHERE registration_id = ?", UUID.class, registrationId));
        assertEquals(false, jdbc.queryForObject("SELECT associated FROM file_metadata WHERE id = ?", Boolean.class, first));
        assertEquals("PENDING", progress(registrationId, "GENERAL_DATA"));
    }

    @Test
    void US10_deleteTwice_returns404TheSecondTime() throws Exception {
        String token = identify("legal-entity", "AAA010101AAA");
        UUID registrationId = start(token);
        upload(registrationId, token);
        remove(registrationId, token).andExpect(status().isNoContent());

        assertProblem(remove(registrationId, token), 404, "FISCAL_DOCUMENT_NOT_FOUND");
    }

    @Test
    void US10_registeredRegistration_returns409WithoutChanges() throws Exception {
        String token = identify("legal-entity", "AAA010101AAA");
        UUID registrationId = start(token);
        UUID fileId = upload(registrationId, token);
        jdbc.update("UPDATE supplier_registration SET status = 'REGISTERED' WHERE id = ?", registrationId);

        assertProblem(remove(registrationId, token), 409, "INVALID_STATE_TRANSITION");
        assertEquals(true, jdbc.queryForObject("SELECT associated FROM file_metadata WHERE id = ?", Boolean.class, fileId));
    }

    @Test
    void US10_registrationOfAnotherRfc_returns403WithoutChanges() throws Exception {
        String owner = identify("individual", "HEGT7610034S2");
        UUID registrationId = start(owner);
        UUID fileId = upload(registrationId, owner);

        assertProblem(remove(registrationId, identify("legal-entity", "AAA010101AAA")), 403, "REGISTRATION_ACCESS_DENIED");
        assertEquals(true, jdbc.queryForObject("SELECT associated FROM file_metadata WHERE id = ?", Boolean.class, fileId));
    }

    @Test
    void US10_unknownRegistration_returns404RegistrationNotFound() throws Exception {
        assertProblem(remove(UUID.randomUUID(), identify("legal-entity", "AAA010101AAA")), 404, "REGISTRATION_NOT_FOUND");
    }

    @Test
    void ADR0019_registrationIdNotUuid_returns400WithParameter() throws Exception {
        remove("not-a-uuid", identify("legal-entity", "AAA010101AAA"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[0].parameter").value("registrationId"));
    }

    @Test
    void ADR0015_withoutToken_returns401() throws Exception {
        assertProblem(mockMvc.perform(delete("/api/v1/supplier-registrations/" + UUID.randomUUID() + "/fiscal-document")),
            401, "REGISTRATION_TOKEN_INVALID");
    }
}
