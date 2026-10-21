package mx.com.fredkorlabs.registration.application.port.in;

/** The seal does not verify against the signed content and certificate: SIGNATURE_VERIFICATION_FAILED (ADR-0004). */
public class SignatureVerificationFailedException extends RuntimeException {
    public SignatureVerificationFailedException(String message) {
        super(message);
    }
}
