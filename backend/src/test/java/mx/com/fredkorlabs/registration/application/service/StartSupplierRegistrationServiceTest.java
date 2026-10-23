package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.RegistrationAlreadyExistsException;
import mx.com.fredkorlabs.registration.application.port.in.RegistrationSessionUnavailableException;
import mx.com.fredkorlabs.registration.application.port.in.StartSupplierRegistrationCommand;
import mx.com.fredkorlabs.registration.application.port.in.SupplierRegistrationResult;
import mx.com.fredkorlabs.registration.application.port.in.SupplierRegistrationResult.SectionProgressItem;
import mx.com.fredkorlabs.registration.application.port.out.DuplicateRegistrationException;
import mx.com.fredkorlabs.registration.application.port.out.NoticeAcceptanceReference;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationContextPort;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationSession;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationStatusView;
import mx.com.fredkorlabs.registration.application.port.out.SignatureEvidenceReference;
import mx.com.fredkorlabs.registration.domain.model.ApplicantIdentity;
import mx.com.fredkorlabs.registration.domain.model.ApplicantSignatureEvidence;
import mx.com.fredkorlabs.registration.domain.model.NoticeAcceptance;
import mx.com.fredkorlabs.registration.domain.model.RegistrationStatus;
import mx.com.fredkorlabs.registration.domain.model.Rfc;
import mx.com.fredkorlabs.registration.domain.model.SupplierRegistration;
import mx.com.fredkorlabs.registration.domain.model.SupplierRegistrationId;
import mx.com.fredkorlabs.registration.testsupport.UnusedSupplierRegistrationPersistence;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StartSupplierRegistrationServiceTest {

    private static final String JTI = "0f8b5c1e-3a2d-4e6f-8a9b-1c2d3e4f5a6b";
    private static final Instant ACCEPTED_AT = Instant.parse("2026-10-21T15:30:00Z");
    private static final RegistrationSession SESSION = new RegistrationSession(
        new ApplicantIdentity(new Rfc("AAA010101AAA"), "EMPRESA EJEMPLO SA DE CV"),
        new ApplicantSignatureEvidence("{\"resultado\":true}"),
        new SignatureEvidenceReference("evidence-1"),
        new NoticeAcceptance("1.0", ACCEPTED_AT),
        new NoticeAcceptanceReference("acceptance-1"));

    private final FakeContexts contexts = new FakeContexts();
    private final FakePersistence persistence = new FakePersistence();
    private final StartSupplierRegistrationService service = new StartSupplierRegistrationService(contexts, persistence);

    private SupplierRegistrationResult start() {
        return service.execute(new StartSupplierRegistrationCommand(JTI));
    }

    @Test
    void US04_CA001_withoutRegistration_insertsInProgressRegistration() {
        SupplierRegistrationResult result = start();

        assertEquals(1, persistence.inserted.size());
        assertEquals(RegistrationStatus.IN_PROGRESS, persistence.inserted.getFirst().status());
        assertEquals("IN_PROGRESS", result.status());
    }

    @Test
    void US04_CA002_newRegistration_hasAllSectionsPendingStartingWithGeneralData() {
        List<SectionProgressItem> progress = start().progress();

        assertEquals(List.of("GENERAL_DATA", "CUCOP", "CONTACTS", "FISCAL_ADDRESS", "COVERAGE", "ELECTRONIC_SIGNATURE"),
            progress.stream().map(SectionProgressItem::section).toList());
        assertTrue(progress.stream().allMatch(item -> item.status().equals("PENDING")));
    }

    @Test
    void US04_CA003_existingRegistration_isRejectedWithoutInserting() {
        persistence.existing = new RegistrationStatusView(
            new SupplierRegistrationId(UUID.randomUUID()), RegistrationStatus.IN_PROGRESS);

        assertThrows(RegistrationAlreadyExistsException.class, this::start);
        assertTrue(persistence.inserted.isEmpty());
    }

    @Test
    void US04_CA003_registeredRegistration_isRejected() {
        persistence.existing = new RegistrationStatusView(
            new SupplierRegistrationId(UUID.randomUUID()), RegistrationStatus.REGISTERED);

        assertThrows(RegistrationAlreadyExistsException.class, this::start);
    }

    @Test
    void US04_CA003_concurrentCreation_detectedOnInsert_isRejected() {
        persistence.insertFailure = new DuplicateRegistrationException("unique rfc", null);

        assertThrows(RegistrationAlreadyExistsException.class, this::start);
    }

    @Test
    void US04_CA004_lookupFailure_propagatesWithoutInserting() {
        persistence.lookupFailure = new IllegalStateException("Database unavailable");

        assertThrows(IllegalStateException.class, this::start);
        assertTrue(persistence.inserted.isEmpty());
    }

    @Test
    void US04_CA004_insertFailure_propagatesInsteadOfReportingCreation() {
        persistence.insertFailure = new IllegalStateException("Database unavailable");

        assertThrows(IllegalStateException.class, this::start);
    }

    @Test
    void US04_CA005_eachRegistration_getsItsOwnGeneratedUuid() {
        UUID first = start().id();
        UUID second = service.execute(new StartSupplierRegistrationCommand(JTI)).id();

        assertNotNull(first);
        assertNotEquals(first, second);
        assertEquals(first, persistence.inserted.getFirst().id().value());
    }

    @Test
    void US04_CA006_registration_takesIdentityFromTheTokenContext() {
        SupplierRegistrationResult result = start();

        assertEquals(List.of(JTI), contexts.resolved);
        assertEquals(List.of(new Rfc("AAA010101AAA")), persistence.queried);
        SupplierRegistration inserted = persistence.inserted.getFirst();
        assertEquals(SESSION.identity(), inserted.applicant());
        assertEquals(List.of(SESSION.evidence()), inserted.acceptedEvidence());
        assertEquals(List.of(SESSION.evidenceReference()), persistence.insertedEvidence);
        assertEquals("AAA010101AAA", result.rfc());
        assertEquals("EMPRESA EJEMPLO SA DE CV", result.legalName());
        assertEquals("LEGAL_ENTITY", result.taxRegime());
        assertEquals(1, result.taxRegimeId());
        assertEquals("aaa010101aaa", result.username());
    }

    @Test
    void US03_CA005_registration_keepsTheTokenAcceptance() {
        SupplierRegistrationResult result = start();

        assertEquals(SESSION.acceptance(), persistence.inserted.getFirst().latestAcceptance());
        assertEquals(List.of(SESSION.acceptanceReference()), persistence.insertedAcceptances);
        assertEquals("1.0", result.noticeVersion());
        assertEquals(ACCEPTED_AT, result.noticeAcceptedAt());
        assertEquals("acceptance-1", result.noticeAcceptanceId());
    }

    @Test
    void ADR0015_tokenWithoutContext_isRejectedWithoutLookup() {
        contexts.session = null;

        assertThrows(RegistrationSessionUnavailableException.class, this::start);
        assertTrue(persistence.queried.isEmpty());
        assertTrue(persistence.inserted.isEmpty());
    }

    private static final class FakeContexts implements RegistrationContextPort {
        private RegistrationSession session = SESSION;
        private final List<String> resolved = new ArrayList<>();

        @Override
        public String issue(ApplicantIdentity identity, SignatureEvidenceReference evidence,
                            NoticeAcceptanceReference acceptance, Instant issuedAt, Instant expiresAt) {
            throw new UnsupportedOperationException("Not used by this use case");
        }

        @Override
        public boolean isActive(String jti, String rfc, Instant now) { return true; }

        @Override
        public Optional<RegistrationSession> resolve(String jti) {
            resolved.add(jti);
            return Optional.ofNullable(session);
        }
    }

    private static final class FakePersistence extends UnusedSupplierRegistrationPersistence {
        private RegistrationStatusView existing;
        private RuntimeException lookupFailure;
        private RuntimeException insertFailure;
        private final List<Rfc> queried = new ArrayList<>();
        private final List<SupplierRegistration> inserted = new ArrayList<>();
        private final List<SignatureEvidenceReference> insertedEvidence = new ArrayList<>();
        private final List<NoticeAcceptanceReference> insertedAcceptances = new ArrayList<>();

        @Override
        public Optional<RegistrationStatusView> findStatusByRfc(Rfc rfc) {
            queried.add(rfc);
            if (lookupFailure != null) throw lookupFailure;
            return Optional.ofNullable(existing);
        }

        @Override
        public void insert(SupplierRegistration registration, SignatureEvidenceReference initialEvidence,
                           NoticeAcceptanceReference acceptance) {
            if (insertFailure != null) throw insertFailure;
            inserted.add(registration);
            insertedEvidence.add(initialEvidence);
            insertedAcceptances.add(acceptance);
        }

    }
}
