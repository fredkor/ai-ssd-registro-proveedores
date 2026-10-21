package mx.com.fredkorlabs.registration;

import mx.com.fredkorlabs.registration.adapter.out.persistence.ApplicantSignatureEvidenceJpaRepository;
import mx.com.fredkorlabs.registration.adapter.out.persistence.RegistrationContextJpaRepository;
import mx.com.fredkorlabs.registration.testsupport.PostgresTestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

import static mx.com.fredkorlabs.registration.testsupport.SignatureFixtures.identifyRequest;
import static mx.com.fredkorlabs.registration.testsupport.SignatureFixtures.signatureBase64;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** End to end with node-forge seals (ADR-0016) and the real PostgreSQL persistence and JWT (ADR-0015). */
@Import(PostgresTestcontainersConfiguration.class)
@SpringBootTest(classes = SupplierRegistrationApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class IdentifyApplicantIntegrationTest {

    private static final String PATH = "/api/v1/applicant-identifications";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private ApplicantSignatureEvidenceJpaRepository evidences;

    @Autowired
    private RegistrationContextJpaRepository contexts;

    String acceptNotice() throws Exception {
        String body = mockMvc.perform(post("/api/v1/registration-notice-acceptances")
                .contentType(MediaType.APPLICATION_JSON).content("{\"version\":\"1.0\"}"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return JsonMapper.shared().readTree(body).get("acceptanceId").asString();
    }

    ResultActions identify(String acceptanceId, String variant, String rfc) throws Exception {
        return mockMvc.perform(post(PATH).contentType(MediaType.APPLICATION_JSON)
            .content(identifyRequest(acceptanceId, variant, rfc)));
    }

    String identifyAndGetToken(String variant, String rfc) throws Exception {
        String body = identify(acceptNotice(), variant, rfc).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return JsonMapper.shared().readTree(body).get("registrationToken").asString();
    }

    @Test
    void US01_CA017_nodeForgeSignature_isAcceptedAndTokenIsLinkedToContext() throws Exception {
        String acceptanceId = acceptNotice();

        String body = identify(acceptanceId, "legal-entity", "AAA010101AAA")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.identity.rfc").value("AAA010101AAA"))
            .andExpect(jsonPath("$.identity.legalName").value("EMPRESA EJEMPLO SA DE CV"))
            .andExpect(jsonPath("$.identity.taxRegime").value("LEGAL_ENTITY"))
            .andExpect(jsonPath("$.identity.username").value("aaa010101aaa"))
            .andReturn().getResponse().getContentAsString();

        JsonNode response = JsonMapper.shared().readTree(body);
        Jwt jwt = jwtDecoder.decode(response.get("registrationToken").asString());
        assertEquals("AAA010101AAA", jwt.getSubject());
        assertEquals(jwt.getExpiresAt().toString(), response.get("tokenExpiresAt").asString());
        var context = contexts.findById(UUID.fromString(jwt.getId())).orElseThrow();
        assertEquals(UUID.fromString(acceptanceId), context.getNoticeAcceptance().getId());
    }

    @Test
    void US01_CA013_evidenceKeepsOriginalSignatureResultText() throws Exception {
        String request = identifyRequest(acceptNotice(), "name-41-before-3", "AAA010101AAA");
        String original = request.substring(request.indexOf("\"signatureResult\":") + "\"signatureResult\":".length(),
            request.length() - 1);

        String body = mockMvc.perform(post(PATH).contentType(MediaType.APPLICATION_JSON).content(request))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.identity.legalName").value("NOMBRE DEL OID 41"))
            .andReturn().getResponse().getContentAsString();

        Jwt jwt = jwtDecoder.decode(JsonMapper.shared().readTree(body).get("registrationToken").asString());
        var evidence = contexts.findById(UUID.fromString(jwt.getId())).orElseThrow().getSignatureEvidence();
        var stored = evidences.findById(evidence.getId()).orElseThrow();
        assertEquals(original, stored.getCompleteResult());
        assertTrue(stored.getCompleteResult().contains("\"razonSocial\":\"RAZON SOCIAL PKI\""));
        assertTrue(stored.getCompleteResult().contains("\"numeroSerie\":30001000000500003416"));
        assertEquals("NOMBRE DEL OID 41", stored.getVerifiedLegalName());
    }

    @Test
    void US01_CA020_certificateWithRepresentative_identifiesHolder() throws Exception {
        identify(acceptNotice(), "legal-entity-with-representative", "AAA010101AAA")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.identity.rfc").value("AAA010101AAA"));
    }

    @Test
    void US01_CA020_representativeRfc_isRejected() throws Exception {
        assertRejectedWithoutPersisting(identify(acceptNotice(), "legal-entity-with-representative", "HEGT7610034S2"),
            422, "SIGNATURE_VERIFICATION_FAILED");
    }

    @Test
    void US01_CA021_missingRfcAttribute_returns400() throws Exception {
        assertRejectedWithoutPersisting(identify(acceptNotice(), "missing-rfc", "AAA010101AAA"), 400, "VALIDATION_ERROR");
    }

    @Test
    void US01_CA021_emptyOrShortFirstSegment_returns400() throws Exception {
        assertRejectedWithoutPersisting(identify(acceptNotice(), "empty-first-rfc-segment", "AAA010101AAA"),
            400, "VALIDATION_ERROR");
        assertRejectedWithoutPersisting(identify(acceptNotice(), "short-rfc", "AAA010101AAA"), 400, "VALIDATION_ERROR");
    }

    @Test
    void US01_CA022_rfcNotMatchingHolder_returns422() throws Exception {
        assertRejectedWithoutPersisting(identify(acceptNotice(), "legal-entity", "BBB010101BBB"),
            422, "SIGNATURE_VERIFICATION_FAILED");
    }

    @Test
    void US01_CA025_missingOrEmptyName_returns400() throws Exception {
        assertRejectedWithoutPersisting(identify(acceptNotice(), "missing-name", "AAA010101AAA"), 400, "VALIDATION_ERROR");
        assertRejectedWithoutPersisting(identify(acceptNotice(), "empty-first-name", "AAA010101AAA"),
            400, "VALIDATION_ERROR");
    }

    @Test
    void US01_CA015_sealFromAnotherKey_returns422WithoutTokenOrEvidence() throws Exception {
        String request = identifyRequest(acceptNotice(), "legal-entity", "AAA010101AAA")
            .replace(signatureBase64("legal-entity"),
                signatureBase64("individual"));

        assertRejectedWithoutPersisting(mockMvc.perform(post(PATH).contentType(MediaType.APPLICATION_JSON).content(request)),
            422, "SIGNATURE_VERIFICATION_FAILED");
    }

    @Test
    void US03_CA004_unknownAcceptance_returns400() throws Exception {
        assertRejectedWithoutPersisting(identify(UUID.randomUUID().toString(), "legal-entity", "AAA010101AAA"),
            400, "VALIDATION_ERROR");
        assertRejectedWithoutPersisting(identify("not-a-uuid", "legal-entity", "AAA010101AAA"), 400, "VALIDATION_ERROR");
    }

    @Test
    void US03_CA007_acceptanceAlreadyUsed_returns400_butFailedAttemptDoesNotConsumeIt() throws Exception {
        String acceptanceId = acceptNotice();
        identify(acceptanceId, "legal-entity", "BBB010101BBB").andExpect(status().isUnprocessableContent());

        identify(acceptanceId, "legal-entity", "AAA010101AAA").andExpect(status().isOk());
        identify(acceptanceId, "legal-entity", "AAA010101AAA")
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void US01_CA019_twoIdentificationsOfSameRfc_keepBothTokensValid() throws Exception {
        String first = identifyAndGetToken("individual", "HEGT7610034S2");
        String second = identifyAndGetToken("individual", "HEGT7610034S2");

        assertNotEquals(first, second);
        assertEquals("HEGT7610034S2", jwtDecoder.decode(first).getSubject());
        assertEquals("HEGT7610034S2", jwtDecoder.decode(second).getSubject());
    }

    private void assertRejectedWithoutPersisting(ResultActions result, int status, String code) throws Exception {
        long evidenceCount = evidences.count();
        long contextCount = contexts.count();
        result.andExpect(status().is(status))
            .andExpect(jsonPath("$.code").value(code))
            .andExpect(jsonPath("$.registrationToken").doesNotExist());
        assertEquals(evidenceCount, evidences.count());
        assertEquals(contextCount, contexts.count());
    }
}
