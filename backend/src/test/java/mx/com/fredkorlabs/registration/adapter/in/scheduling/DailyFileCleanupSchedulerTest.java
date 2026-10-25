package mx.com.fredkorlabs.registration.adapter.in.scheduling;

import mx.com.fredkorlabs.registration.application.port.in.CleanUpUnassociatedFilesResult;
import mx.com.fredkorlabs.registration.application.port.in.CleanUpUnassociatedFilesResult.FileIssue;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class DailyFileCleanupSchedulerTest {

    @Test
    void ADR0010_runsDailyAt2InMexicoCity() throws Exception {
        Scheduled scheduled = DailyFileCleanupScheduler.class.getMethod("cleanUp").getAnnotation(Scheduled.class);

        assertEquals("0 0 2 * * *", scheduled.cron());
        assertEquals("America/Mexico_City", scheduled.zone());
    }

    @Test
    void ADR0010_cleanUp_invokesTheUseCaseOnce() {
        AtomicInteger runs = new AtomicInteger();
        FileIssue issue = new FileIssue(UUID.randomUUID(), "2026/10/23/AAA010101AAA/file.pdf", "busy");

        new DailyFileCleanupScheduler(() -> {
            runs.incrementAndGet();
            return new CleanUpUnassociatedFilesResult(1, List.of(issue), List.of(issue));
        }).cleanUp();

        assertEquals(1, runs.get());
    }

    @Test
    void ADR0010_failedRun_isLoggedWithoutStoppingTheScheduler() {
        DailyFileCleanupScheduler scheduler = new DailyFileCleanupScheduler(() -> {
            throw new IllegalStateException("database unavailable");
        });

        assertDoesNotThrow(scheduler::cleanUp);
    }
}
