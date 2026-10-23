package mx.com.fredkorlabs.registration.adapter.out.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

/** Read-only CUCoP generic item row (level 3). The parent is referenced by id. */
@Entity
@Immutable
@Table(name = "cucop_generic_item")
public class CucopGenericItemJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "concept_id", nullable = false)
    private Integer conceptId;

    @Column(name = "key", nullable = false, length = 50)
    private String key;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "level", nullable = false)
    private Integer level;

    protected CucopGenericItemJpaEntity() {
    }

    public Integer getId() { return id; }

    public Integer getConceptId() { return conceptId; }

    public String getKey() { return key; }

    public String getDescription() { return description; }

    public Integer getLevel() { return level; }
}
