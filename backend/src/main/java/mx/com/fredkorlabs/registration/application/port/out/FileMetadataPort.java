package mx.com.fredkorlabs.registration.application.port.out;

import java.util.Optional;
import java.util.UUID;

/** General file metadata (ADR-0010); each section decides formats, quantity and associations. */
public interface FileMetadataPort {

    void register(StoredFileMetadata metadata);

    Optional<StoredFileMetadata> find(UUID fileId);
}
