package mx.com.fredkorlabs.registration;

import mx.com.fredkorlabs.registration.adapter.out.persistence.SupplierRegistrationJpaRepository;
import mx.com.fredkorlabs.registration.testsupport.DocumentsVolumeTestConfiguration;
import mx.com.fredkorlabs.registration.testsupport.PostgresTestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static mx.com.fredkorlabs.registration.testsupport.SignatureFixtures.identifyRequest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Upload through a real servlet container: the multipart limit of 5 MiB is enforced by the container and the
 * parts are read with the Servlet API, which MockMvc does not exercise (US10 CA-008, ADR-0010).
 */
@Import({PostgresTestcontainersConfiguration.class, DocumentsVolumeTestConfiguration.class})
@SpringBootTest(classes = SupplierRegistrationApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UploadFiscalDocumentServerTest {

    private static final byte[] PDF = "%PDF-1.7\n%%EOF".getBytes(StandardCharsets.US_ASCII);
    private static final String BOUNDARY = "registration-boundary";

    @LocalServerPort
    private int port;

    @Autowired
    private SupplierRegistrationJpaRepository registrations;

    @Autowired
    private JdbcTemplate jdbc;

    private final HttpClient client = HttpClient.newHttpClient();
    private String token;
    private UUID registrationId;

    private record FilePart(String name, String fileName, byte[] content) {}

    @BeforeEach
    void startRegistration() throws Exception {
        String acceptance = postJson("/api/v1/registration-notice-acceptances", "{\"version\":\"1.0\"}", null)
            .get("acceptanceId").asString();
        token = postJson("/api/v1/applicant-identifications", identifyRequest(acceptance, "legal-entity", "AAA010101AAA"),
            null).get("registrationToken").asString();
        registrationId = UUID.fromString(postJson("/api/v1/supplier-registrations", null, token).get("id").asString());
    }

    @AfterEach
    void removeRegistrations() {
        jdbc.update("DELETE FROM registration_fiscal_document");
        jdbc.update("DELETE FROM file_metadata");
        registrations.deleteAll();
    }

    private JsonNode postJson(String path, String json, String bearer) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
            .header("Content-Type", "application/json")
            .POST(json == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(json));
        if (bearer != null) request.header("Authorization", "Bearer " + bearer);
        return JsonMapper.shared().readTree(client.send(request.build(), HttpResponse.BodyHandlers.ofString()).body());
    }

    private HttpResponse<String> upload(List<FilePart> files, List<String[]> fields) throws Exception {
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        for (FilePart file : files) {
            body.write(("--" + BOUNDARY + "\r\nContent-Disposition: form-data; name=\"" + file.name()
                + "\"; filename=\"" + file.fileName() + "\"\r\nContent-Type: application/pdf\r\n\r\n")
                .getBytes(StandardCharsets.UTF_8));
            body.write(file.content());
            body.write("\r\n".getBytes(StandardCharsets.UTF_8));
        }
        for (String[] field : fields) {
            body.write(("--" + BOUNDARY + "\r\nContent-Disposition: form-data; name=\"" + field[0] + "\"\r\n\r\n"
                + field[1] + "\r\n").getBytes(StandardCharsets.UTF_8));
        }
        body.write(("--" + BOUNDARY + "--\r\n").getBytes(StandardCharsets.UTF_8));
        HttpRequest request = HttpRequest.newBuilder(URI.create(
                "http://localhost:" + port + "/api/v1/supplier-registrations/" + registrationId + "/fiscal-document"))
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "multipart/form-data; boundary=" + BOUNDARY)
            .POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray()))
            .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static byte[] pdfOfSize(int size) {
        byte[] content = new byte[size];
        System.arraycopy(PDF, 0, content, 0, PDF.length);
        return content;
    }

    private static JsonNode json(HttpResponse<String> response) {
        return JsonMapper.shared().readTree(response.body());
    }

    @Test
    void US10_CA008_exactMaximumSize_isAcceptedByTheContainer() throws Exception {
        HttpResponse<String> response = upload(List.of(new FilePart("file", "cedula.pdf", pdfOfSize(5_242_880))), List.of());

        assertEquals(200, response.statusCode(), response.body());
        assertEquals(5_242_880, json(response).get("sizeBytes").asLong());
    }

    @Test
    void US10_CA008_oneByteOverTheMaximum_isRejectedByTheContainerWith413() throws Exception {
        HttpResponse<String> response = upload(List.of(new FilePart("file", "cedula.pdf", pdfOfSize(5_242_881))), List.of());

        assertEquals(413, response.statusCode(), response.body());
        assertEquals("application/problem+json", response.headers().firstValue("Content-Type").orElseThrow());
        assertEquals("DOCUMENT_TOO_LARGE", json(response).get("code").asString());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM file_metadata WHERE owner_registration_id = ?",
            Integer.class, registrationId));
    }

    @Test
    void ADR0019_undefinedFilePart_isRejected() throws Exception {
        HttpResponse<String> response = upload(List.of(new FilePart("file", "cedula.pdf", PDF),
            new FilePart("extra", "otra.pdf", PDF)), List.of());

        assertEquals(400, response.statusCode(), response.body());
        assertEquals("extra", json(response).get("errors").get(0).get("parameter").asString());
    }

    @Test
    void ADR0019_repeatedFilePart_isRejected() throws Exception {
        HttpResponse<String> response = upload(List.of(new FilePart("file", "cedula.pdf", PDF),
            new FilePart("file", "otra.pdf", PDF)), List.of());

        assertEquals(400, response.statusCode(), response.body());
        assertEquals("file", json(response).get("errors").get(0).get("parameter").asString());
    }

    @Test
    void ADR0019_textPart_isRejected() throws Exception {
        HttpResponse<String> response = upload(List.of(new FilePart("file", "cedula.pdf", PDF)),
            List.<String[]>of(new String[] {"comment", "x"}));

        assertEquals(400, response.statusCode(), response.body());
        assertEquals("comment", json(response).get("errors").get(0).get("parameter").asString());
        assertEquals(0, jdbc.queryForObject(
            "SELECT COUNT(*) FROM registration_fiscal_document WHERE registration_id = ?", Integer.class, registrationId));
    }
}
