package mx.com.fredkorlabs.registration.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import mx.com.fredkorlabs.registration.domain.model.RegistrationStatus;

import java.util.UUID;

@Entity
@Table(name = "supplier_registration")
public class SupplierRegistrationJpaEntity {

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
    }

    public UUID getId() { return id; }

    public String getRfc() { return rfc; }

    public String getLegalName() { return legalName; }

    public RegistrationStatus getStatus() { return status; }

    public UUID getNoticeAcceptanceId() { return noticeAcceptanceId; }

    public UUID getInitialEvidenceId() { return initialEvidenceId; }
}
