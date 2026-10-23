package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.RegistrationAccessDeniedException;
import mx.com.fredkorlabs.registration.application.port.in.RegistrationNotFoundException;
import mx.com.fredkorlabs.registration.application.port.in.SaveGeneralDataCommand;
import mx.com.fredkorlabs.registration.application.port.in.SectionSaveResult;
import mx.com.fredkorlabs.registration.application.port.out.GeneralDataSelection;
import mx.com.fredkorlabs.registration.application.port.out.NoticeAcceptanceReference;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationContextPort;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationSession;
import mx.com.fredkorlabs.registration.application.port.out.SignatureEvidenceReference;
import mx.com.fredkorlabs.registration.application.port.out.StoredRegistration;
import mx.com.fredkorlabs.registration.domain.exception.FiscalDocumentRequiredException;
import mx.com.fredkorlabs.registration.domain.exception.InvalidGeneralDataException;
import mx.com.fredkorlabs.registration.domain.exception.InvalidRegistrationStateException;
import mx.com.fredkorlabs.registration.domain.model.ApplicantIdentity;
import mx.com.fredkorlabs.registration.domain.model.ApplicantSignatureEvidence;
import mx.com.fredkorlabs.registration.domain.model.FiscalIdentificationDocument;
import mx.com.fredkorlabs.registration.domain.model.GeneralDataCatalogContext;
import mx.com.fredkorlabs.registration.domain.model.LegalOwnershipOption;
import mx.com.fredkorlabs.registration.domain.model.NoticeAcceptance;
import mx.com.fredkorlabs.registration.domain.model.RegistrationSection;
import mx.com.fredkorlabs.registration.domain.model.RegistrationStatus;
import mx.com.fredkorlabs.registration.domain.model.Rfc;
import mx.com.fredkorlabs.registration.domain.model.SectionStatus;
import mx.com.fredkorlabs.registration.domain.model.StoredDocumentReference;
import mx.com.fredkorlabs.registration.domain.model.SupplierRegistration;
import mx.com.fredkorlabs.registration.domain.model.SupplierRegistrationId;
import mx.com.fredkorlabs.registration.domain.model.TaxRegime;
import mx.com.fredkorlabs.registration.testsupport.UnusedGeneralDataCatalog;
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

class SaveGeneralDataServiceTest {

    private static final UUID REGISTRATION_ID = UUID.fromString("7b1f8f8e-1c2d-4a4b-9a0e-5f6a7b8c9d0e");
    private static final Instant NOW = Instant.parse("2026-10-23T05:30:00Z");
    private static final RegistrationSession SESSION = new RegistrationSession(
        new ApplicantIdentity(new Rfc("AAA010101AAA"), "EMPRESA SA"),
        new ApplicantSignatureEvidence("{}"), new SignatureEvidenceReference("evidence-1"),
        new NoticeAcceptance("1.0", NOW), new NoticeAcceptanceReference("acceptance-1"));
    private static final LegalOwnershipOption MX_MORAL =
        new LegalOwnershipOption(1, "MX00001", "1.- SOCIEDAD ANÓNIMA (S.A.)", "MX", TaxRegime.LEGAL_ENTITY);

    private final FakePersistence persistence = new FakePersistence();
    private final List<GeneralDataSelection> resolved = new ArrayList<>();
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
    private final SaveGeneralDataService service = new SaveGeneralDataService(contexts, persistence,
        new UnusedGeneralDataCatalog() {
            @Override
            public GeneralDataCatalogContext resolveGeneralDataReferences(GeneralDataSelection selection) {
                resolved.add(selection);
                return new GeneralDataCatalogContext("MX", MX_MORAL, true, 5, true, true, true, true, true);
            }
        });

