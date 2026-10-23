package mx.com.fredkorlabs.registration.adapter.out.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

/** Read-only country catalog row; the registration never administers catalogs. */
@Entity
@Immutable
@Table(name = "country")
public class CountryJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 500)
    private String name;

    @Column(name = "international_name", nullable = false, length = 500)
    private String internationalName;

    protected CountryJpaEntity() {
    }

    public Integer getId() { return id; }

    public String getCode() { return code; }

    public String getName() { return name; }

    public String getInternationalName() { return internationalName; }
}
