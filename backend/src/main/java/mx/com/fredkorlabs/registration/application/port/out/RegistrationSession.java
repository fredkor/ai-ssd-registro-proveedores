package mx.com.fredkorlabs.registration.application.port.out;

import mx.com.fredkorlabs.registration.domain.model.ApplicantIdentity;
import mx.com.fredkorlabs.registration.domain.model.ApplicantSignatureEvidence;
import mx.com.fredkorlabs.registration.domain.model.NoticeAcceptance;

import java.util.Objects;

/** Identity, signature evidence and notice acceptance bound to a token's context (ADR-0015). */
public record RegistrationSession(
    ApplicantIdentity identity,
    ApplicantSignatureEvidence evidence,
    SignatureEvidenceReference evidenceReference,
    NoticeAcceptance acceptance,
    NoticeAcceptanceReference acceptanceReference
) {
    public RegistrationSession {
        Objects.requireNonNull(identity, "Identity is required");
        Objects.requireNonNull(evidence, "Evidence is required");
        Objects.requireNonNull(evidenceReference, "Evidence reference is required");
        Objects.requireNonNull(acceptance, "Acceptance is required");
        Objects.requireNonNull(acceptanceReference, "Acceptance reference is required");
    }
}
