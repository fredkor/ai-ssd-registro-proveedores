package mx.com.fredkorlabs.registration.domain;

import mx.com.fredkorlabs.registration.domain.exception.InvalidNoticeVersionException;
import mx.com.fredkorlabs.registration.domain.model.NoticeAcceptance;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class NoticeAcceptanceTest {

    private static final Instant ACCEPTED_AT = Instant.parse("2026-10-20T15:30:00Z");

    @Test
    void US03_CA003_noticeAcceptance_keepsVersionAndInstant() {
        NoticeAcceptance acceptance = new NoticeAcceptance("1.0", ACCEPTED_AT);
        assertEquals("1.0", acceptance.version());
        assertEquals(ACCEPTED_AT, acceptance.acceptedAt());
    }

    @Test
    void US03_CA007_noticeAcceptance_withVersionOtherThanFixedNotice_isRejected() {
        assertThrows(InvalidNoticeVersionException.class, () -> new NoticeAcceptance("2.0", ACCEPTED_AT));
    }

    @Test
    void US03_CA007_noticeAcceptance_withoutVersion_isRejected() {
        assertThrows(InvalidNoticeVersionException.class, () -> new NoticeAcceptance(null, ACCEPTED_AT));
    }

    @Test
    void US03_CA003_noticeAcceptance_withoutInstant_isRejected() {
        assertThrows(IllegalArgumentException.class, () -> new NoticeAcceptance("1.0", null));
    }
}
