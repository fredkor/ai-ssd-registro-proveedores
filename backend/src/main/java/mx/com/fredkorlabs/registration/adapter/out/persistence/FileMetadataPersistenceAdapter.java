package mx.com.fredkorlabs.registration.adapter.out.persistence;

import mx.com.fredkorlabs.registration.application.port.out.FileMetadataPort;
import mx.com.fredkorlabs.registration.application.port.out.StoredFileMetadata;
import mx.com.fredkorlabs.registration.domain.model.SupplierRegistrationId;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class FileMetadataPersistenceAdapter implements FileMetadataPort {

    private final FileMetadataJpaRepository files;
    private final RegistrationFiscalDocumentJpaRepository fiscalDocuments;

    public FileMetadataPersistenceAdapter(FileMetadataJpaRepository files,
                                          RegistrationFiscalDocumentJpaRepository fiscalDocuments) {
        this.files = files;
        this.fiscalDocuments = fiscalDocuments;
    }

    @Override
    public void register(StoredFileMetadata metadata) {
        files.saveAndFlush(new FileMetadataJpaEntity(metadata.id(), metadata.owner().value(), metadata.originalName(),
            metadata.relativePath(), metadata.extension(), metadata.mediaType(), metadata.sizeBytes(),
            metadata.uploadedAt(), metadata.associated()));
    }

    @Override
    public void markNotAssociated(UUID fileId) {
        files.findById(fileId).orElseThrow().markNotAssociated();
        files.flush();
    }

    @Override
    public Optional<StoredFileMetadata> find(UUID fileId) {
        return files.findById(fileId).map(FileMetadataPersistenceAdapter::toMetadata);
    }

    @Override
    public List<StoredFileMetadata> lockNotAssociatedUploadedBefore(Instant instant) {
        return files.findByAssociatedFalseAndUploadedAtBeforeOrderByUploadedAtAsc(instant).stream()
            .map(FileMetadataPersistenceAdapter::toMetadata)
            .toList();
    }

    @Override
    public boolean isReferenced(UUID fileId) {
        // The fiscal document is today the only section with files; future ones add their references here.
        return fiscalDocuments.existsByFileId(fileId);
    }

    @Override
    public void delete(UUID fileId) {
        files.deleteById(fileId);
        files.flush();
    }

    private static StoredFileMetadata toMetadata(FileMetadataJpaEntity file) {
        return new StoredFileMetadata(file.getId(), new SupplierRegistrationId(file.getOwnerRegistrationId()),
            file.getOriginalName(), file.getRelativePath(), file.getExtension(), file.getMediaType(),
            file.getSizeBytes(), file.getUploadedAt(), file.isAssociated());
    }
}