    private void given(String rfc, RegistrationStatus status, boolean withDocument) {
        Map<RegistrationSection, SectionStatus> progress = new EnumMap<>(RegistrationSection.class);
        for (RegistrationSection section : RegistrationSection.values()) progress.put(section, SectionStatus.PENDING);
        progress.put(RegistrationSection.CUCOP, SectionStatus.COMPLETED);
        SupplierRegistrationId id = new SupplierRegistrationId(REGISTRATION_ID);
        persistence.stored = new StoredRegistration(SupplierRegistration.reconstitute(id, status,
                new ApplicantIdentity(new Rfc(rfc), "EMPRESA SA"), new NoticeAcceptance("1.0", NOW),
                List.of(new ApplicantSignatureEvidence("{}")), null,
                withDocument ? new FiscalIdentificationDocument(id, "cedula.pdf",
                    new StoredDocumentReference(UUID.randomUUID())) : null,
                null, progress),
            new NoticeAcceptanceReference("acceptance-1"), Set.of(new SignatureEvidenceReference("evidence-1")));
    }

    private static SaveGeneralDataCommand command(String nationality) {
        return new SaveGeneralDataCommand(REGISTRATION_ID, "jti", "NATIONAL", 151, nationality, null, 1, 5, 21,
            1, 2, 3, 4, 2, null, List.of(new SaveGeneralDataCommand.Phone("5512345678", null)));
    }

    @Test
    void US08_CA006_validSection_isSavedCompletedAndContinuesToCucop() {
        given("AAA010101AAA", RegistrationStatus.IN_PROGRESS, true);

        SectionSaveResult result = service.execute(command("Mexicana"));

        assertEquals(new SectionSaveResult(REGISTRATION_ID, "GENERAL_DATA", "COMPLETED", "CUCOP"), result);
        SupplierRegistration saved = persistence.saved.getFirst();
        assertEquals("Mexicana", saved.generalData().nationality().value());
        assertEquals(SectionStatus.COMPLETED, saved.sectionStatus(RegistrationSection.GENERAL_DATA));
        assertEquals(SectionStatus.COMPLETED, saved.sectionStatus(RegistrationSection.CUCOP));
        assertEquals(List.of(new GeneralDataSelection(151, 1, 5, 21, 1, 2, 3, 4, 2)), resolved);
        assertTrue(persistence.locked);
    }

    @Test
    void US08_CA007_invalidSection_isRejectedWithoutSaving() {
        given("AAA010101AAA", RegistrationStatus.IN_PROGRESS, true);

        assertThrows(InvalidGeneralDataException.class, () -> service.execute(command("Alemana")));
        assertTrue(persistence.saved.isEmpty());
    }

    @Test
    void US10_CA003_withoutFiscalDocument_isRejectedWithoutSaving() {
        given("AAA010101AAA", RegistrationStatus.IN_PROGRESS, false);

        assertThrows(FiscalDocumentRequiredException.class, () -> service.execute(command("Mexicana")));
        assertTrue(persistence.saved.isEmpty());
    }

    @Test
    void US08_registeredRegistration_isRejectedBeforeValidating() {
        given("AAA010101AAA", RegistrationStatus.REGISTERED, true);

        assertThrows(InvalidRegistrationStateException.class, () -> service.execute(command("Alemana")));
        assertTrue(resolved.isEmpty());
    }

    @Test
    void US08_registrationOfAnotherRfc_isDenied() {
        given("HEGT7610034S2", RegistrationStatus.IN_PROGRESS, true);

        assertThrows(RegistrationAccessDeniedException.class, () -> service.execute(command("Mexicana")));
        assertTrue(persistence.saved.isEmpty());
    }

    @Test
    void US08_unknownRegistration_isNotFound() {
        assertThrows(RegistrationNotFoundException.class, () -> service.execute(command("Mexicana")));
    }

    @Test
    void US06_CA004_saveFailure_propagatesSoTheTransactionRollsBack() {
        given("AAA010101AAA", RegistrationStatus.IN_PROGRESS, true);
        persistence.saveFailure = new IllegalStateException("Database unavailable");

        assertThrows(IllegalStateException.class, () -> service.execute(command("Mexicana")));
    }

    private static final class FakePersistence extends UnusedSupplierRegistrationPersistence {
        private StoredRegistration stored;
        private boolean locked;
        private RuntimeException saveFailure;
        private final List<SupplierRegistration> saved = new ArrayList<>();

        @Override
        public Optional<StoredRegistration> findForUpdate(SupplierRegistrationId id) {
            locked = true;
            return Optional.ofNullable(stored);
        }

        @Override
        public void saveGeneralData(SupplierRegistration registration) {
            if (saveFailure != null) throw saveFailure;
            saved.add(registration);
        }
    }
}
