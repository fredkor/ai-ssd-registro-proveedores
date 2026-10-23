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
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static mx.com.fredkorlabs.registration.testsupport.SignatureFixtures.identifyRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** End to end: the fiscal document is stored in the volume and associated immediately (US10, ADR-0010). */
@Import({PostgresTestcontainersConfiguration.class, DocumentsVolumeTestConfiguration.class})
@SpringBootTest(classes = SupplierRegistrationApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class UploadFiscalDocumentIntegrationTest {

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

    private static MockMultipartHttpServletRequestBuilder uploadRequest(UUID registrationId, String token) {
        MockMultipartHttpServletRequestBuilder request =
            multipart("/api/v1/supplier-registrations/" + registrationId + "/fiscal-document");
        request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        return request;
    }

    private ResultActions upload(UUID registrationId, String token, String name, byte[] content) throws Exception {
        return mockMvc.perform(uploadRequest(registrationId, token)
            .file(new MockMultipartFile("file", name, "application/pdf", content)));
    }

    private static byte[] pdfOfSize(int size) {
        byte[] content = new byte[size];
        System.arraycopy(PDF, 0, content, 0, PDF.length);
        return content;
    }

    private void assertProblem(ResultActions result, int status, String code) throws Exception {
        result.andExpect(status().is(status))
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.code").value(code));
    }

    private long storedFiles() throws Exception {
        try (var paths = Files.walk(DocumentsVolumeTestConfiguration.ROOT)) {
            return paths.filter(Files::isRegularFile).count();
        }
    }

    @Test
    void US10_CA001_validPdf_isStoredAssociatedAndReturnedWithItsUuid() throws Exception {
        String token = identify("legal-entity", "AAA010101AAA");
        UUID registrationId = start(token);

        JsonNode response = JsonMapper.shared().readTree(upload(registrationId, token, "Cédula Fiscal.pdf", PDF)
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));

        assertEquals(Set.of("id", "originalName", "extension", "sizeBytes", "uploadedAt"),
            Set.copyOf(response.propertyNames()));
        UUID fileId = UUID.fromString(response.get("id").asString());
        assertNotEquals(registrationId, fileId);
        assertEquals("Cédula Fiscal.pdf", response.get("originalName").asString());
        assertEquals("pdf", response.get("extension").asString());
        assertEquals(PDF.length, response.get("sizeBytes").asLong());
        assertTrue(response.get("uploadedAt").asString().endsWith("Z"));

        Map<String, Object> metadata = jdbc.queryForMap("SELECT * FROM file_metadata WHERE id = ?", fileId);
        assertEquals(registrationId, metadata.get("owner_registration_id"));
        assertEquals(true, metadata.get("associated"));
        assertEquals("application/pdf", metadata.get("media_type"));
        String relativePath = (String) metadata.get("relative_path");
        assertTrue(relativePath.matches("\\d{4}/\\d{2}/\\d{2}/AAA010101AAA/" + fileId + "\\.pdf"), relativePath);
        assertArrayEquals(PDF, Files.readAllBytes(DocumentsVolumeTestConfiguration.ROOT.resolve(relativePath)));
        assertEquals(fileId, jdbc.queryForObject(
            "SELECT file_id FROM registration_fiscal_document WHERE registration_id = ?", UUID.class, registrationId));
        assertEquals("PENDING", jdbc.queryForObject(
            "SELECT status FROM section_progress WHERE registration_id = ? AND section = 'GENERAL_DATA'",
            String.class, registrationId));
    }

    @Test
    void US10_CA008_exactMaximumSize_isAccepted() throws Exception {
        String token = identify("legal-entity", "AAA010101AAA");

        upload(start(token), token, "CEDULA.PDF", pdfOfSize(5_242_880))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sizeBytes").value(5_242_880));
    }

    @Test
    void US10_CA008_oneByteOverTheMaximum_returns413WithoutStoring() throws Exception {
        String token = identify("legal-entity", "AAA010101AAA");
        UUID registrationId = start(token);
        long before = storedFiles();

        assertProblem(upload(registrationId, token, "cedula.pdf", pdfOfSize(5_242_881)), 413, "DOCUMENT_TOO_LARGE");
        assertEquals(before, storedFiles());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM file_metadata WHERE owner_registration_id = ?",
            Integer.class, registrationId));
    }

    @Test
    void US10_CA002_otherExtension_returns415() throws Exception {
        String token = identify("legal-entity", "AAA010101AAA");

        assertProblem(upload(start(token), token, "cedula.txt", PDF), 415, "DOCUMENT_FORMAT_NOT_SUPPORTED");
    }

    @Test
    void US10_CA009_pdfNameAndMimeWithoutPdfContent_returns415WithoutStoring() throws Exception {
        String token = identify("legal-entity", "AAA010101AAA");
        UUID registrationId = start(token);
        long before = storedFiles();

        assertProblem(upload(registrationId, token, "cedula.pdf", "plain text".getBytes(StandardCharsets.US_ASCII)),
            415, "DOCUMENT_FORMAT_NOT_SUPPORTED");
        assertEquals(before, storedFiles());
        assertEquals(0, jdbc.queryForObject(
            "SELECT COUNT(*) FROM registration_fiscal_document WHERE registration_id = ?", Integer.class, registrationId));
    }

    @Test
    void US10_CA004_secondDocument_returns409AndKeepsTheFirst() throws Exception {
        String token = identify("legal-entity", "AAA010101AAA");
        UUID registrationId = start(token);
        String first = JsonMapper.shared().readTree(upload(registrationId, token, "cedula.pdf", PDF)
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("id").asString();

        assertProblem(upload(registrationId, token, "otra.pdf", PDF), 409, "FISCAL_DOCUMENT_ALREADY_EXISTS");
        assertEquals(UUID.fromString(first), jdbc.queryForObject(
            "SELECT file_id FROM registration_fiscal_document WHERE registration_id = ?", UUID.class, registrationId));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM file_metadata WHERE owner_registration_id = ?",
            Integer.class, registrationId));
    }

    @Test
    void US10_CA005_resume_showsTheAssociatedDocumentWithoutNext() throws Exception {
        String token = identify("legal-entity", "AAA010101AAA");
        UUID registrationId = start(token);
        String fileId = JsonMapper.shared().readTree(upload(registrationId, token, "cedula.pdf", PDF)
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("id").asString();

        mockMvc.perform(post("/api/v1/supplier-registrations/" + registrationId + "/resume")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + identify("legal-entity", "AAA010101AAA")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fiscalDocument.id").value(fileId))
            .andExpect(jsonPath("$.fiscalDocument.originalName").value("cedula.pdf"))
            .andExpect(jsonPath("$.fiscalDocument.extension").value("pdf"))
            .andExpect(jsonPath("$.fiscalDocument.sizeBytes").value(PDF.length))
            .andExpect(jsonPath("$.progress[0].status").value("PENDING"));
    }

    @Test
    void US10_registeredRegistration_returns409InvalidStateTransition() throws Exception {
        String token = identify("legal-entity", "AAA010101AAA");
        UUID registrationId = start(token);
        jdbc.update("UPDATE supplier_registration SET status = 'REGISTERED' WHERE id = ?", registrationId);

        assertProblem(upload(registrationId, token, "cedula.pdf", PDF), 409, "INVALID_STATE_TRANSITION");
    }

    @Test
    void US10_registrationOfAnotherRfc_returns403() throws Exception {
        UUID registrationId = start(identify("individual", "HEGT7610034S2"));

        assertProblem(upload(registrationId, identify("legal-entity", "AAA010101AAA"), "cedula.pdf", PDF),
            403, "REGISTRATION_ACCESS_DENIED");
    }

    @Test
    void US10_unknownRegistration_returns404() throws Exception {
        assertProblem(upload(UUID.randomUUID(), identify("legal-entity", "AAA010101AAA"), "cedula.pdf", PDF),
            404, "REGISTRATION_NOT_FOUND");
    }

    @Test
    void ADR0019_missingFilePart_returns400WithParameter() throws Exception {
        String token = identify("legal-entity", "AAA010101AAA");

        assertProblem(mockMvc.perform(uploadRequest(start(token), token)
                .file(new MockMultipartFile("document", "cedula.pdf", "application/pdf", PDF))),
            400, "VALIDATION_ERROR");
    }

    @Test
    void ADR0019_undefinedOrRepeatedPart_returns400WithParameter() throws Exception {
        String token = identify("legal-entity", "AAA010101AAA");
        UUID registrationId = start(token);

        mockMvc.perform(uploadRequest(registrationId, token)
                .file(new MockMultipartFile("file", "cedula.pdf", "application/pdf", PDF))
                .file(new MockMultipartFile("extra", "otra.pdf", "application/pdf", PDF)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[0].parameter").value("extra"));
        mockMvc.perform(uploadRequest(registrationId, token)
                .file(new MockMultipartFile("file", "cedula.pdf", "application/pdf", PDF))
                .file(new MockMultipartFile("file", "otra.pdf", "application/pdf", PDF)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[0].parameter").value("file"));
        mockMvc.perform(uploadRequest(registrationId, token)
                .file(new MockMultipartFile("file", "cedula.pdf", "application/pdf", PDF))
                .param("comment", "x"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[0].parameter").value("comment"));
        assertEquals(0, jdbc.queryForObject(
            "SELECT COUNT(*) FROM registration_fiscal_document WHERE registration_id = ?", Integer.class, registrationId));
    }

    @Test
    void ADR0015_withoutToken_returns401() throws Exception {
        assertProblem(mockMvc.perform(multipart("/api/v1/supplier-registrations/" + UUID.randomUUID() + "/fiscal-document")
                .file(new MockMultipartFile("file", "cedula.pdf", "application/pdf", PDF))),
            401, "REGISTRATION_TOKEN_INVALID");
    }
}
