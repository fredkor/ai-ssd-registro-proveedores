package mx.com.fredkorlabs.registration.adapter.out.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import mx.com.fredkorlabs.registration.adapter.out.catalog.CucopCatalogAdapter;
import mx.com.fredkorlabs.registration.application.port.out.DuplicateRegistrationException;
import mx.com.fredkorlabs.registration.application.port.out.FiscalDocumentView;
import mx.com.fredkorlabs.registration.application.port.out.NoticeAcceptanceReference;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationStatusView;
import mx.com.fredkorlabs.registration.application.port.out.SignatureEvidenceReference;
import mx.com.fredkorlabs.registration.application.port.out.StoredRegistration;
import mx.com.fredkorlabs.registration.domain.model.ApplicantIdentity;
import mx.com.fredkorlabs.registration.domain.model.ApplicantSignatureEvidence;
import mx.com.fredkorlabs.registration.domain.model.CucopParticipation;
import mx.com.fredkorlabs.registration.domain.model.CucopSpecificItem;
import mx.com.fredkorlabs.registration.domain.model.ForeignFiscalCode;
import mx.com.fredkorlabs.registration.domain.model.GeneralData;
import mx.com.fredkorlabs.registration.domain.model.Nationality;
import mx.com.fredkorlabs.registration.domain.model.NoticeAcceptance;
import mx.com.fredkorlabs.registration.domain.model.Origin;
import mx.com.fredkorlabs.registration.domain.model.ParticipationType;
import mx.com.fredkorlabs.registration.domain.model.PhoneNumber;
import mx.com.fredkorlabs.registration.domain.model.RegistrationSection;
import mx.com.fredkorlabs.registration.domain.model.RegistrationStatus;
import mx.com.fredkorlabs.registration.domain.model.Rfc;
import mx.com.fredkorlabs.registration.domain.model.SectionStatus;
import mx.com.fredkorlabs.registration.domain.model.StoredDocumentReference;
import mx.com.fredkorlabs.registration.domain.model.SupplierRegistration;
import mx.com.fredkorlabs.registration.domain.model.SupplierRegistrationId;
import mx.com.fredkorlabs.registration.domain.model.Website;
import mx.com.fredkorlabs.registration.testsupport.PostgresTestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({SupplierRegistrationPersistenceAdapter.class, FileMetadataPersistenceAdapter.class,
    CucopCatalogAdapter.class, PostgresTestcontainersConfiguration.class})
class SupplierRegistrationPersistenceAdapterTest {

    @Autowired
    private SupplierRegistrationPersistenceAdapter adapter;

    @Autowired
    private SupplierRegistrationJpaRepository registrations;

    @Autowired
    private NoticeAcceptanceJpaRepository acceptances;

    @Autowired
    private ApplicantSignatureEvidenceJpaRepository evidences;

    @Autowired
    private EntityManager entityManager;

    private SupplierRegistrationJpaEntity registration(String rfc, RegistrationStatus status) {
        UUID acceptance = acceptances.saveAndFlush(
            new NoticeAcceptanceJpaEntity("1.0", Instant.parse("2026-10-21T15:30:00Z"))).getId();
        UUID evidence = evidences.saveAndFlush(
            new ApplicantSignatureEvidenceJpaEntity(rfc, "EMPRESA SA", "{}")).getId();
        return registrations.saveAndFlush(new SupplierRegistrationJpaEntity(
            UUID.randomUUID(), rfc, "EMPRESA SA", status, acceptance, evidence));
    }

    @Test
    void US02_CA001_unknownRfc_isEmpty() {
        assertTrue(adapter.findStatusByRfc(new Rfc("ZZZ010101ZZZ")).isEmpty());
    }

    @Test
    void US02_CA002_inProgressRegistration_isFoundByRfc() {
        SupplierRegistrationJpaEntity saved = registration("BBB010101BBB", RegistrationStatus.IN_PROGRESS);

        Optional<RegistrationStatusView> found = adapter.findStatusByRfc(new Rfc("BBB010101BBB"));

        assertEquals(saved.getId(), found.orElseThrow().id().value());
        assertEquals(RegistrationStatus.IN_PROGRESS, found.orElseThrow().status());
    }

