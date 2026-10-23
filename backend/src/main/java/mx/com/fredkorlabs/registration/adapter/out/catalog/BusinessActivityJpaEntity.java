package mx.com.fredkorlabs.registration.adapter.out.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

/** Read-only business activity (Giro) catalog row. */
@Entity
@Immutable
@Table(name = "business_activity")
public class BusinessActivityJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    protected BusinessActivityJpaEntity() {
    }

    public Integer getId() { return id; }

    public String getDescription() { return description; }
}
