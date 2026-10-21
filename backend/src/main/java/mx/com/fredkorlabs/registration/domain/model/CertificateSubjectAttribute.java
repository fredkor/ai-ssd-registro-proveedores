package mx.com.fredkorlabs.registration.domain.model;

import java.util.Objects;

public record CertificateSubjectAttribute(String oid, String value) {
    public CertificateSubjectAttribute {
        Objects.requireNonNull(oid, "Attribute OID is required");
        Objects.requireNonNull(value, "Attribute value is required");
    }
}
