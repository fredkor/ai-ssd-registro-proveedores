package mx.com.fredkorlabs.registration.application.port.in;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Projection of a registration: identity, latest notice acceptance, progress in section order and the
 * associated fiscal document and the saved General Data ({@code null} when absent).
 */
public record SupplierRegistrationResult(
    UUID id,
    String status,
    String rfc,
    String legalName,
    String taxRegime,
    String username,
    String noticeVersion,
    Instant noticeAcceptedAt,
    String noticeAcceptanceId,
    List<SectionProgressItem> progress,
    FiscalDocumentItem fiscalDocument,
    GeneralDataItem generalData
) {

    public record SectionProgressItem(String section, String status) {}

    public record FiscalDocumentItem(UUID id, String originalName, String extension, long sizeBytes,
                                     Instant uploadedAt) {}

    /** {@code phones} is null for individuals; optional values are null when absent. */
    public record GeneralDataItem(String origin, int countryId, String nationality, String foreignFiscalCode,
                                  int legalOwnershipId, Integer sectorId, Integer subsectorId, int businessActivityId,
                                  int stratificationId, int primaryActivityId, int workerCountRangeId,
                                  int annualSalesRangeId, String website, List<PhoneItem> phones) {}

    public record PhoneItem(String number, String extension) {}
}
