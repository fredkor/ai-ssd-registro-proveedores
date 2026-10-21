package mx.com.fredkorlabs.registration.application.port.out;

import mx.com.fredkorlabs.registration.domain.model.NoticeAcceptance;

public interface NoticeAcceptancePersistencePort {
    NoticeAcceptanceReference save(NoticeAcceptance acceptance);
}
