package mx.com.fredkorlabs.registration.adapter.out.persistence;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.MapKeyEnumerated;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import mx.com.fredkorlabs.registration.domain.model.RegistrationSection;
import mx.com.fredkorlabs.registration.domain.model.RegistrationStatus;
import mx.com.fredkorlabs.registration.domain.model.SectionStatus;
import org.springframework.data.domain.Persistable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Registration row with its section progress and signature evidence history (design §1, §5). */
@Entity
@Table(name = "supplier_registration")
public class SupplierRegistrationJpaEntity implements Persistable<UUID> {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "rfc", nullable = false, updatable = false, length = 13)
    private String rfc;

    @Column(name = "legal_name", nullable = false, columnDefinition = "text")
    private String legalName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private RegistrationStatus status;

    @Column(name = "notice_acceptance_id", nullable = false)
    private UUID noticeAcceptanceId;

    @Column(name = "initial_evidence_id", nullable = false, updatable = false)
    private UUID initialEvidenceId;

    @ElementCollection
    @CollectionTable(name = "section_progress", joinColumns = @JoinColumn(name = "registration_id"))
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "section", length = 32)
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private Map<RegistrationSection, SectionStatus> sectionProgress = new HashMap<>();

    // The initial evidence is also part of the history (design §5).
    @ElementCollection
    @CollectionTable(name = "registration_signature_evidence", joinColumns = @JoinColumn(name = "registration_id"))
    @Column(name = "signature_evidence_id", nullable = false)
    private Set<UUID> signatureEvidenceIds = new HashSet<>();

    @Transient
    private boolean persisted;

    protected SupplierRegistrationJpaEntity() {
    }

    public SupplierRegistrationJpaEntity(UUID id, String rfc, String legalName, RegistrationStatus status,
                                         UUID noticeAcceptanceId, UUID initialEvidenceId) {
        this.id = id;
        this.rfc = rfc;
        this.legalName = legalName;
        this.status = status;
        this.noticeAcceptanceId = noticeAcceptanceId;
        this.initialEvidenceId = initialEvidenceId;
        this.signatureEvidenceIds.add(initialEvidenceId);
    }

    @Override
    public UUID getId() { return id; }

    @Override
    public boolean isNew() { return !persisted; }

    @PostLoad
    @PostPersist
    void markPersisted() { persisted = true; }

    public String getRfc() { return rfc; }

    public String getLegalName() { return legalName; }

    public RegistrationStatus getStatus() { return status; }

    public UUID getNoticeAcceptanceId() { return noticeAcceptanceId; }

    public UUID getInitialEvidenceId() { return initialEvidenceId; }

    public Map<RegistrationSection, SectionStatus> getSectionProgress() { return sectionProgress; }

    public Set<UUID> getSignatureEvidenceIds() { return signatureEvidenceIds; }
}
