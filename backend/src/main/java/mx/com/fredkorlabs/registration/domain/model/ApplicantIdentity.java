package mx.com.fredkorlabs.registration.domain.model;

import java.util.Objects;

public record ApplicantIdentity(Rfc rfc, String legalName) {
    public ApplicantIdentity {
        Objects.requireNonNull(rfc, "RFC is required");
        Objects.requireNonNull(legalName, "Legal name is required");
    }

    public TaxRegime taxRegime() {
        return rfc.value().length() == 12 ? TaxRegime.LEGAL_ENTITY : TaxRegime.INDIVIDUAL;
    }

    public Username username() {
        return new Username(rfc.value().toLowerCase());
    }
}
