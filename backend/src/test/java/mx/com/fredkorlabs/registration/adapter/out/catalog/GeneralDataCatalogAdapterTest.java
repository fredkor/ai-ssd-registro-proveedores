package mx.com.fredkorlabs.registration.adapter.out.catalog;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import mx.com.fredkorlabs.registration.application.port.out.CatalogRecord;
import mx.com.fredkorlabs.registration.application.port.out.CountryRecord;
import mx.com.fredkorlabs.registration.application.port.out.GeneralDataSelection;
import mx.com.fredkorlabs.registration.domain.model.GeneralDataCatalogContext;
import mx.com.fredkorlabs.registration.domain.model.LegalOwnershipOption;
import mx.com.fredkorlabs.registration.domain.model.TaxRegime;
import mx.com.fredkorlabs.registration.testsupport.PostgresTestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({GeneralDataCatalogAdapter.class, PostgresTestcontainersConfiguration.class})
class GeneralDataCatalogAdapterTest {

    @Autowired
    private GeneralDataCatalogAdapter adapter;

    @Autowired
    private CountryJpaRepository countries;

    @Autowired
    private EntityManager entityManager;

    @Test
    void US08_CA011_taxRegimes_loadTheWholeCatalogOrderedById() {
        assertEquals(List.of(new CatalogRecord(1, "PERSONA MORAL"), new CatalogRecord(2, "PERSONA FÍSICA")),
            adapter.findTaxRegimes());
    }

    @Test
    void INV14_catalogIdsOfTheDomain_matchTheTaxRegimeCatalog() {
        Map<Integer, String> catalog = adapter.findTaxRegimes().stream()
            .collect(Collectors.toMap(CatalogRecord::id, CatalogRecord::description));

        assertEquals("PERSONA MORAL", catalog.get(TaxRegime.LEGAL_ENTITY.catalogId()));
        assertEquals("PERSONA FÍSICA", catalog.get(TaxRegime.INDIVIDUAL.catalogId()));
    }

    @Test
    void US08_CA010_countries_loadTheWholeCatalogOrderedById() {
        List<CountryRecord> found = adapter.findCountries();

        assertEquals(249, found.size());
        assertEquals(new CountryRecord(1, "AF", "AFGANISTÁN"), found.getFirst());
        assertEquals(new CountryRecord(250, "SZ", "ESWATINI"), found.getLast());
        List<Integer> ids = found.stream().map(CountryRecord::id).toList();
        assertEquals(ids.stream().sorted().toList(), ids);
    }

    @Test
    void US08_CA009_mexico_isInTheCatalogWithItsCode() {
        assertTrue(adapter.findCountries().contains(new CountryRecord(151, "MX", "MÉXICO")));
    }

    @Test
    void ADR0017_migration_keepsOriginalValuesIncludingApostrophes() {
        assertEquals("KOREA, DEMOCRATIC PEOPLE'S REPUBLIC OF", countries.findById(49).orElseThrow().getInternationalName());
        assertEquals("ÅLAND ISLANDS", countries.findById(2).orElseThrow().getInternationalName());
    }

    @Test
    void ADR0017_database_rejectsDuplicateCountryCode() {
        assertThrows(PersistenceException.class, () -> entityManager.createNativeQuery(
                "INSERT INTO country (id, code, name, international_name) VALUES (999, 'MX', 'OTRO', 'OTHER')")
            .executeUpdate());
    }

    @Test
    void ADR0017_schema_usesApprovedSqlTypes() {
        assertEquals("integer", columnType("id"));
        assertEquals("character varying(50)", columnType("code"));
        assertEquals("character varying(500)", columnType("name"));
        assertEquals("character varying(500)", columnType("international_name"));
    }

    @Test
    void US08_CA014_legalOwnershipCandidates_areCountryRowsPlusNotDef() {
        List<LegalOwnershipOption> candidates = adapter.findLegalOwnershipCandidates("AT");

        assertEquals(23, candidates.size());
        assertTrue(candidates.contains(new LegalOwnershipOption(99, "NOT_DEF", "RNS.LEGALSTRUCTURE/NOTDEFINED", null, null)));
        assertTrue(candidates.contains(new LegalOwnershipOption(122, "AT00001", "EINZELUNTERNEHMEN", "AT", null)));
    }

