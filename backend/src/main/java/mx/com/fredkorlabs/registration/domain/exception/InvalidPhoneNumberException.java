package mx.com.fredkorlabs.registration.domain.exception;

public class InvalidPhoneNumberException extends RegistrationDomainException {
    public InvalidPhoneNumberException(String message) {
        super(message);
    }
}
