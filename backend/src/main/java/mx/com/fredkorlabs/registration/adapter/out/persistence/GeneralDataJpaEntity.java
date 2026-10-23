package mx.com.fredkorlabs.registration.adapter.out.persistence;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.springframework.data.domain.Persistable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Saved General Data of a registration (design §2); phones are replaced as a whole on each save. */
@Entity
@Table(name = "general_data")
public class GeneralDataJpaEntity implements Persistable<UUID> {

    @Id
    @Column(name = "registration_id", nullable = false, updatable = false)
    private UUID registrationId;

    @Column(name = "origin_id", nullable = false)
    private Integer originId;

    @Column(name = "country_id", nullable = false)
    private Integer countryId;

    @Column(name = "nationality", nullable = false, length = 250)
    private String nationality;

    @Column(name = "foreign_fiscal_code", length = 20)
    private String foreignFiscalCode;

    @Column(name = "legal_ownership_id", nullable = false)
    private Integer legalOwnershipId;

    @Column(name = "sector_id")
    private Integer sectorId;

    @Column(name = "subsector_id")
    private Integer subsectorId;

    @Column(name = "business_activity_id", nullable = false)
    private Integer businessActivityId;

    @Column(name = "stratification_id", nullable = false)
    private Integer stratificationId;

    @Column(name = "primary_activity_id", nullable = false)
    private Integer primaryActivityId;

    @Column(name = "worker_count_range_id", nullable = false)
    private Integer workerCountRangeId;

    @Column(name = "annual_sales_range_id", nullable = false)
    private Integer annualSalesRangeId;

    @Column(name = "website", length = 2048)
    private String website;

    @ElementCollection
    @CollectionTable(name = "registration_phone", joinColumns = @JoinColumn(name = "registration_id"))
    @OrderColumn(name = "position")
    private List<PhoneEmbeddable> phones = new ArrayList<>();

    @Transient
    private boolean persisted;

    protected GeneralDataJpaEntity() {
    }

    public GeneralDataJpaEntity(UUID registrationId) {
        this.registrationId = registrationId;
    }

    public void replace(Integer originId, Integer countryId, String nationality, String foreignFiscalCode,
                        Integer legalOwnershipId, Integer sectorId, Integer subsectorId, Integer businessActivityId,
                        Integer stratificationId, Integer primaryActivityId, Integer workerCountRangeId,
                        Integer annualSalesRangeId, String website, List<PhoneEmbeddable> phones) {
        this.originId = originId;
        this.countryId = countryId;
        this.nationality = nationality;
        this.foreignFiscalCode = foreignFiscalCode;
        this.legalOwnershipId = legalOwnershipId;
        this.sectorId = sectorId;
        this.subsectorId = subsectorId;
        this.businessActivityId = businessActivityId;
        this.stratificationId = stratificationId;
        this.primaryActivityId = primaryActivityId;
        this.workerCountRangeId = workerCountRangeId;
        this.annualSalesRangeId = annualSalesRangeId;
        this.website = website;
        this.phones.clear();
        this.phones.addAll(phones);
    }

    @Override
    public UUID getId() { return registrationId; }

    @Override
    public boolean isNew() { return !persisted; }

    @PostLoad
    @PostPersist
    void markPersisted() { persisted = true; }

    public Integer getOriginId() { return originId; }

    public Integer getCountryId() { return countryId; }

    public String getNationality() { return nationality; }

    public String getForeignFiscalCode() { return foreignFiscalCode; }

    public Integer getLegalOwnershipId() { return legalOwnershipId; }

    public Integer getSectorId() { return sectorId; }

    public Integer getSubsectorId() { return subsectorId; }

    public Integer getBusinessActivityId() { return businessActivityId; }

    public Integer getStratificationId() { return stratificationId; }

    public Integer getPrimaryActivityId() { return primaryActivityId; }

    public Integer getWorkerCountRangeId() { return workerCountRangeId; }

    public Integer getAnnualSalesRangeId() { return annualSalesRangeId; }

    public String getWebsite() { return website; }

    public List<PhoneEmbeddable> getPhones() { return phones; }
}
