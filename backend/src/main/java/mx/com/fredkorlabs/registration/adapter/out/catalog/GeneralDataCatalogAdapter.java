package mx.com.fredkorlabs.registration.adapter.out.catalog;

import mx.com.fredkorlabs.registration.application.port.out.CatalogRecord;
import mx.com.fredkorlabs.registration.application.port.out.CountryRecord;
import mx.com.fredkorlabs.registration.application.port.out.GeneralDataCatalogPort;
import mx.com.fredkorlabs.registration.domain.model.LegalOwnershipOption;
import mx.com.fredkorlabs.registration.domain.model.TaxRegime;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GeneralDataCatalogAdapter implements GeneralDataCatalogPort {

    private final CountryJpaRepository countries;
    private final LegalOwnershipJpaRepository legalOwnerships;
    private final SectorJpaRepository sectors;
    private final SubsectorJpaRepository subsectors;

    public GeneralDataCatalogAdapter(CountryJpaRepository countries, LegalOwnershipJpaRepository legalOwnerships,
                                     SectorJpaRepository sectors, SubsectorJpaRepository subsectors) {
        this.countries = countries;
        this.legalOwnerships = legalOwnerships;
        this.sectors = sectors;
        this.subsectors = subsectors;
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
