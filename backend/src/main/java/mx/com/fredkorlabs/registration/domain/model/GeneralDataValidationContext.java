package mx.com.fredkorlabs.registration.domain.model;

public record GeneralDataValidationContext(
    boolean countryExists,
    boolean legalOwnershipCompatible,
    boolean subsectorBelongsToSector,
    boolean businessActivityExists,
    boolean stratificationExists,
    boolean primaryActivityExists,
    boolean workerCountRangeExists,
    boolean annualSalesRangeExists
) {}
