package mx.com.fredkorlabs.registration.adapter.in.transaction;

import mx.com.fredkorlabs.registration.application.port.in.IdentifyApplicantCommand;
import mx.com.fredkorlabs.registration.application.port.in.IdentifyApplicantResult;
import mx.com.fredkorlabs.registration.application.port.in.IdentifyApplicantUseCase;
import org.springframework.transaction.annotation.Transactional;

public class TransactionalIdentifyApplicantUseCase implements IdentifyApplicantUseCase {

    private final IdentifyApplicantUseCase delegate;

    public TransactionalIdentifyApplicantUseCase(IdentifyApplicantUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional
    public IdentifyApplicantResult execute(IdentifyApplicantCommand command) {
        return delegate.execute(command);
    }
}
