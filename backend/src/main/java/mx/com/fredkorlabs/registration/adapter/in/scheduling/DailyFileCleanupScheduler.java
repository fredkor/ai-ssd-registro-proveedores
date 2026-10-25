package mx.com.fredkorlabs.registration.adapter.in.scheduling;

import mx.com.fredkorlabs.registration.application.port.in.CleanUpUnassociatedFilesResult;
import mx.com.fredkorlabs.registration.application.port.in.CleanUpUnassociatedFilesUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Runs the daily file cleanup at 02:00 Mexico City time (ADR-0010, RNF-04) and logs its outcome (RNF-09). */
@Component
public class DailyFileCleanupScheduler {

    static final String CRON = "0 0 2 * * *";
    static final String ZONE = "America/Mexico_City";

    private static final Logger log = LoggerFactory.getLogger(DailyFileCleanupScheduler.class);

    private final CleanUpUnassociatedFilesUseCase cleanUpUnassociatedFiles;

    public DailyFileCleanupScheduler(CleanUpUnassociatedFilesUseCase cleanUpUnassociatedFiles) {
        this.cleanUpUnassociatedFiles = cleanUpUnassociatedFiles;
    }

    @Scheduled(cron = CRON, zone = ZONE)
    public void cleanUp() {
        CleanUpUnassociatedFilesResult result;
        try {
            result = cleanUpUnassociatedFiles.execute();
        } catch (RuntimeException e) {
            log.error("Daily file cleanup failed; the next run retries it", e);
            return;
        }
        log.info("Daily file cleanup removed {} files", result.removed());
        result.failures().forEach(failure -> log.error(
            "Could not delete file {} at {}; its metadata is kept for the next run: {}",
            failure.fileId(), failure.relativePath(), failure.cause()));
        result.referencedAnomalies().forEach(anomaly -> log.warn(
            "File {} at {} is marked as not associated but is still referenced; it was kept",
            anomaly.fileId(), anomaly.relativePath()));
    }
}
