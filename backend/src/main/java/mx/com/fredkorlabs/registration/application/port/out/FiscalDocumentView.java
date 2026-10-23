package mx.com.fredkorlabs.registration.application.port.out;

import java.time.Instant;
import java.util.UUID;

/** Metadata of the fiscal document currently associated with a registration, for its projection. */
public record FiscalDocumentView(UUID id, String originalName, String extension, long sizeBytes, Instant uploadedAt) {}
