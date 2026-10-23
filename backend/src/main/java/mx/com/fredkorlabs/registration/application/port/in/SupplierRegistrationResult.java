package mx.com.fredkorlabs.registration.application.port.in;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Projection of a registration: identity, latest notice acceptance, progress in section order and the
 * associated fiscal document ({@code null} when none is associated).
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
    FiscalDocumentItem fiscalDocument
) {

    public record SectionProgressItem(String section, String status) {}

    public record FiscalDocumentItem(UUID id, String originalName, String extension, long sizeBytes,
                                     Instant uploadedAt) {}
}
