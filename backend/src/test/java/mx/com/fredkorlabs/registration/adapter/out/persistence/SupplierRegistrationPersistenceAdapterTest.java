package mx.com.fredkorlabs.registration.adapter.out.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import mx.com.fredkorlabs.registration.application.port.out.DuplicateRegistrationException;
import mx.com.fredkorlabs.registration.application.port.out.FiscalDocumentView;
import mx.com.fredkorlabs.registration.application.port.out.NoticeAcceptanceReference;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationStatusView;
import mx.com.fredkorlabs.registration.application.port.out.SignatureEvidenceReference;
import mx.com.fredkorlabs.registration.application.port.out.StoredRegistration;
import mx.com.fredkorlabs.registration.domain.model.ApplicantIdentity;
import mx.com.fredkorlabs.registration.domain.model.ApplicantSignatureEvidence;
import mx.com.fredkorlabs.registration.domain.model.NoticeAcceptance;
import mx.com.fredkorlabs.registration.domain.model.RegistrationSection;
import mx.com.fredkorlabs.registration.domain.model.RegistrationStatus;
import mx.com.fredkorlabs.registration.domain.model.Rfc;
import mx.com.fredkorlabs.registration.domain.model.SectionStatus;
import mx.com.fredkorlabs.registration.domain.model.StoredDocumentReference;
import mx.com.fredkorlabs.registration.domain.model.SupplierRegistration;
import mx.com.fredkorlabs.registration.domain.model.SupplierRegistrationId;
import mx.com.fredkorlabs.registration.testsupport.PostgresTestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({SupplierRegistrationPersistenceAdapter.class, PostgresTestcontainersConfiguration.class})
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
