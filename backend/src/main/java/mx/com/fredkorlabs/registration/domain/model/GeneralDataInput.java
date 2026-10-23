package mx.com.fredkorlabs.registration.domain.model;

import java.util.List;

/**
 * Submitted General Data, already trimmed (ADR-0009) but not yet validated. {@code phones} is null when the
 * submission sends null (individuals) and empty when it sends an empty collection.
 */
public record GeneralDataInput(
    Origin origin,
    Integer countryId,
    String nationality,
    String foreignFiscalCode,
    Integer legalOwnershipId,
    Integer sectorId,
    Integer subsectorId,
    Integer businessActivityId,
    Integer stratificationId,
    Integer primaryActivityId,
    Integer workerCountRangeId,
    Integer annualSalesRangeId,
    String website,
    List<PhoneInput> phones
) {

    public record PhoneInput(String number, String extension) {}
}
