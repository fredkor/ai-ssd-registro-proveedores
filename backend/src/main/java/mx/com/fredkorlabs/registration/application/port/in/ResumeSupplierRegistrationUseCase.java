package mx.com.fredkorlabs.registration.application.port.in;

public interface ResumeSupplierRegistrationUseCase {
    SupplierRegistrationResult execute(ResumeSupplierRegistrationCommand command);
}
