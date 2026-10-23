package mx.com.fredkorlabs.registration.application.port.in;

/** No fiscal document is associated with the registration: FISCAL_DOCUMENT_NOT_FOUND (ADR-0004). */
public class FiscalDocumentNotFoundException extends RuntimeException {
    public FiscalDocumentNotFoundException(String message) {
        super(message);
    }
}
