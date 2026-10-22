package mx.com.fredkorlabs.registration.adapter.out.persistence;

import mx.com.fredkorlabs.registration.application.port.out.RegistrationStatusView;
import mx.com.fredkorlabs.registration.application.port.out.SupplierRegistrationPersistencePort;
import mx.com.fredkorlabs.registration.domain.model.Rfc;
import mx.com.fredkorlabs.registration.domain.model.SupplierRegistrationId;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SupplierRegistrationPersistenceAdapter implements SupplierRegistrationPersistencePort {

    private final SupplierRegistrationJpaRepository repository;

    public SupplierRegistrationPersistenceAdapter(SupplierRegistrationJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<RegistrationStatusView> findStatusByRfc(Rfc rfc) {
        return repository.findByRfc(rfc.value())
            .map(entity -> new RegistrationStatusView(new SupplierRegistrationId(entity.getId()), entity.getStatus()));
    }
}
