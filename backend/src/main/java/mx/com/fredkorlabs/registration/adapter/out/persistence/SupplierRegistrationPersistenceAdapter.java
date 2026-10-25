package mx.com.fredkorlabs.registration.adapter.out.persistence;

import mx.com.fredkorlabs.registration.application.port.out.CucopCatalogPort;
import mx.com.fredkorlabs.registration.application.port.out.DuplicateRegistrationException;
import mx.com.fredkorlabs.registration.application.port.out.FiscalDocumentView;
import mx.com.fredkorlabs.registration.application.port.out.NoticeAcceptanceReference;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationStatusView;
import mx.com.fredkorlabs.registration.application.port.out.SignatureEvidenceReference;
import mx.com.fredkorlabs.registration.application.port.out.StoredRegistration;
import mx.com.fredkorlabs.registration.application.port.out.SupplierRegistrationPersistencePort;
import mx.com.fredkorlabs.registration.domain.model.ApplicantIdentity;
import mx.com.fredkorlabs.registration.domain.model.ApplicantSignatureEvidence;
import mx.com.fredkorlabs.registration.domain.model.CucopParticipation;
import mx.com.fredkorlabs.registration.domain.model.CucopSpecificItem;
import mx.com.fredkorlabs.registration.domain.model.FiscalIdentificationDocument;
import mx.com.fredkorlabs.registration.domain.model.ForeignFiscalCode;
import mx.com.fredkorlabs.registration.domain.model.GeneralData;
import mx.com.fredkorlabs.registration.domain.model.Nationality;
import mx.com.fredkorlabs.registration.domain.model.NoticeAcceptance;
import mx.com.fredkorlabs.registration.domain.model.Origin;
import mx.com.fredkorlabs.registration.domain.model.PhoneNumber;
import mx.com.fredkorlabs.registration.domain.model.RegistrationSection;
import mx.com.fredkorlabs.registration.domain.model.Rfc;
import mx.com.fredkorlabs.registration.domain.model.StoredDocumentReference;
import mx.com.fredkorlabs.registration.domain.model.SupplierRegistration;
import mx.com.fredkorlabs.registration.domain.model.SupplierRegistrationId;
import mx.com.fredkorlabs.registration.domain.model.TaxRegime;
import mx.com.fredkorlabs.registration.domain.model.Website;
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
    private final RegistrationFiscalDocumentJpaRepository fiscalDocuments;
    private final FileMetadataJpaRepository files;
    private final GeneralDataJpaRepository generalData;
    private final CucopParticipationJpaRepository cucopParticipations;
    private final CucopCatalogPort cucopCatalog;

    public SupplierRegistrationPersistenceAdapter(SupplierRegistrationJpaRepository repository,
                                                  NoticeAcceptanceJpaRepository acceptances,
                                                  ApplicantSignatureEvidenceJpaRepository evidences,
                                                  RegistrationFiscalDocumentJpaRepository fiscalDocuments,
                                                  FileMetadataJpaRepository files,
                                                  GeneralDataJpaRepository generalData,
                                                  CucopParticipationJpaRepository cucopParticipations,
                                                  CucopCatalogPort cucopCatalog) {
        this.repository = repository;
        this.acceptances = acceptances;
        this.evidences = evidences;
        this.fiscalDocuments = fiscalDocuments;
        this.files = files;
        this.generalData = generalData;
        this.cucopParticipations = cucopParticipations;
        this.cucopCatalog = cucopCatalog;
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
    public Optional<StoredRegistration> find(SupplierRegistrationId id) {
        return repository.findById(id.value()).map(this::toStoredRegistration);
    }

    @Override
    public void saveResume(SupplierRegistration registration, SignatureEvidenceReference newEvidence,
                           NoticeAcceptanceReference newAcceptance) {
        SupplierRegistrationJpaEntity entity = repository.findById(registration.id().value()).orElseThrow();
        entity.resume(registration.applicant().legalName(), UUID.fromString(newAcceptance.value()),
            UUID.fromString(newEvidence.value()));
        repository.flush();
    }

    @Override
    public void saveFiscalDocumentAssociation(SupplierRegistrationId registrationId, StoredDocumentReference document) {
        fiscalDocuments.saveAndFlush(new RegistrationFiscalDocumentJpaEntity(registrationId.value(), document.value()));
    }

    @Override
    public void saveFiscalDocumentDissociation(SupplierRegistration registration) {
        UUID id = registration.id().value();
        fiscalDocuments.deleteById(id);
        repository.findById(id).orElseThrow().getSectionProgress()
            .put(RegistrationSection.GENERAL_DATA, registration.sectionStatus(RegistrationSection.GENERAL_DATA));
        repository.flush();
    }

    @Override
    public void saveGeneralData(SupplierRegistration registration) {
        UUID id = registration.id().value();
        GeneralData data = registration.generalData();
        GeneralDataJpaEntity entity = generalData.findById(id).orElseGet(() -> new GeneralDataJpaEntity(id));
        entity.replace(originId(data.origin()), data.countryId(), data.nationality().value(),
            data.foreignFiscalCode() == null ? null : data.foreignFiscalCode().value(), data.legalOwnershipId(),
            data.sectorId(), data.subsectorId(), data.businessActivityId(), data.stratificationId(),
            data.primaryActivityId(), data.workerCountRangeId(), data.annualSalesRangeId(),
            data.website() == null ? null : data.website().value(),
            data.phones() == null ? List.of() : data.phones().stream()
                .map(phone -> new PhoneEmbeddable(phone.number(), phone.extension()))
                .toList());
        generalData.save(entity);
        repository.findById(id).orElseThrow().getSectionProgress()
            .put(RegistrationSection.GENERAL_DATA, registration.sectionStatus(RegistrationSection.GENERAL_DATA));
        repository.flush();
    }

    @Override
    public void saveCucopParticipation(SupplierRegistration registration) {
        UUID id = registration.id().value();
        CucopParticipation participation = registration.cucopParticipation();
        CucopParticipationJpaEntity entity = cucopParticipations.findById(id)
            .orElseGet(() -> new CucopParticipationJpaEntity(id));
        entity.replace(participation.participationTypes(),
            participation.specificItems().stream().map(CucopSpecificItem::id).toList());
        cucopParticipations.save(entity);
        repository.findById(id).orElseThrow().getSectionProgress()
            .put(RegistrationSection.CUCOP, registration.sectionStatus(RegistrationSection.CUCOP));
        repository.flush();
    }

    // Stored selections were valid when saved; the FK guarantees each one is still a specific item.
    private CucopParticipation toCucopParticipation(CucopParticipationJpaEntity entity) {
        return new CucopParticipation(Set.copyOf(entity.getParticipationTypes()),
            // The catalog belongs to its own adapter; it is read through its port (architecture §3).
            Set.copyOf(cucopCatalog.findSpecificItems(entity.getSpecificItemIds())));
    }

    // Stored data was valid when saved: it is rebuilt without re-running the save rules (domain model §8).
    private GeneralData toGeneralData(GeneralDataJpaEntity entity, TaxRegime regime) {
        return new GeneralData(originOf(entity.getOriginId()), entity.getCountryId(),
            new Nationality(entity.getNationality()),
            entity.getForeignFiscalCode() == null ? null : new ForeignFiscalCode(entity.getForeignFiscalCode()),
            entity.getLegalOwnershipId(), entity.getSectorId(), entity.getSubsectorId(), entity.getBusinessActivityId(),
            entity.getStratificationId(), entity.getPrimaryActivityId(), entity.getWorkerCountRangeId(),
            entity.getAnnualSalesRangeId(), entity.getWebsite() == null ? null : new Website(entity.getWebsite()),
            regime == TaxRegime.INDIVIDUAL ? null : entity.getPhones().stream()
                .map(phone -> new PhoneNumber(phone.getNumber(), phone.getExtension()))
                .toList());
    }

    // Origin catalog ids (catalog-values.md, catalog 2; domain/catalogs.md): NACIONAL 1, EXTRANJERO 2.
    private static int originId(Origin origin) {
        return origin == Origin.NATIONAL ? 1 : 2;
    }

    private static Origin originOf(int originId) {
        return switch (originId) {
            case 1 -> Origin.NATIONAL;
            case 2 -> Origin.FOREIGN;
            default -> throw new IllegalStateException("Unknown origin in general data");
        };
    }

    private StoredRegistration toStoredRegistration(SupplierRegistrationJpaEntity entity) {
        NoticeAcceptanceJpaEntity acceptance = acceptances.findById(entity.getNoticeAcceptanceId()).orElseThrow();
        Map<UUID, ApplicantSignatureEvidenceJpaEntity> history = evidences.findAllById(entity.getSignatureEvidenceIds())
            .stream().collect(Collectors.toMap(ApplicantSignatureEvidenceJpaEntity::getId, Function.identity()));
        // The initial evidence first; the history has no order of its own.
        List<ApplicantSignatureEvidence> evidence = new ArrayList<>();
        evidence.add(new ApplicantSignatureEvidence(history.remove(entity.getInitialEvidenceId()).getCompleteResult()));
        history.values().forEach(other -> evidence.add(new ApplicantSignatureEvidence(other.getCompleteResult())));

        SupplierRegistrationId id = new SupplierRegistrationId(entity.getId());
        FileMetadataJpaEntity document = fiscalDocuments.findById(entity.getId())
            .map(association -> files.findById(association.getFileId()).orElseThrow())
            .orElse(null);
        ApplicantIdentity applicant = new ApplicantIdentity(new Rfc(entity.getRfc()), entity.getLegalName());
        SupplierRegistration registration = SupplierRegistration.reconstitute(
            id,
            entity.getStatus(),
            applicant,
            new NoticeAcceptance(acceptance.getVersion(), acceptance.getAcceptedAt()),
            evidence,
            generalData.findById(entity.getId()).map(saved -> toGeneralData(saved, applicant.taxRegime())).orElse(null),
            document == null ? null : new FiscalIdentificationDocument(id, document.getOriginalName(),
                new StoredDocumentReference(document.getId())),
            cucopParticipations.findById(entity.getId()).map(this::toCucopParticipation).orElse(null),
            entity.getSectionProgress());
        return new StoredRegistration(
            registration,
            new NoticeAcceptanceReference(entity.getNoticeAcceptanceId().toString()),
            entity.getSignatureEvidenceIds().stream()
                .map(evidenceId -> new SignatureEvidenceReference(evidenceId.toString()))
                .collect(Collectors.toSet()),
            document == null ? null : new FiscalDocumentView(document.getId(), document.getOriginalName(),
                document.getExtension(), document.getSizeBytes(), document.getUploadedAt()));
    }
}
