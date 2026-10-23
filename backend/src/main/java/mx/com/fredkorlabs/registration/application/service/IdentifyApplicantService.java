package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.IdentifyApplicantCommand;
import mx.com.fredkorlabs.registration.application.port.in.IdentifyApplicantResult;
import mx.com.fredkorlabs.registration.application.port.in.IdentifyApplicantUseCase;
import mx.com.fredkorlabs.registration.application.port.in.InvalidIdentificationDataException;
import mx.com.fredkorlabs.registration.application.port.in.SignatureResultInput;
import mx.com.fredkorlabs.registration.application.port.in.SignatureVerificationFailedException;
import mx.com.fredkorlabs.registration.application.port.out.ApplicantSignatureEvidencePersistencePort;
import mx.com.fredkorlabs.registration.application.port.out.ApplicantSignatureVerificationPort;
import mx.com.fredkorlabs.registration.application.port.out.NoticeAcceptanceAlreadyUsedException;
import mx.com.fredkorlabs.registration.application.port.out.NoticeAcceptancePersistencePort;
import mx.com.fredkorlabs.registration.application.port.out.NoticeAcceptanceReference;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationContextPort;
import mx.com.fredkorlabs.registration.application.port.out.SignatureCheck;
import mx.com.fredkorlabs.registration.application.port.out.SignatureEvidenceReference;
import mx.com.fredkorlabs.registration.domain.model.ApplicantIdentity;
import mx.com.fredkorlabs.registration.domain.model.ApplicantSignatureEvidence;
import mx.com.fredkorlabs.registration.domain.model.CertificateIdentity;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class IdentifyApplicantService implements IdentifyApplicantUseCase {

    static final Duration TOKEN_VALIDITY = Duration.ofMinutes(60);

    private final NoticeAcceptancePersistencePort noticeAcceptancePersistence;
    private final ApplicantSignatureVerificationPort signatureVerification;
    private final ApplicantSignatureEvidencePersistencePort signatureEvidencePersistence;
    private final RegistrationContextPort registrationContext;
    private final Clock clock;

    public IdentifyApplicantService(NoticeAcceptancePersistencePort noticeAcceptancePersistence,
                                    ApplicantSignatureVerificationPort signatureVerification,
                                    ApplicantSignatureEvidencePersistencePort signatureEvidencePersistence,
                                    RegistrationContextPort registrationContext,
                                    Clock clock) {
        this.noticeAcceptancePersistence = noticeAcceptancePersistence;
        this.signatureVerification = signatureVerification;
        this.signatureEvidencePersistence = signatureEvidencePersistence;
        this.registrationContext = registrationContext;
        this.clock = clock;
    }

    @Override
    public IdentifyApplicantResult execute(IdentifyApplicantCommand command) {
        SignatureResultInput signatureResult = command.signatureResult();
        if (!signatureResult.successful()) {
            throw new InvalidIdentificationDataException("Signature result is not successful");
        }
        NoticeAcceptanceReference acceptance = availableAcceptance(command.noticeAcceptanceId());

        // Order (ADR-0004/ADR-0016): unreadable data -> 400, negative verification or RFC mismatch -> 422.
        SignatureCheck check = signatureVerification.verify(
            signatureResult.certificate(), signatureResult.signedContent(), signatureResult.signature());
        if (check.status() == SignatureCheck.Status.UNREADABLE_CERTIFICATE) {
            throw new InvalidIdentificationDataException("Certificate cannot be read");
        }
        CertificateIdentity certificateIdentity = CertificateIdentity.from(check.subject());
        if (check.status() == SignatureCheck.Status.INVALID) {
            throw new SignatureVerificationFailedException("Seal does not match the signed content and certificate");
        }
        ApplicantIdentity identity = certificateIdentity.matching(signatureResult.rfc());

        SignatureEvidenceReference evidence = signatureEvidencePersistence.save(
            new ApplicantSignatureEvidence(signatureResult.completeResult()), identity);

        // JWT iat/exp are whole seconds; keep the response and the context aligned with them.
        Instant issuedAt = clock.instant().truncatedTo(ChronoUnit.SECONDS);
        Instant expiresAt = issuedAt.plus(TOKEN_VALIDITY);
        String token = issueToken(identity, evidence, acceptance, issuedAt, expiresAt);

        return new IdentifyApplicantResult(
            identity.rfc().value(),
            identity.legalName(),
            identity.taxRegime().name(),
            identity.taxRegime().catalogId(),
            identity.username().value(),
            token,
            expiresAt);
    }

    private NoticeAcceptanceReference availableAcceptance(String noticeAcceptanceId) {
        if (noticeAcceptanceId == null || noticeAcceptanceId.isBlank()) {
            throw new InvalidIdentificationDataException("Notice acceptance is required");
        }
        NoticeAcceptanceReference reference = new NoticeAcceptanceReference(noticeAcceptanceId);
        if (!noticeAcceptancePersistence.isAvailableForIdentification(reference)) {
            throw new InvalidIdentificationDataException("Notice acceptance not found or already used");
        }
        return reference;
    }

    private String issueToken(ApplicantIdentity identity, SignatureEvidenceReference evidence,
                              NoticeAcceptanceReference acceptance, Instant issuedAt, Instant expiresAt) {
        try {
            return registrationContext.issue(identity, evidence, acceptance, issuedAt, expiresAt);
        } catch (NoticeAcceptanceAlreadyUsedException e) {
            throw new InvalidIdentificationDataException("Notice acceptance not found or already used");
        }
    }
}
