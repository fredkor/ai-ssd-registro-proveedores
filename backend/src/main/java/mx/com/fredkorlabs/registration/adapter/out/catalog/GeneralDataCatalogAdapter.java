package mx.com.fredkorlabs.registration.adapter.out.catalog;

import mx.com.fredkorlabs.registration.application.port.out.CatalogRecord;
import mx.com.fredkorlabs.registration.application.port.out.CountryRecord;
import mx.com.fredkorlabs.registration.application.port.out.GeneralDataCatalogPort;
import mx.com.fredkorlabs.registration.application.port.out.GeneralDataSelection;
import mx.com.fredkorlabs.registration.domain.model.GeneralDataCatalogContext;
import mx.com.fredkorlabs.registration.domain.model.LegalOwnershipOption;
import mx.com.fredkorlabs.registration.domain.model.TaxRegime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class GeneralDataCatalogAdapter implements GeneralDataCatalogPort {

    private final CountryJpaRepository countries;
    private final LegalOwnershipJpaRepository legalOwnerships;
    private final SectorJpaRepository sectors;
    private final SubsectorJpaRepository subsectors;
    private final BusinessActivityJpaRepository businessActivities;
    private final StratificationJpaRepository stratifications;
    private final PrimaryActivityJpaRepository primaryActivities;
    private final WorkerCountRangeJpaRepository workerCountRanges;
    private final AnnualSalesRangeJpaRepository annualSalesRanges;

    public GeneralDataCatalogAdapter(CountryJpaRepository countries, LegalOwnershipJpaRepository legalOwnerships,
                                     SectorJpaRepository sectors, SubsectorJpaRepository subsectors,
                                     BusinessActivityJpaRepository businessActivities,
                                     StratificationJpaRepository stratifications,
                                     PrimaryActivityJpaRepository primaryActivities,
                                     WorkerCountRangeJpaRepository workerCountRanges,
                                     AnnualSalesRangeJpaRepository annualSalesRanges) {
        this.countries = countries;
        this.legalOwnerships = legalOwnerships;
        this.sectors = sectors;
        this.subsectors = subsectors;
        this.businessActivities = businessActivities;
        this.stratifications = stratifications;
        this.primaryActivities = primaryActivities;
        this.workerCountRanges = workerCountRanges;
        this.annualSalesRanges = annualSalesRanges;
    }

    @Override
    public List<CountryRecord> findCountries() {
        return countries.findAllByOrderByIdAsc().stream()
            .map(country -> new CountryRecord(country.getId(), country.getCode(), country.getName()))
            .toList();
    }

    @Override
    public boolean countryExists(String countryCode) {
        return countries.existsByCode(countryCode);
    }

    @Override
    public List<LegalOwnershipOption> findLegalOwnershipCandidates(String countryCode) {
        return legalOwnerships.findByCountryOrNotDefined(countryCode).stream()
            .map(option -> new LegalOwnershipOption(option.getId(), option.getKey(), option.getDescription(),
                option.getIsoCountry(), regimeOf(option.getRegime())))
            .toList();
    }

    @Override
    public List<CatalogRecord> findSectors() {
        return sectors.findAllByOrderByIdAsc().stream()
            .map(sector -> new CatalogRecord(sector.getId(), sector.getDescription()))
            .toList();
    }

    @Override
    public List<CatalogRecord> findSubsectors(int sectorId) {
        return subsectors.findBySectorIdOrderByIdAsc(sectorId).stream()
            .map(subsector -> new CatalogRecord(subsector.getId(), subsector.getDescription()))
            .toList();
    }

    @Override
    public List<CatalogRecord> findBusinessActivities() {
        return businessActivities.findAllByOrderByIdAsc().stream()
            .map(activity -> new CatalogRecord(activity.getId(), activity.getDescription()))
            .toList();
    }

    @Override
    public List<CatalogRecord> findActiveStratifications() {
        return stratifications.findByActiveTrueOrderByIdAsc().stream()
            .map(stratification -> new CatalogRecord(stratification.getId(), stratification.getDescription()))
            .toList();
    }

    @Override
    public List<CatalogRecord> findPrimaryActivities() {
        return primaryActivities.findAllByOrderByIdAsc().stream()
            .map(activity -> new CatalogRecord(activity.getId(), activity.getDescription()))
            .toList();
    }

    @Override
    public List<CatalogRecord> findWorkerCountRanges() {
        return workerCountRanges.findAllByOrderByIdAsc().stream()
            .map(range -> new CatalogRecord(range.getId(), range.getDescription()))
            .toList();
    }

    @Override
    public List<CatalogRecord> findAnnualSalesRanges() {
        return annualSalesRanges.findAllByOrderByIdAsc().stream()
            .map(range -> new CatalogRecord(range.getId(), range.getDescription()))
            .toList();
    }

    @Override
    public GeneralDataCatalogContext resolveGeneralDataReferences(GeneralDataSelection selection) {
        return new GeneralDataCatalogContext(
            find(countries, selection.countryId()).map(CountryJpaEntity::getCode).orElse(null),
            find(legalOwnerships, selection.legalOwnershipId())
                .map(option -> new LegalOwnershipOption(option.getId(), option.getKey(), option.getDescription(),
                    option.getIsoCountry(), regimeOf(option.getRegime())))
                .orElse(null),
            exists(sectors, selection.sectorId()),
            find(subsectors, selection.subsectorId()).map(SubsectorJpaEntity::getSectorId).orElse(null),
            exists(businessActivities, selection.businessActivityId()),
            find(stratifications, selection.stratificationId()).map(StratificationJpaEntity::isActive).orElse(false),
            exists(primaryActivities, selection.primaryActivityId()),
            exists(workerCountRanges, selection.workerCountRangeId()),
            exists(annualSalesRanges, selection.annualSalesRangeId()));
    }

    private static <T> Optional<T> find(JpaRepository<T, Integer> repository, Integer id) {
        return id == null ? Optional.empty() : repository.findById(id);
    }

    private static boolean exists(JpaRepository<?, Integer> repository, Integer id) {
        return id != null && repository.existsById(id);
    }

    // tax_regime descriptions and their technical codes (domain/catalogs.md).
    private static TaxRegime regimeOf(String description) {
        if (description == null) return null;
        return switch (description) {
            case "PERSONA MORAL" -> TaxRegime.LEGAL_ENTITY;
            case "PERSONA FÍSICA" -> TaxRegime.INDIVIDUAL;
            default -> throw new IllegalStateException("Unknown tax regime in legal ownership catalog");
        };
    }
}
