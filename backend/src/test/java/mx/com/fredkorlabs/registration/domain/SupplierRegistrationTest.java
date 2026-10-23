package mx.com.fredkorlabs.registration.domain;

import mx.com.fredkorlabs.registration.domain.exception.FiscalDocumentAlreadyExistsException;
import mx.com.fredkorlabs.registration.domain.exception.InvalidGeneralDataException;
import mx.com.fredkorlabs.registration.domain.exception.InvalidRegistrationStateException;
import mx.com.fredkorlabs.registration.domain.model.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SupplierRegistrationTest {

    // --- Helpers ---

    private SupplierRegistration createLegalEntityRegistration() {
        return SupplierRegistration.start(
            new SupplierRegistrationId(UUID.randomUUID()),
            new ApplicantIdentity(new Rfc("XAXX01010100"), "Corp SA de CV"),
            new NoticeAcceptance("1.0", Instant.now()),
            new ApplicantSignatureEvidence("{\"rfc\":\"XAXX01010100\"}")
        );
    }

    private SupplierRegistration createIndividualRegistration() {
        return SupplierRegistration.start(
            new SupplierRegistrationId(UUID.randomUUID()),
            new ApplicantIdentity(new Rfc("XAXX010101000"), "Juan Pérez López"),
            new NoticeAcceptance("1.0", Instant.now()),
            new ApplicantSignatureEvidence("{\"rfc\":\"XAXX010101000\"}")
        );
    }

    private GeneralData legalEntityGeneralData() {
        return new GeneralData(
            Origin.NATIONAL, 1,
            new Nationality("Mexicana"),
            null,
            1, 1, 1,
            1, 1, 1, 1, 1,
            null,
            List.of(new PhoneNumber("5512345678", null))
        );
    }

    private GeneralData individualGeneralData() {
        return new GeneralData(
            Origin.NATIONAL, 1,
            new Nationality("Mexicana"),
            null,
            1, null, null,
            1, 1, 1, 1, 1,
            null,
            List.of()
        );
    }

    private GeneralDataValidationContext allValidContext() {
        return new GeneralDataValidationContext(true, true, true, true, true, true, true, true);
    }

    private FiscalIdentificationDocument createDocument(SupplierRegistrationId regId) {
        return new FiscalIdentificationDocument(regId, "cedula.pdf",
            new StoredDocumentReference(UUID.randomUUID()));
    }

    private CucopParticipation validParticipation() {
        CucopSpecificItem item = new CucopSpecificItem(1, 1, "10101001", "Partida test", 4);
        return new CucopParticipation(
            Set.of(ParticipationType.GOODS_AND_SERVICES_SUPPLIER),
            Set.of(item)
        );
    }

    // --- US04: start ---

    @Test
    void US04_CA001_start_createsInProgressStatus() {
        SupplierRegistration reg = createLegalEntityRegistration();
        assertEquals(RegistrationStatus.IN_PROGRESS, reg.status());
    }

    @Test
    void US04_CA005_start_assignsNonNullId() {
        SupplierRegistration reg = createLegalEntityRegistration();
        assertNotNull(reg.id());
        assertNotNull(reg.id().value());
    }

    @Test
    void US04_CA006_start_associatesIdentity() {
        SupplierRegistration reg = createLegalEntityRegistration();
        assertEquals("XAXX01010100", reg.applicant().rfc().value());
        assertEquals("Corp SA de CV", reg.applicant().legalName());
    }

    // --- US08: saveGeneralData ---

    @Test
    void US08_CA006_saveGeneralData_validLegalEntity_marksGeneralDataCompleted() {
        SupplierRegistration reg = createLegalEntityRegistration();
        reg.associateFiscalDocument(createDocument(reg.id()));
        reg.saveGeneralData(legalEntityGeneralData(), allValidContext(), true);
        assertEquals(SectionStatus.COMPLETED, reg.sectionStatus(RegistrationSection.GENERAL_DATA));
    }

    @Test
    void US08_CA004_saveGeneralData_individualWithSectorAndSubsector_isRejected() {
        SupplierRegistration reg = createIndividualRegistration();
        reg.associateFiscalDocument(createDocument(reg.id()));
        GeneralData dataWithSector = new GeneralData(
            Origin.NATIONAL, 1, new Nationality("Mexicana"), null,
            1, 1, 1,  // sectorId and subsectorId should not be present for INDIVIDUAL
            1, 1, 1, 1, 1, null, List.of()
        );
        assertThrows(InvalidGeneralDataException.class,
            () -> reg.saveGeneralData(dataWithSector, allValidContext(), true));
    }

    // --- US09: phones ---

    @Test
    void US09_CA004_saveGeneralData_legalEntityWithoutPhones_isRejected() {
        SupplierRegistration reg = createLegalEntityRegistration();
        reg.associateFiscalDocument(createDocument(reg.id()));
        GeneralData noPhones = new GeneralData(
            Origin.NATIONAL, 1, new Nationality("Mexicana"), null,
            1, 1, 1, 1, 1, 1, 1, 1, null, List.of()
        );
        assertThrows(InvalidGeneralDataException.class,
            () -> reg.saveGeneralData(noPhones, allValidContext(), true));
    }

    // --- US10: fiscal document ---

    @Test
    void US10_CA007_dissociateFiscalDocument_leavesGeneralDataPendingAndCucopIntact() {
        SupplierRegistration reg = createLegalEntityRegistration();
        reg.associateFiscalDocument(createDocument(reg.id()));
        reg.saveGeneralData(legalEntityGeneralData(), allValidContext(), true);
        reg.saveCucopParticipation(validParticipation());

        assertEquals(SectionStatus.COMPLETED, reg.sectionStatus(RegistrationSection.GENERAL_DATA));
        assertEquals(SectionStatus.COMPLETED, reg.sectionStatus(RegistrationSection.CUCOP));

        reg.dissociateFiscalDocument();

        assertEquals(SectionStatus.PENDING, reg.sectionStatus(RegistrationSection.GENERAL_DATA));
        // INV-37: CUCoP must remain COMPLETED
        assertEquals(SectionStatus.COMPLETED, reg.sectionStatus(RegistrationSection.CUCOP));
        assertNull(reg.associatedDocument());
    }

    // --- US11: saveCucopParticipation ---

    @Test
    void US11_CA011_saveCucopParticipation_valid_marksCucopCompleted() {
        SupplierRegistration reg = createLegalEntityRegistration();
        reg.associateFiscalDocument(createDocument(reg.id()));
        reg.saveGeneralData(legalEntityGeneralData(), allValidContext(), true);
        reg.saveCucopParticipation(validParticipation());
        assertEquals(SectionStatus.COMPLETED, reg.sectionStatus(RegistrationSection.CUCOP));
    }

    // --- US06: INV-37 cross-section isolation ---

    @Test
    void US06_CA006_saveGeneralData_afterCucop_doesNotChangeCucopProgress() {
        SupplierRegistration reg = createLegalEntityRegistration();
        reg.associateFiscalDocument(createDocument(reg.id()));
        reg.saveGeneralData(legalEntityGeneralData(), allValidContext(), true);
        reg.saveCucopParticipation(validParticipation());

        // Save GeneralData again; the fiscal document stays associated
        reg.saveGeneralData(legalEntityGeneralData(), allValidContext(), true);

        // CUCOP must still be COMPLETED
        assertEquals(SectionStatus.COMPLETED, reg.sectionStatus(RegistrationSection.CUCOP));
        assertNotNull(reg.cucopParticipation());
    }

    // --- start initializes all sections as PENDING ---

    @Test
    void US04_CA001_start_allSectionsInitiallyPending() {
        SupplierRegistration reg = createLegalEntityRegistration();
        for (RegistrationSection section : RegistrationSection.values()) {
            assertEquals(SectionStatus.PENDING, reg.sectionStatus(section),
                "Section " + section + " should be PENDING");
        }
    }

    @Test
    void US04_CA002_start_listsProgressInWizardOrderStartingWithGeneralData() {
        SupplierRegistration reg = createLegalEntityRegistration();

        assertEquals(List.of(RegistrationSection.values()),
            reg.progress().stream().map(SectionProgress::section).toList());
        assertEquals(RegistrationSection.GENERAL_DATA, reg.progress().getFirst().section());
        assertTrue(reg.progress().stream().allMatch(p -> p.status() == SectionStatus.PENDING));
    }

    @Test
    void US03_CA005_start_keepsAcceptanceAndInitialEvidence() {
        NoticeAcceptance acceptance = new NoticeAcceptance("1.0", Instant.parse("2026-10-21T15:30:00Z"));
        ApplicantSignatureEvidence evidence = new ApplicantSignatureEvidence("{\"rfc\":\"XAXX01010100\"}");

        SupplierRegistration reg = SupplierRegistration.start(new SupplierRegistrationId(UUID.randomUUID()),
            new ApplicantIdentity(new Rfc("XAXX01010100"), "Corp SA de CV"), acceptance, evidence);

        assertEquals(acceptance, reg.latestAcceptance());
        assertEquals(List.of(evidence), reg.acceptedEvidence());
        assertNull(reg.generalData());
        assertNull(reg.cucopParticipation());
    }

    private SupplierRegistration reconstituted(RegistrationStatus status, Map<RegistrationSection, SectionStatus> progress) {
        return SupplierRegistration.reconstitute(
            new SupplierRegistrationId(UUID.fromString("7b1f8f8e-1c2d-4a4b-9a0e-5f6a7b8c9d0e")), status,
            new ApplicantIdentity(new Rfc("XAXX01010100"), "Corp SA de CV"),
            new NoticeAcceptance("1.0", Instant.parse("2026-10-21T15:30:00Z")),
            List.of(new ApplicantSignatureEvidence("{\"initial\":true}")), null, null, null, progress);
    }

    private static Map<RegistrationSection, SectionStatus> progressWithCucopCompleted() {
        Map<RegistrationSection, SectionStatus> progress = new EnumMap<>(RegistrationSection.class);
        for (RegistrationSection section : RegistrationSection.values()) progress.put(section, SectionStatus.PENDING);
        progress.put(RegistrationSection.GENERAL_DATA, SectionStatus.COMPLETED);
        progress.put(RegistrationSection.CUCOP, SectionStatus.COMPLETED);
        return progress;
    }

    @Test
    void US05_CA003_reconstitute_keepsStoredStatusAndProgressWithoutTransitions() {
        SupplierRegistration reg = reconstituted(RegistrationStatus.IN_PROGRESS, progressWithCucopCompleted());

        assertEquals(RegistrationStatus.IN_PROGRESS, reg.status());
        assertEquals(SectionStatus.COMPLETED, reg.sectionStatus(RegistrationSection.GENERAL_DATA));
        assertEquals(SectionStatus.COMPLETED, reg.sectionStatus(RegistrationSection.CUCOP));
        assertEquals(SectionStatus.PENDING, reg.sectionStatus(RegistrationSection.CONTACTS));
        assertEquals(1, reg.acceptedEvidence().size());
    }

    @Test
    void US05_CA003_reconstitute_requiresProgressForEverySection() {
        Map<RegistrationSection, SectionStatus> incomplete = new EnumMap<>(RegistrationSection.class);
        incomplete.put(RegistrationSection.GENERAL_DATA, SectionStatus.PENDING);

        assertThrows(IllegalArgumentException.class, () -> reconstituted(RegistrationStatus.IN_PROGRESS, incomplete));
    }

    @Test
    void US05_CA009_resume_updatesLegalNameAndAcceptanceKeepingRfcIdAndProgress() {
        SupplierRegistration reg = reconstituted(RegistrationStatus.IN_PROGRESS, progressWithCucopCompleted());
        NoticeAcceptance newAcceptance = new NoticeAcceptance("1.0", Instant.parse("2026-10-22T10:00:00Z"));

        reg.resume("Corp Nueva SA de CV", newAcceptance, new ApplicantSignatureEvidence("{\"new\":true}"));

        assertEquals(UUID.fromString("7b1f8f8e-1c2d-4a4b-9a0e-5f6a7b8c9d0e"), reg.id().value());
        assertEquals("XAXX01010100", reg.applicant().rfc().value());
        assertEquals("Corp Nueva SA de CV", reg.applicant().legalName());
        assertEquals(newAcceptance, reg.latestAcceptance());
        assertEquals(SectionStatus.COMPLETED, reg.sectionStatus(RegistrationSection.CUCOP));
    }

    @Test
    void US05_CA010_resume_appendsEvidenceKeepingTheInitialOne() {
        SupplierRegistration reg = reconstituted(RegistrationStatus.IN_PROGRESS, progressWithCucopCompleted());

        reg.resume("Corp SA de CV", new NoticeAcceptance("1.0", Instant.parse("2026-10-22T10:00:00Z")),
            new ApplicantSignatureEvidence("{\"new\":true}"));

        assertEquals(List.of("{\"initial\":true}", "{\"new\":true}"),
            reg.acceptedEvidence().stream().map(ApplicantSignatureEvidence::completeResult).toList());
    }

    @Test
    void US05_CA006_registered_cannotBeResumed() {
        SupplierRegistration reg = reconstituted(RegistrationStatus.REGISTERED, progressWithCucopCompleted());

        assertThrows(InvalidRegistrationStateException.class, reg::ensureResumable);
        assertThrows(InvalidRegistrationStateException.class, () -> reg.resume("Otro",
            new NoticeAcceptance("1.0", Instant.now()), new ApplicantSignatureEvidence("{}")));
        assertEquals("Corp SA de CV", reg.applicant().legalName());
    }

    @Test
    void US10_CA004_secondFiscalDocument_isRejected() {
        SupplierRegistration reg = createLegalEntityRegistration();
        reg.associateFiscalDocument(createDocument(reg.id()));

        assertThrows(FiscalDocumentAlreadyExistsException.class, reg::ensureFiscalDocumentCanBeAssociated);
        assertThrows(FiscalDocumentAlreadyExistsException.class,
            () -> reg.associateFiscalDocument(createDocument(reg.id())));
    }

    @Test
    void US10_CA001_fiscalDocument_isAssociatedWithoutCompletingGeneralData() {
        SupplierRegistration reg = createLegalEntityRegistration();
        FiscalIdentificationDocument document = createDocument(reg.id());

        reg.associateFiscalDocument(document);

        assertSame(document, reg.associatedDocument());
        assertEquals(SectionStatus.PENDING, reg.sectionStatus(RegistrationSection.GENERAL_DATA));
    }

    @Test
    void US10_registeredRegistration_cannotTakeAFiscalDocument() {
        SupplierRegistration reg = reconstituted(RegistrationStatus.REGISTERED, progressWithCucopCompleted());

        assertThrows(InvalidRegistrationStateException.class, reg::ensureFiscalDocumentCanBeAssociated);
    }

    @Test
    void US10_registeredRegistration_cannotDropItsFiscalDocument() {
        SupplierRegistration reg = reconstituted(RegistrationStatus.REGISTERED, progressWithCucopCompleted());

        assertThrows(InvalidRegistrationStateException.class, reg::ensureFiscalDocumentCanBeDissociated);
        assertThrows(InvalidRegistrationStateException.class, reg::dissociateFiscalDocument);
    }

    @Test
    void US10_dissociate_requiresAnAssociatedDocument() {
        SupplierRegistration reg = createLegalEntityRegistration();

        assertThrows(IllegalStateException.class, reg::dissociateFiscalDocument);
    }
}
