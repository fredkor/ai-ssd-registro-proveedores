package mx.com.fredkorlabs.registration.domain.exception;

/** A fiscal document is already associated with the registration (INV-27). */
public class FiscalDocumentAlreadyExistsException extends RegistrationDomainException {
    public FiscalDocumentAlreadyExistsException(String message) {
        super(message);
    }
}
