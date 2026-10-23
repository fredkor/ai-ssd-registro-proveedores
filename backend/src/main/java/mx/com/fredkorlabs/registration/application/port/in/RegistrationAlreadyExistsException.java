package mx.com.fredkorlabs.registration.application.port.in;

/** A registration already exists for the RFC, including a concurrent creation: DUPLICATE_RFC (ADR-0004). */
public class RegistrationAlreadyExistsException extends RuntimeException {
    public RegistrationAlreadyExistsException(String message) {
        super(message);
    }

    public RegistrationAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
