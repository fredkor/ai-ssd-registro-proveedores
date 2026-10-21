package mx.com.fredkorlabs.registration.domain.exception;

public class InvalidRegistrationStateException extends RegistrationDomainException {
    public InvalidRegistrationStateException(String message) {
        super(message);
    }
}
