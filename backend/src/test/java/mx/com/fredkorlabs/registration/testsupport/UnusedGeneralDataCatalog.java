package mx.com.fredkorlabs.registration.testsupport;

import mx.com.fredkorlabs.registration.application.port.out.CatalogRecord;
import mx.com.fredkorlabs.registration.application.port.out.CountryRecord;
import mx.com.fredkorlabs.registration.application.port.out.GeneralDataCatalogPort;
import mx.com.fredkorlabs.registration.domain.model.LegalOwnershipOption;

import java.util.List;

/** Catalog test double whose queries all fail; each test overrides only the queries its use case makes. */
public class UnusedGeneralDataCatalog implements GeneralDataCatalogPort {

    @Override
    public List<CountryRecord> findCountries() {
        throw unused();
    }

    @Override
    public boolean countryExists(String countryCode) {
        throw unused();
    }

    @Override
    public List<LegalOwnershipOption> findLegalOwnershipCandidates(String countryCode) {
        throw unused();
    }

    @Override
    public List<CatalogRecord> findSectors() {
        throw unused();
    }

    @Override
    public List<CatalogRecord> findSubsectors(int sectorId) {
        throw unused();
    }

    @Override
    public List<CatalogRecord> findBusinessActivities() {
        throw unused();
    }

    @Override
    public List<CatalogRecord> findActiveStratifications() {
        throw unused();
    }

    private static UnsupportedOperationException unused() {
        return new UnsupportedOperationException("Not used by this use case");
    }
}
