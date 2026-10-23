package mx.com.fredkorlabs.registration.domain.exception;

/** General Data cannot be completed without an associated fiscal document (INV-27). */
public class FiscalDocumentRequiredException extends RegistrationDomainException {
    public FiscalDocumentRequiredException(String message) {
        super(message);
    }
}
