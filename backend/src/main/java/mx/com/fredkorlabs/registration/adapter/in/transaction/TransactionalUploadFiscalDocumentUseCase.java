package mx.com.fredkorlabs.registration.adapter.in.transaction;

import mx.com.fredkorlabs.registration.application.port.in.UploadFiscalDocumentCommand;
import mx.com.fredkorlabs.registration.application.port.in.UploadFiscalDocumentResult;
import mx.com.fredkorlabs.registration.application.port.in.UploadFiscalDocumentUseCase;
import org.springframework.transaction.annotation.Transactional;

public class TransactionalUploadFiscalDocumentUseCase implements UploadFiscalDocumentUseCase {

    private final UploadFiscalDocumentUseCase delegate;

    public TransactionalUploadFiscalDocumentUseCase(UploadFiscalDocumentUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional
    public UploadFiscalDocumentResult execute(UploadFiscalDocumentCommand command) {
        return delegate.execute(command);
    }
}
