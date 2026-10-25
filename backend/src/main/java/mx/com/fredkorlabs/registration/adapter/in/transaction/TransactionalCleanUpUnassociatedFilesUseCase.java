package mx.com.fredkorlabs.registration.adapter.in.transaction;

import mx.com.fredkorlabs.registration.application.port.in.CleanUpUnassociatedFilesResult;
import mx.com.fredkorlabs.registration.application.port.in.CleanUpUnassociatedFilesUseCase;
import org.springframework.transaction.annotation.Transactional;

public class TransactionalCleanUpUnassociatedFilesUseCase implements CleanUpUnassociatedFilesUseCase {

    private final CleanUpUnassociatedFilesUseCase delegate;

    public TransactionalCleanUpUnassociatedFilesUseCase(CleanUpUnassociatedFilesUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional
    public CleanUpUnassociatedFilesResult execute() {
        return delegate.execute();
    }
}
