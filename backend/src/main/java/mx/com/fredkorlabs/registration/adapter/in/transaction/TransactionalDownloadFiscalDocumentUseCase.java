package mx.com.fredkorlabs.registration.adapter.in.transaction;

import mx.com.fredkorlabs.registration.application.port.in.DownloadFiscalDocumentQuery;
import mx.com.fredkorlabs.registration.application.port.in.DownloadFiscalDocumentResult;
import mx.com.fredkorlabs.registration.application.port.in.DownloadFiscalDocumentUseCase;
import org.springframework.transaction.annotation.Transactional;

/** Read-only: the registration and its document are read consistently, without locking the row. */
public class TransactionalDownloadFiscalDocumentUseCase implements DownloadFiscalDocumentUseCase {

    private final DownloadFiscalDocumentUseCase delegate;

    public TransactionalDownloadFiscalDocumentUseCase(DownloadFiscalDocumentUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional(readOnly = true)
    public DownloadFiscalDocumentResult execute(DownloadFiscalDocumentQuery query) {
        return delegate.execute(query);
    }
}
