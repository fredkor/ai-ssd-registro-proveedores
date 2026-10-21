package mx.com.fredkorlabs.registration.application.port.out;

import mx.com.fredkorlabs.registration.domain.model.ApplicantIdentity;
import mx.com.fredkorlabs.registration.domain.model.ApplicantSignatureEvidence;

public interface ApplicantSignatureEvidencePersistencePort {
    SignatureEvidenceReference save(ApplicantSignatureEvidence evidence, ApplicantIdentity verifiedIdentity);
}
