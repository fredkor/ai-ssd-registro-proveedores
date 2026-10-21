package mx.com.fredkorlabs.registration.application.port.out;

public interface ApplicantSignatureVerificationPort {

    /**
     * Reads the certificate and verifies the seal over the UTF-8 bytes of the signed content (ADR-0016).
     * Technical failures that prevent verifying propagate as exceptions.
     */
    SignatureCheck verify(byte[] certificate, String signedContent, byte[] signature);
}
