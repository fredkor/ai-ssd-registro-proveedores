package mx.com.fredkorlabs.registration.adapter.out.persistence;

import mx.com.fredkorlabs.registration.application.port.out.FileMetadataPort;
import mx.com.fredkorlabs.registration.application.port.out.StoredFileMetadata;
import org.springframework.stereotype.Component;

@Component
public class FileMetadataPersistenceAdapter implements FileMetadataPort {

    private final FileMetadataJpaRepository files;

    public FileMetadataPersistenceAdapter(FileMetadataJpaRepository files) {
        this.files = files;
    }

    @Override
    public void register(StoredFileMetadata metadata) {
        files.saveAndFlush(new FileMetadataJpaEntity(metadata.id(), metadata.owner().value(), metadata.originalName(),
            metadata.relativePath(), metadata.extension(), metadata.mediaType(), metadata.sizeBytes(),
            metadata.uploadedAt(), metadata.associated()));
    }
}
