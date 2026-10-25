package mx.com.fredkorlabs.registration;

import mx.com.fredkorlabs.registration.adapter.out.persistence.ApplicantSignatureEvidenceJpaRepository;
import mx.com.fredkorlabs.registration.adapter.out.persistence.SupplierRegistrationJpaEntity;
import mx.com.fredkorlabs.registration.adapter.out.persistence.SupplierRegistrationJpaRepository;
import mx.com.fredkorlabs.registration.adapter.out.persistence.SupplierRegistrationPersistenceAdapter;
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
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;

import static mx.com.fredkorlabs.registration.testsupport.SignatureFixtures.identifyRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** A failure after the resume was written rolls back name, acceptance and evidence link (US05 CA-011, US03 CA-008). */
@Import(PostgresTestcontainersConfiguration.class)
@SpringBootTest(classes = SupplierRegistrationApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class ResumeSupplierRegistrationRollbackTest {

    private static final String PATH = "/api/v1/supplier-registrations";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SupplierRegistrationJpaRepository registrations;

    @Autowired
    private ApplicantSignatureEvidenceJpaRepository evidences;

    @Autowired
    private JdbcTemplate jdbc;

    @MockitoSpyBean
    private SupplierRegistrationPersistenceAdapter persistence;

    @AfterEach
    void removeRegistrations() {
        registrations.deleteAll();
    }

    private String token(String variant) throws Exception {
        String acceptance = JsonMapper.shared().readTree(mockMvc.perform(post("/api/v1/registration-notice-acceptances")
                .contentType(MediaType.APPLICATION_JSON).content("{\"version\":\"1.0\"}"))
            .andReturn().getResponse().getContentAsString()).get("acceptanceId").asString();
        return JsonMapper.shared().readTree(mockMvc.perform(post("/api/v1/applicant-identifications")
                .contentType(MediaType.APPLICATION_JSON).content(identifyRequest(acceptance, variant, "AAA010101AAA")))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("registrationToken").asString();
    }

    private record FailedResume(UUID id, SupplierRegistrationJpaEntity before, long evidencesBefore) {}

    /** Creates a registration with CUCoP completed and resumes it with a failure after writing. */
    private FailedResume resumeFailingAfterWriting() throws Exception {
        JsonNode created = JsonMapper.shared().readTree(mockMvc.perform(post(PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token("legal-entity")))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        UUID id = UUID.fromString(created.get("id").asString());
        jdbc.update("UPDATE section_progress SET status = 'COMPLETED' WHERE registration_id = ? AND section = 'CUCOP'", id);
        SupplierRegistrationJpaEntity before = registrations.findById(id).orElseThrow();
        String resumeToken = token("name-41-before-3");
        long evidencesBefore = evidences.count();
        willAnswer(invocation -> {
            invocation.callRealMethod();
            throw new IllegalStateException("Commit failure");
        }).given(persistence).saveResume(any(), any(), any());

        mockMvc.perform(post(PATH + "/" + id + "/resume")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + resumeToken))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
            .andExpect(jsonPath("$.id").doesNotExist());
        return new FailedResume(id, before, evidencesBefore);
    }

    @Test
    void US05_CA011_failureAfterWriting_rollsBackAndKeepsPreviousState() throws Exception {
        FailedResume failed = resumeFailingAfterWriting();

        SupplierRegistrationJpaEntity after = registrations.findById(failed.id()).orElseThrow();
        assertEquals("EMPRESA EJEMPLO SA DE CV", after.getLegalName());
        assertEquals(List.of(failed.before().getInitialEvidenceId()), jdbc.queryForList(
            "SELECT signature_evidence_id FROM registration_signature_evidence WHERE registration_id = ?", UUID.class,
            failed.id()));
        assertEquals("COMPLETED", jdbc.queryForObject(
            "SELECT status FROM section_progress WHERE registration_id = ? AND section = 'CUCOP'", String.class,
            failed.id()));
    }

    @Test
    void US03_CA008_failedResume_keepsThePreviousAcceptanceAndEvidence() throws Exception {
        FailedResume failed = resumeFailingAfterWriting();

        SupplierRegistrationJpaEntity after = registrations.findById(failed.id()).orElseThrow();
        assertEquals(failed.before().getNoticeAcceptanceId(), after.getNoticeAcceptanceId());
        // The evidence kept when identifying for the resume is not deleted by the failed resumption.
        assertEquals(failed.evidencesBefore(), evidences.count());
    }
}
