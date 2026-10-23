package mx.com.fredkorlabs.registration.adapter.out.catalog;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import mx.com.fredkorlabs.registration.application.port.out.CucopCatalog;
import mx.com.fredkorlabs.registration.domain.model.CucopChapter;
import mx.com.fredkorlabs.registration.domain.model.CucopConcept;
import mx.com.fredkorlabs.registration.domain.model.CucopGenericItem;
import mx.com.fredkorlabs.registration.domain.model.CucopSpecificItem;
import mx.com.fredkorlabs.registration.testsupport.PostgresTestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({CucopCatalogAdapter.class, PostgresTestcontainersConfiguration.class})
class CucopCatalogAdapterTest {

    @Autowired
    private CucopCatalogAdapter adapter;

    @Autowired
    private EntityManager entityManager;

    private long count(String sql) {
        return ((Number) entityManager.createNativeQuery(sql).getSingleResult()).longValue();
    }

    @Test
    void US11_CA015_catalog_loadsTheFourLevels() {
        CucopCatalog catalog = adapter.findCatalog();

        assertEquals(7, catalog.chapters().size());
        assertEquals(39, catalog.concepts().size());
        assertEquals(206, catalog.genericItems().size());
        assertEquals(322, catalog.specificItems().size());
        assertTrue(catalog.chapters().contains(new CucopChapter(6, "1000", "SERVICIOS PERSONALES", 1)));
        assertTrue(catalog.concepts().contains(new CucopConcept(1, 1, "2100",
            "MATERIALES DE ADMINISTRACION, EMISION DE DOCUMENTOS Y ARTICULOS OFICIALES", 2)));
        assertTrue(catalog.genericItems().contains(new CucopGenericItem(242, 38, "1220",
            "SUELDOS BASE AL PERSONAL EVENTUAL", 3)));
        assertTrue(catalog.specificItems().contains(new CucopSpecificItem(1, 1, "21101",
            "MATERIALES Y ÚTILES DE OFICINA", 4)));
    }

    @Test
    void INV32_everyNode_hasAtLeastOneSpecificItem() {
        assertEquals(0, count("""
            SELECT COUNT(*) FROM cucop_generic_item g
            WHERE NOT EXISTS (SELECT 1 FROM cucop_specific_item s WHERE s.generic_item_id = g.id)"""));
        assertEquals(0, count("""
            SELECT COUNT(*) FROM cucop_concept c
            WHERE NOT EXISTS (SELECT 1 FROM cucop_generic_item g WHERE g.concept_id = c.id)"""));
        assertEquals(0, count("""
            SELECT COUNT(*) FROM cucop_chapter ch
            WHERE NOT EXISTS (SELECT 1 FROM cucop_concept c WHERE c.chapter_id = ch.id)"""));
    }

    @Test
    void INV32_specificKeys_startWithTheirGenericPrefix() {
        assertEquals(0, count("""
            SELECT COUNT(*) FROM cucop_specific_item s JOIN cucop_generic_item g ON g.id = s.generic_item_id
            WHERE substring(s.key, 1, 3) <> substring(g.key, 1, 3)"""));
    }

    @Test
    void ADR0017_database_rejectsUnknownParent() {
        assertThrows(PersistenceException.class, () -> entityManager.createNativeQuery(
            "INSERT INTO cucop_concept (id, chapter_id, key, description, level) VALUES (999, 999, '9900', 'X', 2)")
            .executeUpdate());
    }

    @Test
    void ADR0017_database_rejectsDuplicateKeyWithinLevel() {
        assertThrows(PersistenceException.class, () -> entityManager.createNativeQuery(
            "INSERT INTO cucop_specific_item (id, generic_item_id, key, description, level) VALUES (999, 1, '21101', 'X', 4)")
            .executeUpdate());
    }

    @Test
    void ADR0017_database_rejectsWrongLevel() {
        assertThrows(PersistenceException.class, () -> entityManager.createNativeQuery(
            "INSERT INTO cucop_chapter (id, key, description, level) VALUES (999, '9999', 'X', 2)")
            .executeUpdate());
    }

    @Test
    void ADR0017_schema_usesApprovedSqlTypesAndParentIndexes() {
        for (String table : List.of("cucop_chapter", "cucop_concept", "cucop_generic_item", "cucop_specific_item")) {
            assertEquals("character varying(50)", columnType(table, "key"));
            assertEquals("character varying(500)", columnType(table, "description"));
            assertEquals("integer", columnType(table, "level"));
        }
        assertEquals(3, count("""
            SELECT COUNT(*) FROM pg_indexes WHERE indexname IN ('cucop_concept_chapter_id_idx',
            'cucop_generic_item_concept_id_idx', 'cucop_specific_item_generic_item_id_idx')"""));
    }

    private String columnType(String table, String column) {
        return (String) entityManager.createNativeQuery(
                "SELECT format_type(atttypid, atttypmod) FROM pg_attribute "
                    + "WHERE attrelid = to_regclass(?1) AND attname = ?2")
            .setParameter(1, table).setParameter(2, column).getSingleResult();
    }
}
