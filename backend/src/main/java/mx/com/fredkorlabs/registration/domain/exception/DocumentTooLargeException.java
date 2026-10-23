package mx.com.fredkorlabs.registration.domain.exception;

/** The document exceeds the maximum size allowed for its type (INV-27). */
public class DocumentTooLargeException extends RegistrationDomainException {
    public DocumentTooLargeException(String message) {
        super(message);
    }
}
