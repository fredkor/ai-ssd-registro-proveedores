package mx.com.fredkorlabs.registration;

import mx.com.fredkorlabs.registration.adapter.out.persistence.ApplicantSignatureEvidenceJpaEntity;
import mx.com.fredkorlabs.registration.adapter.out.persistence.ApplicantSignatureEvidenceJpaRepository;
import mx.com.fredkorlabs.registration.adapter.out.persistence.NoticeAcceptanceJpaEntity;
import mx.com.fredkorlabs.registration.adapter.out.persistence.NoticeAcceptanceJpaRepository;
import mx.com.fredkorlabs.registration.adapter.out.persistence.SupplierRegistrationJpaEntity;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.UUID;

import static mx.com.fredkorlabs.registration.testsupport.SignatureFixtures.identifyRequest;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** End to end: the RFC comes only from the registration token issued by identifyApplicant. */
@Import(PostgresTestcontainersConfiguration.class)
@SpringBootTest(classes = SupplierRegistrationApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class CheckRegistrationExistenceIntegrationTest {

    private static final String PATH = "/api/v1/supplier-registrations/existence";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SupplierRegistrationJpaRepository registrations;

    @Autowired
    private NoticeAcceptanceJpaRepository acceptances;

    @Autowired
    private ApplicantSignatureEvidenceJpaRepository evidences;

    @AfterEach
    void removeRegistrations() {
        registrations.deleteAll();
    }

    private String tokenFor(String variant, String rfc) throws Exception {
        String acceptance = JsonMapper.shared().readTree(mockMvc.perform(post("/api/v1/registration-notice-acceptances")
                .contentType(MediaType.APPLICATION_JSON).content("{\"version\":\"1.0\"}"))
            .andReturn().getResponse().getContentAsString()).get("acceptanceId").asString();
        String body = mockMvc.perform(post("/api/v1/applicant-identifications").contentType(MediaType.APPLICATION_JSON)
                .content(identifyRequest(acceptance, variant, rfc)))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return JsonMapper.shared().readTree(body).get("registrationToken").asString();
    }

    private UUID existingRegistration(String rfc, RegistrationStatus status) {
        UUID acceptance = acceptances.save(new NoticeAcceptanceJpaEntity("1.0", Instant.now())).getId();
        UUID evidence = evidences.save(new ApplicantSignatureEvidenceJpaEntity(rfc, "EMPRESA", "{}")).getId();
        return registrations.save(new SupplierRegistrationJpaEntity(
            UUID.randomUUID(), rfc, "EMPRESA", status, acceptance, evidence)).getId();
    }

    private ResultActions checkWith(String token) throws Exception {
        return mockMvc.perform(get(PATH).header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
    }

    @Test
    void US02_CA001_withoutRegistration_returnsExistsFalseOnly() throws Exception {
        checkWith(tokenFor("legal-entity", "AAA010101AAA"))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"exists\":false}", org.springframework.test.json.JsonCompareMode.STRICT));
    }

    @Test
    void US02_CA002_inProgressRegistration_returnsIdAndStatus() throws Exception {
        UUID id = existingRegistration("AAA010101AAA", RegistrationStatus.IN_PROGRESS);

        checkWith(tokenFor("legal-entity", "AAA010101AAA"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exists").value(true))
            .andExpect(jsonPath("$.registrationId").value(id.toString()))
            .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void US02_CA003_registeredRegistration_returnsRegistered() throws Exception {
        existingRegistration("HEGT7610034S2", RegistrationStatus.REGISTERED);

        checkWith(tokenFor("individual", "HEGT7610034S2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exists").value(true))
            .andExpect(jsonPath("$.status").value("REGISTERED"));
    }

    @Test
    void US02_CA005_registrationOfAnotherRfc_isNotVisible() throws Exception {
        existingRegistration("HEGT7610034S2", RegistrationStatus.IN_PROGRESS);

        checkWith(tokenFor("legal-entity", "AAA010101AAA"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exists").value(false));
    }

    @Test
    void US02_CA005_rfcQueryParameter_cannotChooseAnotherRegistration() throws Exception {
        existingRegistration("HEGT7610034S2", RegistrationStatus.IN_PROGRESS);

        mockMvc.perform(get(PATH).param("rfc", "HEGT7610034S2")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenFor("legal-entity", "AAA010101AAA")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exists").value(false));
    }

    @Test
    void US02_CA004_withoutToken_returns401() throws Exception {
        mockMvc.perform(get(PATH))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("REGISTRATION_TOKEN_INVALID"));
    }
}
