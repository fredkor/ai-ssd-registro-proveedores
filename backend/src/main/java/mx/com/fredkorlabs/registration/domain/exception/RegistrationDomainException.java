package mx.com.fredkorlabs.registration.domain.exception;

public abstract class RegistrationDomainException extends RuntimeException {
    protected RegistrationDomainException(String message) {
        super(message);
    }
}
