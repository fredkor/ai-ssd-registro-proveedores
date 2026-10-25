package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.DeleteFiscalDocumentCommand;
import mx.com.fredkorlabs.registration.application.port.in.FiscalDocumentNotFoundException;
import mx.com.fredkorlabs.registration.application.port.in.RegistrationAccessDeniedException;
import mx.com.fredkorlabs.registration.application.port.in.RegistrationNotFoundException;
import mx.com.fredkorlabs.registration.application.port.out.FileMetadataPort;
import mx.com.fredkorlabs.registration.application.port.out.NoticeAcceptanceReference;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationContextPort;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationSession;
import mx.com.fredkorlabs.registration.application.port.out.SignatureEvidenceReference;
import mx.com.fredkorlabs.registration.application.port.out.StoredFileMetadata;
import mx.com.fredkorlabs.registration.application.port.out.StoredRegistration;
import mx.com.fredkorlabs.registration.domain.exception.InvalidRegistrationStateException;
import mx.com.fredkorlabs.registration.domain.model.ApplicantIdentity;
import mx.com.fredkorlabs.registration.domain.model.ApplicantSignatureEvidence;
import mx.com.fredkorlabs.registration.domain.model.FiscalIdentificationDocument;
import mx.com.fredkorlabs.registration.domain.model.NoticeAcceptance;
import mx.com.fredkorlabs.registration.domain.model.RegistrationSection;
import mx.com.fredkorlabs.registration.domain.model.RegistrationStatus;
import mx.com.fredkorlabs.registration.domain.model.Rfc;
import mx.com.fredkorlabs.registration.domain.model.SectionStatus;
import mx.com.fredkorlabs.registration.domain.model.StoredDocumentReference;
import mx.com.fredkorlabs.registration.domain.model.SupplierRegistration;
import mx.com.fredkorlabs.registration.domain.model.SupplierRegistrationId;
import mx.com.fredkorlabs.registration.testsupport.UnusedSupplierRegistrationPersistence;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DeleteFiscalDocumentServiceTest {

    private static final UUID REGISTRATION_ID = UUID.fromString("7b1f8f8e-1c2d-4a4b-9a0e-5f6a7b8c9d0e");
    private static final UUID FILE_ID = UUID.fromString("8bd04b95-88ae-4dd8-bd19-27229040225e");
    private static final Instant NOW = Instant.parse("2026-10-23T05:30:00Z");
    private static final RegistrationSession SESSION = new RegistrationSession(
        new ApplicantIdentity(new Rfc("AAA010101AAA"), "EMPRESA SA"),
        new ApplicantSignatureEvidence("{}"), new SignatureEvidenceReference("evidence-1"),
        new NoticeAcceptance("1.0", NOW), new NoticeAcceptanceReference("acceptance-1"));

    private final FakePersistence persistence = new FakePersistence();
    private final List<UUID> markedNotAssociated = new ArrayList<>();
    private final RegistrationContextPort contexts = new RegistrationContextPort() {
        @Override
        public String issue(ApplicantIdentity identity, SignatureEvidenceReference evidence,
                            NoticeAcceptanceReference acceptance, Instant issuedAt, Instant expiresAt) {
            throw new UnsupportedOperationException("Not used by this use case");
        }

        @Override
        public boolean isActive(String jti, String rfc, Instant now) { return true; }

        @Override
        public Optional<RegistrationSession> resolve(String jti) { return Optional.of(SESSION); }
    };
    private final FileMetadataPort metadata = new FileMetadataPort() {
        @Override
        public void register(StoredFileMetadata file) { throw new UnsupportedOperationException("Not used"); }

        @Override
        public Optional<StoredFileMetadata> find(UUID fileId) { throw new UnsupportedOperationException("Not used"); }

        @Override
        public void markNotAssociated(UUID fileId) { markedNotAssociated.add(fileId); }

        @Override
        public List<StoredFileMetadata> lockNotAssociatedUploadedBefore(Instant instant) {
            throw new UnsupportedOperationException("Not used by this use case");
        }

        @Override
        public boolean isReferenced(UUID fileId) { throw new UnsupportedOperationException("Not used by this use case"); }

        @Override
        public void delete(UUID fileId) { throw new UnsupportedOperationException("Not used by this use case"); }
    };
    private final DeleteFiscalDocumentService service = new DeleteFiscalDocumentService(contexts, persistence, metadata);

    private void given(String rfc, RegistrationStatus status, boolean withDocument) {
        Map<RegistrationSection, SectionStatus> progress = new EnumMap<>(RegistrationSection.class);
        for (RegistrationSection section : RegistrationSection.values()) progress.put(section, SectionStatus.PENDING);
        progress.put(RegistrationSection.GENERAL_DATA, SectionStatus.COMPLETED);
        progress.put(RegistrationSection.CUCOP, SectionStatus.COMPLETED);
        SupplierRegistrationId id = new SupplierRegistrationId(REGISTRATION_ID);
        SupplierRegistration registration = SupplierRegistration.reconstitute(id, status,
            new ApplicantIdentity(new Rfc(rfc), "EMPRESA SA"), new NoticeAcceptance("1.0", NOW),
            List.of(new ApplicantSignatureEvidence("{}")), null,
            withDocument ? new FiscalIdentificationDocument(id, "cedula.pdf", new StoredDocumentReference(FILE_ID)) : null,
            null, progress);
        persistence.stored = new StoredRegistration(registration, new NoticeAcceptanceReference("acceptance-1"),
            Set.of(new SignatureEvidenceReference("evidence-1")));
    }

    private void delete() {
        service.execute(new DeleteFiscalDocumentCommand(REGISTRATION_ID, "jti"));
    }

    @Test
    void US10_CA006_delete_dissociatesAndMarksTheMetadataNotAssociated() {
        given("AAA010101AAA", RegistrationStatus.IN_PROGRESS, true);

        delete();

        SupplierRegistration saved = persistence.dissociated.getFirst();
        assertNull(saved.associatedDocument());
        assertEquals(List.of(FILE_ID), markedNotAssociated);
        assertTrue(persistence.locked);
    }

    @Test
    void US10_CA007_delete_leavesGeneralDataPendingAndCucopIntact() {
        given("AAA010101AAA", RegistrationStatus.IN_PROGRESS, true);

        delete();

        SupplierRegistration saved = persistence.dissociated.getFirst();
        assertEquals(SectionStatus.PENDING, saved.sectionStatus(RegistrationSection.GENERAL_DATA));
        assertEquals(SectionStatus.COMPLETED, saved.sectionStatus(RegistrationSection.CUCOP));
    }

    @Test
    void US10_withoutDocument_isFiscalDocumentNotFoundWithoutChanges() {
        given("AAA010101AAA", RegistrationStatus.IN_PROGRESS, false);

        assertThrows(FiscalDocumentNotFoundException.class, this::delete);
        assertNoChanges();
    }

    @Test
    void US10_registeredRegistration_isRejectedWithoutChanges() {
        given("AAA010101AAA", RegistrationStatus.REGISTERED, true);

        assertThrows(InvalidRegistrationStateException.class, this::delete);
        assertNoChanges();
    }

    @Test
    void US10_registrationOfAnotherRfc_isDenied() {
        given("HEGT7610034S2", RegistrationStatus.IN_PROGRESS, true);

        assertThrows(RegistrationAccessDeniedException.class, this::delete);
        assertNoChanges();
    }

    @Test
    void US10_unknownRegistration_isNotFound() {
        assertThrows(RegistrationNotFoundException.class, this::delete);
        assertNoChanges();
    }

    private void assertNoChanges() {
        assertTrue(persistence.dissociated.isEmpty());
        assertTrue(markedNotAssociated.isEmpty());
    }

    private static final class FakePersistence extends UnusedSupplierRegistrationPersistence {
        private StoredRegistration stored;
        private boolean locked;
        private final List<SupplierRegistration> dissociated = new ArrayList<>();

        @Override
        public Optional<StoredRegistration> findForUpdate(SupplierRegistrationId id) {
            locked = true;
            return Optional.ofNullable(stored);
        }

        @Override
        public void saveFiscalDocumentDissociation(SupplierRegistration registration) {
            dissociated.add(registration);
        }
    }
}
