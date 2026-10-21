package mx.com.fredkorlabs.registration.domain.model;

import mx.com.fredkorlabs.registration.domain.exception.InvalidGeneralDataException;

import java.util.List;

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
    public GeneralData {
        if (origin == null) {
            throw new InvalidGeneralDataException("Origin is required");
        }
        if (nationality == null) {
            throw new InvalidGeneralDataException("Nationality is required");
        }
        if (origin == Origin.FOREIGN && foreignFiscalCode == null) {
            throw new InvalidGeneralDataException("Foreign fiscal code is required for foreign origin");
        }
        if (origin == Origin.NATIONAL && foreignFiscalCode != null) {
            throw new InvalidGeneralDataException("Foreign fiscal code is not applicable for national origin");
        }
        phones = phones != null ? List.copyOf(phones) : List.of();
    }
}
