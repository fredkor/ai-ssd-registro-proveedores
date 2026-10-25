package mx.com.fredkorlabs.registration.adapter.out.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import mx.com.fredkorlabs.registration.application.port.out.NoticeAcceptanceReference;
import mx.com.fredkorlabs.registration.domain.model.NoticeAcceptance;
import mx.com.fredkorlabs.registration.testsupport.PostgresTestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.Instant;
import java.util.UUID;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({NoticeAcceptancePersistenceAdapter.class, PostgresTestcontainersConfiguration.class})
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
        long before = repository.count();

        NoticeAcceptanceReference first = adapter.save(new NoticeAcceptance("1.0", ACCEPTED_AT));
        NoticeAcceptanceReference second = adapter.save(new NoticeAcceptance("1.0", ACCEPTED_AT));

        assertNotEquals(first, second);
        assertEquals(before + 2, repository.count());
    }

    @Test
    void ADR0017_database_rejectsVersionOtherThanFixedNotice() {
        assertThrows(PersistenceException.class, () -> entityManager.createNativeQuery(
                "INSERT INTO notice_acceptance (id, version, accepted_at) VALUES (gen_random_uuid(), '2.0', CURRENT_TIMESTAMP)")
            .executeUpdate());
    }

    @Test
    void ADR0017_noticeAcceptanceSchema_usesApprovedSqlTypes() {
        assertEquals("uuid", columnType("id"));
        assertEquals("character varying(3)", columnType("version"));
        assertEquals("timestamp(6) with time zone", columnType("accepted_at"));
    }

    private String columnType(String column) {
        return (String) entityManager.createNativeQuery(
                "SELECT format_type(atttypid, atttypmod) FROM pg_attribute "
                    + "WHERE attrelid = 'notice_acceptance'::regclass AND attname = ?1")
            .setParameter(1, column)
            .getSingleResult();
    }
}
