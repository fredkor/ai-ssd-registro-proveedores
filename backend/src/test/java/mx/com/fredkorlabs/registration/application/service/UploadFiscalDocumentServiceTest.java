package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.RegistrationAccessDeniedException;
import mx.com.fredkorlabs.registration.application.port.in.RegistrationNotFoundException;
import mx.com.fredkorlabs.registration.application.port.in.UploadFiscalDocumentCommand;
import mx.com.fredkorlabs.registration.application.port.in.UploadFiscalDocumentResult;
import mx.com.fredkorlabs.registration.application.port.out.FileMetadataPort;
import mx.com.fredkorlabs.registration.application.port.out.FileStoragePort;
import mx.com.fredkorlabs.registration.application.port.out.NoticeAcceptanceReference;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationContextPort;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationSession;
import mx.com.fredkorlabs.registration.application.port.out.SignatureEvidenceReference;
import mx.com.fredkorlabs.registration.application.port.out.StoredFileMetadata;
import mx.com.fredkorlabs.registration.application.port.out.StoredRegistration;
import mx.com.fredkorlabs.registration.domain.exception.DocumentFormatNotSupportedException;
import mx.com.fredkorlabs.registration.domain.exception.DocumentTooLargeException;
import mx.com.fredkorlabs.registration.domain.exception.FiscalDocumentAlreadyExistsException;
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

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UploadFiscalDocumentServiceTest {

    private static final UUID REGISTRATION_ID = UUID.fromString("7b1f8f8e-1c2d-4a4b-9a0e-5f6a7b8c9d0e");
    private static final String JTI = "0f8b5c1e-3a2d-4e6f-8a9b-1c2d3e4f5a6b";
    private static final Instant NOW = Instant.parse("2026-10-23T05:30:00.123456789Z");
    private static final byte[] PDF = "%PDF-1.7\n%%EOF".getBytes(StandardCharsets.US_ASCII);
    private static final RegistrationSession SESSION = new RegistrationSession(
        new ApplicantIdentity(new Rfc("AAA010101AAA"), "EMPRESA SA"),
        new ApplicantSignatureEvidence("{}"), new SignatureEvidenceReference("evidence-1"),
        new NoticeAcceptance("1.0", NOW), new NoticeAcceptanceReference("acceptance-1"));

    private final FakePersistence persistence = new FakePersistence();
    private final List<String> storedPaths = new ArrayList<>();
    private final List<StoredFileMetadata> registered = new ArrayList<>();
    private final FileStoragePort storage = new FileStoragePort() {
        @Override
        public void store(String relativePath, byte[] content) { storedPaths.add(relativePath); }

        @Override
        public byte[] read(String relativePath) { throw new UnsupportedOperationException("Not used by this use case"); }

        @Override
        public boolean delete(String relativePath) { throw new UnsupportedOperationException("Not used by this use case"); }
    };
    private final FileMetadataPort metadata = new FileMetadataPort() {
        @Override
        public void register(StoredFileMetadata file) { registered.add(file); }

        @Override
        public Optional<StoredFileMetadata> find(UUID fileId) {
            throw new UnsupportedOperationException("Not used by this use case");
        }

        @Override
        public void markNotAssociated(UUID fileId) {
            throw new UnsupportedOperationException("Not used by this use case");
        }

        @Override
        public List<StoredFileMetadata> lockNotAssociatedUploadedBefore(Instant instant) {
            throw new UnsupportedOperationException("Not used by this use case");
        }

        @Override
        public boolean isReferenced(UUID fileId) { throw new UnsupportedOperationException("Not used by this use case"); }

        @Override
        public void delete(UUID fileId) { throw new UnsupportedOperationException("Not used by this use case"); }
    };
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
    private final UploadFiscalDocumentService service = new UploadFiscalDocumentService(contexts, persistence,
        storage, metadata, Clock.fixed(NOW, ZoneOffset.UTC));

    private static SupplierRegistration registration(String rfc, RegistrationStatus status,
                                                     FiscalIdentificationDocument document) {
        Map<RegistrationSection, SectionStatus> progress = new EnumMap<>(RegistrationSection.class);
        for (RegistrationSection section : RegistrationSection.values()) progress.put(section, SectionStatus.PENDING);
        return SupplierRegistration.reconstitute(new SupplierRegistrationId(REGISTRATION_ID), status,
            new ApplicantIdentity(new Rfc(rfc), "EMPRESA SA"), new NoticeAcceptance("1.0", NOW),
            List.of(new ApplicantSignatureEvidence("{}")), null, document, null, progress);
    }

    private void given(SupplierRegistration registration) {
        persistence.stored = new StoredRegistration(registration, new NoticeAcceptanceReference("acceptance-1"),
            Set.of(new SignatureEvidenceReference("evidence-1")));
    }

    private UploadFiscalDocumentResult upload(String name, byte[] content) {
        return service.execute(new UploadFiscalDocumentCommand(REGISTRATION_ID, JTI, name, content));
    }

    @Test
    void US10_CA001_upload_storesContentRegistersMetadataAndAssociatesIt() {
        given(registration("AAA010101AAA", RegistrationStatus.IN_PROGRESS, null));

        UploadFiscalDocumentResult result = upload("Cédula Fiscal.PDF", PDF);

        assertEquals("Cédula Fiscal.PDF", result.originalName());
        assertEquals("pdf", result.extension());
        assertEquals(PDF.length, result.sizeBytes());
        assertEquals(Instant.parse("2026-10-23T05:30:00.123456Z"), result.uploadedAt());
        assertEquals(List.of("2026/10/23/AAA010101AAA/" + result.id() + ".pdf"), storedPaths);
        StoredFileMetadata stored = registered.getFirst();
        assertEquals(result.id(), stored.id());
        assertEquals(new SupplierRegistrationId(REGISTRATION_ID), stored.owner());
        assertEquals("application/pdf", stored.mediaType());
        assertTrue(stored.associated());
        assertEquals(List.of(new StoredDocumentReference(result.id())), persistence.associated);
    }

    @Test
    void US10_CA004_existingDocument_isRejectedWithoutStoringAnything() {
        given(registration("AAA010101AAA", RegistrationStatus.IN_PROGRESS, new FiscalIdentificationDocument(
            new SupplierRegistrationId(REGISTRATION_ID), "previa.pdf", new StoredDocumentReference(UUID.randomUUID()))));

        assertThrows(FiscalDocumentAlreadyExistsException.class, () -> upload("cedula.txt", PDF));
        assertNothingStored();
    }

    @Test
    void US10_registeredRegistration_isRejectedWithoutStoringAnything() {
        given(registration("AAA010101AAA", RegistrationStatus.REGISTERED, null));

        assertThrows(InvalidRegistrationStateException.class, () -> upload("cedula.pdf", PDF));
        assertNothingStored();
    }

    @Test
    void US10_CA008_tooLarge_isRejectedWithoutStoringAnything() {
        given(registration("AAA010101AAA", RegistrationStatus.IN_PROGRESS, null));
        byte[] tooLarge = new byte[5_242_881];
        System.arraycopy(PDF, 0, tooLarge, 0, PDF.length);

        assertThrows(DocumentTooLargeException.class, () -> upload("cedula.pdf", tooLarge));
        assertNothingStored();
    }

    @Test
    void US10_CA009_contentThatIsNotPdf_isRejectedWithoutStoringAnything() {
        given(registration("AAA010101AAA", RegistrationStatus.IN_PROGRESS, null));

        assertThrows(DocumentFormatNotSupportedException.class,
            () -> upload("cedula.pdf", "not a pdf".getBytes(StandardCharsets.US_ASCII)));
        assertNothingStored();
    }

    @Test
    void US10_registrationOfAnotherRfc_isDenied() {
        given(registration("HEGT7610034S2", RegistrationStatus.IN_PROGRESS, null));

        assertThrows(RegistrationAccessDeniedException.class, () -> upload("cedula.pdf", PDF));
        assertNothingStored();
    }

    @Test
    void US10_unknownRegistration_isNotFound() {
        assertThrows(RegistrationNotFoundException.class, () -> upload("cedula.pdf", PDF));
        assertNothingStored();
    }

    @Test
    void US10_associationFailure_propagatesSoTheTransactionRollsBack() {
        given(registration("AAA010101AAA", RegistrationStatus.IN_PROGRESS, null));
        persistence.associationFailure = new IllegalStateException("Database unavailable");

        assertThrows(IllegalStateException.class, () -> upload("cedula.pdf", PDF));
    }

    private void assertNothingStored() {
        assertTrue(storedPaths.isEmpty());
        assertTrue(registered.isEmpty());
        assertTrue(persistence.associated.isEmpty());
    }

    private static final class FakePersistence extends UnusedSupplierRegistrationPersistence {
        private StoredRegistration stored;
        private RuntimeException associationFailure;
        private final List<StoredDocumentReference> associated = new ArrayList<>();

        @Override
        public Optional<StoredRegistration> findForUpdate(SupplierRegistrationId id) {
            return Optional.ofNullable(stored);
        }

        @Override
        public void saveFiscalDocumentAssociation(SupplierRegistrationId registrationId,
                                                  StoredDocumentReference document) {
            if (associationFailure != null) throw associationFailure;
            associated.add(document);
        }
    }
}
