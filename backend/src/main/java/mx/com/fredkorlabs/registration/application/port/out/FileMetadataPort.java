package mx.com.fredkorlabs.registration.application.port.out;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** General file metadata (ADR-0010); each section decides formats, quantity and associations. */
public interface FileMetadataPort {

    void register(StoredFileMetadata metadata);

    Optional<StoredFileMetadata> find(UUID fileId);

    /** Logical deletion: keeps the metadata row and its location for the daily cleanup (ADR-0010). */
    void markNotAssociated(UUID fileId);

    /** Locks the metadata not associated and uploaded before {@code instant} for the daily cleanup (ADR-0010). */
    List<StoredFileMetadata> lockNotAssociatedUploadedBefore(Instant instant);

    /** Whether a business record still references the file; each section with documents adds its references. */
    boolean isReferenced(UUID fileId);

    /** Removes the metadata of a file whose content the daily cleanup already deleted (ADR-0010). */
    void delete(UUID fileId);
}
