package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.RegistrationAccessDeniedException;
import mx.com.fredkorlabs.registration.application.port.in.RegistrationNotFoundException;
import mx.com.fredkorlabs.registration.application.port.in.RegistrationSessionUnavailableException;
import mx.com.fredkorlabs.registration.application.port.in.ResumeSupplierRegistrationCommand;
import mx.com.fredkorlabs.registration.application.port.in.SupplierRegistrationResult;
import mx.com.fredkorlabs.registration.application.port.in.SupplierRegistrationResult.SectionProgressItem;
import mx.com.fredkorlabs.registration.application.port.out.NoticeAcceptanceReference;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationContextPort;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationSession;
import mx.com.fredkorlabs.registration.application.port.out.SignatureEvidenceReference;
import mx.com.fredkorlabs.registration.application.port.out.StoredRegistration;
import mx.com.fredkorlabs.registration.domain.exception.InvalidRegistrationStateException;
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ResumeSupplierRegistrationServiceTest {

    private static final UUID REGISTRATION_ID = UUID.fromString("7b1f8f8e-1c2d-4a4b-9a0e-5f6a7b8c9d0e");
    private static final String JTI = "0f8b5c1e-3a2d-4e6f-8a9b-1c2d3e4f5a6b";
    private static final NoticeAcceptance PREVIOUS_ACCEPTANCE =
        new NoticeAcceptance("1.0", Instant.parse("2026-10-20T15:30:00Z"));
    private static final NoticeAcceptance NEW_ACCEPTANCE =
        new NoticeAcceptance("1.0", Instant.parse("2026-10-22T09:00:00Z"));
    private static final ApplicantSignatureEvidence INITIAL_EVIDENCE = new ApplicantSignatureEvidence("{\"initial\":true}");
    private static final RegistrationSession SESSION = new RegistrationSession(
        new ApplicantIdentity(new Rfc("AAA010101AAA"), "EMPRESA RENOMBRADA SA DE CV"),
        new ApplicantSignatureEvidence("{\"new\":true}"),
        new SignatureEvidenceReference("evidence-2"),
        NEW_ACCEPTANCE,
        new NoticeAcceptanceReference("acceptance-2"));

    private final FakeContexts contexts = new FakeContexts();
    private final FakePersistence persistence = new FakePersistence();
    private final ResumeSupplierRegistrationService service = new ResumeSupplierRegistrationService(contexts, persistence);

    private static StoredRegistration stored(String rfc, RegistrationStatus status) {
        Map<RegistrationSection, SectionStatus> progress = new EnumMap<>(RegistrationSection.class);
        for (RegistrationSection section : RegistrationSection.values()) progress.put(section, SectionStatus.PENDING);
        progress.put(RegistrationSection.GENERAL_DATA, SectionStatus.COMPLETED);
        progress.put(RegistrationSection.CUCOP, SectionStatus.COMPLETED);
        SupplierRegistration registration = SupplierRegistration.reconstitute(new SupplierRegistrationId(REGISTRATION_ID),
            status, new ApplicantIdentity(new Rfc(rfc), "EMPRESA ORIGINAL SA DE CV"), PREVIOUS_ACCEPTANCE,
            List.of(INITIAL_EVIDENCE), null, null, null, progress);
        return new StoredRegistration(registration, new NoticeAcceptanceReference("acceptance-1"),
            Set.of(new SignatureEvidenceReference("evidence-1")));
    }

    private SupplierRegistrationResult resume() {
        return service.execute(new ResumeSupplierRegistrationCommand(REGISTRATION_ID, JTI));
    }

    @Test
    void US05_CA001_ownInProgressRegistration_isResumed() {
        persistence.stored = stored("AAA010101AAA", RegistrationStatus.IN_PROGRESS);

        SupplierRegistrationResult result = resume();

        assertEquals("IN_PROGRESS", result.status());
        assertEquals(List.of(new SupplierRegistrationId(REGISTRATION_ID)), persistence.locked);
        assertEquals(1, persistence.saved.size());
    }

    @Test
    void US05_CA008_resume_usesTheSameRegistrationWithoutCreatingAnother() {
        persistence.stored = stored("AAA010101AAA", RegistrationStatus.IN_PROGRESS);

        assertEquals(REGISTRATION_ID, resume().id());
        assertEquals(REGISTRATION_ID, persistence.saved.getFirst().id().value());
        assertEquals(0, persistence.inserts);
    }

    @Test
    void US05_CA003_resume_returnsStoredProgressWithoutResettingIt() {
        persistence.stored = stored("AAA010101AAA", RegistrationStatus.IN_PROGRESS);

        List<SectionProgressItem> progress = resume().progress();

        assertEquals(new SectionProgressItem("GENERAL_DATA", "COMPLETED"), progress.get(0));
        assertEquals(new SectionProgressItem("CUCOP", "COMPLETED"), progress.get(1));
        assertEquals(new SectionProgressItem("CONTACTS", "PENDING"), progress.get(2));
    }

    @Test
    void US05_CA009_resume_takesLegalNameFromTheNewCertificate() {
        persistence.stored = stored("AAA010101AAA", RegistrationStatus.IN_PROGRESS);

        SupplierRegistrationResult result = resume();

        assertEquals("EMPRESA RENOMBRADA SA DE CV", result.legalName());
        assertEquals("AAA010101AAA", result.rfc());
        assertEquals("EMPRESA RENOMBRADA SA DE CV", persistence.saved.getFirst().applicant().legalName());
    }

    @Test
    void US05_CA010_resume_linksTheNewEvidenceKeepingPreviousOnes() {
        persistence.stored = stored("AAA010101AAA", RegistrationStatus.IN_PROGRESS);

        resume();

        assertEquals(List.of(SESSION.evidenceReference()), persistence.savedEvidence);
        assertEquals(List.of(INITIAL_EVIDENCE, SESSION.evidence()), persistence.saved.getFirst().acceptedEvidence());
    }

    @Test
    void US03_CA007_resume_replacesTheAcceptanceWithTheTokenOne() {
        persistence.stored = stored("AAA010101AAA", RegistrationStatus.IN_PROGRESS);

        SupplierRegistrationResult result = resume();

        assertEquals(List.of(SESSION.acceptanceReference()), persistence.savedAcceptances);
        assertEquals(NEW_ACCEPTANCE, persistence.saved.getFirst().latestAcceptance());
        assertEquals("acceptance-2", result.noticeAcceptanceId());
        assertEquals(NEW_ACCEPTANCE.acceptedAt(), result.noticeAcceptedAt());
    }

    @Test
    void US05_CA004_registrationOfAnotherRfc_isDeniedWithoutChanges() {
        persistence.stored = stored("HEGT7610034S2", RegistrationStatus.IN_PROGRESS);

        assertThrows(RegistrationAccessDeniedException.class, this::resume);
        assertTrue(persistence.saved.isEmpty());
    }

    @Test
    void US05_CA006_registeredRegistration_isRejectedWithoutChanges() {
        persistence.stored = stored("AAA010101AAA", RegistrationStatus.REGISTERED);

        assertThrows(InvalidRegistrationStateException.class, this::resume);
        assertTrue(persistence.saved.isEmpty());
    }

    @Test
    void US05_CA001_unknownRegistration_isNotFound() {
        assertThrows(RegistrationNotFoundException.class, this::resume);
        assertTrue(persistence.saved.isEmpty());
    }

    @Test
    void US05_CA005_lookupFailure_propagatesWithoutSaving() {
        persistence.lookupFailure = new IllegalStateException("Database unavailable");

        assertThrows(IllegalStateException.class, this::resume);
        assertTrue(persistence.saved.isEmpty());
    }

    @Test
    void US05_CA011_saveFailure_propagatesSoTheTransactionRollsBack() {
        persistence.stored = stored("AAA010101AAA", RegistrationStatus.IN_PROGRESS);
        persistence.saveFailure = new IllegalStateException("Database unavailable");

        assertThrows(IllegalStateException.class, this::resume);
    }

    @Test
    void US05_identificationAlreadyApplied_returnsCurrentStateWithoutChanges() {
        StoredRegistration current = stored("AAA010101AAA", RegistrationStatus.IN_PROGRESS);
        persistence.stored = new StoredRegistration(current.registration(), current.acceptanceReference(),
            Set.of(new SignatureEvidenceReference("evidence-1"), SESSION.evidenceReference()));

        SupplierRegistrationResult result = resume();

        assertTrue(persistence.saved.isEmpty());
        assertEquals("EMPRESA ORIGINAL SA DE CV", result.legalName());
        assertEquals("acceptance-1", result.noticeAcceptanceId());
        assertEquals(PREVIOUS_ACCEPTANCE.acceptedAt(), result.noticeAcceptedAt());
    }

    @Test
    void US05_CA006_identificationAlreadyApplied_onRegistered_isStillRejected() {
        StoredRegistration current = stored("AAA010101AAA", RegistrationStatus.REGISTERED);
        persistence.stored = new StoredRegistration(current.registration(), current.acceptanceReference(),
            Set.of(SESSION.evidenceReference()));

        assertThrows(InvalidRegistrationStateException.class, this::resume);
    }

    @Test
    void ADR0015_tokenWithoutContext_isRejectedWithoutLookup() {
        contexts.session = null;

        assertThrows(RegistrationSessionUnavailableException.class, this::resume);
        assertTrue(persistence.locked.isEmpty());
    }

    private static final class FakeContexts implements RegistrationContextPort {
        private RegistrationSession session = SESSION;

        @Override
        public String issue(ApplicantIdentity identity, SignatureEvidenceReference evidence,
                            NoticeAcceptanceReference acceptance, Instant issuedAt, Instant expiresAt) {
            throw new UnsupportedOperationException("Not used by this use case");
        }

        @Override
        public boolean isActive(String jti, String rfc, Instant now) { return true; }

        @Override
        public Optional<RegistrationSession> resolve(String jti) {
            return Optional.ofNullable(session);
        }
    }

    private static final class FakePersistence extends UnusedSupplierRegistrationPersistence {
        private StoredRegistration stored;
        private RuntimeException lookupFailure;
        private RuntimeException saveFailure;
        private int inserts;
        private final List<SupplierRegistrationId> locked = new ArrayList<>();
        private final List<SupplierRegistration> saved = new ArrayList<>();
        private final List<SignatureEvidenceReference> savedEvidence = new ArrayList<>();
        private final List<NoticeAcceptanceReference> savedAcceptances = new ArrayList<>();

        @Override
        public void insert(SupplierRegistration registration, SignatureEvidenceReference initialEvidence,
                           NoticeAcceptanceReference acceptance) {
            inserts++;
        }

        @Override
        public Optional<StoredRegistration> findForUpdate(SupplierRegistrationId id) {
            locked.add(id);
            if (lookupFailure != null) throw lookupFailure;
            return Optional.ofNullable(stored);
        }

        @Override
        public void saveResume(SupplierRegistration registration, SignatureEvidenceReference newEvidence,
                               NoticeAcceptanceReference newAcceptance) {
            if (saveFailure != null) throw saveFailure;
            saved.add(registration);
            savedEvidence.add(newEvidence);
            savedAcceptances.add(newAcceptance);
        }
    }
}
