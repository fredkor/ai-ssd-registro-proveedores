package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.CatalogOptionsResult.CatalogOption;
import mx.com.fredkorlabs.registration.application.port.out.CatalogRecord;
import mx.com.fredkorlabs.registration.testsupport.UnusedGeneralDataCatalog;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GetAnnualSalesRangesServiceTest {

    @Test
    void US08_CA022_annualSalesRanges_areProjectedInCatalogOrder() {
        GetAnnualSalesRangesService service = new GetAnnualSalesRangesService(new UnusedGeneralDataCatalog() {
            @Override
            public List<CatalogRecord> findAnnualSalesRanges() {
                return List.of(new CatalogRecord(1, "$0 - $4 MILLONES"), new CatalogRecord(2, "$4 - $100 MILLONES"));
            }
        });

        assertEquals(List.of(new CatalogOption(1, "$0 - $4 MILLONES"), new CatalogOption(2, "$4 - $100 MILLONES")),
            service.execute().items());
    }

    @Test
    void ADR0020_catalogFailure_propagates() {
        GetAnnualSalesRangesService service = new GetAnnualSalesRangesService(new UnusedGeneralDataCatalog() {
            @Override
            public List<CatalogRecord> findAnnualSalesRanges() {
                throw new IllegalStateException("Database unavailable");
            }
        });

        assertThrows(IllegalStateException.class, service::execute);
    }
}
