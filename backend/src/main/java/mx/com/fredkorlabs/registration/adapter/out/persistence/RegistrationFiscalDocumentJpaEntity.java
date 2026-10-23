package mx.com.fredkorlabs.registration.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.springframework.data.domain.Persistable;

import java.util.UUID;

/** Active fiscal document reference: at most one per registration and one registration per file (design §4). */
@Entity
@Table(name = "registration_fiscal_document")
public class RegistrationFiscalDocumentJpaEntity implements Persistable<UUID> {

    @Id
    @Column(name = "registration_id", nullable = false, updatable = false)
    private UUID registrationId;

    @Column(name = "file_id", nullable = false, updatable = false)
    private UUID fileId;

    @Transient
    private boolean persisted;

    protected RegistrationFiscalDocumentJpaEntity() {
    }

    public RegistrationFiscalDocumentJpaEntity(UUID registrationId, UUID fileId) {
        this.registrationId = registrationId;
        this.fileId = fileId;
    }

    @Override
    public UUID getId() { return registrationId; }

    @Override
    public boolean isNew() { return !persisted; }

    @PostLoad
    @PostPersist
    void markPersisted() { persisted = true; }

    public UUID getFileId() { return fileId; }
}
