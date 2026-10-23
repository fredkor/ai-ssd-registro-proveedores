package mx.com.fredkorlabs.registration.adapter.out.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

/** Read-only CUCoP chapter row (level 1). */
@Entity
@Immutable
@Table(name = "cucop_chapter")
public class CucopChapterJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "key", nullable = false, length = 50)
    private String key;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "level", nullable = false)
    private Integer level;

    protected CucopChapterJpaEntity() {
    }

    public Integer getId() { return id; }

    public String getKey() { return key; }

    public String getDescription() { return description; }

    public Integer getLevel() { return level; }
}
