package mx.com.fredkorlabs.registration.application.port.out;

import mx.com.fredkorlabs.registration.domain.model.Rfc;

import java.util.Optional;

public interface SupplierRegistrationPersistencePort {

    /** Empty only when no registration exists for the RFC; query failures propagate (INV-09). */
    Optional<RegistrationStatusView> findStatusByRfc(Rfc rfc);
}
