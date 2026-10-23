package mx.com.fredkorlabs.registration.application.port.in;

/** The requested registration does not exist: REGISTRATION_NOT_FOUND (ADR-0004). */
public class RegistrationNotFoundException extends RuntimeException {
    public RegistrationNotFoundException(String message) {
        super(message);
    }
}
