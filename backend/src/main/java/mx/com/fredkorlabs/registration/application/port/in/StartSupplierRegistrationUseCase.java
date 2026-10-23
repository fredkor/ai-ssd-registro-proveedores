package mx.com.fredkorlabs.registration.application.port.in;

public interface StartSupplierRegistrationUseCase {
    SupplierRegistrationResult execute(StartSupplierRegistrationCommand command);
}
