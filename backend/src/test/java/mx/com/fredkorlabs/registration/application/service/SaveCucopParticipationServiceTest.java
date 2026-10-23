package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.RegistrationAccessDeniedException;
import mx.com.fredkorlabs.registration.application.port.in.RegistrationNotFoundException;
import mx.com.fredkorlabs.registration.application.port.in.SaveCucopParticipationCommand;
import mx.com.fredkorlabs.registration.application.port.in.SectionSaveResult;
import mx.com.fredkorlabs.registration.application.port.out.NoticeAcceptanceReference;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationContextPort;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationSession;
import mx.com.fredkorlabs.registration.application.port.out.SignatureEvidenceReference;
import mx.com.fredkorlabs.registration.application.port.out.StoredRegistration;
import mx.com.fredkorlabs.registration.domain.exception.InvalidCucopParticipationException;
import mx.com.fredkorlabs.registration.domain.exception.InvalidRegistrationStateException;
import mx.com.fredkorlabs.registration.domain.model.ApplicantIdentity;
import mx.com.fredkorlabs.registration.domain.model.ApplicantSignatureEvidence;
import mx.com.fredkorlabs.registration.domain.model.CucopSpecificItem;
import mx.com.fredkorlabs.registration.domain.model.NoticeAcceptance;
import mx.com.fredkorlabs.registration.domain.model.ParticipationType;
import mx.com.fredkorlabs.registration.domain.model.RegistrationSection;
import mx.com.fredkorlabs.registration.domain.model.RegistrationStatus;
import mx.com.fredkorlabs.registration.domain.model.Rfc;
import mx.com.fredkorlabs.registration.domain.model.SectionStatus;
import mx.com.fredkorlabs.registration.domain.model.SupplierRegistration;
import mx.com.fredkorlabs.registration.domain.model.SupplierRegistrationId;
import mx.com.fredkorlabs.registration.testsupport.UnusedCucopCatalog;
import mx.com.fredkorlabs.registration.testsupport.UnusedSupplierRegistrationPersistence;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SaveCucopParticipationServiceTest {

    private static final UUID REGISTRATION_ID = UUID.fromString("7b1f8f8e-1c2d-4a4b-9a0e-5f6a7b8c9d0e");
    private static final Instant NOW = Instant.parse("2026-10-23T05:30:00Z");
    private static final RegistrationSession SESSION = new RegistrationSession(
        new ApplicantIdentity(new Rfc("AAA010101AAA"), "EMPRESA SA"),
        new ApplicantSignatureEvidence("{}"), new SignatureEvidenceReference("evidence-1"),
        new NoticeAcceptance("1.0", NOW), new NoticeAcceptanceReference("acceptance-1"));
    private static final CucopSpecificItem PAPER = new CucopSpecificItem(1, 1, "21101", "MATERIALES Y ÚTILES", 4);
    private static final CucopSpecificItem TONER = new CucopSpecificItem(2, 1, "21102", "TÓNER", 4);

    private final FakePersistence persistence = new FakePersistence();
    private final List<Collection<Integer>> resolved = new ArrayList<>();
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
    private final SaveCucopParticipationService service = new SaveCucopParticipationService(contexts, persistence,
        new UnusedCucopCatalog() {
            @Override
            public List<CucopSpecificItem> findSpecificItems(Collection<Integer> ids) {
                resolved.add(List.copyOf(ids));
                return Set.of(PAPER, TONER).stream().filter(item -> ids.contains(item.id())).toList();
            }
        });

    private void given(String rfc, RegistrationStatus status, SectionStatus generalData) {
        Map<RegistrationSection, SectionStatus> progress = new EnumMap<>(RegistrationSection.class);
        for (RegistrationSection section : RegistrationSection.values()) progress.put(section, SectionStatus.PENDING);
        progress.put(RegistrationSection.GENERAL_DATA, generalData);
        persistence.stored = new StoredRegistration(SupplierRegistration.reconstitute(
                new SupplierRegistrationId(REGISTRATION_ID), status,
                new ApplicantIdentity(new Rfc(rfc), "EMPRESA SA"), new NoticeAcceptance("1.0", NOW),
                List.of(new ApplicantSignatureEvidence("{}")), null, null, null, progress),
            new NoticeAcceptanceReference("acceptance-1"), Set.of(new SignatureEvidenceReference("evidence-1")));
    }

    private static SaveCucopParticipationCommand command(List<String> types, List<Integer> ids) {
        return new SaveCucopParticipationCommand(REGISTRATION_ID, "jti", types, ids);
    }

    @Test
    void US11_CA011_validSelection_isSavedCompletedAndContinuesToContacts() {
        given("AAA010101AAA", RegistrationStatus.IN_PROGRESS, SectionStatus.COMPLETED);

        SectionSaveResult result = service.execute(command(
            List.of("GOODS_AND_SERVICES_SUPPLIER", "PUBLIC_WORKS_CONTRACTOR"), List.of(2, 1)));

        assertEquals(new SectionSaveResult(REGISTRATION_ID, "CUCOP", "COMPLETED", "CONTACTS"), result);
        SupplierRegistration saved = persistence.saved.getFirst();
        assertEquals(Set.of(ParticipationType.values()), saved.cucopParticipation().participationTypes());
        assertEquals(Set.of(PAPER, TONER), saved.cucopParticipation().specificItems());
        assertEquals(SectionStatus.COMPLETED, saved.sectionStatus(RegistrationSection.CUCOP));
        assertEquals(List.of(List.of(2, 1)), resolved);
        assertTrue(persistence.locked);
    }

    @Test
    void US11_CA012_itemOutsideTheCatalog_isRejectedWithoutSaving() {
        given("AAA010101AAA", RegistrationStatus.IN_PROGRESS, SectionStatus.COMPLETED);

        InvalidCucopParticipationException error = assertThrows(InvalidCucopParticipationException.class,
            () -> service.execute(command(List.of("GOODS_AND_SERVICES_SUPPLIER"), List.of(1, 999))));

        assertEquals("specificItemIds/1", error.violations().getFirst().field());
        assertTrue(persistence.saved.isEmpty());
    }

    @Test
    void US11_CA001_generalDataPending_isRejectedBeforeValidating() {
        given("AAA010101AAA", RegistrationStatus.IN_PROGRESS, SectionStatus.PENDING);

        assertThrows(InvalidRegistrationStateException.class,
            () -> service.execute(command(List.of(), List.of())));
        assertTrue(resolved.isEmpty());
        assertTrue(persistence.saved.isEmpty());
    }

    @Test
    void US11_registeredRegistration_isRejectedBeforeValidating() {
        given("AAA010101AAA", RegistrationStatus.REGISTERED, SectionStatus.COMPLETED);

        assertThrows(InvalidRegistrationStateException.class,
            () -> service.execute(command(List.of("GOODS_AND_SERVICES_SUPPLIER"), List.of(1))));
        assertTrue(resolved.isEmpty());
    }

    @Test
    void US11_registrationOfAnotherRfc_isDenied() {
        given("HEGT7610034S2", RegistrationStatus.IN_PROGRESS, SectionStatus.COMPLETED);

        assertThrows(RegistrationAccessDeniedException.class,
            () -> service.execute(command(List.of("GOODS_AND_SERVICES_SUPPLIER"), List.of(1))));
        assertTrue(persistence.saved.isEmpty());
    }

    @Test
    void US11_unknownRegistration_isNotFound() {
        assertThrows(RegistrationNotFoundException.class,
            () -> service.execute(command(List.of("GOODS_AND_SERVICES_SUPPLIER"), List.of(1))));
    }

    @Test
    void US11_CA012_saveFailure_propagatesSoTheTransactionRollsBack() {
        given("AAA010101AAA", RegistrationStatus.IN_PROGRESS, SectionStatus.COMPLETED);
        persistence.saveFailure = new IllegalStateException("Database unavailable");

        assertThrows(IllegalStateException.class,
            () -> service.execute(command(List.of("GOODS_AND_SERVICES_SUPPLIER"), List.of(1))));
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
        public void saveCucopParticipation(SupplierRegistration registration) {
            if (saveFailure != null) throw saveFailure;
            saved.add(registration);
        }
    }
}
