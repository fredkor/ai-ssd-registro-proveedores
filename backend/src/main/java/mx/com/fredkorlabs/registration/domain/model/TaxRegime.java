package mx.com.fredkorlabs.registration.domain.model;

public enum TaxRegime {
    LEGAL_ENTITY(1),
    INDIVIDUAL(2);

    private final int catalogId;

    TaxRegime(int catalogId) {
        this.catalogId = catalogId;
    }

    /** ID of the regime in the tax_regime catalog (catalogs.md): 1 PERSONA MORAL, 2 PERSONA FÍSICA. */
    public int catalogId() {
        return catalogId;
    }
}
