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
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

import static mx.com.fredkorlabs.registration.testsupport.SignatureFixtures.identifyRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** End to end: the associated fiscal document is downloaded with its original name, never its path (US10). */
@Import({PostgresTestcontainersConfiguration.class, DocumentsVolumeTestConfiguration.class})
@SpringBootTest(classes = SupplierRegistrationApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class DownloadFiscalDocumentIntegrationTest {

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

    private UUID upload(UUID registrationId, String token, String name) throws Exception {
        return UUID.fromString(JsonMapper.shared().readTree(mockMvc.perform(
                multipart("/api/v1/supplier-registrations/" + registrationId + "/fiscal-document")
                    .file(new MockMultipartFile("file", name, "application/pdf", PDF))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("id").asString());
    }

    private ResultActions download(Object registrationId, String token) throws Exception {
        return mockMvc.perform(get("/api/v1/supplier-registrations/" + registrationId + "/fiscal-document")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
    }

    private void assertProblem(ResultActions result, int status, String code) throws Exception {
        result.andExpect(status().is(status))
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.code").value(code));
    }

    @Test
    void US10_CA005_uploadedDocument_isDownloadedWithItsOriginalNameAndBytes() throws Exception {
        String token = identify("legal-entity", "AAA010101AAA");
        UUID registrationId = start(token);
        UUID fileId = upload(registrationId, token, "Cédula Fiscal.pdf");

        MockHttpServletResponse response = download(registrationId, token)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_PDF))
            .andReturn().getResponse();

        assertArrayEquals(PDF, response.getContentAsByteArray());
        assertEquals(PDF.length, response.getContentLength());
        String disposition = response.getHeader(HttpHeaders.CONTENT_DISPOSITION);
        assertEquals("attachment; filename=\"C_dula Fiscal.pdf\"; filename*=UTF-8''C%C3%A9dula%20Fiscal.pdf", disposition);
        String relativePath = jdbc.queryForObject("SELECT relative_path FROM file_metadata WHERE id = ?", String.class, fileId);
        for (String header : response.getHeaderNames()) {
            assertFalse(response.getHeader(header).contains(relativePath), "Header " + header + " exposes the path");
            assertFalse(response.getHeader(header).contains(DocumentsVolumeTestConfiguration.ROOT.toString()));
        }
    }

    @Test
    void US10_CA005_afterResuming_theDocumentIsStillDownloadable() throws Exception {
        String token = identify("legal-entity", "AAA010101AAA");
        UUID registrationId = start(token);
        upload(registrationId, token, "cedula.pdf");
        String renewed = identify("legal-entity", "AAA010101AAA");
        mockMvc.perform(post("/api/v1/supplier-registrations/" + registrationId + "/resume")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + renewed))
            .andExpect(status().isOk());

        assertArrayEquals(PDF, download(registrationId, renewed).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray());
    }

    @Test
    void US10_registeredRegistration_canStillDownloadItsDocument() throws Exception {
        String token = identify("legal-entity", "AAA010101AAA");
        UUID registrationId = start(token);
        upload(registrationId, token, "cedula.pdf");
        jdbc.update("UPDATE supplier_registration SET status = 'REGISTERED' WHERE id = ?", registrationId);

        download(registrationId, token).andExpect(status().isOk());
    }

    @Test
    void US10_withoutDocument_returns404FiscalDocumentNotFound() throws Exception {
        String token = identify("legal-entity", "AAA010101AAA");

        assertProblem(download(start(token), token), 404, "FISCAL_DOCUMENT_NOT_FOUND");
    }

    @Test
    void US10_CA011_dissociatedDocument_isNotDownloadableAlthoughItsContentRemains() throws Exception {
        String token = identify("legal-entity", "AAA010101AAA");
        UUID registrationId = start(token);
        UUID fileId = upload(registrationId, token, "cedula.pdf");
        // Logical deletion as ADR-0010 defines it: reference removed, metadata kept and marked not associated.
        jdbc.update("DELETE FROM registration_fiscal_document WHERE registration_id = ?", registrationId);
        jdbc.update("UPDATE file_metadata SET associated = false WHERE id = ?", fileId);
        String relativePath = jdbc.queryForObject("SELECT relative_path FROM file_metadata WHERE id = ?", String.class, fileId);

        assertProblem(download(registrationId, token), 404, "FISCAL_DOCUMENT_NOT_FOUND");
        assertTrue(Files.exists(DocumentsVolumeTestConfiguration.ROOT.resolve(relativePath)));
    }

    @Test
    void ADR0010_missingContentInTheVolume_returns500() throws Exception {
        String token = identify("legal-entity", "AAA010101AAA");
        UUID registrationId = start(token);
        UUID fileId = upload(registrationId, token, "cedula.pdf");
        String relativePath = jdbc.queryForObject("SELECT relative_path FROM file_metadata WHERE id = ?", String.class, fileId);
        Files.delete(DocumentsVolumeTestConfiguration.ROOT.resolve(relativePath));

        assertProblem(download(registrationId, token), 500, "INTERNAL_ERROR");
    }

    @Test
    void US10_registrationOfAnotherRfc_returns403() throws Exception {
        String owner = identify("individual", "HEGT7610034S2");
        UUID registrationId = start(owner);
        upload(registrationId, owner, "cedula.pdf");

        assertProblem(download(registrationId, identify("legal-entity", "AAA010101AAA")), 403,
            "REGISTRATION_ACCESS_DENIED");
    }

    @Test
    void US10_unknownRegistration_returns404RegistrationNotFound() throws Exception {
        assertProblem(download(UUID.randomUUID(), identify("legal-entity", "AAA010101AAA")), 404,
            "REGISTRATION_NOT_FOUND");
    }

    @Test
    void ADR0019_registrationIdNotUuid_returns400WithParameter() throws Exception {
        download("not-a-uuid", identify("legal-entity", "AAA010101AAA"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[0].parameter").value("registrationId"));
    }

    @Test
    void ADR0015_withoutToken_returns401() throws Exception {
        assertProblem(mockMvc.perform(get("/api/v1/supplier-registrations/" + UUID.randomUUID() + "/fiscal-document")),
            401, "REGISTRATION_TOKEN_INVALID");
    }
}
