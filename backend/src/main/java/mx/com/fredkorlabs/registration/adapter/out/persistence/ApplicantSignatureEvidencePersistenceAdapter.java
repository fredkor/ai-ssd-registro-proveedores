package mx.com.fredkorlabs.registration.adapter.out.persistence;

import mx.com.fredkorlabs.registration.application.port.out.ApplicantSignatureEvidencePersistencePort;
import mx.com.fredkorlabs.registration.application.port.out.SignatureEvidenceReference;
import mx.com.fredkorlabs.registration.domain.model.ApplicantIdentity;
import mx.com.fredkorlabs.registration.domain.model.ApplicantSignatureEvidence;
import org.springframework.stereotype.Component;

@Component
public class ApplicantSignatureEvidencePersistenceAdapter implements ApplicantSignatureEvidencePersistencePort {

    private final ApplicantSignatureEvidenceJpaRepository repository;

    public ApplicantSignatureEvidencePersistenceAdapter(ApplicantSignatureEvidenceJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public SignatureEvidenceReference save(ApplicantSignatureEvidence evidence, ApplicantIdentity verifiedIdentity) {
        ApplicantSignatureEvidenceJpaEntity saved = repository.saveAndFlush(new ApplicantSignatureEvidenceJpaEntity(
            verifiedIdentity.rfc().value(), verifiedIdentity.legalName(), evidence.completeResult()));
        return new SignatureEvidenceReference(saved.getId().toString());
    }
}
