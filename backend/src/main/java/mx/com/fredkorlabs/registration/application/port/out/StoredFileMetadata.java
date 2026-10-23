package mx.com.fredkorlabs.registration.application.port.out;

import mx.com.fredkorlabs.registration.domain.model.SupplierRegistrationId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Metadata of a stored file (ADR-0010); {@code relativePath} is relative to the volume root. */
public record StoredFileMetadata(
    UUID id,
    SupplierRegistrationId owner,
    String originalName,
    String relativePath,
    String extension,
    String mediaType,
    long sizeBytes,
    Instant uploadedAt,
    boolean associated
) {
    public StoredFileMetadata {
        Objects.requireNonNull(id, "File id is required");
        Objects.requireNonNull(owner, "Owner registration is required");
        Objects.requireNonNull(originalName, "Original name is required");
        Objects.requireNonNull(relativePath, "Relative path is required");
        Objects.requireNonNull(uploadedAt, "Upload instant is required");
    }
}
