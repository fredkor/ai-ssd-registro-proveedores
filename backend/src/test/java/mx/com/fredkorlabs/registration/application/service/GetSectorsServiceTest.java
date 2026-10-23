package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.CatalogOptionsResult.CatalogOption;
import mx.com.fredkorlabs.registration.application.port.out.CatalogRecord;
import mx.com.fredkorlabs.registration.application.port.out.CountryRecord;
import mx.com.fredkorlabs.registration.application.port.out.GeneralDataCatalogPort;
import mx.com.fredkorlabs.registration.domain.model.LegalOwnershipOption;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class GetSectorsServiceTest {

    @Test
    void US08_CA022_sectors_areProjectedInCatalogOrder() {
        GetSectorsService service = new GetSectorsService(sectors(() -> List.of(
            new CatalogRecord(1, "11 AGRICULTURA"), new CatalogRecord(2, "21 MINERÍA"))));

        assertEquals(List.of(new CatalogOption(1, "11 AGRICULTURA"), new CatalogOption(2, "21 MINERÍA")),
            service.execute().items());
    }

    @Test
    void ADR0020_catalogFailure_propagates() {
        GetSectorsService service = new GetSectorsService(sectors(() -> {
            throw new IllegalStateException("Database unavailable");
        }));

        assertThrows(IllegalStateException.class, service::execute);
    }

    private static GeneralDataCatalogPort sectors(Supplier<List<CatalogRecord>> sectors) {
        return new GeneralDataCatalogPort() {
            @Override
            public List<CountryRecord> findCountries() {
                throw new UnsupportedOperationException("Not used by this use case");
            }

            @Override
            public boolean countryExists(String countryCode) {
                throw new UnsupportedOperationException("Not used by this use case");
            }

            @Override
            public List<LegalOwnershipOption> findLegalOwnershipCandidates(String countryCode) {
                throw new UnsupportedOperationException("Not used by this use case");
            }

            @Override
            public List<CatalogRecord> findSectors() {
                return sectors.get();
            }
        };
    }
}
