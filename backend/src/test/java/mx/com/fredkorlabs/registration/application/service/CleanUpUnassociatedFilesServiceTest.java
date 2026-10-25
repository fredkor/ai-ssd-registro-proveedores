package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.CleanUpUnassociatedFilesResult;
import mx.com.fredkorlabs.registration.application.port.out.FileMetadataPort;
import mx.com.fredkorlabs.registration.application.port.out.FileStoragePort;
import mx.com.fredkorlabs.registration.application.port.out.StoredFileMetadata;
import mx.com.fredkorlabs.registration.domain.model.SupplierRegistrationId;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** ADR-0010 daily cleanup with a fixed clock: more than 24 hours, exclusions and retries. */
class CleanUpUnassociatedFilesServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-25T08:00:00Z");
    private static final SupplierRegistrationId OWNER = new SupplierRegistrationId(UUID.randomUUID());

    private final Map<UUID, StoredFileMetadata> metadataRows = new LinkedHashMap<>();
    private final Set<UUID> referenced = new HashSet<>();
    private final Set<String> content = new HashSet<>();
    private final Set<String> undeletable = new HashSet<>();
    private final List<Instant> lockCutoffs = new ArrayList<>();

    private final FileMetadataPort metadata = new FileMetadataPort() {
        @Override
        public void register(StoredFileMetadata file) { throw new UnsupportedOperationException("Not used by this use case"); }

        @Override
        public Optional<StoredFileMetadata> find(UUID fileId) {
            throw new UnsupportedOperationException("Not used by this use case");
        }

        @Override
        public void markNotAssociated(UUID fileId) {
            throw new UnsupportedOperationException("Not used by this use case");
        }

        @Override
        public List<StoredFileMetadata> lockNotAssociatedUploadedBefore(Instant instant) {
            lockCutoffs.add(instant);
            return metadataRows.values().stream()
                .filter(file -> !file.associated() && file.uploadedAt().isBefore(instant))
                .toList();
        }

        @Override
        public boolean isReferenced(UUID fileId) { return referenced.contains(fileId); }

        @Override
        public void delete(UUID fileId) { metadataRows.remove(fileId); }
    };
    private final FileStoragePort storage = new FileStoragePort() {
        @Override
        public void store(String relativePath, byte[] bytes) { throw new UnsupportedOperationException("Not used"); }

        @Override
        public byte[] read(String relativePath) { throw new UnsupportedOperationException("Not used"); }

        @Override
        public boolean delete(String relativePath) {
            if (undeletable.contains(relativePath)) {
                throw new UncheckedIOException("Document content could not be deleted", new IOException("busy"));
            }
            return content.remove(relativePath);
        }
    };
    private final CleanUpUnassociatedFilesService service =
        new CleanUpUnassociatedFilesService(metadata, storage, Clock.fixed(NOW, ZoneOffset.UTC));

    private StoredFileMetadata file(Duration age, boolean associated, boolean withContent) {
        UUID id = UUID.randomUUID();
        String path = "2026/10/23/AAA010101AAA/" + id + ".pdf";
        StoredFileMetadata file = new StoredFileMetadata(id, OWNER, "cedula.pdf", path, "pdf", "application/pdf", 14,
            NOW.minus(age), associated);
        metadataRows.put(id, file);
        if (withContent) content.add(path);
        return file;
    }

    @Test
    void ADR0010_notAssociatedForMoreThan24Hours_isDeletedContentFirstThenMetadata() {
        StoredFileMetadata old = file(Duration.ofHours(24).plusSeconds(1), false, true);

        CleanUpUnassociatedFilesResult result = service.execute();

        assertEquals(1, result.removed());
        assertFalse(content.contains(old.relativePath()));
        assertFalse(metadataRows.containsKey(old.id()));
        assertEquals(List.of(NOW.minus(Duration.ofHours(24))), lockCutoffs);
    }

    @Test
    void ADR0010_exactly24HoursOrLess_isKept() {
        StoredFileMetadata exactly = file(Duration.ofHours(24), false, true);
        StoredFileMetadata recent = file(Duration.ofHours(1), false, true);

        CleanUpUnassociatedFilesResult result = service.execute();

        assertEquals(0, result.removed());
        assertTrue(metadataRows.containsKey(exactly.id()));
        assertTrue(metadataRows.containsKey(recent.id()));
        assertTrue(content.contains(exactly.relativePath()));
    }

    @Test
    void ADR0010_associatedFile_isNeverACandidate() {
        StoredFileMetadata associated = file(Duration.ofDays(30), true, true);

        assertEquals(0, service.execute().removed());

        assertTrue(metadataRows.containsKey(associated.id()));
        assertTrue(content.contains(associated.relativePath()));
    }

    @Test
    void ADR0010_stillReferencedFile_isKeptAndReportedAsAnomaly() {
        StoredFileMetadata incoherent = file(Duration.ofDays(2), false, true);
        referenced.add(incoherent.id());

        CleanUpUnassociatedFilesResult result = service.execute();

        assertEquals(0, result.removed());
        assertEquals(List.of(incoherent.id()), result.referencedAnomalies().stream()
            .map(CleanUpUnassociatedFilesResult.FileIssue::fileId).toList());
        assertTrue(metadataRows.containsKey(incoherent.id()));
        assertTrue(content.contains(incoherent.relativePath()));
    }

    @Test
    void ADR0010_contentDeletionFailure_keepsItsMetadataAndContinuesWithTheNext() {
        StoredFileMetadata failing = file(Duration.ofDays(3), false, true);
        StoredFileMetadata next = file(Duration.ofDays(2), false, true);
        undeletable.add(failing.relativePath());

        CleanUpUnassociatedFilesResult result = service.execute();

        assertEquals(1, result.removed());
        assertEquals(List.of(failing.id()), result.failures().stream()
            .map(CleanUpUnassociatedFilesResult.FileIssue::fileId).toList());
        assertEquals(failing.relativePath(), result.failures().getFirst().relativePath());
        assertTrue(metadataRows.containsKey(failing.id()));
        assertFalse(metadataRows.containsKey(next.id()));
    }

    @Test
    void ADR0010_contentAlreadyMissingAfterAnInterruptedRun_onlyRemovesTheMetadata() {
        StoredFileMetadata interrupted = file(Duration.ofDays(2), false, false);

        CleanUpUnassociatedFilesResult result = service.execute();

        assertEquals(1, result.removed());
        assertTrue(result.failures().isEmpty());
        assertFalse(metadataRows.containsKey(interrupted.id()));
    }
}
