package mx.com.fredkorlabs.registration.adapter.out.persistence;

import mx.com.fredkorlabs.registration.application.port.out.NoticeAcceptancePersistencePort;
import mx.com.fredkorlabs.registration.application.port.out.NoticeAcceptanceReference;
import mx.com.fredkorlabs.registration.domain.model.NoticeAcceptance;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class NoticeAcceptancePersistenceAdapter implements NoticeAcceptancePersistencePort {

    private final NoticeAcceptanceJpaRepository repository;
    private final RegistrationContextJpaRepository contexts;

    public NoticeAcceptancePersistenceAdapter(NoticeAcceptanceJpaRepository repository,
                                              RegistrationContextJpaRepository contexts) {
        this.repository = repository;
        this.contexts = contexts;
    }

    @Override
    public NoticeAcceptanceReference save(NoticeAcceptance acceptance) {
        NoticeAcceptanceJpaEntity saved = repository.saveAndFlush(
            new NoticeAcceptanceJpaEntity(acceptance.version(), acceptance.acceptedAt()));
        return new NoticeAcceptanceReference(saved.getId().toString());
    }

    @Override
    public boolean isAvailableForIdentification(NoticeAcceptanceReference reference) {
        UUID id;
        try {
            id = UUID.fromString(reference.value());
        } catch (IllegalArgumentException e) {
            return false;
        }
        return repository.existsById(id) && !contexts.existsByNoticeAcceptanceId(id);
    }
}
