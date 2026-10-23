package mx.com.fredkorlabs.registration.adapter.out.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

/** Read-only subsector catalog row; it belongs to exactly one sector. */
@Entity
@Immutable
@Table(name = "subsector")
public class SubsectorJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "sector_id", nullable = false)
    private Integer sectorId;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    protected SubsectorJpaEntity() {
    }

    public Integer getId() { return id; }

    public Integer getSectorId() { return sectorId; }

    public String getDescription() { return description; }
}
