package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.CatalogOptionsResult.CatalogOption;
import mx.com.fredkorlabs.registration.application.port.out.CatalogRecord;
import mx.com.fredkorlabs.registration.testsupport.UnusedGeneralDataCatalog;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GetPrimaryActivitiesServiceTest {

    @Test
    void US08_CA022_primaryActivities_areProjectedInCatalogOrder() {
        GetPrimaryActivitiesService service = new GetPrimaryActivitiesService(new UnusedGeneralDataCatalog() {
            @Override
            public List<CatalogRecord> findPrimaryActivities() {
                return List.of(new CatalogRecord(1, "RECURSOS NATURALES"), new CatalogRecord(2, "CONSTRUCCIÓN"));
            }
        });

        assertEquals(List.of(new CatalogOption(1, "RECURSOS NATURALES"), new CatalogOption(2, "CONSTRUCCIÓN")),
            service.execute().items());
    }

    @Test
    void ADR0020_catalogFailure_propagates() {
        GetPrimaryActivitiesService service = new GetPrimaryActivitiesService(new UnusedGeneralDataCatalog() {
            @Override
            public List<CatalogRecord> findPrimaryActivities() {
                throw new IllegalStateException("Database unavailable");
            }
        });

        assertThrows(IllegalStateException.class, service::execute);
    }
}
