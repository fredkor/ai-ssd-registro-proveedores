package mx.com.fredkorlabs.registration.adapter.out.persistence;

import mx.com.fredkorlabs.registration.application.port.out.NoticeAcceptancePersistencePort;
import mx.com.fredkorlabs.registration.application.port.out.NoticeAcceptanceReference;
import mx.com.fredkorlabs.registration.domain.model.NoticeAcceptance;
import org.springframework.stereotype.Component;

@Component
public class NoticeAcceptancePersistenceAdapter implements NoticeAcceptancePersistencePort {

    private final NoticeAcceptanceJpaRepository repository;

    public NoticeAcceptancePersistenceAdapter(NoticeAcceptanceJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public NoticeAcceptanceReference save(NoticeAcceptance acceptance) {
        NoticeAcceptanceJpaEntity saved = repository.saveAndFlush(
            new NoticeAcceptanceJpaEntity(acceptance.version(), acceptance.acceptedAt()));
        return new NoticeAcceptanceReference(saved.getId().toString());
    }
}
