package mx.com.fredkorlabs.registration.adapter.out.persistence;

import mx.com.fredkorlabs.registration.application.port.out.FileMetadataPort;
import mx.com.fredkorlabs.registration.application.port.out.StoredFileMetadata;
import mx.com.fredkorlabs.registration.domain.model.SupplierRegistrationId;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

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

    @Override
    public Optional<StoredFileMetadata> find(UUID fileId) {
        return files.findById(fileId).map(file -> new StoredFileMetadata(file.getId(),
            new SupplierRegistrationId(file.getOwnerRegistrationId()), file.getOriginalName(), file.getRelativePath(),
            file.getExtension(), file.getMediaType(), file.getSizeBytes(), file.getUploadedAt(), file.isAssociated()));
    }
}
