package mx.com.fredkorlabs.registration.adapter.out.document;

import mx.com.fredkorlabs.registration.application.port.out.FileStoragePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Document content in the persistent volume (ADR-0010). Paths come from the application, never from the
 * client, and must stay under the root.
 */
public class VolumeFileStorageAdapter implements FileStoragePort {

    private static final Logger log = LoggerFactory.getLogger(VolumeFileStorageAdapter.class);

    private final Path root;

    public VolumeFileStorageAdapter(Path root) {
        this.root = root.toAbsolutePath().normalize();
    }

    @Override
    public void store(String relativePath, byte[] content) {
        Path target = resolve(relativePath);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, content, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new UncheckedIOException("Document content could not be stored", e);
        }
        // Metadata and association commit afterwards; if they do not, the written content is removed.
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status != STATUS_COMMITTED) {
                        discard(relativePath, target);
                    }
                }
            });
        }
    }

    @Override
    public byte[] read(String relativePath) {
        try {
            return Files.readAllBytes(resolve(relativePath));
        } catch (IOException e) {
            throw new UncheckedIOException("Document content could not be read", e);
        }
    }

    @Override
    public boolean delete(String relativePath) {
        try {
            return Files.deleteIfExists(resolve(relativePath));
        } catch (IOException e) {
            throw new UncheckedIOException("Document content could not be deleted", e);
        }
    }

    private static void discard(String relativePath, Path target) {
        try {
            Files.deleteIfExists(target);
        } catch (IOException | RuntimeException e) {
            // Left without metadata and outside the daily cleanup: reconcile manually (ADR-0010).
            log.error("Could not remove document content after a failed upload: {}", relativePath, e);
        }
    }

    private Path resolve(String relativePath) {
        Path target = root.resolve(relativePath).normalize();
        if (!target.startsWith(root) || target.equals(root)) {
            throw new IllegalArgumentException("Document path must stay under the volume root");
        }
        return target;
    }
}
