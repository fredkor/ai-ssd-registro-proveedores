package mx.com.fredkorlabs.registration.application.port.in;

import java.time.Instant;
import java.util.UUID;

/** The stored and associated fiscal document; {@code id} is the file's own UUID (ADR-0010). */
public record UploadFiscalDocumentResult(UUID id, String originalName, String extension, long sizeBytes,
                                         Instant uploadedAt) {}
