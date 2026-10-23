package mx.com.fredkorlabs.registration.application.port.out;

/** General file metadata (ADR-0010); each section decides formats, quantity and associations. */
public interface FileMetadataPort {

    void register(StoredFileMetadata metadata);
}
