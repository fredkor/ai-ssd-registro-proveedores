package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.AcceptRegistrationNoticeCommand;
import mx.com.fredkorlabs.registration.application.port.in.AcceptRegistrationNoticeResult;
import mx.com.fredkorlabs.registration.application.port.out.NoticeAcceptancePersistencePort;
import mx.com.fredkorlabs.registration.application.port.out.NoticeAcceptanceReference;
import mx.com.fredkorlabs.registration.domain.exception.InvalidNoticeVersionException;
import mx.com.fredkorlabs.registration.domain.model.NoticeAcceptance;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AcceptRegistrationNoticeServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-20T15:30:00Z");

    private final RecordingPersistence persistence = new RecordingPersistence();
    private final AcceptRegistrationNoticeService service =
        new AcceptRegistrationNoticeService(persistence, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void US03_CA003_accept_persistsVersionAndBackendTime() {
        service.execute(new AcceptRegistrationNoticeCommand("1.0"));

        assertEquals(List.of(new NoticeAcceptance("1.0", NOW)), persistence.saved);
    }

    @Test
    void US03_CA003_accept_returnsPersistedReferenceVersionAndTime() {
        AcceptRegistrationNoticeResult result = service.execute(new AcceptRegistrationNoticeCommand("1.0"));

        assertEquals("ref-1", result.acceptanceId());
        assertEquals("1.0", result.version());
        assertEquals(NOW, result.acceptedAt());
    }

    @Test
    void US03_CA003_accept_usesInstantIndependentOfClockZone() {
        AcceptRegistrationNoticeService mexicoCityClockService = new AcceptRegistrationNoticeService(
            persistence, Clock.fixed(NOW, ZoneId.of("America/Mexico_City")));

        AcceptRegistrationNoticeResult result = mexicoCityClockService.execute(new AcceptRegistrationNoticeCommand("1.0"));

        assertEquals(NOW, result.acceptedAt());
    }

    @Test
    void US03_CA003_accept_keepsBackendTimeAtPersistedMicrosecondPrecision() {
        Instant withNanos = Instant.parse("2026-10-20T15:30:00.123456789Z");
        AcceptRegistrationNoticeService nanosClockService =
            new AcceptRegistrationNoticeService(persistence, Clock.fixed(withNanos, ZoneOffset.UTC));

        AcceptRegistrationNoticeResult result = nanosClockService.execute(new AcceptRegistrationNoticeCommand("1.0"));

        assertEquals(Instant.parse("2026-10-20T15:30:00.123456Z"), result.acceptedAt());
        assertEquals(result.acceptedAt(), persistence.saved.getFirst().acceptedAt());
    }

    @Test
    void US03_CA004_persistenceFailure_propagatesWithoutAcceptance() {
        NoticeAcceptancePersistencePort failing = acceptance -> {
            throw new IllegalStateException("Database unavailable");
        };
        AcceptRegistrationNoticeService failingService =
            new AcceptRegistrationNoticeService(failing, Clock.fixed(NOW, ZoneOffset.UTC));

        assertThrows(IllegalStateException.class,
            () -> failingService.execute(new AcceptRegistrationNoticeCommand("1.0")));
    }

    @Test
    void US03_CA007_eachAcceptance_isPersistedAsNewAcceptance() {
        AcceptRegistrationNoticeResult first = service.execute(new AcceptRegistrationNoticeCommand("1.0"));
        AcceptRegistrationNoticeResult second = service.execute(new AcceptRegistrationNoticeCommand("1.0"));

        assertEquals(2, persistence.saved.size());
        assertNotEquals(first.acceptanceId(), second.acceptanceId());
    }

    @Test
    void US03_CA007_acceptOtherVersion_isRejectedWithoutPersisting() {
        assertThrows(InvalidNoticeVersionException.class,
            () -> service.execute(new AcceptRegistrationNoticeCommand("2.0")));
        assertTrue(persistence.saved.isEmpty());
    }

    private static final class RecordingPersistence implements NoticeAcceptancePersistencePort {
        private final List<NoticeAcceptance> saved = new ArrayList<>();

        @Override
        public NoticeAcceptanceReference save(NoticeAcceptance acceptance) {
            saved.add(acceptance);
            return new NoticeAcceptanceReference("ref-" + saved.size());
        }
    }
}
