package mx.com.fredkorlabs.registration.application.port.in;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Projection of a registration: identity, latest notice acceptance and progress, in section order. */
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
    List<SectionProgressItem> progress
) {

    public record SectionProgressItem(String section, String status) {}
}
