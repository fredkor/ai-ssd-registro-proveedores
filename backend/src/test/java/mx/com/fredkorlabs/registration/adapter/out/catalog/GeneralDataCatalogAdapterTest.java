package mx.com.fredkorlabs.registration.adapter.out.catalog;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import mx.com.fredkorlabs.registration.application.port.out.CountryRecord;
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

    private String columnType(String column) {
        return (String) entityManager.createNativeQuery(
                "SELECT format_type(atttypid, atttypmod) FROM pg_attribute "
                    + "WHERE attrelid = 'country'::regclass AND attname = ?1")
            .setParameter(1, column).getSingleResult();
    }
}
