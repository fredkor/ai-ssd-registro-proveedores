package mx.com.fredkorlabs.registration.adapter.in.transaction;

import mx.com.fredkorlabs.registration.application.port.in.SaveGeneralDataCommand;
import mx.com.fredkorlabs.registration.application.port.in.SaveGeneralDataUseCase;
import mx.com.fredkorlabs.registration.application.port.in.SectionSaveResult;
import org.springframework.transaction.annotation.Transactional;

public class TransactionalSaveGeneralDataUseCase implements SaveGeneralDataUseCase {

    private final SaveGeneralDataUseCase delegate;

    public TransactionalSaveGeneralDataUseCase(SaveGeneralDataUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional
    public SectionSaveResult execute(SaveGeneralDataCommand command) {
        return delegate.execute(command);
    }
}
