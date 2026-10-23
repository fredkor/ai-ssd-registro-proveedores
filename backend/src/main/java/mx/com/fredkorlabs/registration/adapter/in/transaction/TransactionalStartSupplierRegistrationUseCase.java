package mx.com.fredkorlabs.registration.adapter.in.transaction;

import mx.com.fredkorlabs.registration.application.port.in.StartSupplierRegistrationCommand;
import mx.com.fredkorlabs.registration.application.port.in.StartSupplierRegistrationUseCase;
import mx.com.fredkorlabs.registration.application.port.in.SupplierRegistrationResult;
import org.springframework.transaction.annotation.Transactional;

public class TransactionalStartSupplierRegistrationUseCase implements StartSupplierRegistrationUseCase {

    private final StartSupplierRegistrationUseCase delegate;

    public TransactionalStartSupplierRegistrationUseCase(StartSupplierRegistrationUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional
    public SupplierRegistrationResult execute(StartSupplierRegistrationCommand command) {
        return delegate.execute(command);
    }
}
