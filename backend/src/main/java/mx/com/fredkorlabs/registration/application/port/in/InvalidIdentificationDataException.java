package mx.com.fredkorlabs.registration.application.port.in;

/** Identification input that cannot be accepted: VALIDATION_ERROR (ADR-0004). */
public class InvalidIdentificationDataException extends RuntimeException {
    public InvalidIdentificationDataException(String message) {
        super(message);
    }
}
