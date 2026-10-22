package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.CheckRegistrationExistenceQuery;
import mx.com.fredkorlabs.registration.application.port.in.CheckRegistrationExistenceResult;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationStatusView;
import mx.com.fredkorlabs.registration.application.port.out.SupplierRegistrationPersistencePort;
import mx.com.fredkorlabs.registration.domain.model.RegistrationStatus;
import mx.com.fredkorlabs.registration.domain.model.Rfc;
import mx.com.fredkorlabs.registration.domain.model.SupplierRegistrationId;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CheckRegistrationExistenceServiceTest {

    private static final UUID REGISTRATION_ID = UUID.fromString("7b1f8f8e-1c2d-4a4b-9a0e-5f6a7b8c9d0e");

    private final FakePersistence persistence = new FakePersistence();
    private final CheckRegistrationExistenceService service = new CheckRegistrationExistenceService(persistence);

    @Test
    void US02_CA001_noRegistration_returnsNotExisting() {
        CheckRegistrationExistenceResult result = service.execute(new CheckRegistrationExistenceQuery("AAA010101AAA"));

        assertFalse(result.exists());
        assertNull(result.registrationId());
        assertNull(result.status());
    }

    @Test
    void US02_CA002_inProgressRegistration_isReportedForResuming() {
        persistence.found = new RegistrationStatusView(new SupplierRegistrationId(REGISTRATION_ID), RegistrationStatus.IN_PROGRESS);

        CheckRegistrationExistenceResult result = service.execute(new CheckRegistrationExistenceQuery("AAA010101AAA"));

        assertTrue(result.exists());
        assertEquals(REGISTRATION_ID, result.registrationId());
        assertEquals("IN_PROGRESS", result.status());
    }

    @Test
    void US02_CA003_registeredRegistration_isReported() {
        persistence.found = new RegistrationStatusView(new SupplierRegistrationId(REGISTRATION_ID), RegistrationStatus.REGISTERED);

        CheckRegistrationExistenceResult result = service.execute(new CheckRegistrationExistenceQuery("AAA010101AAA"));

        assertTrue(result.exists());
        assertEquals("REGISTERED", result.status());
    }

    @Test
    void US02_CA004_lookupFailure_propagatesInsteadOfReportingNotExisting() {
        persistence.failure = new IllegalStateException("Database unavailable");

        assertThrows(IllegalStateException.class,
            () -> service.execute(new CheckRegistrationExistenceQuery("AAA010101AAA")));
    }

    @Test
    void US02_CA005_lookupUsesTheIdentifiedRfcUnchanged() {
        service.execute(new CheckRegistrationExistenceQuery("HEGT7610034S2"));

        assertEquals(List.of(new Rfc("HEGT7610034S2")), persistence.queried);
    }

    private static final class FakePersistence implements SupplierRegistrationPersistencePort {
        private RegistrationStatusView found;
        private RuntimeException failure;
        private final List<Rfc> queried = new ArrayList<>();

        @Override
        public Optional<RegistrationStatusView> findStatusByRfc(Rfc rfc) {
            queried.add(rfc);
            if (failure != null) throw failure;
            return Optional.ofNullable(found);
        }
    }
}
