package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.GetCountriesResult;
import mx.com.fredkorlabs.registration.application.port.in.GetCountriesResult.CountryItem;
import mx.com.fredkorlabs.registration.application.port.out.CountryRecord;
import mx.com.fredkorlabs.registration.application.port.out.GeneralDataCatalogPort;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GetCountriesServiceTest {

    @Test
    void US08_CA010_countries_areProjectedWithSpanishNameAndCodeInCatalogOrder() {
        GeneralDataCatalogPort catalog = () -> List.of(
            new CountryRecord(1, "AF", "AFGANISTÁN"), new CountryRecord(151, "MX", "MÉXICO"));

        GetCountriesResult result = new GetCountriesService(catalog).execute();

        assertEquals(List.of(new CountryItem(1, "AFGANISTÁN", "AF"), new CountryItem(151, "MÉXICO", "MX")),
            result.items());
    }

    @Test
    void ADR0020_catalogFailure_propagates() {
        GeneralDataCatalogPort catalog = () -> {
            throw new IllegalStateException("Database unavailable");
        };

        assertThrows(IllegalStateException.class, () -> new GetCountriesService(catalog).execute());
    }
}
