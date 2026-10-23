package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.CatalogOptionsResult.CatalogOption;
import mx.com.fredkorlabs.registration.application.port.out.CatalogRecord;
import mx.com.fredkorlabs.registration.testsupport.UnusedGeneralDataCatalog;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GetStratificationsServiceTest {

    @Test
    void US08_CA022_stratifications_projectTheActiveEntriesInCatalogOrder() {
        GetStratificationsService service = new GetStratificationsService(new UnusedGeneralDataCatalog() {
            @Override
            public List<CatalogRecord> findActiveStratifications() {
                return List.of(new CatalogRecord(4, "MICRO"), new CatalogRecord(6, "PEQUEÑA"));
            }
        });

        assertEquals(List.of(new CatalogOption(4, "MICRO"), new CatalogOption(6, "PEQUEÑA")), service.execute().items());
    }

    @Test
    void ADR0020_catalogFailure_propagates() {
        GetStratificationsService service = new GetStratificationsService(new UnusedGeneralDataCatalog() {
            @Override
            public List<CatalogRecord> findActiveStratifications() {
                throw new IllegalStateException("Database unavailable");
            }
        });

        assertThrows(IllegalStateException.class, service::execute);
    }
}
