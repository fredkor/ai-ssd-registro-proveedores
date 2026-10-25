package mx.com.fredkorlabs.registration.adapter.out.persistence;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import jakarta.persistence.EntityManager;
import mx.com.fredkorlabs.registration.application.port.out.NoticeAcceptanceAlreadyUsedException;
import mx.com.fredkorlabs.registration.application.port.out.NoticeAcceptanceReference;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationSession;
import mx.com.fredkorlabs.registration.application.port.out.SignatureEvidenceReference;
import mx.com.fredkorlabs.registration.domain.model.ApplicantIdentity;
import mx.com.fredkorlabs.registration.domain.model.ApplicantSignatureEvidence;
import mx.com.fredkorlabs.registration.domain.model.NoticeAcceptance;
import mx.com.fredkorlabs.registration.domain.model.Rfc;
import mx.com.fredkorlabs.registration.testsupport.PostgresTestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({NoticeAcceptancePersistenceAdapter.class, ApplicantSignatureEvidencePersistenceAdapter.class, PostgresTestcontainersConfiguration.class})
class IdentificationPersistenceAdapterTest {

    private static final Instant ISSUED_AT = Instant.parse("2026-10-21T15:30:00Z");
    private static final Instant EXPIRES_AT = Instant.parse("2026-10-21T16:30:00Z");
    private static final ApplicantIdentity IDENTITY = new ApplicantIdentity(new Rfc("AAA010101AAA"), "EMPRESA SA");
    private static final String COMPLETE_RESULT =
        "{ \"resultado\" : true, \"numeroSerie\": 30001000000500003416.000, \"curp\":null, \"razonSocial\":\"ÑANDÚ\" }";

    @Autowired
    private NoticeAcceptancePersistenceAdapter acceptances;

    @Autowired
    private ApplicantSignatureEvidencePersistenceAdapter evidences;

    @Autowired
    private ApplicantSignatureEvidenceJpaRepository evidenceRepository;

    @Autowired
    private RegistrationContextJpaRepository contextRepository;

    @Autowired
    private NoticeAcceptanceJpaRepository acceptanceRepository;

    @Autowired
    private EntityManager entityManager;

