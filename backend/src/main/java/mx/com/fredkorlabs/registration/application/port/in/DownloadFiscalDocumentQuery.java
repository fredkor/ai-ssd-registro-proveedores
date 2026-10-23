package mx.com.fredkorlabs.registration.application.port.in;

import java.util.UUID;

/** {@code registrationSessionId} is the jti of the validated registration token (ADR-0015). */
public record DownloadFiscalDocumentQuery(UUID registrationId, String registrationSessionId) {}
