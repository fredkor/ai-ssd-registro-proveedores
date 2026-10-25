package mx.com.fredkorlabs.registration.application.port.in;

/** Daily physical deletion of files not associated for more than 24 hours (ADR-0010); not an endpoint. */
public interface CleanUpUnassociatedFilesUseCase {
    CleanUpUnassociatedFilesResult execute();
}
