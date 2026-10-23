package mx.com.fredkorlabs.registration.adapter.in.transaction;

import mx.com.fredkorlabs.registration.application.port.in.SaveCucopParticipationCommand;
import mx.com.fredkorlabs.registration.application.port.in.SaveCucopParticipationUseCase;
import mx.com.fredkorlabs.registration.application.port.in.SectionSaveResult;
import org.springframework.transaction.annotation.Transactional;

public class TransactionalSaveCucopParticipationUseCase implements SaveCucopParticipationUseCase {

    private final SaveCucopParticipationUseCase delegate;

    public TransactionalSaveCucopParticipationUseCase(SaveCucopParticipationUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional
    public SectionSaveResult execute(SaveCucopParticipationCommand command) {
        return delegate.execute(command);
    }
}
