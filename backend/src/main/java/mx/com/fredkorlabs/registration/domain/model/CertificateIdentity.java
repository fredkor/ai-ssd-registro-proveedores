package mx.com.fredkorlabs.registration.domain.model;

import mx.com.fredkorlabs.registration.domain.exception.ApplicantRfcMismatchException;
import mx.com.fredkorlabs.registration.domain.exception.InvalidCertificateIdentityException;

import java.util.List;
import java.util.Set;

/**
 * Holder RFC and legal name read from the subject of the certificate that verifies the seal (ADR-0016).
 */
public record CertificateIdentity(String holderRfc, String legalName) {

    static final String RFC_OID = "2.5.4.45";
    static final Set<String> NAME_OIDS = Set.of("2.5.4.3", "2.5.4.41", "2.5.4.10");

    public static CertificateIdentity from(List<CertificateSubjectAttribute> subject) {
        return new CertificateIdentity(holderRfc(subject), legalName(subject));
    }

    public ApplicantIdentity matching(String receivedRfc) {
        if (!holderRfc.equals(receivedRfc)) {
            throw new ApplicantRfcMismatchException("Received RFC does not match the certificate holder RFC");
        }
        return new ApplicantIdentity(new Rfc(holderRfc), legalName);
    }

    // Only index 0 of the "/"-separated value identifies the holder; later segments (e.g. the
    // legal representative RFC) are never alternatives. No trimming or case changes.
    private static String holderRfc(List<CertificateSubjectAttribute> subject) {
        CertificateSubjectAttribute attribute = subject.stream()
            .filter(candidate -> RFC_OID.equals(candidate.oid()))
            .findFirst()
            .orElseThrow(() -> new InvalidCertificateIdentityException("Certificate has no RFC attribute"));
        String holder = attribute.value().split("/", -1)[0];
        if (holder.isEmpty()) {
            throw new InvalidCertificateIdentityException("Certificate RFC attribute has an empty first segment");
        }
        if (holder.length() != 12 && holder.length() != 13) {
            throw new InvalidCertificateIdentityException("Certificate holder RFC must be 12 or 13 characters");
        }
        return holder;
    }

    // First matching attribute in certificate order; the OID set is a filter, not a priority.
    private static String legalName(List<CertificateSubjectAttribute> subject) {
        CertificateSubjectAttribute attribute = subject.stream()
            .filter(candidate -> NAME_OIDS.contains(candidate.oid()))
            .findFirst()
            .orElseThrow(() -> new InvalidCertificateIdentityException("Certificate has no legal name attribute"));
        if (attribute.value().isEmpty()) {
            throw new InvalidCertificateIdentityException("Certificate legal name attribute is empty");
        }
        return attribute.value();
    }
}
