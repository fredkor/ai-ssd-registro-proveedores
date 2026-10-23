package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.GetCountriesResult;
import mx.com.fredkorlabs.registration.application.port.in.GetCountriesResult.CountryItem;
import mx.com.fredkorlabs.registration.application.port.out.CountryRecord;
import mx.com.fredkorlabs.registration.application.port.out.GeneralDataCatalogPort;
import mx.com.fredkorlabs.registration.domain.model.LegalOwnershipOption;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class GetCountriesServiceTest {

    @Test
    void US08_CA010_countries_areProjectedWithSpanishNameAndCodeInCatalogOrder() {
        GeneralDataCatalogPort catalog = countries(() -> List.of(
            new CountryRecord(1, "AF", "AFGANISTÁN"), new CountryRecord(151, "MX", "MÉXICO")));

        GetCountriesResult result = new GetCountriesService(catalog).execute();

        assertEquals(List.of(new CountryItem(1, "AFGANISTÁN", "AF"), new CountryItem(151, "MÉXICO", "MX")),
            result.items());
    }

    @Test
    void ADR0020_catalogFailure_propagates() {
        GeneralDataCatalogPort catalog = countries(() -> {
            throw new IllegalStateException("Database unavailable");
        });

        assertThrows(IllegalStateException.class, () -> new GetCountriesService(catalog).execute());
    }

    private static GeneralDataCatalogPort countries(Supplier<List<CountryRecord>> countries) {
        return new GeneralDataCatalogPort() {
            @Override
            public List<CountryRecord> findCountries() {
                return countries.get();
            }

            @Override
            public boolean countryExists(String countryCode) {
                throw new UnsupportedOperationException("Not used by this use case");
            }

            @Override
            public List<LegalOwnershipOption> findLegalOwnershipCandidates(String countryCode) {
                throw new UnsupportedOperationException("Not used by this use case");
            }
        };
    }
}