    @Test
    void US02_CA003_registeredRegistration_isFoundWithItsStatus() {
        registration("CCC010101CCC", RegistrationStatus.REGISTERED);

        assertEquals(RegistrationStatus.REGISTERED,
            adapter.findStatusByRfc(new Rfc("CCC010101CCC")).orElseThrow().status());
    }

    @Test
    void US02_CA005_rfcIsComparedExactly() {
        registration("DDD010101DDD", RegistrationStatus.IN_PROGRESS);

        assertTrue(adapter.findStatusByRfc(new Rfc("ddd010101ddd")).isEmpty());
    }

    @Test
    void US04_CA003_database_rejectsSecondRegistrationForSameRfc() {
        registration("EEE010101EEE", RegistrationStatus.IN_PROGRESS);

        assertThrows(Exception.class, () -> registration("EEE010101EEE", RegistrationStatus.IN_PROGRESS));
    }

    @Test
    void US02_CA003_database_rejectsUnknownStatus() {
        SupplierRegistrationJpaEntity saved = registration("FFF010101FFF", RegistrationStatus.IN_PROGRESS);

        assertThrows(PersistenceException.class, () -> entityManager.createNativeQuery(
                "UPDATE supplier_registration SET status = 'DRAFT' WHERE id = ?1")
            .setParameter(1, saved.getId()).executeUpdate());
    }

    private record Inserted(SupplierRegistration registration, UUID acceptanceId, UUID evidenceId) {}

    private Inserted insert(String rfc) {
        Instant acceptedAt = Instant.parse("2026-10-21T15:30:00Z");
        UUID acceptance = acceptances.saveAndFlush(new NoticeAcceptanceJpaEntity("1.0", acceptedAt)).getId();
        UUID evidence = evidences.saveAndFlush(new ApplicantSignatureEvidenceJpaEntity(rfc, "EMPRESA SA", "{}")).getId();
        SupplierRegistration registration = SupplierRegistration.start(new SupplierRegistrationId(UUID.randomUUID()),
            new ApplicantIdentity(new Rfc(rfc), "EMPRESA SA"), new NoticeAcceptance("1.0", acceptedAt),
            new ApplicantSignatureEvidence("{}"));
        adapter.insert(registration, new SignatureEvidenceReference(evidence.toString()),
            new NoticeAcceptanceReference(acceptance.toString()));
        return new Inserted(registration, acceptance, evidence);
    }

    @Test
    void US04_CA001_insert_storesRegistrationInProgressWithIdentity() {
        Inserted inserted = insert("GGG010101GGG");
        entityManager.clear();

        SupplierRegistrationJpaEntity stored = registrations.findById(inserted.registration().id().value()).orElseThrow();
        assertEquals("GGG010101GGG", stored.getRfc());
        assertEquals("EMPRESA SA", stored.getLegalName());
        assertEquals(RegistrationStatus.IN_PROGRESS, stored.getStatus());
    }

    @Test
    void US04_CA002_insert_storesSixPendingSections() {
        Inserted inserted = insert("HHH010101HHH");
        entityManager.clear();

        Map<RegistrationSection, SectionStatus> progress =
            registrations.findById(inserted.registration().id().value()).orElseThrow().getSectionProgress();
        assertEquals(Set.of(RegistrationSection.values()), progress.keySet());
        assertTrue(progress.values().stream().allMatch(status -> status == SectionStatus.PENDING));
    }

    @Test
    void US03_CA005_insert_linksAcceptanceAndInitialEvidenceInHistory() {
        Inserted inserted = insert("JJJ010101JJJ");
        entityManager.clear();

        SupplierRegistrationJpaEntity stored = registrations.findById(inserted.registration().id().value()).orElseThrow();
        assertEquals(inserted.acceptanceId(), stored.getNoticeAcceptanceId());
        assertEquals(inserted.evidenceId(), stored.getInitialEvidenceId());
        assertEquals(Set.of(inserted.evidenceId()), stored.getSignatureEvidenceIds());
    }

    @Test
    void US04_CA003_insert_secondRegistrationForSameRfc_isDuplicate() {
        insert("KKK010101KKK");

        assertThrows(DuplicateRegistrationException.class, () -> insert("KKK010101KKK"));
    }

