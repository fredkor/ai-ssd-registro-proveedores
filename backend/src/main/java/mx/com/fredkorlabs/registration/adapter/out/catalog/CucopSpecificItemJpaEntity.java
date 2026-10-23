package mx.com.fredkorlabs.registration.adapter.out.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

/** Read-only CUCoP specific item row (level 4). The parent is referenced by id. */
@Entity
@Immutable
@Table(name = "cucop_specific_item")
public class CucopSpecificItemJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "generic_item_id", nullable = false)
    private Integer genericItemId;

    @Column(name = "key", nullable = false, length = 50)
    private String key;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "level", nullable = false)
    private Integer level;

    protected CucopSpecificItemJpaEntity() {
    }

    public Integer getId() { return id; }

    public Integer getGenericItemId() { return genericItemId; }

    public String getKey() { return key; }

    public String getDescription() { return description; }

    public Integer getLevel() { return level; }
}
