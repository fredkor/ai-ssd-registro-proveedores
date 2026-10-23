package mx.com.fredkorlabs.registration.domain.exception;

/** The document extension or content is not an accepted format (INV-27). */
public class DocumentFormatNotSupportedException extends RegistrationDomainException {
    public DocumentFormatNotSupportedException(String message) {
        super(message);
    }
}
