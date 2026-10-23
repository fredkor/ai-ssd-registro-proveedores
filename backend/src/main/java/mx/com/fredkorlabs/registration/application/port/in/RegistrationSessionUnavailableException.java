package mx.com.fredkorlabs.registration.application.port.in;

/** The token's context is no longer available: REGISTRATION_TOKEN_INVALID (ADR-0015). */
public class RegistrationSessionUnavailableException extends RuntimeException {
    public RegistrationSessionUnavailableException(String message) {
        super(message);
    }
}
