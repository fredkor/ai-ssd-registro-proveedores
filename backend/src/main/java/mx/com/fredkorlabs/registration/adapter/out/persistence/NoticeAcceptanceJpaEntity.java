package mx.com.fredkorlabs.registration.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notice_acceptance")
@Check(constraints = "version = '1.0'")
public class NoticeAcceptanceJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "version", nullable = false, updatable = false, length = 3)
    private String version;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "accepted_at", nullable = false, updatable = false)
    private Instant acceptedAt;

    protected NoticeAcceptanceJpaEntity() {
    }

    public NoticeAcceptanceJpaEntity(String version, Instant acceptedAt) {
        this.version = version;
        this.acceptedAt = acceptedAt;
    }

    public UUID getId() { return id; }

    public String getVersion() { return version; }

    public Instant getAcceptedAt() { return acceptedAt; }
}
