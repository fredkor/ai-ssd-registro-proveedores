package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.CatalogOptionsResult.CatalogOption;
import mx.com.fredkorlabs.registration.application.port.out.CatalogRecord;
import mx.com.fredkorlabs.registration.application.port.out.GeneralDataCatalogPort;
import mx.com.fredkorlabs.registration.testsupport.UnusedGeneralDataCatalog;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class GetTaxRegimesServiceTest {

    @Test
    void US08_CA011_taxRegimes_areProjectedInCatalogOrder() {
        GetTaxRegimesService service = new GetTaxRegimesService(taxRegimes(() -> List.of(
            new CatalogRecord(1, "PERSONA MORAL"), new CatalogRecord(2, "PERSONA FÍSICA"))));

        assertEquals(List.of(new CatalogOption(1, "PERSONA MORAL"), new CatalogOption(2, "PERSONA FÍSICA")),
            service.execute().items());
    }

    @Test
    void ADR0020_catalogFailure_propagates() {
        GetTaxRegimesService service = new GetTaxRegimesService(taxRegimes(() -> {
            throw new IllegalStateException("Database unavailable");
        }));

        assertThrows(IllegalStateException.class, service::execute);
    }

    private static GeneralDataCatalogPort taxRegimes(Supplier<List<CatalogRecord>> taxRegimes) {
        return new UnusedGeneralDataCatalog() {
            @Override
            public List<CatalogRecord> findTaxRegimes() {
                return taxRegimes.get();
            }
        };
    }
}
