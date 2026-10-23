package mx.com.fredkorlabs.registration.adapter.in.transaction;

import mx.com.fredkorlabs.registration.application.port.in.DeleteFiscalDocumentCommand;
import mx.com.fredkorlabs.registration.application.port.in.DeleteFiscalDocumentUseCase;
import org.springframework.transaction.annotation.Transactional;

public class TransactionalDeleteFiscalDocumentUseCase implements DeleteFiscalDocumentUseCase {

    private final DeleteFiscalDocumentUseCase delegate;

    public TransactionalDeleteFiscalDocumentUseCase(DeleteFiscalDocumentUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional
    public void execute(DeleteFiscalDocumentCommand command) {
        delegate.execute(command);
    }
}
