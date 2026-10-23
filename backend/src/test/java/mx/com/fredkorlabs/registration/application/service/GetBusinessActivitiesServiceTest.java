package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.CatalogOptionsResult.CatalogOption;
import mx.com.fredkorlabs.registration.application.port.out.CatalogRecord;
import mx.com.fredkorlabs.registration.testsupport.UnusedGeneralDataCatalog;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GetBusinessActivitiesServiceTest {

    @Test
    void US08_CA022_businessActivities_areProjectedInCatalogOrder() {
        GetBusinessActivitiesService service = new GetBusinessActivitiesService(new UnusedGeneralDataCatalog() {
            @Override
            public List<CatalogRecord> findBusinessActivities() {
                return List.of(new CatalogRecord(1, "INDUSTRIAL"), new CatalogRecord(2, "COMERCIAL"));
            }
        });

        assertEquals(List.of(new CatalogOption(1, "INDUSTRIAL"), new CatalogOption(2, "COMERCIAL")),
            service.execute().items());
    }

    @Test
    void ADR0020_catalogFailure_propagates() {
        GetBusinessActivitiesService service = new GetBusinessActivitiesService(new UnusedGeneralDataCatalog() {
            @Override
            public List<CatalogRecord> findBusinessActivities() {
                throw new IllegalStateException("Database unavailable");
            }
        });

        assertThrows(IllegalStateException.class, service::execute);
    }
}
