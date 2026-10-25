package mx.com.fredkorlabs.registration.application.port.out;

/** File content in the documents volume, addressed by a path relative to its root (ADR-0010). */
public interface FileStoragePort {

    /**
     * Writes new content. When called inside a transaction that does not commit, the content is removed
     * again; a failed removal is logged for manual reconciliation (ADR-0010).
     */
    void store(String relativePath, byte[] content);

    /** Reads stored content; a missing file is a technical inconsistency, never an absent document. */
    byte[] read(String relativePath);

    /** Deletes the content; false when it no longer existed, e.g. after an interrupted cleanup (ADR-0010). */
    boolean delete(String relativePath);
}