    @Test
    void US04_CA002_database_rejectsUnknownSection() {
        Inserted inserted = insert("LLL010101LLL");

        assertThrows(PersistenceException.class, () -> entityManager.createNativeQuery(
                "INSERT INTO section_progress (registration_id, section, status) VALUES (?1, 'SUMMARY', 'PENDING')")
            .setParameter(1, inserted.registration().id().value()).executeUpdate());
    }

    @Test
    void US04_CA001_schema_usesApprovedSqlTypes() {
        assertEquals("character varying(32)", columnType("section_progress", "section"));
        assertEquals("character varying(32)", columnType("section_progress", "status"));
        assertEquals("uuid", columnType("registration_signature_evidence", "signature_evidence_id"));
    }

    @Test
    void US05_CA003_findForUpdate_reconstitutesStoredStateAndReferences() {
        Inserted inserted = insert("MMM010101MMM");
        UUID id = inserted.registration().id().value();
        entityManager.createNativeQuery("UPDATE section_progress SET status = 'COMPLETED' "
            + "WHERE registration_id = ?1 AND section IN ('GENERAL_DATA', 'CUCOP')").setParameter(1, id).executeUpdate();
        entityManager.clear();

        StoredRegistration stored = adapter.findForUpdate(new SupplierRegistrationId(id)).orElseThrow();

        SupplierRegistration registration = stored.registration();
        assertEquals(RegistrationStatus.IN_PROGRESS, registration.status());
        assertEquals(new ApplicantIdentity(new Rfc("MMM010101MMM"), "EMPRESA SA"), registration.applicant());
        assertEquals(new NoticeAcceptance("1.0", Instant.parse("2026-10-21T15:30:00Z")), registration.latestAcceptance());
        assertEquals(SectionStatus.COMPLETED, registration.sectionStatus(RegistrationSection.GENERAL_DATA));
        assertEquals(SectionStatus.COMPLETED, registration.sectionStatus(RegistrationSection.CUCOP));
        assertEquals(SectionStatus.PENDING, registration.sectionStatus(RegistrationSection.CONTACTS));
        assertEquals(new NoticeAcceptanceReference(inserted.acceptanceId().toString()), stored.acceptanceReference());
        assertEquals(Set.of(new SignatureEvidenceReference(inserted.evidenceId().toString())), stored.evidenceReferences());
    }

    @Test
    void US05_CA001_findForUpdate_unknownId_isEmpty() {
        assertTrue(adapter.findForUpdate(new SupplierRegistrationId(UUID.randomUUID())).isEmpty());
    }

    @Test
    void US05_CA010_saveResume_updatesNameAndAcceptanceAndAppendsEvidence() {
        Inserted inserted = insert("NNN010101NNN");
        UUID id = inserted.registration().id().value();
        entityManager.clear();
        UUID newAcceptance = acceptances.saveAndFlush(
            new NoticeAcceptanceJpaEntity("1.0", Instant.parse("2026-10-22T09:00:00Z"))).getId();
        UUID newEvidence = evidences.saveAndFlush(
            new ApplicantSignatureEvidenceJpaEntity("NNN010101NNN", "EMPRESA NUEVA SA", "{\"new\":true}")).getId();
        SupplierRegistration registration = adapter.findForUpdate(new SupplierRegistrationId(id)).orElseThrow().registration();
        registration.resume("EMPRESA NUEVA SA", new NoticeAcceptance("1.0", Instant.parse("2026-10-22T09:00:00Z")),
            new ApplicantSignatureEvidence("{\"new\":true}"));

        adapter.saveResume(registration, new SignatureEvidenceReference(newEvidence.toString()),
            new NoticeAcceptanceReference(newAcceptance.toString()));
        entityManager.clear();

        SupplierRegistrationJpaEntity stored = registrations.findById(id).orElseThrow();
        assertEquals("NNN010101NNN", stored.getRfc());
        assertEquals("EMPRESA NUEVA SA", stored.getLegalName());
        assertEquals(newAcceptance, stored.getNoticeAcceptanceId());
        assertEquals(inserted.evidenceId(), stored.getInitialEvidenceId());
        assertEquals(Set.of(inserted.evidenceId(), newEvidence), stored.getSignatureEvidenceIds());
        assertTrue(stored.getSectionProgress().values().stream().allMatch(status -> status == SectionStatus.PENDING));
        assertTrue(acceptances.existsById(inserted.acceptanceId()));
    }