    @Test
    void US08_CA003_legalOwnershipCandidates_mapRegimeTextToTechnicalCode() {
        List<LegalOwnershipOption> mexico = adapter.findLegalOwnershipCandidates("MX");

        assertEquals(27, mexico.stream().filter(option -> option.regime() == TaxRegime.LEGAL_ENTITY).count());
        assertEquals(4, mexico.stream().filter(option -> option.regime() == TaxRegime.INDIVIDUAL).count());
        assertTrue(mexico.contains(new LegalOwnershipOption(6, "MX00006",
            "4.- PERSONA FÍSICA CON ACTIVIDAD EMPRESARIAL (EMPRESARIO INDIVIDUAL)", "MX", TaxRegime.INDIVIDUAL)));
    }

    @Test
    void ADR0020_countryExists_comparesCodesExactly() {
        assertTrue(adapter.countryExists("MX"));
        assertFalse(adapter.countryExists("mx"));
        assertFalse(adapter.countryExists("ZZ"));
    }

    @Test
    void ADR0017_legalOwnershipMigration_loadsCatalogsWithOriginalIds() {
        assertEquals(180L, ((Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM legal_ownership")
            .getSingleResult()).longValue());
        assertEquals(List.of("PERSONA MORAL", "PERSONA FÍSICA"), entityManager.createNativeQuery(
            "SELECT description FROM tax_regime ORDER BY id").getResultList());
        assertEquals("EUROPÄISCHEN GESELLSCHAFT\u00a0(SE)", entityManager.createNativeQuery(
            "SELECT description FROM legal_ownership WHERE key = 'AT00022'").getSingleResult());
    }

    @Test
    void ADR0017_database_rejectsUnknownCountryInLegalOwnership() {
        assertThrows(PersistenceException.class, () -> entityManager.createNativeQuery(
                "INSERT INTO legal_ownership (id, key, description, iso_country, regime) VALUES (999, 'ZZ00001', 'X', 'ZZ', null)")
            .executeUpdate());
    }

    @Test
    void ADR0017_database_rejectsUnknownRegimeText() {
        assertThrows(PersistenceException.class, () -> entityManager.createNativeQuery(
                "INSERT INTO legal_ownership (id, key, description, iso_country, regime) VALUES (998, 'MX99999', 'X', 'MX', 'PERSONA FISICA')")
            .executeUpdate());
    }

    @Test
    void ADR0017_legalOwnershipSchema_usesApprovedSqlTypes() {
        assertEquals("character varying(50)", columnType("legal_ownership", "key"));
        assertEquals("character varying(500)", columnType("legal_ownership", "description"));
        assertEquals("character varying(50)", columnType("legal_ownership", "iso_country"));
        assertEquals("character varying(500)", columnType("legal_ownership", "regime"));
        assertEquals("character varying(500)", columnType("tax_regime", "description"));
    }

    @Test
    void US08_CA022_sectors_loadTheWholeCatalogOrderedById() {
        List<CatalogRecord> found = adapter.findSectors();

        assertEquals(20, found.size());
        assertEquals(new CatalogRecord(1,
            "11 AGRICULTURA, CRÍA Y EXPLOTACIÓN DE ANIMALES, APROVECHAMIENTO FORESTAL, PESCA Y CAZA"), found.getFirst());
        assertEquals(new CatalogRecord(5, "31-33 INDUSTRIAS MANUFACTURERAS"), found.get(4));
        assertEquals(20, found.getLast().id());
        List<Integer> ids = found.stream().map(CatalogRecord::id).toList();
        assertEquals(ids.stream().sorted().toList(), ids);
    }

    @Test
    void ADR0017_sectorSchema_usesApprovedSqlTypes() {
        assertEquals("integer", columnType("sector", "id"));
        assertEquals("character varying(500)", columnType("sector", "description"));
    }

    @Test
    void US08_CA023_subsectors_belongToTheSelectedSectorOrderedById() {
        List<CatalogRecord> found = adapter.findSubsectors(5);

        assertEquals(21, found.size());
        List<Integer> ids = found.stream().map(CatalogRecord::id).toList();
        assertEquals(ids.stream().sorted().toList(), ids);
        assertEquals(List.of(new CatalogRecord(1, "111 AGRICULTURA"), new CatalogRecord(2, "112 CRÍA Y EXPLOTACIÓN DE ANIMALES"),
                new CatalogRecord(3, "113 APROVECHAMIENTO FORESTAL"), new CatalogRecord(4, "114 PESCA, CAZA Y CAPTURA"),
                new CatalogRecord(5, "115 SERVICIOS RELACIONADOS CON LAS ACTIVIDADES AGROPECUARIAS Y FORESTALES")),
            adapter.findSubsectors(1));
    }

    @Test
    void ADR0020_subsectorsOfUnknownSector_areEmpty() {
        assertTrue(adapter.findSubsectors(999).isEmpty());
    }

    @Test
    void ADR0017_subsectorMigration_loadsCatalogWithSectorReferences() {
        assertEquals(94L, ((Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM subsector")
            .getSingleResult()).longValue());
        assertThrows(PersistenceException.class, () -> entityManager.createNativeQuery(
                "INSERT INTO subsector (id, sector_id, description) VALUES (999, 999, 'SIN SECTOR')")
            .executeUpdate());
    }

    @Test
    void ADR0017_subsectorSchema_usesApprovedTypesAndSectorIndex() {
        assertEquals("integer", columnType("subsector", "sector_id"));
        assertEquals("character varying(500)", columnType("subsector", "description"));
        assertEquals(1L, ((Number) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM pg_indexes WHERE tablename = 'subsector' AND indexname = 'subsector_sector_id_idx'")
            .getSingleResult()).longValue());
    }

    @Test
    void US08_CA022_businessActivities_loadTheWholeCatalogOrderedById() {
        assertEquals(List.of(new CatalogRecord(1, "INDUSTRIAL"), new CatalogRecord(2, "COMERCIAL"),
            new CatalogRecord(3, "DE SERVICIOS")), adapter.findBusinessActivities());
    }

    @Test
    void ADR0017_businessActivitySchema_usesApprovedSqlTypes() {
        assertEquals("integer", columnType("business_activity", "id"));
        assertEquals("character varying(500)", columnType("business_activity", "description"));
    }

    @Test
    void US08_CA022_stratifications_returnOnlyActiveEntriesOrderedById() {
        assertEquals(List.of(new CatalogRecord(1, "COOPERATIVAS"), new CatalogRecord(2, "GRANDE"),
                new CatalogRecord(3, "MEDIANA"), new CatalogRecord(4, "MICRO"), new CatalogRecord(6, "PEQUEÑA")),
            adapter.findActiveStratifications());
    }

    @Test
    void ADR0017_stratificationMigration_keepsInactiveEntry() {
        assertEquals(6L, ((Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM stratification")
            .getSingleResult()).longValue());
        assertEquals(List.of("NO MIPYME"), entityManager.createNativeQuery(
            "SELECT description FROM stratification WHERE NOT active").getResultList());
    }

    @Test
    void ADR0017_stratificationSchema_usesApprovedSqlTypes() {
        assertEquals("character varying(500)", columnType("stratification", "description"));
        assertEquals("boolean", columnType("stratification", "active"));
    }

    @Test
    void US08_CA022_primaryActivities_loadTheWholeCatalogOrderedById() {
        List<CatalogRecord> found = adapter.findPrimaryActivities();

        assertEquals(8, found.size());
        assertEquals(new CatalogRecord(1, "RECURSOS NATURALES"), found.getFirst());
        assertEquals(new CatalogRecord(3, "FABRICACIÓN PRODUCCIÓN O ELABORACIÓN"), found.get(2));
        assertEquals(new CatalogRecord(8, "COMERCIO AL POR MENOR"), found.getLast());
    }

    @Test
    void ADR0017_primaryActivitySchema_usesApprovedSqlTypes() {
        assertEquals("integer", columnType("primary_activity", "id"));
        assertEquals("character varying(500)", columnType("primary_activity", "description"));
    }

    @Test
    void US08_CA022_workerCountRanges_loadTheWholeCatalogOrderedById() {
        assertEquals(List.of(new CatalogRecord(1, "0"), new CatalogRecord(2, "1-10"), new CatalogRecord(3, "11-30"),
                new CatalogRecord(4, "31-50"), new CatalogRecord(5, "51-100"), new CatalogRecord(6, "101-251"),
                new CatalogRecord(7, "MÁS DE 251")),
            adapter.findWorkerCountRanges());
    }

    @Test
    void ADR0017_workerCountRangeSchema_usesApprovedSqlTypes() {
        assertEquals("integer", columnType("worker_count_range", "id"));
        assertEquals("character varying(500)", columnType("worker_count_range", "description"));
    }

    @Test
    void US08_CA022_annualSalesRanges_loadTheWholeCatalogOrderedById() {
        assertEquals(List.of(new CatalogRecord(1, "$0 - $4 MILLONES"), new CatalogRecord(2, "$4 - $100 MILLONES"),
                new CatalogRecord(3, "$100 - $250 MILLONES"), new CatalogRecord(4, "MÁS DE $250 MILLONES")),
            adapter.findAnnualSalesRanges());
    }

    @Test
    void ADR0017_annualSalesRangeSchema_usesApprovedSqlTypes() {
        assertEquals("integer", columnType("annual_sales_range", "id"));
        assertEquals("character varying(500)", columnType("annual_sales_range", "description"));
    }

    @Test
    void US08_CA008_generalDataReferences_resolveEverySelectionInOneCall() {
        GeneralDataCatalogContext context = adapter.resolveGeneralDataReferences(
            new GeneralDataSelection(151, 1, 5, 21, 1, 2, 3, 4, 2));

        assertEquals("MX", context.countryCode());
        assertEquals(new LegalOwnershipOption(1, "MX00001", "1.- SOCIEDAD ANÓNIMA (S.A.)", "MX", TaxRegime.LEGAL_ENTITY),
            context.legalOwnership());
        assertTrue(context.sectorExists());
        assertEquals(5, context.subsectorSectorId());
        assertTrue(context.businessActivityExists());
        assertTrue(context.stratificationActive());
        assertTrue(context.primaryActivityExists());
        assertTrue(context.workerCountRangeExists());
        assertTrue(context.annualSalesRangeExists());
    }

    @Test
    void US08_CA008_unknownOrInactiveSelections_resolveAsNotCurrent() {
        GeneralDataCatalogContext context = adapter.resolveGeneralDataReferences(
            new GeneralDataSelection(9999, 9999, 9999, 9999, 9999, 5, 9999, 9999, 9999));

        assertNull(context.countryCode());
        assertNull(context.legalOwnership());
        assertFalse(context.sectorExists());
        assertNull(context.subsectorSectorId());
        assertFalse(context.businessActivityExists());
        assertFalse(context.stratificationActive(), "NO MIPYME (5) is inactive");
        assertFalse(context.primaryActivityExists());
        assertFalse(context.workerCountRangeExists());
        assertFalse(context.annualSalesRangeExists());
    }

    @Test
    void US08_CA008_absentSelections_resolveWithoutQueryingNullIds() {
        GeneralDataCatalogContext context = adapter.resolveGeneralDataReferences(
            new GeneralDataSelection(4, 99, null, null, 1, 2, 3, 4, 2));

        assertEquals("DE", context.countryCode());
        assertTrue(context.legalOwnership().isNotDefined());
        assertFalse(context.sectorExists());
        assertNull(context.subsectorSectorId());
    }

    private String columnType(String column) {
        return columnType("country", column);
    }

    private String columnType(String table, String column) {
        return (String) entityManager.createNativeQuery(
                "SELECT format_type(atttypid, atttypmod) FROM pg_attribute "
                    + "WHERE attrelid = to_regclass(?1) AND attname = ?2")
            .setParameter(1, table).setParameter(2, column).getSingleResult();
    }
}
