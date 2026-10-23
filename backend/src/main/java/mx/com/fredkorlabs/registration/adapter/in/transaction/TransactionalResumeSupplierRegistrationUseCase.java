package mx.com.fredkorlabs.registration.adapter.in.transaction;

import mx.com.fredkorlabs.registration.application.port.in.ResumeSupplierRegistrationCommand;
import mx.com.fredkorlabs.registration.application.port.in.ResumeSupplierRegistrationUseCase;
import mx.com.fredkorlabs.registration.application.port.in.SupplierRegistrationResult;
import org.springframework.transaction.annotation.Transactional;

public class TransactionalResumeSupplierRegistrationUseCase implements ResumeSupplierRegistrationUseCase {

    private final ResumeSupplierRegistrationUseCase delegate;

    public TransactionalResumeSupplierRegistrationUseCase(ResumeSupplierRegistrationUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional
    public SupplierRegistrationResult execute(ResumeSupplierRegistrationCommand command) {
        return delegate.execute(command);
    }
}