    @Autowired
    private FileMetadataJpaRepository files;

    private UUID registerFile(UUID owner) {
        UUID fileId = UUID.randomUUID();
        files.saveAndFlush(new FileMetadataJpaEntity(fileId, owner, "cedula.pdf",
            "2026/10/23/RFC/" + fileId + ".pdf", "pdf", "application/pdf", 9L,
            Instant.parse("2026-10-23T05:30:00.123456Z"), true));
        return fileId;
    }

    @Test
    void US10_CA005_associatedDocument_isReconstitutedWithItsMetadata() {
        Inserted inserted = insert("PPP010101PPP");
        SupplierRegistrationId id = inserted.registration().id();
        UUID fileId = registerFile(id.value());
        adapter.saveFiscalDocumentAssociation(id, new StoredDocumentReference(fileId));
        entityManager.clear();

        StoredRegistration stored = adapter.findForUpdate(id).orElseThrow();

        assertEquals(new StoredDocumentReference(fileId), stored.registration().associatedDocument().storedFile());
        assertEquals("cedula.pdf", stored.registration().associatedDocument().originalName());
        assertEquals(new FiscalDocumentView(fileId, "cedula.pdf", "pdf", 9L,
            Instant.parse("2026-10-23T05:30:00.123456Z")), stored.fiscalDocument());
    }

    @Autowired
    private FileMetadataPersistenceAdapter fileMetadata;

    @Test
    void US10_CA006_dissociation_removesReferenceMarksMetadataAndKeepsOtherProgress() {
        SupplierRegistrationId id = insert("UUU010101UUU").registration().id();
        UUID fileId = registerFile(id.value());
        adapter.saveFiscalDocumentAssociation(id, new StoredDocumentReference(fileId));
        entityManager.createNativeQuery("UPDATE section_progress SET status = 'COMPLETED' "
            + "WHERE registration_id = ?1 AND section IN ('GENERAL_DATA', 'CUCOP')").setParameter(1, id.value()).executeUpdate();
        entityManager.clear();
        SupplierRegistration registration = adapter.findForUpdate(id).orElseThrow().registration();
        registration.dissociateFiscalDocument();

        adapter.saveFiscalDocumentDissociation(registration);
        fileMetadata.markNotAssociated(fileId);
        entityManager.clear();

        StoredRegistration stored = adapter.findForUpdate(id).orElseThrow();
        assertNull(stored.fiscalDocument());
        assertEquals(SectionStatus.PENDING, stored.registration().sectionStatus(RegistrationSection.GENERAL_DATA));
        assertEquals(SectionStatus.COMPLETED, stored.registration().sectionStatus(RegistrationSection.CUCOP));
        assertFalse(files.findById(fileId).orElseThrow().isAssociated());
        assertEquals(id.value(), files.findById(fileId).orElseThrow().getOwnerRegistrationId());
    }

    private SupplierRegistration withGeneralData(SupplierRegistrationId id, GeneralData data) {
        UUID fileId = registerFile(id.value());
        adapter.saveFiscalDocumentAssociation(id, new StoredDocumentReference(fileId));
        entityManager.clear();
        SupplierRegistration registration = adapter.findForUpdate(id).orElseThrow().registration();
        registration.saveGeneralData(data);
        adapter.saveGeneralData(registration);
        entityManager.clear();
        return adapter.findForUpdate(id).orElseThrow().registration();
    }

    private static GeneralData legalEntityData(List<PhoneNumber> phones) {
        return new GeneralData(Origin.NATIONAL, 151, new Nationality("Mexicana"), null, 1, 5, 21, 1, 2, 3, 4, 2,
            new Website("https://empresa.com"), phones);
    }

