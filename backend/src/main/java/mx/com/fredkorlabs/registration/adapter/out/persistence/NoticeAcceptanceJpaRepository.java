package mx.com.fredkorlabs.registration.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NoticeAcceptanceJpaRepository extends JpaRepository<NoticeAcceptanceJpaEntity, UUID> {
}
