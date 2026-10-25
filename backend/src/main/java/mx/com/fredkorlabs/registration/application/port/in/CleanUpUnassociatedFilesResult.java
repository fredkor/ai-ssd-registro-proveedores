package mx.com.fredkorlabs.registration.application.port.in;

import java.util.List;
import java.util.UUID;

/**
 * Outcome of a daily cleanup run: files removed, files whose content could not be deleted (their metadata is
 * kept for the next run) and files still referenced although marked as not associated (ADR-0010).
 */
public record CleanUpUnassociatedFilesResult(int removed, List<FileIssue> failures, List<FileIssue> referencedAnomalies) {

    public CleanUpUnassociatedFilesResult {
        failures = List.copyOf(failures);
        referencedAnomalies = List.copyOf(referencedAnomalies);
    }

    public record FileIssue(UUID fileId, String relativePath, String cause) {}
}