    @Test
    void US06_CA005_generalData_isSavedAndReconstitutedWithItsPhonesInOrder() {
        SupplierRegistrationId id = insert("VVV010101VVV").registration().id();
        GeneralData data = legalEntityData(List.of(new PhoneNumber("5512345678", "12"),
            new PhoneNumber("0012345678", null)));

        SupplierRegistration stored = withGeneralData(id, data);

        assertEquals(data, stored.generalData());
        assertEquals(SectionStatus.COMPLETED, stored.sectionStatus(RegistrationSection.GENERAL_DATA));
        assertEquals(List.of(0, 1), entityManager.createNativeQuery(
                "SELECT position FROM registration_phone WHERE registration_id = ?1 ORDER BY position")
            .setParameter(1, id.value()).getResultList());
    }

    @Test
    void US09_CA007_newSave_replacesThePhoneCollectionAsAWhole() {
        SupplierRegistrationId id = insert("WWW010101WWW").registration().id();
        withGeneralData(id, legalEntityData(List.of(new PhoneNumber("5512345678", null),
            new PhoneNumber("5587654321", null))));
        SupplierRegistration registration = adapter.findForUpdate(id).orElseThrow().registration();
        GeneralData replacement = legalEntityData(List.of(new PhoneNumber("5511111111", "9")));
        registration.saveGeneralData(replacement);

        adapter.saveGeneralData(registration);
        entityManager.clear();

        assertEquals(replacement, adapter.findForUpdate(id).orElseThrow().registration().generalData());
        assertEquals(1L, ((Number) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM registration_phone WHERE registration_id = ?1")
            .setParameter(1, id.value()).getSingleResult()).longValue());
    }

    @Test
    void US09_CA001_individual_hasNoPhoneRowsAndReconstitutesNullPhones() {
        // A 13-character RFC is an individual (INV-14).
        SupplierRegistrationId id = insert("XYZA010101ABC").registration().id();
        GeneralData data = new GeneralData(Origin.FOREIGN, 4, new Nationality("Alemana"),
            new ForeignFiscalCode("DE123"), 99, null, null, 1, 2, 3, 4, 2, null, null);

        SupplierRegistration stored = withGeneralData(id, data);

        assertEquals(data, stored.generalData());
        assertNull(stored.generalData().phones());
        assertEquals(0L, ((Number) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM registration_phone WHERE registration_id = ?1")
            .setParameter(1, id.value()).getSingleResult()).longValue());
    }

    @Test
    void ADR0017_generalData_rejectsSelectionsOutsideTheCatalogs() {
        SupplierRegistrationId id = insert("ZZZA010101ZZ").registration().id();

        assertThrows(PersistenceException.class, () -> entityManager.createNativeQuery("""
                INSERT INTO general_data (registration_id, origin_id, country_id, nationality, legal_ownership_id,
                business_activity_id, stratification_id, primary_activity_id, worker_count_range_id,
                annual_sales_range_id) VALUES (?1, 1, 9999, 'Mexicana', 1, 1, 2, 3, 4, 2)""")
            .setParameter(1, id.value()).executeUpdate());
    }

    @Test
    void ADR0017_generalDataSchema_usesApprovedSqlTypes() {
        assertEquals("character varying(250)", columnType("general_data", "nationality"));
        assertEquals("character varying(20)", columnType("general_data", "foreign_fiscal_code"));
        assertEquals("character varying(2048)", columnType("general_data", "website"));
        assertEquals("integer", columnType("general_data", "origin_id"));
        assertEquals("character varying(10)", columnType("registration_phone", "number"));
        assertEquals("character varying(5)", columnType("registration_phone", "extension"));
    }

    @Test
    void INV14_insert_storesTheTaxRegimeDerivedFromTheRfc() {
        UUID legalEntity = insert("TRA010101TRA").registration().id().value();
        UUID individual = insert("HEGT7610034S2").registration().id().value();
        entityManager.clear();

        assertEquals(1, registrations.findById(legalEntity).orElseThrow().getTaxRegimeId());
        assertEquals(2, registrations.findById(individual).orElseThrow().getTaxRegimeId());
    }

    @Test
    void INV14_database_rejectsTaxRegimeContradictingTheRfcLength() {
        UUID id = insert("TRB010101TRB").registration().id().value();

        assertThrows(PersistenceException.class, () -> entityManager.createNativeQuery(
                "UPDATE supplier_registration SET tax_regime_id = 2 WHERE id = ?1")
            .setParameter(1, id).executeUpdate());
    }

