package mx.com.fredkorlabs.registration.adapter.out.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationStatusView;
import mx.com.fredkorlabs.registration.domain.model.RegistrationStatus;
import mx.com.fredkorlabs.registration.domain.model.Rfc;
import mx.com.fredkorlabs.registration.testsupport.PostgresTestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.Optional;
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
}
