package mx.com.fredkorlabs.registration.application.port.in;

import java.time.Instant;

public record AcceptRegistrationNoticeResult(String acceptanceId, String version, Instant acceptedAt) {}
