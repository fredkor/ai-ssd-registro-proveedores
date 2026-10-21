package mx.com.fredkorlabs.registration.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.UUID;

/** One row per issued registration token (ADR-0015); the jti is assigned before signing. */
@Entity
@Table(name = "registration_context")
public class RegistrationContextJpaEntity implements Persistable<UUID> {

    @Id
    @Column(name = "jti", nullable = false, updatable = false)
    private UUID jti;

    @Column(name = "rfc", nullable = false, updatable = false, length = 13)
    private String rfc;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "issued_at", nullable = false, updatable = false)
    private Instant issuedAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "expires_at", nullable = false, updatable = false)
    private Instant expiresAt;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "signature_evidence_id", nullable = false, updatable = false, unique = true)
    private ApplicantSignatureEvidenceJpaEntity signatureEvidence;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "notice_acceptance_id", nullable = false, updatable = false, unique = true)
    private NoticeAcceptanceJpaEntity noticeAcceptance;

    @Transient
    private boolean persisted;

    protected RegistrationContextJpaEntity() {
    }

    public RegistrationContextJpaEntity(UUID jti, String rfc, Instant issuedAt, Instant expiresAt,
                                        ApplicantSignatureEvidenceJpaEntity signatureEvidence,
                                        NoticeAcceptanceJpaEntity noticeAcceptance) {
        this.jti = jti;
        this.rfc = rfc;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.signatureEvidence = signatureEvidence;
        this.noticeAcceptance = noticeAcceptance;
    }

    @Override
    public UUID getId() { return jti; }

    @Override
    public boolean isNew() { return !persisted; }

    @PostLoad
    @PostPersist
    void markPersisted() { persisted = true; }

    public String getRfc() { return rfc; }

    public Instant getIssuedAt() { return issuedAt; }

    public Instant getExpiresAt() { return expiresAt; }

    public ApplicantSignatureEvidenceJpaEntity getSignatureEvidence() { return signatureEvidence; }

    public NoticeAcceptanceJpaEntity getNoticeAcceptance() { return noticeAcceptance; }
}
