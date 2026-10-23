package mx.com.fredkorlabs.registration.adapter.out.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;
import java.util.UUID;

public interface SupplierRegistrationJpaRepository extends JpaRepository<SupplierRegistrationJpaEntity, UUID> {

    Optional<SupplierRegistrationJpaEntity> findByRfc(String rfc);

    // SELECT ... FOR UPDATE on the registration row (registration-database-design §7.2).
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<SupplierRegistrationJpaEntity> findLockedById(UUID id);
}
