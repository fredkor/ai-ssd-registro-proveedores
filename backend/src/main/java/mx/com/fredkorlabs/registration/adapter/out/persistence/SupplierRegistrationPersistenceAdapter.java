package mx.com.fredkorlabs.registration.adapter.out.persistence;

import mx.com.fredkorlabs.registration.application.port.out.DuplicateRegistrationException;
import mx.com.fredkorlabs.registration.application.port.out.NoticeAcceptanceReference;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationStatusView;
import mx.com.fredkorlabs.registration.application.port.out.SignatureEvidenceReference;
import mx.com.fredkorlabs.registration.application.port.out.StoredRegistration;
import mx.com.fredkorlabs.registration.application.port.out.SupplierRegistrationPersistencePort;
import mx.com.fredkorlabs.registration.domain.model.ApplicantIdentity;
import mx.com.fredkorlabs.registration.domain.model.ApplicantSignatureEvidence;
import mx.com.fredkorlabs.registration.domain.model.NoticeAcceptance;
import mx.com.fredkorlabs.registration.domain.model.Rfc;
import mx.com.fredkorlabs.registration.domain.model.SupplierRegistration;
import mx.com.fredkorlabs.registration.domain.model.SupplierRegistrationId;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class SupplierRegistrationPersistenceAdapter implements SupplierRegistrationPersistencePort {

    // Unique keys that a second registration for the same RFC collides with (V2 and V3 migrations).
    private static final Set<String> DUPLICATE_CONSTRAINTS = Set.of(
        "supplier_registration_rfc_uk",
        "supplier_registration_notice_acceptance_uk",
        "supplier_registration_initial_evidence_uk",
        "registration_signature_evidence_evidence_uk");

    private final SupplierRegistrationJpaRepository repository;
    private final NoticeAcceptanceJpaRepository acceptances;
    private final ApplicantSignatureEvidenceJpaRepository evidences;

    public SupplierRegistrationPersistenceAdapter(SupplierRegistrationJpaRepository repository,
                                                  NoticeAcceptanceJpaRepository acceptances,
                                                  ApplicantSignatureEvidenceJpaRepository evidences) {
        this.repository = repository;
        this.acceptances = acceptances;
        this.evidences = evidences;
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

    @Override
    public Optional<StoredRegistration> findForUpdate(SupplierRegistrationId id) {
        return repository.findLockedById(id.value()).map(this::toStoredRegistration);
    }

    @Override
    public void saveResume(SupplierRegistration registration, SignatureEvidenceReference newEvidence,
                           NoticeAcceptanceReference newAcceptance) {
        SupplierRegistrationJpaEntity entity = repository.findById(registration.id().value()).orElseThrow();
        entity.resume(registration.applicant().legalName(), UUID.fromString(newAcceptance.value()),
            UUID.fromString(newEvidence.value()));
        repository.flush();
    }

    // General Data, fiscal document and CUCoP are not persisted yet; they are absent until their operations exist.
    private StoredRegistration toStoredRegistration(SupplierRegistrationJpaEntity entity) {
        NoticeAcceptanceJpaEntity acceptance = acceptances.findById(entity.getNoticeAcceptanceId()).orElseThrow();
        Map<UUID, ApplicantSignatureEvidenceJpaEntity> history = evidences.findAllById(entity.getSignatureEvidenceIds())
            .stream().collect(Collectors.toMap(ApplicantSignatureEvidenceJpaEntity::getId, Function.identity()));
        // The initial evidence first; the history has no order of its own.
        List<ApplicantSignatureEvidence> evidence = new ArrayList<>();
        evidence.add(new ApplicantSignatureEvidence(history.remove(entity.getInitialEvidenceId()).getCompleteResult()));
        history.values().forEach(other -> evidence.add(new ApplicantSignatureEvidence(other.getCompleteResult())));

        SupplierRegistration registration = SupplierRegistration.reconstitute(
            new SupplierRegistrationId(entity.getId()),
            entity.getStatus(),
            new ApplicantIdentity(new Rfc(entity.getRfc()), entity.getLegalName()),
            new NoticeAcceptance(acceptance.getVersion(), acceptance.getAcceptedAt()),
            evidence,
            null,
            null,
            null,
            entity.getSectionProgress());
        return new StoredRegistration(
            registration,
            new NoticeAcceptanceReference(entity.getNoticeAcceptanceId().toString()),
            entity.getSignatureEvidenceIds().stream()
                .map(evidenceId -> new SignatureEvidenceReference(evidenceId.toString()))
                .collect(Collectors.toSet()));
    }
}
