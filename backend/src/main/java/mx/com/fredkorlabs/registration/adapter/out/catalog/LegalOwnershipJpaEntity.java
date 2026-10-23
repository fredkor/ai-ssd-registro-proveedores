package mx.com.fredkorlabs.registration.adapter.out.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

/** Read-only legal ownership row; country and regime are related by code and text (catalog design). */
@Entity
@Immutable
@Table(name = "legal_ownership")
public class LegalOwnershipJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "key", nullable = false, length = 50)
    private String key;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "iso_country", length = 50)
    private String isoCountry;

    @Column(name = "regime", length = 500)
    private String regime;

    protected LegalOwnershipJpaEntity() {
    }

    public Integer getId() { return id; }

    public String getKey() { return key; }

    public String getDescription() { return description; }

    public String getIsoCountry() { return isoCountry; }

    public String getRegime() { return regime; }
}
