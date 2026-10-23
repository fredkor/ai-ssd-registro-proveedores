package mx.com.fredkorlabs.registration;

import mx.com.fredkorlabs.registration.adapter.out.persistence.SupplierRegistrationJpaRepository;
import mx.com.fredkorlabs.registration.domain.model.RegistrationStatus;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.StreamSupport;

import static mx.com.fredkorlabs.registration.testsupport.SignatureFixtures.identifyRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** End to end: identity, evidence and acceptance come only from the token issued by identifyApplicant. */
@Import(PostgresTestcontainersConfiguration.class)
@SpringBootTest(classes = SupplierRegistrationApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class StartSupplierRegistrationIntegrationTest {

    private static final String PATH = "/api/v1/supplier-registrations";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SupplierRegistrationJpaRepository registrations;

    @Autowired
    private JdbcTemplate jdbc;

    @AfterEach
    void removeRegistrations() {
        registrations.deleteAll();
    }

    private record Identification(String token, String acceptanceId) {}

    private Identification identify(String variant, String rfc) throws Exception {
        String acceptance = JsonMapper.shared().readTree(mockMvc.perform(post("/api/v1/registration-notice-acceptances")
                .contentType(MediaType.APPLICATION_JSON).content("{\"version\":\"1.0\"}"))
            .andReturn().getResponse().getContentAsString()).get("acceptanceId").asString();
        String body = mockMvc.perform(post("/api/v1/applicant-identifications").contentType(MediaType.APPLICATION_JSON)
                .content(identifyRequest(acceptance, variant, rfc)))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return new Identification(JsonMapper.shared().readTree(body).get("registrationToken").asString(), acceptance);
    }

    private ResultActions startWith(String token) throws Exception {
        return mockMvc.perform(post(PATH).header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
    }

    private static JsonNode body(MvcResult result) throws Exception {
        return JsonMapper.shared().readTree(result.getResponse().getContentAsString());
    }

    @Test
    void US04_CA001_identifiedApplicantWithoutRegistration_creates201InProgress() throws Exception {
        MvcResult result = startWith(identify("legal-entity", "AAA010101AAA").token())
            .andExpect(status().isCreated())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
            .andReturn();

        UUID id = UUID.fromString(body(result).get("id").asString());
        assertEquals("/api/v1/supplier-registrations/" + id, result.getResponse().getHeader(HttpHeaders.LOCATION));
        assertEquals(RegistrationStatus.IN_PROGRESS, registrations.findById(id).orElseThrow().getStatus());
    }

    @Test
    void US04_CA002_newRegistration_returnsSixPendingSectionsWithoutSavedSections() throws Exception {
        JsonNode response = body(startWith(identify("legal-entity", "AAA010101AAA").token())
            .andExpect(status().isCreated()).andReturn());

        assertEquals(Set.of("id", "status", "identity", "noticeAcceptance", "progress"), Set.copyOf(response.propertyNames()));
        List<String> sections = StreamSupport.stream(response.get("progress").spliterator(), false)
            .map(item -> item.get("section").asString()).toList();
        assertEquals(List.of("GENERAL_DATA", "CUCOP", "CONTACTS", "FISCAL_ADDRESS", "COVERAGE", "ELECTRONIC_SIGNATURE"),
            sections);
        response.get("progress").forEach(item -> assertEquals("PENDING", item.get("status").asString()));
    }

    @Test
    void US04_CA003_secondStartForSameRfc_returns409DuplicateRfc() throws Exception {
        startWith(identify("legal-entity", "AAA010101AAA").token()).andExpect(status().isCreated());

        startWith(identify("legal-entity", "AAA010101AAA").token())
            .andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.code").value("DUPLICATE_RFC"))
            .andExpect(jsonPath("$.type").value("urn:fredkorlabs:registration:problem:duplicate-rfc"))
            .andExpect(jsonPath("$.instance").value(PATH))
            .andExpect(jsonPath("$.errors").isEmpty());
        assertEquals(1, registrations.count());
    }

    @Test
    void US04_CA003_sameTokenTwice_returns409() throws Exception {
        String token = identify("legal-entity", "AAA010101AAA").token();
        startWith(token).andExpect(status().isCreated());

        startWith(token).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("DUPLICATE_RFC"));
    }

    @Test
    void US04_CA003_registeredRfc_cannotStartAgain() throws Exception {
        String token = identify("individual", "HEGT7610034S2").token();
        UUID id = UUID.fromString(body(startWith(token).andExpect(status().isCreated()).andReturn()).get("id").asString());
        // Finalization is a later stage; mark it REGISTERED directly in the database.
        jdbc.update("UPDATE supplier_registration SET status = 'REGISTERED' WHERE id = ?", id);

        startWith(identify("individual", "HEGT7610034S2").token())
            .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("DUPLICATE_RFC"));
    }

    @Test
    void US04_CA003_concurrentStartsForSameRfc_createOnlyOneRegistration() throws Exception {
        List<String> tokens = List.of(identify("legal-entity", "AAA010101AAA").token(),
            identify("legal-entity", "AAA010101AAA").token());
        CountDownLatch ready = new CountDownLatch(1);
        List<Future<Integer>> responses = new ArrayList<>();
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            for (String token : tokens) {
                responses.add(executor.submit(() -> {
                    ready.await();
                    return startWith(token).andReturn().getResponse().getStatus();
                }));
            }
            ready.countDown();
            List<Integer> statuses = new ArrayList<>();
            for (Future<Integer> response : responses) statuses.add(response.get());
            statuses.sort(null);
            assertEquals(List.of(201, 409), statuses);
        }
        assertEquals(1, registrations.count());
    }

    @Test
    void US04_CA005_registrationsGetDistinctGeneratedUuids() throws Exception {
        String first = body(startWith(identify("legal-entity", "AAA010101AAA").token())
            .andExpect(status().isCreated()).andReturn()).get("id").asString();
        String second = body(startWith(identify("individual", "HEGT7610034S2").token())
            .andExpect(status().isCreated()).andReturn()).get("id").asString();

        assertNotEquals(UUID.fromString(first), UUID.fromString(second));
    }

    @Test
    void US04_CA006_registration_carriesTheVerifiedIdentity() throws Exception {
        startWith(identify("individual", "HEGT7610034S2").token())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.identity.rfc").value("HEGT7610034S2"))
            .andExpect(jsonPath("$.identity.legalName").value("JOSÉ PÉREZ NÚÑEZ"))
            .andExpect(jsonPath("$.identity.taxRegime").value("INDIVIDUAL"))
            .andExpect(jsonPath("$.identity.username").value("hegt7610034s2"));
    }

    @Test
    void US03_CA005_registration_keepsTheAcceptanceUsedToIdentify() throws Exception {
        Identification identification = identify("legal-entity", "AAA010101AAA");

        JsonNode acceptance = body(startWith(identification.token()).andExpect(status().isCreated()).andReturn())
            .get("noticeAcceptance");

        assertEquals(identification.acceptanceId(), acceptance.get("acceptanceId").asString());
        assertEquals("1.0", acceptance.get("version").asString());
        assertTrue(acceptance.get("acceptedAt").asString().endsWith("Z"));
        assertEquals(UUID.fromString(identification.acceptanceId()),
            registrations.findAll().getFirst().getNoticeAcceptanceId());
    }

    @Test
    void US04_CA003_existenceAfterStart_reportsTheCreatedRegistration() throws Exception {
        String token = identify("legal-entity", "AAA010101AAA").token();
        String id = body(startWith(token).andExpect(status().isCreated()).andReturn()).get("id").asString();

        mockMvc.perform(get(PATH + "/existence").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(jsonPath("$.exists").value(true))
            .andExpect(jsonPath("$.registrationId").value(id))
            .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void ADR0015_withoutToken_returns401() throws Exception {
        mockMvc.perform(post(PATH))
            .andExpect(status().isUnauthorized())
            .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
            .andExpect(jsonPath("$.code").value("REGISTRATION_TOKEN_INVALID"));
        assertEquals(0, registrations.count());
    }
}
