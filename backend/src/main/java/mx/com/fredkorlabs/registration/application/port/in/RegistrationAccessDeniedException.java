package mx.com.fredkorlabs.registration.application.port.in;

/** The token's RFC does not own the requested registration: REGISTRATION_ACCESS_DENIED (ADR-0004, ADR-0015). */
public class RegistrationAccessDeniedException extends RuntimeException {
    public RegistrationAccessDeniedException(String message) {
        super(message);
    }
}