    @Test
    void ADR0017_taxRegime_isAnIntegerReferencingTheCatalog() {
        assertEquals("integer", columnType("supplier_registration", "tax_regime_id"));
        assertEquals(1L, ((Number) entityManager.createNativeQuery("""
                SELECT COUNT(*) FROM information_schema.table_constraints tc
                JOIN information_schema.key_column_usage k ON tc.constraint_name = k.constraint_name
                WHERE tc.table_name = 'supplier_registration' AND tc.constraint_type = 'FOREIGN KEY'
                AND k.column_name = 'tax_regime_id'""").getSingleResult()).longValue());
    }

    private static final CucopSpecificItem OFFICE = new CucopSpecificItem(1, 1, "21101",
        "MATERIALES Y ÚTILES DE OFICINA", 4);
    private static final CucopSpecificItem PRINTING = new CucopSpecificItem(2, 2, "21201",
        "MATERIALES Y ÚTILES DE IMPRESIÓN Y REPRODUCCIÓN", 4);

    private SupplierRegistration withCucop(SupplierRegistrationId id, CucopParticipation participation) {
        SupplierRegistration registration = withGeneralData(id,
            legalEntityData(List.of(new PhoneNumber("5512345678", null))));
        registration.saveCucopParticipation(participation);
        adapter.saveCucopParticipation(registration);
        entityManager.clear();
        return adapter.findForUpdate(id).orElseThrow().registration();
    }

    @Test
    void US11_CA011_cucop_isSavedAndReconstitutedWithItsProgress() {
        SupplierRegistrationId id = insert("CUA010101CUA").registration().id();
        CucopParticipation participation = new CucopParticipation(Set.of(ParticipationType.values()),
            Set.of(OFFICE, PRINTING));

        SupplierRegistration stored = withCucop(id, participation);

        assertEquals(participation, stored.cucopParticipation());
        assertEquals(SectionStatus.COMPLETED, stored.sectionStatus(RegistrationSection.CUCOP));
        assertEquals(SectionStatus.COMPLETED, stored.sectionStatus(RegistrationSection.GENERAL_DATA));
    }

    @Test
    void US11_CA011_newSave_replacesTypesAndItemsAsAWhole() {
        SupplierRegistrationId id = insert("CUB010101CUB").registration().id();
        withCucop(id, new CucopParticipation(Set.of(ParticipationType.values()), Set.of(OFFICE, PRINTING)));
        SupplierRegistration registration = adapter.findForUpdate(id).orElseThrow().registration();
        CucopParticipation replacement = new CucopParticipation(Set.of(ParticipationType.PUBLIC_WORKS_CONTRACTOR),
            Set.of(PRINTING));
        registration.saveCucopParticipation(replacement);

        adapter.saveCucopParticipation(registration);
        entityManager.clear();

        assertEquals(replacement, adapter.findForUpdate(id).orElseThrow().registration().cucopParticipation());
        assertEquals(List.of(2), entityManager.createNativeQuery(
                "SELECT specific_item_id FROM registration_cucop_item WHERE registration_id = ?1")
            .setParameter(1, id.value()).getResultList());
    }

    @Test
    void US11_withoutCucop_reconstitutesNoParticipation() {
        SupplierRegistrationId id = insert("CUC010101CUC").registration().id();
        entityManager.clear();

        assertNull(adapter.findForUpdate(id).orElseThrow().registration().cucopParticipation());
    }

    @Test
    void INV37_savingGeneralDataAgain_keepsTheCucopSelection() {
        SupplierRegistrationId id = insert("CUD010101CUD").registration().id();
        CucopParticipation participation = new CucopParticipation(
            Set.of(ParticipationType.GOODS_AND_SERVICES_SUPPLIER), Set.of(OFFICE));
        SupplierRegistration registration = withCucop(id, participation);
        registration.saveGeneralData(legalEntityData(List.of(new PhoneNumber("5511111111", null))));

        adapter.saveGeneralData(registration);
        entityManager.clear();

        SupplierRegistration stored = adapter.findForUpdate(id).orElseThrow().registration();
        assertEquals(participation, stored.cucopParticipation());
        assertEquals(SectionStatus.COMPLETED, stored.sectionStatus(RegistrationSection.CUCOP));
    }

