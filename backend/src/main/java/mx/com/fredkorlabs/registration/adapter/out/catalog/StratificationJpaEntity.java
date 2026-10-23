package mx.com.fredkorlabs.registration.adapter.out.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

/** Read-only stratification catalog row; {@code active} decides whether the API exposes it. */
@Entity
@Immutable
@Table(name = "stratification")
public class StratificationJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "active", nullable = false)
    private boolean active;

    protected StratificationJpaEntity() {
    }

    public Integer getId() { return id; }

    public String getDescription() { return description; }

    public boolean isActive() { return active; }
}
