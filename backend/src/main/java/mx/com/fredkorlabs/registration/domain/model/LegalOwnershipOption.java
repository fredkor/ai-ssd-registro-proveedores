package mx.com.fredkorlabs.registration.domain.model;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Legal ownership catalog entry (domain/catalogs.md §4). {@code countryCode} and {@code regime} are null
 * for general options such as NOT_DEF.
 */
public record LegalOwnershipOption(int id, String key, String description, String countryCode, TaxRegime regime) {

    public static final String NOT_DEFINED_KEY = "NOT_DEF";

    public LegalOwnershipOption {
        Objects.requireNonNull(key, "Key is required");
        Objects.requireNonNull(description, "Description is required");
    }

    public boolean isNotDefined() {
        return NOT_DEFINED_KEY.equals(key);
    }

    /**
     * INV-20: NATIONAL matches country and regime; FOREIGN matches the country or NOT_DEF, without a
     * regime filter, even when the country has its own options.
     */
    public boolean appliesTo(Origin origin, String selectedCountryCode, TaxRegime selectedRegime) {
        return switch (origin) {
            case NATIONAL -> selectedCountryCode.equals(countryCode) && selectedRegime == regime;
            case FOREIGN -> selectedCountryCode.equals(countryCode) || isNotDefined();
        };
    }

    /** The applicable options among {@code candidates}, in ascending key order. */
    public static List<LegalOwnershipOption> applicable(List<LegalOwnershipOption> candidates, Origin origin,
                                                        String selectedCountryCode, TaxRegime selectedRegime) {
        return candidates.stream()
            .filter(option -> option.appliesTo(origin, selectedCountryCode, selectedRegime))
            .sorted(Comparator.comparing(LegalOwnershipOption::key))
            .toList();
    }
}
