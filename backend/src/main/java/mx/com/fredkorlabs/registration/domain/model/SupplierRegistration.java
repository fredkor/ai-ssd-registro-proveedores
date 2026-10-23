package mx.com.fredkorlabs.registration.domain.model;

import mx.com.fredkorlabs.registration.domain.exception.FiscalDocumentAlreadyExistsException;
import mx.com.fredkorlabs.registration.domain.exception.FiscalDocumentRequiredException;
import mx.com.fredkorlabs.registration.domain.exception.InvalidRegistrationStateException;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SupplierRegistration {

    private final SupplierRegistrationId id;
    private RegistrationStatus status;
    private ApplicantIdentity applicant;
    private NoticeAcceptance latestAcceptance;
    private final List<ApplicantSignatureEvidence> acceptedEvidence;
    private GeneralData generalData;
    private FiscalIdentificationDocument associatedDocument;
    private CucopParticipation cucopParticipation;
    private final EnumMap<RegistrationSection, SectionStatus> sectionProgress;

    private SupplierRegistration(SupplierRegistrationId id,
                                 RegistrationStatus status,
                                 ApplicantIdentity applicant,
                                 NoticeAcceptance latestAcceptance,
                                 List<ApplicantSignatureEvidence> acceptedEvidence,
                                 GeneralData generalData,
                                 FiscalIdentificationDocument associatedDocument,
                                 CucopParticipation cucopParticipation,
                                 EnumMap<RegistrationSection, SectionStatus> sectionProgress) {
        this.id = id;
        this.status = status;
        this.applicant = applicant;
        this.latestAcceptance = latestAcceptance;
        this.acceptedEvidence = acceptedEvidence;
        this.generalData = generalData;
        this.associatedDocument = associatedDocument;
        this.cucopParticipation = cucopParticipation;
        this.sectionProgress = sectionProgress;
    }

    public static SupplierRegistration start(SupplierRegistrationId id,
                                             ApplicantIdentity identity,
                                             NoticeAcceptance acceptance,
                                             ApplicantSignatureEvidence initialEvidence) {
        List<ApplicantSignatureEvidence> evidence = new ArrayList<>();
        evidence.add(initialEvidence);

        EnumMap<RegistrationSection, SectionStatus> progress = new EnumMap<>(RegistrationSection.class);
        for (RegistrationSection section : RegistrationSection.values()) {
            progress.put(section, SectionStatus.PENDING);
        }

        return new SupplierRegistration(id, RegistrationStatus.IN_PROGRESS, identity,
                acceptance, evidence, null, null, null, progress);
    }

    /** Rebuilds the stored state without running the creation nor any transition (domain model §8). */
    public static SupplierRegistration reconstitute(SupplierRegistrationId id,
                                                    RegistrationStatus status,
                                                    ApplicantIdentity applicant,
                                                    NoticeAcceptance latestAcceptance,
                                                    List<ApplicantSignatureEvidence> acceptedEvidence,
                                                    GeneralData generalData,
                                                    FiscalIdentificationDocument associatedDocument,
                                                    CucopParticipation cucopParticipation,
                                                    Map<RegistrationSection, SectionStatus> sectionProgress) {
        Objects.requireNonNull(id, "Registration id is required");
        Objects.requireNonNull(status, "Registration status is required");
        Objects.requireNonNull(applicant, "Applicant identity is required");
        Objects.requireNonNull(latestAcceptance, "Notice acceptance is required");
        if (acceptedEvidence == null || acceptedEvidence.isEmpty()) {
            throw new IllegalArgumentException("At least the initial signature evidence is required");
        }
        EnumMap<RegistrationSection, SectionStatus> progress = new EnumMap<>(RegistrationSection.class);
        progress.putAll(sectionProgress);
        if (progress.size() != RegistrationSection.values().length || progress.containsValue(null)) {
            throw new IllegalArgumentException("Every section must have a progress status");
        }
        return new SupplierRegistration(id, status, applicant, latestAcceptance, new ArrayList<>(acceptedEvidence),
                generalData, associatedDocument, cucopParticipation, progress);
    }

    /** INV-13: only a registration IN_PROGRESS can be resumed as a draft. */
    public void ensureResumable() {
        validateInProgress();
    }

    public void resume(String newLegalName,
                       NoticeAcceptance newAcceptance,
                       ApplicantSignatureEvidence newEvidence) {
        ensureResumable();
        this.applicant = new ApplicantIdentity(this.applicant.rfc(), newLegalName);
        this.latestAcceptance = newAcceptance;
        this.acceptedEvidence.add(newEvidence);
    }

    /** INV-34: General Data is saved only while the registration is IN_PROGRESS. */
    public void ensureGeneralDataCanBeSaved() {
        validateInProgress();
    }

    /**
     * Replaces General Data with a validated whole (INV-34) and completes the section. It requires the fiscal
     * document already associated (INV-27); CUCoP is not affected (INV-37).
     */
    public void saveGeneralData(GeneralData data) {
        ensureGeneralDataCanBeSaved();
        Objects.requireNonNull(data, "General Data is required");
        if (associatedDocument == null) {
            throw new FiscalDocumentRequiredException("A fiscal identification document must be uploaded first");
        }
        this.generalData = data;
        sectionProgress.put(RegistrationSection.GENERAL_DATA, SectionStatus.COMPLETED);
    }

    public void saveCucopParticipation(CucopParticipation participation) {
        validateInProgress();

        // INV-30: general data must be saved first
        if (sectionProgress.get(RegistrationSection.GENERAL_DATA) != SectionStatus.COMPLETED) {
            throw new InvalidRegistrationStateException(
                "General data must be saved before CUCoP participation");
        }

        this.cucopParticipation = participation;
        sectionProgress.put(RegistrationSection.CUCOP, SectionStatus.COMPLETED);
    }

    /** INV-27/INV-28: only IN_PROGRESS, and at most one fiscal document associated at a time. */
    public void ensureFiscalDocumentCanBeAssociated() {
        validateInProgress();
        if (associatedDocument != null) {
            throw new FiscalDocumentAlreadyExistsException("A fiscal document is already associated");
        }
    }

    /** Associates the uploaded document immediately; it does not complete General Data (INV-28). */
    public void associateFiscalDocument(FiscalIdentificationDocument document) {
        ensureFiscalDocumentCanBeAssociated();
        this.associatedDocument = document;
    }

    /** INV-29: only a registration IN_PROGRESS can drop its fiscal document. */
    public void ensureFiscalDocumentCanBeDissociated() {
        validateInProgress();
    }

    public void dissociateFiscalDocument() {
        ensureFiscalDocumentCanBeDissociated();
        if (associatedDocument == null) {
            throw new IllegalStateException("No fiscal document is associated");
        }
        this.associatedDocument = null;
        // INV-29: leaves GENERAL_DATA PENDING; INV-37: CUCOP is not affected
        sectionProgress.put(RegistrationSection.GENERAL_DATA, SectionStatus.PENDING);
    }

    private void validateInProgress() {
        if (this.status != RegistrationStatus.IN_PROGRESS) {
            throw new InvalidRegistrationStateException(
                "Operation requires registration in IN_PROGRESS status, got: " + status);
        }
    }

    // --- Queries ---

    public SupplierRegistrationId id() { return id; }
    public RegistrationStatus status() { return status; }
    public ApplicantIdentity applicant() { return applicant; }
    public NoticeAcceptance latestAcceptance() { return latestAcceptance; }
    public List<ApplicantSignatureEvidence> acceptedEvidence() { return List.copyOf(acceptedEvidence); }
    public GeneralData generalData() { return generalData; }
    public FiscalIdentificationDocument associatedDocument() { return associatedDocument; }
    public CucopParticipation cucopParticipation() { return cucopParticipation; }

    public SectionStatus sectionStatus(RegistrationSection section) {
        return sectionProgress.getOrDefault(section, SectionStatus.PENDING);
    }

    public List<SectionProgress> progress() {
        return sectionProgress.entrySet().stream()
            .map(e -> new SectionProgress(e.getKey(), e.getValue()))
            .toList();
    }
}
