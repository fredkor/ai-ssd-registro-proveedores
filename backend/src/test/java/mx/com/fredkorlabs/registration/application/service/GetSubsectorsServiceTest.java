package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.CatalogOptionsResult.CatalogOption;
import mx.com.fredkorlabs.registration.application.port.in.GetSubsectorsQuery;
import mx.com.fredkorlabs.registration.application.port.out.CatalogRecord;
import mx.com.fredkorlabs.registration.testsupport.UnusedGeneralDataCatalog;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GetSubsectorsServiceTest {

    private final List<Integer> queriedSectors = new ArrayList<>();
    private final GetSubsectorsService service = new GetSubsectorsService(new UnusedGeneralDataCatalog() {
        @Override
        public List<CatalogRecord> findSubsectors(int sectorId) {
            queriedSectors.add(sectorId);
            return sectorId == 1
                ? List.of(new CatalogRecord(1, "111 AGRICULTURA"), new CatalogRecord(2, "112 CRÍA Y EXPLOTACIÓN DE ANIMALES"))
                : List.of();
        }
    });

    @Test
    void US08_CA023_subsectors_ofTheSelectedSectorAreProjected() {
        assertEquals(List.of(new CatalogOption(1, "111 AGRICULTURA"), new CatalogOption(2, "112 CRÍA Y EXPLOTACIÓN DE ANIMALES")),
            service.execute(new GetSubsectorsQuery(1)).items());
        assertEquals(List.of(1), queriedSectors);
    }

    @Test
    void ADR0020_unknownSector_returnsEmptyItems() {
        assertTrue(service.execute(new GetSubsectorsQuery(999)).items().isEmpty());
    }
}
