package mx.com.fredkorlabs.registration.adapter.out.persistence;

import mx.com.fredkorlabs.registration.application.port.out.DuplicateRegistrationException;
import mx.com.fredkorlabs.registration.application.port.out.NoticeAcceptanceReference;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationStatusView;
import mx.com.fredkorlabs.registration.application.port.out.SignatureEvidenceReference;
import mx.com.fredkorlabs.registration.application.port.out.SupplierRegistrationPersistencePort;
import mx.com.fredkorlabs.registration.domain.model.Rfc;
import mx.com.fredkorlabs.registration.domain.model.SupplierRegistration;
import mx.com.fredkorlabs.registration.domain.model.SupplierRegistrationId;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
public class SupplierRegistrationPersistenceAdapter implements SupplierRegistrationPersistencePort {

    // Unique keys that a second registration for the same RFC collides with (V2 and V3 migrations).
    private static final Set<String> DUPLICATE_CONSTRAINTS = Set.of(
        "supplier_registration_rfc_uk",
        "supplier_registration_notice_acceptance_uk",
        "supplier_registration_initial_evidence_uk",
        "registration_signature_evidence_evidence_uk");

    private final SupplierRegistrationJpaRepository repository;

    public SupplierRegistrationPersistenceAdapter(SupplierRegistrationJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<RegistrationStatusView> findStatusByRfc(Rfc rfc) {
        return repository.findByRfc(rfc.value())
            .map(entity -> new RegistrationStatusView(new SupplierRegistrationId(entity.getId()), entity.getStatus()));
    }

    @Override
    public void insert(SupplierRegistration registration, SignatureEvidenceReference initialEvidence,
                       NoticeAcceptanceReference acceptance) {
        SupplierRegistrationJpaEntity entity = new SupplierRegistrationJpaEntity(
            registration.id().value(),
            registration.applicant().rfc().value(),
            registration.applicant().legalName(),
            registration.status(),
            UUID.fromString(acceptance.value()),
            UUID.fromString(initialEvidence.value()));
        registration.progress().forEach(progress ->
            entity.getSectionProgress().put(progress.section(), progress.status()));
        try {
            repository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException e) {
            if (e.getCause() instanceof ConstraintViolationException violation
                    && DUPLICATE_CONSTRAINTS.contains(violation.getConstraintName())) {
                throw new DuplicateRegistrationException("Registration already exists for the RFC", e);
            }
            throw e;
        }
    }
}
