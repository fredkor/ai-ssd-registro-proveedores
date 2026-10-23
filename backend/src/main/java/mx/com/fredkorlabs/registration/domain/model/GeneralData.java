package mx.com.fredkorlabs.registration.domain.model;

import mx.com.fredkorlabs.registration.domain.exception.InvalidGeneralDataException;
import mx.com.fredkorlabs.registration.domain.exception.RegistrationDomainException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Saved General Data (domain model §5.2). {@code foreignFiscalCode}, {@code sectorId}, {@code subsectorId} and
 * {@code website} may be null; {@code phones} is null for individuals.
 */
public record GeneralData(
    Origin origin,
    int countryId,
    Nationality nationality,
    ForeignFiscalCode foreignFiscalCode,
    int legalOwnershipId,
    Integer sectorId,
    Integer subsectorId,
    int businessActivityId,
    int stratificationId,
    int primaryActivityId,
    int workerCountRangeId,
    int annualSalesRangeId,
    Website website,
    List<PhoneNumber> phones
) {
    public static final String NATIONAL_COUNTRY_CODE = "MX";
    public static final String NATIONAL_NATIONALITY = "Mexicana";

    public GeneralData {
        Objects.requireNonNull(origin, "Origin is required");
        Objects.requireNonNull(nationality, "Nationality is required");
        phones = phones == null ? null : List.copyOf(phones);
    }

    /**
     * Validates a whole submission against the applicant's tax regime and the resolved catalogs and reports every
     * broken rule at once (INV-16 to INV-26). Reasons never repeat the submitted value.
     */
    public static GeneralData validate(GeneralDataInput input, TaxRegime regime, GeneralDataCatalogContext catalog) {
        List<GeneralDataViolation> violations = new ArrayList<>();
        Origin origin = input.origin();
        if (origin == null) violations.add(new GeneralDataViolation("origin", "is required"));

        // INV-17: country from the catalog; Mexico for NATIONAL.
        if (input.countryId() == null) {
            violations.add(new GeneralDataViolation("countryId", "is required"));
        } else if (catalog.countryCode() == null) {
            violations.add(new GeneralDataViolation("countryId", "is not in the country catalog"));
        } else if (origin == Origin.NATIONAL && !NATIONAL_COUNTRY_CODE.equals(catalog.countryCode())) {
            violations.add(new GeneralDataViolation("countryId", "must be Mexico for NATIONAL origin"));
        }

        // INV-18: Mexicana for NATIONAL; up to 250 characters for FOREIGN.
        Nationality nationality = valueOf(violations, "nationality", "is required", input.nationality(), Nationality::new,
            "must not exceed 250 characters");
        if (nationality != null && origin == Origin.NATIONAL && !NATIONAL_NATIONALITY.equals(nationality.value())) {
            violations.add(new GeneralDataViolation("nationality", "must be Mexicana for NATIONAL origin"));
        }

        // INV-19: foreign fiscal code only for FOREIGN, required there.
        ForeignFiscalCode foreignFiscalCode = null;
        if (origin == Origin.FOREIGN) {
            foreignFiscalCode = valueOf(violations, "foreignFiscalCode", "is required for FOREIGN origin",
                input.foreignFiscalCode(), ForeignFiscalCode::new, "must not exceed 20 characters");
        } else if (origin == Origin.NATIONAL && input.foreignFiscalCode() != null) {
            violations.add(new GeneralDataViolation("foreignFiscalCode", "must be null for NATIONAL origin"));
        }

        // INV-20: same rule as the legal ownership query, with the applicant's tax regime.
        if (input.legalOwnershipId() == null) {
            violations.add(new GeneralDataViolation("legalOwnershipId", "is required"));
        } else if (catalog.legalOwnership() == null) {
            violations.add(new GeneralDataViolation("legalOwnershipId", "is not in the legal ownership catalog"));
        } else if (origin != null && catalog.countryCode() != null
                && !catalog.legalOwnership().appliesTo(origin, catalog.countryCode(), regime)) {
            violations.add(new GeneralDataViolation("legalOwnershipId",
                "does not apply to the selected origin, country and tax regime"));
        }

        // INV-21 and INV-26: sector, subsector and phones only for legal entities.
        List<PhoneNumber> phones = null;
        if (regime == TaxRegime.LEGAL_ENTITY) {
            validateSector(violations, input, catalog);
            phones = phones(violations, input.phones());
        } else {
            requireAbsentForIndividuals(violations, "sectorId", input.sectorId());
            requireAbsentForIndividuals(violations, "subsectorId", input.subsectorId());
            requireAbsentForIndividuals(violations, "phones", input.phones());
        }

        // INV-22 and INV-23: the remaining selections must be current catalog entries.
        requireCatalogEntry(violations, "businessActivityId", input.businessActivityId(), catalog.businessActivityExists());
        requireCatalogEntry(violations, "stratificationId", input.stratificationId(), catalog.stratificationActive());
        requireCatalogEntry(violations, "primaryActivityId", input.primaryActivityId(), catalog.primaryActivityExists());
        requireCatalogEntry(violations, "workerCountRangeId", input.workerCountRangeId(), catalog.workerCountRangeExists());
        requireCatalogEntry(violations, "annualSalesRangeId", input.annualSalesRangeId(), catalog.annualSalesRangeExists());

        // INV-24: optional, absolute http or https URL with a host.
        Website website = input.website() == null ? null : valueOf(violations, "website", "is required",
            input.website(), Website::new, "must be an absolute http or https URL with a valid host of at most 2048 characters");

        if (!violations.isEmpty()) {
            throw new InvalidGeneralDataException(violations);
        }
        return new GeneralData(origin, input.countryId(), nationality, foreignFiscalCode, input.legalOwnershipId(),
            input.sectorId(), input.subsectorId(), input.businessActivityId(), input.stratificationId(),
            input.primaryActivityId(), input.workerCountRangeId(), input.annualSalesRangeId(), website, phones);
    }

    private static void validateSector(List<GeneralDataViolation> violations, GeneralDataInput input,
                                       GeneralDataCatalogContext catalog) {
        if (input.sectorId() == null) {
            violations.add(new GeneralDataViolation("sectorId", "is required for legal entities"));
        } else if (!catalog.sectorExists()) {
            violations.add(new GeneralDataViolation("sectorId", "is not in the sector catalog"));
        }
        if (input.subsectorId() == null) {
            violations.add(new GeneralDataViolation("subsectorId", "is required for legal entities"));
        } else if (catalog.subsectorSectorId() == null) {
            violations.add(new GeneralDataViolation("subsectorId", "is not in the subsector catalog"));
        } else if (input.sectorId() != null && !input.sectorId().equals(catalog.subsectorSectorId())) {
            violations.add(new GeneralDataViolation("subsectorId", "does not belong to the selected sector"));
        }
    }

    private static List<PhoneNumber> phones(List<GeneralDataViolation> violations,
                                            List<GeneralDataInput.PhoneInput> submitted) {
        if (submitted == null || submitted.isEmpty()) {
            violations.add(new GeneralDataViolation("phones", "must contain at least one phone for legal entities"));
            return List.of();
        }
        List<PhoneNumber> phones = new ArrayList<>();
        for (int i = 0; i < submitted.size(); i++) {
            GeneralDataInput.PhoneInput phone = submitted.get(i);
            int before = violations.size();
            if (phone == null || phone.number() == null) {
                violations.add(new GeneralDataViolation("phones/" + i + "/number", "is required"));
            } else if (!phone.number().matches("[0-9]{10}")) {
                violations.add(new GeneralDataViolation("phones/" + i + "/number", "must contain exactly 10 digits"));
            }
            if (phone != null && phone.extension() != null && phone.extension().length() > 5) {
                violations.add(new GeneralDataViolation("phones/" + i + "/extension", "must not exceed 5 characters"));
            }
            if (violations.size() == before) {
                phones.add(new PhoneNumber(phone.number(), phone.extension()));
            }
        }
        return phones;
    }

    private static void requireAbsentForIndividuals(List<GeneralDataViolation> violations, String field, Object value) {
        if (value != null) {
            violations.add(new GeneralDataViolation(field, "must be null for individuals"));
        }
    }

    private static void requireCatalogEntry(List<GeneralDataViolation> violations, String field, Integer id,
                                            boolean current) {
        if (id == null) {
            violations.add(new GeneralDataViolation(field, "is required"));
        } else if (!current) {
            violations.add(new GeneralDataViolation(field, "is not a current catalog entry"));
        }
    }

    private static <T> T valueOf(List<GeneralDataViolation> violations, String field, String missingReason,
                                 String value, Function<String, T> factory, String invalidReason) {
        if (value == null || value.isBlank()) {
            violations.add(new GeneralDataViolation(field, missingReason));
            return null;
        }
        try {
            return factory.apply(value);
        } catch (RegistrationDomainException e) {
            violations.add(new GeneralDataViolation(field, invalidReason));
            return null;
        }
    }
}
