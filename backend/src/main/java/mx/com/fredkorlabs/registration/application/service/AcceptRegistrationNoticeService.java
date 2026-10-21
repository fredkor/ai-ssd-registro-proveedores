package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.AcceptRegistrationNoticeCommand;
import mx.com.fredkorlabs.registration.application.port.in.AcceptRegistrationNoticeResult;
import mx.com.fredkorlabs.registration.application.port.in.AcceptRegistrationNoticeUseCase;
import mx.com.fredkorlabs.registration.application.port.out.NoticeAcceptancePersistencePort;
import mx.com.fredkorlabs.registration.application.port.out.NoticeAcceptanceReference;
import mx.com.fredkorlabs.registration.domain.model.NoticeAcceptance;

import java.time.Clock;
import java.time.temporal.ChronoUnit;

public class AcceptRegistrationNoticeService implements AcceptRegistrationNoticeUseCase {

    private final NoticeAcceptancePersistencePort noticeAcceptancePersistence;
    private final Clock clock;

    public AcceptRegistrationNoticeService(NoticeAcceptancePersistencePort noticeAcceptancePersistence, Clock clock) {
        this.noticeAcceptancePersistence = noticeAcceptancePersistence;
        this.clock = clock;
    }

    @Override
    public AcceptRegistrationNoticeResult execute(AcceptRegistrationNoticeCommand command) {
        // Microseconds: the precision persisted for accepted_at (registration-database-design §7.1),
        // so the returned instant matches the stored one.
        NoticeAcceptance acceptance = new NoticeAcceptance(command.version(), clock.instant().truncatedTo(ChronoUnit.MICROS));
        NoticeAcceptanceReference reference = noticeAcceptancePersistence.save(acceptance);
        return new AcceptRegistrationNoticeResult(reference.value(), acceptance.version(), acceptance.acceptedAt());
    }
}
