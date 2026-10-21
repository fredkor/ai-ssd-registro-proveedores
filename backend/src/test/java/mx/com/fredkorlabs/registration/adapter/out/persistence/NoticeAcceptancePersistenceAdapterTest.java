package mx.com.fredkorlabs.registration.adapter.out.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import mx.com.fredkorlabs.registration.application.port.out.NoticeAcceptanceReference;
import mx.com.fredkorlabs.registration.domain.model.NoticeAcceptance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(NoticeAcceptancePersistenceAdapter.class)
class NoticeAcceptancePersistenceAdapterTest {

    private static final Instant ACCEPTED_AT = Instant.parse("2026-10-20T15:30:00.123456Z");

    @Autowired
    private NoticeAcceptancePersistenceAdapter adapter;

    @Autowired
    private NoticeAcceptanceJpaRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void US03_CA003_save_storesVersionAndAcceptedAt() {
        NoticeAcceptanceReference reference = adapter.save(new NoticeAcceptance("1.0", ACCEPTED_AT));
        entityManager.clear();

        NoticeAcceptanceJpaEntity stored = repository.findById(UUID.fromString(reference.value())).orElseThrow();
        assertEquals("1.0", stored.getVersion());
        assertEquals(ACCEPTED_AT, stored.getAcceptedAt());
    }

    @Test
    void US03_CA007_saveTwice_generatesDistinctReferences() {
        NoticeAcceptanceReference first = adapter.save(new NoticeAcceptance("1.0", ACCEPTED_AT));
        NoticeAcceptanceReference second = adapter.save(new NoticeAcceptance("1.0", ACCEPTED_AT));

        assertNotEquals(first, second);
        assertEquals(2, repository.count());
    }

    @Test
    void US03_CA007_database_rejectsVersionOtherThanFixedNotice() {
        assertThrows(PersistenceException.class, () -> entityManager.createNativeQuery(
                "INSERT INTO notice_acceptance (id, version, accepted_at) VALUES (RANDOM_UUID(), '2.0', CURRENT_TIMESTAMP)")
            .executeUpdate());
    }

    @Test
    void US03_CA003_schema_usesApprovedSqlTypes() {
        assertEquals("UUID", columnType("ID"));
        assertEquals("CHARACTER VARYING", columnType("VERSION"));
        assertEquals("TIMESTAMP WITH TIME ZONE", columnType("ACCEPTED_AT"));
    }

    private String columnType(String column) {
        return (String) entityManager.createNativeQuery(
                "SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'NOTICE_ACCEPTANCE' AND COLUMN_NAME = ?1")
            .setParameter(1, column)
            .getSingleResult();
    }
}
