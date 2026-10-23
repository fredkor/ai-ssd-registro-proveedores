package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.CatalogOptionsResult.CatalogOption;
import mx.com.fredkorlabs.registration.application.port.out.CatalogRecord;
import mx.com.fredkorlabs.registration.testsupport.UnusedGeneralDataCatalog;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GetWorkerCountRangesServiceTest {

    @Test
    void US08_CA022_workerCountRanges_areProjectedInCatalogOrder() {
        GetWorkerCountRangesService service = new GetWorkerCountRangesService(new UnusedGeneralDataCatalog() {
            @Override
            public List<CatalogRecord> findWorkerCountRanges() {
                return List.of(new CatalogRecord(1, "0"), new CatalogRecord(2, "1-10"));
            }
        });

        assertEquals(List.of(new CatalogOption(1, "0"), new CatalogOption(2, "1-10")),
            service.execute().items());
    }

    @Test
    void ADR0020_catalogFailure_propagates() {
        GetWorkerCountRangesService service = new GetWorkerCountRangesService(new UnusedGeneralDataCatalog() {
            @Override
            public List<CatalogRecord> findWorkerCountRanges() {
                throw new IllegalStateException("Database unavailable");
            }
        });

        assertThrows(IllegalStateException.class, service::execute);
    }
}
