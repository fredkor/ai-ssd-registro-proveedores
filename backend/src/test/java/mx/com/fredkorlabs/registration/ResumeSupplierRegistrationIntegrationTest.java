package mx.com.fredkorlabs.registration;

import mx.com.fredkorlabs.registration.adapter.out.persistence.SupplierRegistrationJpaEntity;
import mx.com.fredkorlabs.registration.adapter.out.persistence.SupplierRegistrationJpaRepository;
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

import java.util.Set;
import java.util.UUID;

import static mx.com.fredkorlabs.registration.testsupport.SignatureFixtures.identifyRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** End to end: a new identification of the same RFC resumes its registration (US05). */
@Import(PostgresTestcontainersConfiguration.class)
@SpringBootTest(classes = SupplierRegistrationApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class ResumeSupplierRegistrationIntegrationTest {

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

    private UUID start(Identification identification) throws Exception {
        MvcResult result = mockMvc.perform(post(PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + identification.token()))
            .andExpect(status().isCreated()).andReturn();
        return UUID.fromString(body(result).get("id").asString());
    }

    private ResultActions resume(Object registrationId, String token) throws Exception {
        return mockMvc.perform(post(PATH + "/" + registrationId + "/resume")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
    }

    private static JsonNode body(MvcResult result) throws Exception {
        return JsonMapper.shared().readTree(result.getResponse().getContentAsString());
    }

    private SupplierRegistrationJpaEntity stored(UUID id) {
        return registrations.findById(id).orElseThrow();
    }

    private Set<UUID> evidenceHistory(UUID id) {
        return Set.copyOf(jdbc.queryForList(
            "SELECT signature_evidence_id FROM registration_signature_evidence WHERE registration_id = ?", UUID.class, id));
    }

    @Test
    void US05_CA001_newIdentificationOfSameRfc_resumesInProgressRegistration() throws Exception {
        UUID id = start(identify("legal-entity", "AAA010101AAA"));

        resume(id, identify("legal-entity", "AAA010101AAA").token())
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void US05_CA008_resume_returnsTheSameRegistrationWithoutCreatingAnother() throws Exception {
        UUID id = start(identify("legal-entity", "AAA010101AAA"));

        resume(id, identify("legal-entity", "AAA010101AAA").token())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id.toString()));
        assertEquals(1, registrations.count());
    }

    @Test
    void US05_CA003_resume_returnsStoredProgressIncludingCompletedSections() throws Exception {
        UUID id = start(identify("legal-entity", "AAA010101AAA"));
        jdbc.update("UPDATE section_progress SET status = 'COMPLETED' WHERE registration_id = ? "
            + "AND section IN ('GENERAL_DATA', 'CUCOP')", id);

        JsonNode response = body(resume(id, identify("legal-entity", "AAA010101AAA").token())
            .andExpect(status().isOk()).andReturn());

        assertEquals(Set.of("id", "status", "identity", "noticeAcceptance", "progress"), Set.copyOf(response.propertyNames()));
        JsonNode progress = response.get("progress");
        assertEquals("GENERAL_DATA", progress.get(0).get("section").asString());
        assertEquals("COMPLETED", progress.get(0).get("status").asString());
        assertEquals("COMPLETED", progress.get(1).get("status").asString());
        assertEquals("PENDING", progress.get(2).get("status").asString());
        assertEquals("COMPLETED", jdbc.queryForObject(
            "SELECT status FROM section_progress WHERE registration_id = ? AND section = 'CUCOP'", String.class, id));
    }

    @Test
    void US05_CA009_resume_updatesLegalNameFromTheNewCertificate() throws Exception {
        UUID id = start(identify("legal-entity", "AAA010101AAA"));

        resume(id, identify("name-41-before-3", "AAA010101AAA").token())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.identity.rfc").value("AAA010101AAA"))
            .andExpect(jsonPath("$.identity.legalName").value("NOMBRE DEL OID 41"));
        assertEquals("NOMBRE DEL OID 41", stored(id).getLegalName());
        assertEquals("AAA010101AAA", stored(id).getRfc());
    }

    @Test
    void US05_CA010_resume_appendsEvidenceKeepingTheInitialOne() throws Exception {
        UUID id = start(identify("legal-entity", "AAA010101AAA"));
        UUID initial = stored(id).getInitialEvidenceId();

        resume(id, identify("legal-entity", "AAA010101AAA").token()).andExpect(status().isOk());
        resume(id, identify("name-41-before-3", "AAA010101AAA").token()).andExpect(status().isOk());

        Set<UUID> history = evidenceHistory(id);
        assertEquals(3, history.size());
        assertTrue(history.contains(initial));
        assertEquals(initial, stored(id).getInitialEvidenceId());
    }

    @Test
    void US03_CA007_resume_replacesTheAcceptanceWithTheLatestOne() throws Exception {
        Identification creation = identify("legal-entity", "AAA010101AAA");
        UUID id = start(creation);
        Identification renewal = identify("legal-entity", "AAA010101AAA");

        resume(id, renewal.token())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.noticeAcceptance.acceptanceId").value(renewal.acceptanceId()))
            .andExpect(jsonPath("$.noticeAcceptance.version").value("1.0"));
        assertEquals(UUID.fromString(renewal.acceptanceId()), stored(id).getNoticeAcceptanceId());
    }

    @Test
    void US05_CA004_registrationOfAnotherRfc_returns403WithoutChanges() throws Exception {
        UUID id = start(identify("individual", "HEGT7610034S2"));

        resume(id, identify("legal-entity", "AAA010101AAA").token())
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.code").value("REGISTRATION_ACCESS_DENIED"))
            .andExpect(jsonPath("$.instance").value(PATH + "/" + id + "/resume"));
        assertEquals("JOSÉ PÉREZ NÚÑEZ", stored(id).getLegalName());
        assertEquals(1, evidenceHistory(id).size());
    }

    @Test
    void US05_CA006_registeredRegistration_returns409WithoutChanges() throws Exception {
        UUID id = start(identify("legal-entity", "AAA010101AAA"));
        UUID acceptance = stored(id).getNoticeAcceptanceId();
        // Finalization is a later stage; mark it REGISTERED directly in the database.
        jdbc.update("UPDATE supplier_registration SET status = 'REGISTERED' WHERE id = ?", id);

        resume(id, identify("name-41-before-3", "AAA010101AAA").token())
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("INVALID_STATE_TRANSITION"));
        assertEquals("EMPRESA EJEMPLO SA DE CV", stored(id).getLegalName());
        assertEquals(acceptance, stored(id).getNoticeAcceptanceId());
    }

    @Test
    void US05_CA001_unknownRegistration_returns404() throws Exception {
        resume(UUID.randomUUID(), identify("legal-entity", "AAA010101AAA").token())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("REGISTRATION_NOT_FOUND"));
    }

    @Test
    void ADR0019_registrationIdNotUuid_returns400WithParameter() throws Exception {
        resume("not-a-uuid", identify("legal-entity", "AAA010101AAA").token())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.errors[0].parameter").value("registrationId"));
    }

    @Test
    void US05_resumeWithTheCreationToken_returnsCurrentStateWithoutChanges() throws Exception {
        Identification creation = identify("legal-entity", "AAA010101AAA");
        UUID id = start(creation);

        resume(id, creation.token())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id.toString()))
            .andExpect(jsonPath("$.noticeAcceptance.acceptanceId").value(creation.acceptanceId()));
        assertEquals(1, evidenceHistory(id).size());
    }

    @Test
    void US05_retryWithTheSameToken_isAppliedOnce() throws Exception {
        UUID id = start(identify("legal-entity", "AAA010101AAA"));
        Identification renewal = identify("name-41-before-3", "AAA010101AAA");

        resume(id, renewal.token()).andExpect(status().isOk());
        resume(id, renewal.token())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.identity.legalName").value("NOMBRE DEL OID 41"))
            .andExpect(jsonPath("$.noticeAcceptance.acceptanceId").value(renewal.acceptanceId()));
        assertEquals(2, evidenceHistory(id).size());
    }

    @Test
    void ADR0015_withoutToken_returns401() throws Exception {
        mockMvc.perform(post(PATH + "/" + UUID.randomUUID() + "/resume"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("REGISTRATION_TOKEN_INVALID"));
    }
}
