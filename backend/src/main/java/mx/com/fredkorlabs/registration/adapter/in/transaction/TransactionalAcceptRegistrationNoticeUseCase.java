package mx.com.fredkorlabs.registration.adapter.in.transaction;

import mx.com.fredkorlabs.registration.application.port.in.AcceptRegistrationNoticeCommand;
import mx.com.fredkorlabs.registration.application.port.in.AcceptRegistrationNoticeResult;
import mx.com.fredkorlabs.registration.application.port.in.AcceptRegistrationNoticeUseCase;
import org.springframework.transaction.annotation.Transactional;

public class TransactionalAcceptRegistrationNoticeUseCase implements AcceptRegistrationNoticeUseCase {

    private final AcceptRegistrationNoticeUseCase delegate;

    public TransactionalAcceptRegistrationNoticeUseCase(AcceptRegistrationNoticeUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional
    public AcceptRegistrationNoticeResult execute(AcceptRegistrationNoticeCommand command) {
        return delegate.execute(command);
    }
}
