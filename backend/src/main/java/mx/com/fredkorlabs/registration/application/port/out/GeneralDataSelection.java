package mx.com.fredkorlabs.registration.application.port.out;

/** Catalog ids of a General Data submission; any of them may be null when the submission omits the value. */
public record GeneralDataSelection(
    Integer countryId,
    Integer legalOwnershipId,
    Integer sectorId,
    Integer subsectorId,
    Integer businessActivityId,
    Integer stratificationId,
    Integer primaryActivityId,
    Integer workerCountRangeId,
    Integer annualSalesRangeId
) {}
