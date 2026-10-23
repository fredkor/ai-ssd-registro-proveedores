package mx.com.fredkorlabs.registration;

import mx.com.fredkorlabs.registration.adapter.out.persistence.FileMetadataPersistenceAdapter;
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
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static mx.com.fredkorlabs.registration.testsupport.SignatureFixtures.identifyRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** A failure after writing rolls back reference, mark and progress together (US10 CA-006, CA-007). */
@Import({PostgresTestcontainersConfiguration.class, DocumentsVolumeTestConfiguration.class})
@SpringBootTest(classes = SupplierRegistrationApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class DeleteFiscalDocumentRollbackTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SupplierRegistrationJpaRepository registrations;

    @Autowired
    private JdbcTemplate jdbc;

    @MockitoSpyBean
    private FileMetadataPersistenceAdapter fileMetadata;

    @AfterEach
    void removeRegistrations() {
        jdbc.update("DELETE FROM registration_fiscal_document");
        jdbc.update("DELETE FROM file_metadata");
        registrations.deleteAll();
    }

    @Test
    void US10_CA006_failureAfterWriting_keepsReferenceMarkAndProgress() throws Exception {
        String acceptance = JsonMapper.shared().readTree(mockMvc.perform(post("/api/v1/registration-notice-acceptances")
                .contentType(MediaType.APPLICATION_JSON).content("{\"version\":\"1.0\"}"))
            .andReturn().getResponse().getContentAsString()).get("acceptanceId").asString();
        String token = JsonMapper.shared().readTree(mockMvc.perform(post("/api/v1/applicant-identifications")
                .contentType(MediaType.APPLICATION_JSON).content(identifyRequest(acceptance, "legal-entity", "AAA010101AAA")))
            .andReturn().getResponse().getContentAsString()).get("registrationToken").asString();
        UUID registrationId = UUID.fromString(JsonMapper.shared().readTree(mockMvc.perform(
                post("/api/v1/supplier-registrations").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andReturn().getResponse().getContentAsString()).get("id").asString());
        UUID fileId = UUID.fromString(JsonMapper.shared().readTree(mockMvc.perform(
                multipart("/api/v1/supplier-registrations/" + registrationId + "/fiscal-document")
                    .file(new MockMultipartFile("file", "cedula.pdf", "application/pdf",
                        "%PDF-1.7\n%%EOF".getBytes(StandardCharsets.US_ASCII)))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("id").asString());
        jdbc.update("UPDATE section_progress SET status = 'COMPLETED' WHERE registration_id = ? AND section = 'GENERAL_DATA'",
            registrationId);
        willAnswer(invocation -> {
            invocation.callRealMethod();
            throw new IllegalStateException("Commit failure");
        }).given(fileMetadata).markNotAssociated(any());

        mockMvc.perform(delete("/api/v1/supplier-registrations/" + registrationId + "/fiscal-document")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));

        assertEquals(fileId, jdbc.queryForObject(
            "SELECT file_id FROM registration_fiscal_document WHERE registration_id = ?", UUID.class, registrationId));
        assertEquals(true, jdbc.queryForObject("SELECT associated FROM file_metadata WHERE id = ?", Boolean.class, fileId));
        assertEquals("COMPLETED", jdbc.queryForObject(
            "SELECT status FROM section_progress WHERE registration_id = ? AND section = 'GENERAL_DATA'",
            String.class, registrationId));
    }
}
