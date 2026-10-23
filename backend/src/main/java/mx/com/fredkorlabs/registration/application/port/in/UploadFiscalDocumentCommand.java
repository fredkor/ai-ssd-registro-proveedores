package mx.com.fredkorlabs.registration.application.port.in;

import java.util.UUID;

/** {@code registrationSessionId} is the jti of the validated token; {@code content} is the whole file. */
public record UploadFiscalDocumentCommand(UUID registrationId, String registrationSessionId, String originalName,
                                          byte[] content) {}
