package mx.com.fredkorlabs.registration.application.port.in;

import java.util.List;
import java.util.UUID;

/**
 * The whole General Data submission, already trimmed (ADR-0009): optional and non-applicable values are null,
 * and {@code phones} is null when the submission sends null. {@code origin} is the contract code.
 */
public record SaveGeneralDataCommand(
    UUID registrationId,
    String registrationSessionId,
    String origin,
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
    List<Phone> phones
) {

    public record Phone(String number, String extension) {}
}
