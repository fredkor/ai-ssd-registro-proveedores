package mx.com.fredkorlabs.registration.domain;

import mx.com.fredkorlabs.registration.domain.exception.InvalidGeneralDataException;
import mx.com.fredkorlabs.registration.domain.model.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
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

        // Save GeneralData again
        reg.associateFiscalDocument(createDocument(reg.id()));
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
}
