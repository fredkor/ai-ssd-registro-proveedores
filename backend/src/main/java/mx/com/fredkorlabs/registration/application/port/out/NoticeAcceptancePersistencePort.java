package mx.com.fredkorlabs.registration.application.port.out;

import mx.com.fredkorlabs.registration.domain.model.NoticeAcceptance;

public interface NoticeAcceptancePersistencePort {
    NoticeAcceptanceReference save(NoticeAcceptance acceptance);

    /** True when the reference exists and no issued token has consumed it yet (ADR-0015). */
    boolean isAvailableForIdentification(NoticeAcceptanceReference reference);
}
