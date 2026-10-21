package mx.com.fredkorlabs.registration.application.port.out;

import mx.com.fredkorlabs.registration.domain.model.CertificateSubjectAttribute;

import java.util.List;

/** Outcome of reading a certificate and verifying a seal; subject attributes keep the certificate order. */
public record SignatureCheck(Status status, List<CertificateSubjectAttribute> subject) {

    public enum Status {
        VALID,
        INVALID,
        UNREADABLE_CERTIFICATE
    }

    public SignatureCheck {
        subject = List.copyOf(subject);
    }

    public static SignatureCheck unreadableCertificate() {
        return new SignatureCheck(Status.UNREADABLE_CERTIFICATE, List.of());
    }
}
