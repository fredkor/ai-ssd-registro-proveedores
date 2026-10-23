package mx.com.fredkorlabs.registration.adapter.out.catalog;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import mx.com.fredkorlabs.registration.application.port.out.CatalogRecord;
import mx.com.fredkorlabs.registration.application.port.out.CountryRecord;
import mx.com.fredkorlabs.registration.domain.model.LegalOwnershipOption;
import mx.com.fredkorlabs.registration.domain.model.TaxRegime;
import mx.com.fredkorlabs.registration.testsupport.PostgresTestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

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
