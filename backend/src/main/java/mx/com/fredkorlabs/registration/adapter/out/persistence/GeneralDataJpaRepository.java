package mx.com.fredkorlabs.registration.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GeneralDataJpaRepository extends JpaRepository<GeneralDataJpaEntity, UUID> {
}