    @Test
    void INV32_database_rejectsSelectionsThatAreNotSpecificItems() {
        SupplierRegistrationId id = insert("CUE010101CUE").registration().id();
        withCucop(id, new CucopParticipation(Set.of(ParticipationType.GOODS_AND_SERVICES_SUPPLIER), Set.of(OFFICE)));

        // 361 is a generic item without a specific item of the same id.
        assertThrows(PersistenceException.class, () -> entityManager.createNativeQuery(
                "INSERT INTO registration_cucop_item (registration_id, specific_item_id) VALUES (?1, 361)")
            .setParameter(1, id.value()).executeUpdate());
    }

    @Test
    void INV31_database_rejectsUnknownParticipationType() {
        SupplierRegistrationId id = insert("CUF010101CUF").registration().id();
        withCucop(id, new CucopParticipation(Set.of(ParticipationType.GOODS_AND_SERVICES_SUPPLIER), Set.of(OFFICE)));

        assertThrows(PersistenceException.class, () -> entityManager.createNativeQuery(
                "INSERT INTO registration_participation_type (registration_id, participation_type) VALUES (?1, 'OTHER')")
            .setParameter(1, id.value()).executeUpdate());
    }

    @Test
    void ADR0017_cucopSchema_usesApprovedSqlTypes() {
        assertEquals("uuid", columnType("cucop_participation", "registration_id"));
        assertEquals("character varying(32)", columnType("registration_participation_type", "participation_type"));
        assertEquals("integer", columnType("registration_cucop_item", "specific_item_id"));
    }

    @Test
    void US10_withoutDocument_reconstitutesNoAssociation() {
        Inserted inserted = insert("QQQ010101QQQ");
        entityManager.clear();

        StoredRegistration stored = adapter.findForUpdate(inserted.registration().id()).orElseThrow();

        assertNull(stored.registration().associatedDocument());
        assertNull(stored.fiscalDocument());
    }

    @Test
    void US10_CA004_database_allowsOneFiscalDocumentPerRegistration() {
        Inserted inserted = insert("RRR010101RRR");
        SupplierRegistrationId id = inserted.registration().id();
        adapter.saveFiscalDocumentAssociation(id, new StoredDocumentReference(registerFile(id.value())));
        UUID other = registerFile(id.value());

        assertThrows(PersistenceException.class, () -> entityManager.createNativeQuery(
                "INSERT INTO registration_fiscal_document (registration_id, file_id) VALUES (?1, ?2)")
            .setParameter(1, id.value()).setParameter(2, other).executeUpdate());
    }

    @Test
    void ADR0010_database_rejectsDocumentOwnedByAnotherRegistration() {
        UUID owner = insert("SSS010101SSS").registration().id().value();
        UUID otherRegistration = insert("TTT010101TTT").registration().id().value();
        UUID fileId = registerFile(owner);

        assertThrows(PersistenceException.class, () -> entityManager.createNativeQuery(
                "INSERT INTO registration_fiscal_document (registration_id, file_id) VALUES (?1, ?2)")
            .setParameter(1, otherRegistration).setParameter(2, fileId).executeUpdate());
    }

    @Test
    void ADR0010_schema_usesApprovedTypesAndCleanupIndex() {
        assertEquals("character varying(255)", columnType("file_metadata", "relative_path"));
        assertEquals("character varying(10)", columnType("file_metadata", "extension"));
        assertEquals("text", columnType("file_metadata", "original_name"));
        assertEquals("bigint", columnType("file_metadata", "size_bytes"));
        assertEquals("timestamp(6) with time zone", columnType("file_metadata", "uploaded_at"));
        assertEquals("boolean", columnType("file_metadata", "associated"));
        assertEquals(1L, ((Number) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM pg_indexes WHERE indexname = 'file_metadata_associated_uploaded_at_idx'")
            .getSingleResult()).longValue());
    }

    private String columnType(String table, String column) {
        return (String) entityManager.createNativeQuery(
                "SELECT format_type(atttypid, atttypmod) FROM pg_attribute "
                    + "WHERE attrelid = to_regclass(?1) AND attname = ?2")
            .setParameter(1, table).setParameter(2, column).getSingleResult();
    }
}
