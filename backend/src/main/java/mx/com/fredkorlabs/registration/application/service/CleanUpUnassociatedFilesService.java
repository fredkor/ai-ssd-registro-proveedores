package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.CleanUpUnassociatedFilesResult;
import mx.com.fredkorlabs.registration.application.port.in.CleanUpUnassociatedFilesResult.FileIssue;
import mx.com.fredkorlabs.registration.application.port.in.CleanUpUnassociatedFilesUseCase;
import mx.com.fredkorlabs.registration.application.port.out.FileMetadataPort;
import mx.com.fredkorlabs.registration.application.port.out.FileStoragePort;
import mx.com.fredkorlabs.registration.application.port.out.StoredFileMetadata;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class CleanUpUnassociatedFilesService implements CleanUpUnassociatedFilesUseCase {

    /** Files not associated are deleted only after strictly more than 24 hours since their upload (ADR-0010). */
    static final Duration RETENTION = Duration.ofHours(24);

    private final FileMetadataPort fileMetadata;
    private final FileStoragePort fileStorage;
    private final Clock clock;

    public CleanUpUnassociatedFilesService(FileMetadataPort fileMetadata, FileStoragePort fileStorage, Clock clock) {
        this.fileMetadata = fileMetadata;
        this.fileStorage = fileStorage;
        this.clock = clock;
    }

    @Override
    public CleanUpUnassociatedFilesResult execute() {
        Instant cutoff = clock.instant().minus(RETENTION);
        int removed = 0;
        List<FileIssue> failures = new ArrayList<>();
        List<FileIssue> anomalies = new ArrayList<>();
        for (StoredFileMetadata file : fileMetadata.lockNotAssociatedUploadedBefore(cutoff)) {
            // Checked again under the lock; never deduced from names or paths (ADR-0010).
            if (file.associated() || !file.uploadedAt().isBefore(cutoff)) {
                continue;
            }
            if (fileMetadata.isReferenced(file.id())) {
                anomalies.add(new FileIssue(file.id(), file.relativePath(), "Marked as not associated but still referenced"));
                continue;
            }
            try {
                // Content that no longer exists comes from an interrupted run: only the metadata is left.
                fileStorage.delete(file.relativePath());
            } catch (RuntimeException e) {
                failures.add(new FileIssue(file.id(), file.relativePath(), e.toString()));
                continue;
            }
            fileMetadata.delete(file.id());
            removed++;
        }
        return new CleanUpUnassociatedFilesResult(removed, failures, anomalies);
    }
}
