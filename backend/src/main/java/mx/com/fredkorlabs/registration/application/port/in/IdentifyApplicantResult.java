package mx.com.fredkorlabs.registration.application.port.in;

import java.time.Instant;

public record IdentifyApplicantResult(
    String rfc,
    String legalName,
    String taxRegime,
    int taxRegimeId,
    String username,
    String registrationToken,
    Instant tokenExpiresAt
) {}