    private KeyPair keyPair;
    private RegistrationContextAdapter contexts;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        keyPair = generator.generateKeyPair();
        RSAKey key = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
            .privateKey((RSAPrivateKey) keyPair.getPrivate()).build();
        contexts = new RegistrationContextAdapter(contextRepository, evidenceRepository, acceptanceRepository,
            new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(key))), "test-issuer", "test-audience");
    }

    // Fixed instants are in the past; these tests check claims and contexts, not expiry validation.
    private NimbusJwtDecoder decoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey((RSAPublicKey) keyPair.getPublic()).build();
        decoder.setJwtValidator(jwt -> OAuth2TokenValidatorResult.success());
        return decoder;
    }

    private NoticeAcceptanceReference newAcceptance() {
        return acceptances.save(new NoticeAcceptance("1.0", ISSUED_AT));
    }

    private SignatureEvidenceReference newEvidence() {
        return evidences.save(new ApplicantSignatureEvidence(COMPLETE_RESULT), IDENTITY);
    }

    @Test
    void US01_CA013_evidence_storesOriginalResultVerbatimWithVerifiedIdentity() {
        SignatureEvidenceReference reference = newEvidence();
        entityManager.clear();

        ApplicantSignatureEvidenceJpaEntity stored =
            evidenceRepository.findById(UUID.fromString(reference.value())).orElseThrow();
        assertEquals(COMPLETE_RESULT, stored.getCompleteResult());
        assertEquals("AAA010101AAA", stored.getRfc());
        assertEquals("EMPRESA SA", stored.getVerifiedLegalName());
    }

    @Test
    void US01_CA027_issue_storesContextAndSignsTokenWithRequiredClaims() {
        NoticeAcceptanceReference acceptance = newAcceptance();
        SignatureEvidenceReference evidence = newEvidence();

        String token = contexts.issue(IDENTITY, evidence, acceptance, ISSUED_AT, EXPIRES_AT);
        entityManager.clear();

        Jwt jwt = decoder().decode(token);
        assertEquals("AAA010101AAA", jwt.getSubject());
        assertEquals("test-issuer", jwt.getClaimAsString("iss"));
        assertEquals("test-audience", jwt.getAudience().getFirst());
        assertEquals(ISSUED_AT, jwt.getIssuedAt());
        assertEquals(EXPIRES_AT, jwt.getExpiresAt());
        assertEquals("RS256", jwt.getHeaders().get("alg"));

        RegistrationContextJpaEntity context = contextRepository.findById(UUID.fromString(jwt.getId())).orElseThrow();
        assertEquals("AAA010101AAA", context.getRfc());
        assertEquals(ISSUED_AT, context.getIssuedAt());
        assertEquals(EXPIRES_AT, context.getExpiresAt());
        assertEquals(UUID.fromString(evidence.value()), context.getSignatureEvidence().getId());
        assertEquals(UUID.fromString(acceptance.value()), context.getNoticeAcceptance().getId());
    }

    @Test
    void US01_CA019_sameRfc_keepsSeveralActiveContexts() {
        String first = contexts.issue(IDENTITY, newEvidence(), newAcceptance(), ISSUED_AT, EXPIRES_AT);
        String second = contexts.issue(IDENTITY, newEvidence(), newAcceptance(), ISSUED_AT, EXPIRES_AT);

        NimbusJwtDecoder decoder = decoder();
        Instant during = ISSUED_AT.plusSeconds(60);
        assertTrue(contexts.isActive(decoder.decode(first).getId(), "AAA010101AAA", during));
        assertTrue(contexts.isActive(decoder.decode(second).getId(), "AAA010101AAA", during));
    }

    @Test
    void US01_CA026_context_isInactiveForOtherRfcUnknownJtiOrFromExpiry() {
        String token = contexts.issue(IDENTITY, newEvidence(), newAcceptance(), ISSUED_AT, EXPIRES_AT);
        String jti = decoder().decode(token).getId();

        assertFalse(contexts.isActive(jti, "BBB010101BBB", ISSUED_AT));
        assertFalse(contexts.isActive(UUID.randomUUID().toString(), "AAA010101AAA", ISSUED_AT));
        assertFalse(contexts.isActive("not-a-uuid", "AAA010101AAA", ISSUED_AT));
        assertFalse(contexts.isActive(jti, "AAA010101AAA", EXPIRES_AT));
        assertTrue(contexts.isActive(jti, "AAA010101AAA", EXPIRES_AT.minusSeconds(1)));
    }

    @Test
    void US03_CA007_acceptance_backsOnlyOneToken() {
        NoticeAcceptanceReference acceptance = newAcceptance();
        contexts.issue(IDENTITY, newEvidence(), acceptance, ISSUED_AT, EXPIRES_AT);

        assertFalse(acceptances.isAvailableForIdentification(acceptance));
        SignatureEvidenceReference otherEvidence = newEvidence();
        assertThrows(NoticeAcceptanceAlreadyUsedException.class,
            () -> contexts.issue(IDENTITY, otherEvidence, acceptance, ISSUED_AT, EXPIRES_AT));
    }

    @Test
    void US03_CA004_acceptanceAvailability_requiresExistingUnusedReference() {
        assertTrue(acceptances.isAvailableForIdentification(newAcceptance()));
        assertFalse(acceptances.isAvailableForIdentification(new NoticeAcceptanceReference(UUID.randomUUID().toString())));
        assertFalse(acceptances.isAvailableForIdentification(new NoticeAcceptanceReference("not-a-uuid")));
    }

    @Test
    void ADR0017_identificationSchema_usesApprovedSqlTypesAndUniqueAcceptance() {
        assertEquals("text", columnType("applicant_signature_evidence", "complete_result"));
        assertEquals("text", columnType("applicant_signature_evidence", "verified_legal_name"));
        assertEquals("uuid", columnType("registration_context", "jti"));
        assertEquals("timestamp(6) with time zone", columnType("registration_context", "expires_at"));
        assertEquals(1L, ((Number) entityManager.createNativeQuery("""
                SELECT COUNT(*) FROM information_schema.table_constraints tc
                JOIN information_schema.key_column_usage k ON tc.constraint_name = k.constraint_name
                WHERE tc.table_name = 'registration_context' AND tc.constraint_type = 'UNIQUE'
                AND k.column_name = 'notice_acceptance_id'""").getSingleResult()).longValue());
    }

    @Test
    void US04_CA006_resolve_returnsIdentityEvidenceAndAcceptanceOfTheContext() {
        NoticeAcceptanceReference acceptance = newAcceptance();
        SignatureEvidenceReference evidence = newEvidence();
        String jti = decoder().decode(contexts.issue(IDENTITY, evidence, acceptance, ISSUED_AT, EXPIRES_AT)).getId();
        entityManager.clear();

        RegistrationSession session = contexts.resolve(jti).orElseThrow();
        assertEquals(IDENTITY, session.identity());
        assertEquals(COMPLETE_RESULT, session.evidence().completeResult());
        assertEquals(evidence, session.evidenceReference());
        assertEquals(new NoticeAcceptance("1.0", ISSUED_AT), session.acceptance());
        assertEquals(acceptance, session.acceptanceReference());
    }

    @Test
    void ADR0015_resolve_unknownOrMalformedJti_isEmpty() {
        assertTrue(contexts.resolve(UUID.randomUUID().toString()).isEmpty());
        assertTrue(contexts.resolve("not-a-uuid").isEmpty());
    }

    private String columnType(String table, String column) {
        return (String) entityManager.createNativeQuery(
                "SELECT format_type(atttypid, atttypmod) FROM pg_attribute "
                    + "WHERE attrelid = to_regclass(?1) AND attname = ?2")
            .setParameter(1, table).setParameter(2, column).getSingleResult();
    }
}
