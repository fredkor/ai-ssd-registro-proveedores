package mx.com.fredkorlabs.registration.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.UUID;

/** Stored file metadata (design §4); the id is assigned by the application before writing the content. */
@Entity
@Table(name = "file_metadata")
public class FileMetadataJpaEntity implements Persistable<UUID> {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "owner_registration_id", nullable = false, updatable = false)
    private UUID ownerRegistrationId;

    @Column(name = "original_name", nullable = false, updatable = false, columnDefinition = "text")
    private String originalName;

    @Column(name = "relative_path", nullable = false, updatable = false, length = 255)
    private String relativePath;

    @Column(name = "extension", nullable = false, updatable = false, length = 10)
    private String extension;

    @Column(name = "media_type", nullable = false, updatable = false, columnDefinition = "text")
    private String mediaType;

    @Column(name = "size_bytes", nullable = false, updatable = false)
    private long sizeBytes;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private Instant uploadedAt;

    @Column(name = "associated", nullable = false)
    private boolean associated;

    @Transient
    private boolean persisted;

    protected FileMetadataJpaEntity() {
    }

    public FileMetadataJpaEntity(UUID id, UUID ownerRegistrationId, String originalName, String relativePath,
                                 String extension, String mediaType, long sizeBytes, Instant uploadedAt,
                                 boolean associated) {
        this.id = id;
        this.ownerRegistrationId = ownerRegistrationId;
        this.originalName = originalName;
        this.relativePath = relativePath;
        this.extension = extension;
        this.mediaType = mediaType;
        this.sizeBytes = sizeBytes;
        this.uploadedAt = uploadedAt;
        this.associated = associated;
    }

    @Override
    public UUID getId() { return id; }

    @Override
    public boolean isNew() { return !persisted; }

    @PostLoad
    @PostPersist
    void markPersisted() { persisted = true; }

    public UUID getOwnerRegistrationId() { return ownerRegistrationId; }

    public String getOriginalName() { return originalName; }

    public String getRelativePath() { return relativePath; }

    public String getExtension() { return extension; }

    public String getMediaType() { return mediaType; }

    public long getSizeBytes() { return sizeBytes; }

    public Instant getUploadedAt() { return uploadedAt; }

    public boolean isAssociated() { return associated; }

    public void markNotAssociated() { this.associated = false; }
}
