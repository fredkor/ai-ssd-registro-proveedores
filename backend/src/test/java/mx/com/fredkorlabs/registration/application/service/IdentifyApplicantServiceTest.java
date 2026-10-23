package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.IdentifyApplicantCommand;
import mx.com.fredkorlabs.registration.application.port.in.IdentifyApplicantResult;
import mx.com.fredkorlabs.registration.application.port.in.InvalidIdentificationDataException;
import mx.com.fredkorlabs.registration.application.port.in.SignatureResultInput;
import mx.com.fredkorlabs.registration.application.port.in.SignatureVerificationFailedException;
import mx.com.fredkorlabs.registration.application.port.out.ApplicantSignatureEvidencePersistencePort;
import mx.com.fredkorlabs.registration.application.port.out.ApplicantSignatureVerificationPort;
import mx.com.fredkorlabs.registration.application.port.out.NoticeAcceptanceAlreadyUsedException;
import mx.com.fredkorlabs.registration.application.port.out.NoticeAcceptancePersistencePort;
import mx.com.fredkorlabs.registration.application.port.out.NoticeAcceptanceReference;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationContextPort;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationSession;
import mx.com.fredkorlabs.registration.application.port.out.SignatureCheck;
import mx.com.fredkorlabs.registration.application.port.out.SignatureEvidenceReference;
import mx.com.fredkorlabs.registration.domain.exception.ApplicantRfcMismatchException;
import mx.com.fredkorlabs.registration.domain.exception.InvalidCertificateIdentityException;
import mx.com.fredkorlabs.registration.domain.model.ApplicantIdentity;
import mx.com.fredkorlabs.registration.domain.model.ApplicantSignatureEvidence;
import mx.com.fredkorlabs.registration.domain.model.CertificateSubjectAttribute;
import mx.com.fredkorlabs.registration.domain.model.NoticeAcceptance;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class IdentifyApplicantServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-21T15:30:00.987654Z");
    private static final String ACCEPTANCE = "acceptance-1";
    private static final String COMPLETE_RESULT = "{\"resultado\":true,\"numeroSerie\":30001000000500003416,\"curp\":null}";
    private static final List<CertificateSubjectAttribute> SUBJECT = List.of(
        new CertificateSubjectAttribute("2.5.4.3", "EMPRESA CERTIFICADO SA"),
        new CertificateSubjectAttribute("2.5.4.45", "AAA010101AAA/HEGT7610034S2"));

    private final FakeAcceptances acceptances = new FakeAcceptances(Set.of(ACCEPTANCE));
    private final FakeVerification verification = new FakeVerification(new SignatureCheck(SignatureCheck.Status.VALID, SUBJECT));
    private final FakeEvidences evidences = new FakeEvidences();
    private final FakeContexts contexts = new FakeContexts();
    private final IdentifyApplicantService service = new IdentifyApplicantService(
        acceptances, verification, evidences, contexts, Clock.fixed(NOW, ZoneOffset.UTC));

    private static IdentifyApplicantCommand command(boolean successful, String rfc) {
        return new IdentifyApplicantCommand(ACCEPTANCE, new SignatureResultInput(
            COMPLETE_RESULT, successful, "Temporal", new byte[] {1}, new byte[] {2}, rfc, "RAZON SOCIAL PKI"));
    }

    @Test
    void US01_CA017_successfulIdentification_issuesTokenLinkedToRfcEvidenceAndAcceptance() {
        IdentifyApplicantResult result = service.execute(command(true, "AAA010101AAA"));

        assertEquals("token-1", result.registrationToken());
        assertEquals("AAA010101AAA", contexts.issued.getFirst().identity().rfc().value());
        assertEquals("evidence-1", contexts.issued.getFirst().evidence().value());
        assertEquals(ACCEPTANCE, contexts.issued.getFirst().acceptance().value());
    }

    @Test
    void US01_CA017_tokenExpiresSixtyMinutesAfterIssueInWholeSeconds() {
        IdentifyApplicantResult result = service.execute(command(true, "AAA010101AAA"));

        assertEquals(Instant.parse("2026-10-21T15:30:00Z"), contexts.issued.getFirst().issuedAt());
        assertEquals(Instant.parse("2026-10-21T16:30:00Z"), result.tokenExpiresAt());
        assertEquals(result.tokenExpiresAt(), contexts.issued.getFirst().expiresAt());
    }

    @Test
    void US01_CA002_resultContainsVerifiedIdentityAndDerivedValues() {
        IdentifyApplicantResult result = service.execute(command(true, "AAA010101AAA"));

        assertEquals("AAA010101AAA", result.rfc());
        assertEquals("EMPRESA CERTIFICADO SA", result.legalName());
        assertEquals("LEGAL_ENTITY", result.taxRegime());
        assertEquals(1, result.taxRegimeId());
        assertEquals("aaa010101aaa", result.username());
    }

    @Test
    void US01_CA013_completeResultIsPersistedUnchanged() {
        service.execute(command(true, "AAA010101AAA"));

        assertEquals(COMPLETE_RESULT, evidences.saved.getFirst().evidence().completeResult());
    }

    @Test
    void US01_CA024_evidenceKeepsReceivedNameWhileIdentityUsesCertificateName() {
        service.execute(command(true, "AAA010101AAA"));

        assertEquals("EMPRESA CERTIFICADO SA", evidences.saved.getFirst().identity().legalName());
        assertTrue(evidences.saved.getFirst().evidence().completeResult().contains("\"resultado\":true"));
    }

    @Test
    void US01_CA016_unsuccessfulResult_isRejectedWithoutTokenOrEvidence() {
        assertThrows(InvalidIdentificationDataException.class, () -> service.execute(command(false, "AAA010101AAA")));
        assertNothingPersisted();
    }

    @Test
    void US03_CA004_unknownOrUsedAcceptance_isRejectedWithoutVerifying() {
        IdentifyApplicantService withoutAcceptance = new IdentifyApplicantService(
            new FakeAcceptances(Set.of()), verification, evidences, contexts, Clock.fixed(NOW, ZoneOffset.UTC));

        assertThrows(InvalidIdentificationDataException.class,
            () -> withoutAcceptance.execute(command(true, "AAA010101AAA")));
        assertEquals(0, verification.calls);
        assertNothingPersisted();
    }

    @Test
    void US03_CA007_acceptanceConsumedConcurrently_isRejected() {
        contexts.failWithAcceptanceAlreadyUsed = true;

        assertThrows(InvalidIdentificationDataException.class, () -> service.execute(command(true, "AAA010101AAA")));
    }

    @Test
    void US01_CA015_unreadableCertificate_isValidationError() {
        verification.result = SignatureCheck.unreadableCertificate();

        assertThrows(InvalidIdentificationDataException.class, () -> service.execute(command(true, "AAA010101AAA")));
        assertNothingPersisted();
    }

    @Test
    void US01_CA015_invalidSeal_isRejectedWithoutTokenOrEvidence() {
        verification.result = new SignatureCheck(SignatureCheck.Status.INVALID, SUBJECT);

        assertThrows(SignatureVerificationFailedException.class, () -> service.execute(command(true, "AAA010101AAA")));
        assertNothingPersisted();
    }

    @Test
    void US01_CA022_rfcMismatch_isRejectedWithoutTokenOrEvidence() {
        assertThrows(ApplicantRfcMismatchException.class, () -> service.execute(command(true, "HEGT7610034S2")));
        assertNothingPersisted();
    }

    @Test
    void US01_CA025_missingCertificateName_isValidationErrorEvenIfSealIsInvalid() {
        verification.result = new SignatureCheck(SignatureCheck.Status.INVALID,
            List.of(new CertificateSubjectAttribute("2.5.4.45", "AAA010101AAA")));

        assertThrows(InvalidCertificateIdentityException.class, () -> service.execute(command(true, "AAA010101AAA")));
        assertNothingPersisted();
    }

    @Test
    void US01_CA019_secondIdentificationOfSameRfc_issuesAnotherTokenWithoutInvalidating() {
        IdentifyApplicantService secondCall = new IdentifyApplicantService(
            new FakeAcceptances(Set.of(ACCEPTANCE)), verification, evidences, contexts, Clock.fixed(NOW, ZoneOffset.UTC));

        IdentifyApplicantResult first = service.execute(command(true, "AAA010101AAA"));
        IdentifyApplicantResult second = secondCall.execute(command(true, "AAA010101AAA"));

        assertNotEquals(first.registrationToken(), second.registrationToken());
        assertEquals(2, contexts.issued.size());
    }

    private void assertNothingPersisted() {
        assertTrue(evidences.saved.isEmpty());
        assertTrue(contexts.issued.isEmpty());
    }

    private record SavedEvidence(ApplicantSignatureEvidence evidence, ApplicantIdentity identity) {}

    private record IssuedContext(ApplicantIdentity identity, SignatureEvidenceReference evidence,
                                 NoticeAcceptanceReference acceptance, Instant issuedAt, Instant expiresAt) {}

    private static final class FakeAcceptances implements NoticeAcceptancePersistencePort {
        private final Set<String> available;

        FakeAcceptances(Set<String> available) { this.available = available; }

        @Override
        public NoticeAcceptanceReference save(NoticeAcceptance acceptance) { throw new UnsupportedOperationException(); }

        @Override
        public boolean isAvailableForIdentification(NoticeAcceptanceReference reference) {
            return available.contains(reference.value());
        }
    }

    private static final class FakeVerification implements ApplicantSignatureVerificationPort {
        private SignatureCheck result;
        private int calls;

        FakeVerification(SignatureCheck result) { this.result = result; }

        @Override
        public SignatureCheck verify(byte[] certificate, String signedContent, byte[] signature) {
            calls++;
            return result;
        }
    }

    private static final class FakeEvidences implements ApplicantSignatureEvidencePersistencePort {
        private final List<SavedEvidence> saved = new ArrayList<>();

        @Override
        public SignatureEvidenceReference save(ApplicantSignatureEvidence evidence, ApplicantIdentity identity) {
            saved.add(new SavedEvidence(evidence, identity));
            return new SignatureEvidenceReference("evidence-" + saved.size());
        }
    }

    private static final class FakeContexts implements RegistrationContextPort {
        private final List<IssuedContext> issued = new ArrayList<>();
        private boolean failWithAcceptanceAlreadyUsed;

        @Override
        public String issue(ApplicantIdentity identity, SignatureEvidenceReference evidence,
                            NoticeAcceptanceReference acceptance, Instant issuedAt, Instant expiresAt) {
            if (failWithAcceptanceAlreadyUsed) {
                throw new NoticeAcceptanceAlreadyUsedException("used", null);
            }
            issued.add(new IssuedContext(identity, evidence, acceptance, issuedAt, expiresAt));
            return "token-" + issued.size();
        }

        @Override
        public boolean isActive(String jti, String rfc, Instant now) { return true; }

        @Override
        public Optional<RegistrationSession> resolve(String jti) { return Optional.empty(); }
    }
}
