package mx.com.fredkorlabs.registration.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SupplierRegistrationJpaRepository extends JpaRepository<SupplierRegistrationJpaEntity, UUID> {

    Optional<SupplierRegistrationJpaEntity> findByRfc(String rfc);
}
