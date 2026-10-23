package mx.com.fredkorlabs.registration.adapter.out.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

/** Read-only annual sales range (Ventas anuales) catalog row. */
@Entity
@Immutable
@Table(name = "annual_sales_range")
public class AnnualSalesRangeJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    protected AnnualSalesRangeJpaEntity() {
    }

    public Integer getId() { return id; }

    public String getDescription() { return description; }
}
