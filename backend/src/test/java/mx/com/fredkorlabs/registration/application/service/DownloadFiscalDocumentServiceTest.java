package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.DownloadFiscalDocumentQuery;
import mx.com.fredkorlabs.registration.application.port.in.DownloadFiscalDocumentResult;
import mx.com.fredkorlabs.registration.application.port.in.FiscalDocumentNotFoundException;
import mx.com.fredkorlabs.registration.application.port.in.RegistrationAccessDeniedException;
import mx.com.fredkorlabs.registration.application.port.in.RegistrationNotFoundException;
import mx.com.fredkorlabs.registration.application.port.out.FileMetadataPort;
import mx.com.fredkorlabs.registration.application.port.out.FileStoragePort;
import mx.com.fredkorlabs.registration.application.port.out.FiscalDocumentView;
import mx.com.fredkorlabs.registration.application.port.out.NoticeAcceptanceReference;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationContextPort;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationSession;
import mx.com.fredkorlabs.registration.application.port.out.SignatureEvidenceReference;
import mx.com.fredkorlabs.registration.application.port.out.StoredFileMetadata;
import mx.com.fredkorlabs.registration.application.port.out.StoredRegistration;
import mx.com.fredkorlabs.registration.domain.model.ApplicantIdentity;
import mx.com.fredkorlabs.registration.domain.model.ApplicantSignatureEvidence;
import mx.com.fredkorlabs.registration.domain.model.NoticeAcceptance;
import mx.com.fredkorlabs.registration.domain.model.RegistrationSection;
import mx.com.fredkorlabs.registration.domain.model.RegistrationStatus;
import mx.com.fredkorlabs.registration.domain.model.Rfc;
import mx.com.fredkorlabs.registration.domain.model.SectionStatus;
import mx.com.fredkorlabs.registration.domain.model.SupplierRegistration;
import mx.com.fredkorlabs.registration.domain.model.SupplierRegistrationId;
import mx.com.fredkorlabs.registration.testsupport.UnusedSupplierRegistrationPersistence;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DownloadFiscalDocumentServiceTest {

    private static final UUID REGISTRATION_ID = UUID.fromString("7b1f8f8e-1c2d-4a4b-9a0e-5f6a7b8c9d0e");
    private static final UUID FILE_ID = UUID.fromString("8bd04b95-88ae-4dd8-bd19-27229040225e");
    private static final Instant UPLOADED_AT = Instant.parse("2026-10-23T05:30:00Z");
    private static final String PATH = "2026/10/23/AAA010101AAA/" + FILE_ID + ".pdf";
    private static final byte[] PDF = "%PDF-1.7\n%%EOF".getBytes(StandardCharsets.US_ASCII);
    private static final RegistrationSession SESSION = new RegistrationSession(
        new ApplicantIdentity(new Rfc("AAA010101AAA"), "EMPRESA SA"),
        new ApplicantSignatureEvidence("{}"), new SignatureEvidenceReference("evidence-1"),
        new NoticeAcceptance("1.0", UPLOADED_AT), new NoticeAcceptanceReference("acceptance-1"));

    private final FakePersistence persistence = new FakePersistence();
    private final List<String> readPaths = new ArrayList<>();
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
        public Optional<StoredFileMetadata> find(UUID fileId) {
            return Optional.of(new StoredFileMetadata(fileId, new SupplierRegistrationId(REGISTRATION_ID),
                "Cédula Fiscal.pdf", PATH, "pdf", "application/pdf", PDF.length, UPLOADED_AT, true));
        }
    };
    private final FileStoragePort storage = new FileStoragePort() {
        @Override
        public void store(String relativePath, byte[] content) { throw new UnsupportedOperationException("Not used"); }

        @Override
        public byte[] read(String relativePath) {
            readPaths.add(relativePath);
            return PDF;
        }
    };
    private final DownloadFiscalDocumentService service =
        new DownloadFiscalDocumentService(contexts, persistence, metadata, storage);

    private void given(String rfc, RegistrationStatus status, FiscalDocumentView document) {
        Map<RegistrationSection, SectionStatus> progress = new EnumMap<>(RegistrationSection.class);
        for (RegistrationSection section : RegistrationSection.values()) progress.put(section, SectionStatus.PENDING);
        SupplierRegistration registration = SupplierRegistration.reconstitute(new SupplierRegistrationId(REGISTRATION_ID),
            status, new ApplicantIdentity(new Rfc(rfc), "EMPRESA SA"), new NoticeAcceptance("1.0", UPLOADED_AT),
            List.of(new ApplicantSignatureEvidence("{}")), null, null, null, progress);
        persistence.stored = new StoredRegistration(registration, new NoticeAcceptanceReference("acceptance-1"),
            Set.of(new SignatureEvidenceReference("evidence-1")), document);
    }

    private static FiscalDocumentView document() {
        return new FiscalDocumentView(FILE_ID, "Cédula Fiscal.pdf", "pdf", PDF.length, UPLOADED_AT);
    }

    private DownloadFiscalDocumentResult download() {
        return service.execute(new DownloadFiscalDocumentQuery(REGISTRATION_ID, "jti"));
    }

    @Test
    void US10_CA005_associatedDocument_isReturnedWithItsOriginalName() {
        given("AAA010101AAA", RegistrationStatus.IN_PROGRESS, document());

        DownloadFiscalDocumentResult result = download();

        assertEquals("Cédula Fiscal.pdf", result.originalName());
        assertEquals("application/pdf", result.mediaType());
        assertArrayEquals(PDF, result.content());
        assertEquals(List.of(PATH), readPaths);
        assertFalse(persistence.locked, "Downloading must not lock the registration");
    }

    @Test
    void US10_registeredRegistration_canStillDownloadItsDocument() {
        given("AAA010101AAA", RegistrationStatus.REGISTERED, document());

        assertArrayEquals(PDF, download().content());
    }

    @Test
    void US10_CA011_withoutAssociatedDocument_isFiscalDocumentNotFound() {
        given("AAA010101AAA", RegistrationStatus.IN_PROGRESS, null);

        assertThrows(FiscalDocumentNotFoundException.class, this::download);
        assertTrue(readPaths.isEmpty());
    }

    @Test
    void US10_registrationOfAnotherRfc_isDenied() {
        given("HEGT7610034S2", RegistrationStatus.IN_PROGRESS, document());

        assertThrows(RegistrationAccessDeniedException.class, this::download);
        assertTrue(readPaths.isEmpty());
    }

    @Test
    void US10_unknownRegistration_isNotFound() {
        assertThrows(RegistrationNotFoundException.class, this::download);
    }

    private static final class FakePersistence extends UnusedSupplierRegistrationPersistence {
        private StoredRegistration stored;
        private boolean locked;

        @Override
        public Optional<StoredRegistration> find(SupplierRegistrationId id) {
            return Optional.ofNullable(stored);
        }

        @Override
        public Optional<StoredRegistration> findForUpdate(SupplierRegistrationId id) {
            locked = true;
            return Optional.ofNullable(stored);
        }
    }
}
