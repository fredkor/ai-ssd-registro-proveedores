package mx.com.fredkorlabs.registration.adapter.out.catalog;

import jakarta.persistence.EntityManager;
import mx.com.fredkorlabs.registration.testsupport.PostgresTestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Supplier status and type: approved catalogs with no operation in this stage, so only the migration is checked. */
@DataJpaTest
@Import(PostgresTestcontainersConfiguration.class)
class SupplierCatalogsMigrationTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    void ADR0017_supplierStatusMigration_loadsCatalogWithOriginalIds() {
        assertEquals(List.of("1 POTENCIAL LICITANTE", "2 LICITANTE", "3 PROVEEDOR Y/O CONTRATISTA"),
            rows("supplier_status"));
    }

    @Test
    void ADR0017_supplierTypeMigration_loadsCatalogWithOriginalIds() {
        assertEquals(List.of("1 RECURSOS NATURALES", "2 CONSTRUCCIÓN", "3 FABRICACIÓN, PRODUCCIÓN O ELABORACIÓN",
                "4 COMERCIO AL POR MAYOR", "5 TRANSPORTE", "6 COMUNICACIÓN", "7 SERVICIOS", "8 COMERCIO AL POR MENOR"),
            rows("supplier_type"));
    }

    @Test
    void ADR0017_supplierCatalogSchemas_useApprovedSqlTypes() {
        for (String table : List.of("supplier_status", "supplier_type")) {
            assertEquals("integer", columnType(table, "id"), table);
            assertEquals("character varying(500)", columnType(table, "description"), table);
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> rows(String table) {
        return entityManager.createNativeQuery("SELECT id || ' ' || description FROM " + table + " ORDER BY id")
            .getResultList();
    }

    private String columnType(String table, String column) {
        return (String) entityManager.createNativeQuery(
                "SELECT format_type(atttypid, atttypmod) FROM pg_attribute "
                    + "WHERE attrelid = to_regclass(?1) AND attname = ?2")
            .setParameter(1, table).setParameter(2, column).getSingleResult();
    }
}
