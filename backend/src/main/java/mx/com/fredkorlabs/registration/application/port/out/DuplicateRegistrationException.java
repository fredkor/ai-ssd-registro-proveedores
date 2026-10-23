package mx.com.fredkorlabs.registration.application.port.out;

/** The store already holds a registration for the RFC, its acceptance or its initial evidence. */
public class DuplicateRegistrationException extends RuntimeException {
    public DuplicateRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
