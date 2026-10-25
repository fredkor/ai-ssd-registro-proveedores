package mx.com.fredkorlabs.registration.adapter.out.document;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class VolumeFileStorageAdapterTest {

    private static final byte[] CONTENT = "%PDF-1.7".getBytes(StandardCharsets.US_ASCII);
    private static final String PATH = "2026/10/23/AAA010101AAA/8bd04b95-88ae-4dd8-bd19-27229040225e.pdf";

    @TempDir
    Path root;

    @AfterEach
    void clearSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private void complete(int status) {
        for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
            synchronization.afterCompletion(status);
        }
    }

    @Test
    void US10_CA001_store_writesTheContentUnderTheRootPath() throws Exception {
        new VolumeFileStorageAdapter(root).store(PATH, CONTENT);

        assertArrayEquals(CONTENT, Files.readAllBytes(root.resolve(PATH)));
    }

    @Test
    void ADR0010_rolledBackTransaction_removesTheWrittenContent() {
        TransactionSynchronizationManager.initSynchronization();
        new VolumeFileStorageAdapter(root).store(PATH, CONTENT);

        complete(TransactionSynchronization.STATUS_ROLLED_BACK);

        assertFalse(Files.exists(root.resolve(PATH)));
    }

    @Test
    void ADR0010_committedTransaction_keepsTheContent() {
        TransactionSynchronizationManager.initSynchronization();
        new VolumeFileStorageAdapter(root).store(PATH, CONTENT);

        complete(TransactionSynchronization.STATUS_COMMITTED);

        assertTrue(Files.exists(root.resolve(PATH)));
    }

    @Test
    void ADR0010_existingFile_isNeverOverwritten() {
        VolumeFileStorageAdapter adapter = new VolumeFileStorageAdapter(root);
        adapter.store(PATH, CONTENT);

        assertThrows(RuntimeException.class, () -> adapter.store(PATH, "other".getBytes(StandardCharsets.US_ASCII)));
    }

    @Test
    void ADR0010_pathOutsideTheRoot_isRejected() {
        VolumeFileStorageAdapter adapter = new VolumeFileStorageAdapter(root);

        assertThrows(IllegalArgumentException.class, () -> adapter.store("../escape.pdf", CONTENT));
        assertThrows(IllegalArgumentException.class, () -> adapter.store("", CONTENT));
    }

    @Test
    void US10_CA005_read_returnsTheStoredContent() {
        VolumeFileStorageAdapter adapter = new VolumeFileStorageAdapter(root);
        adapter.store(PATH, CONTENT);

        assertArrayEquals(CONTENT, adapter.read(PATH));
    }

    @Test
    void ADR0010_missingContent_isATechnicalFailure() {
        assertThrows(RuntimeException.class, () -> new VolumeFileStorageAdapter(root).read(PATH));
    }

    @Test
    void ADR0010_readOutsideTheRoot_isRejected() {
        assertThrows(IllegalArgumentException.class, () -> new VolumeFileStorageAdapter(root).read("../secret.pdf"));
    }

    @Test
    void ADR0010_delete_removesExistingContentAndReportsMissingContent() throws Exception {
        VolumeFileStorageAdapter storage = new VolumeFileStorageAdapter(root);
        storage.store(PATH, CONTENT);

        assertTrue(storage.delete(PATH));
        assertFalse(Files.exists(root.resolve(PATH)));
        assertFalse(storage.delete(PATH));
    }

    @Test
    void ADR0010_deleteOutsideTheRoot_isRejected() {
        assertThrows(IllegalArgumentException.class, () -> new VolumeFileStorageAdapter(root).delete("../outside.pdf"));
    }
}
