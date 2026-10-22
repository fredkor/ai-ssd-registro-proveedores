package mx.com.fredkorlabs.registration.application.port.in;

public interface CheckRegistrationExistenceUseCase {
    CheckRegistrationExistenceResult execute(CheckRegistrationExistenceQuery query);
}
