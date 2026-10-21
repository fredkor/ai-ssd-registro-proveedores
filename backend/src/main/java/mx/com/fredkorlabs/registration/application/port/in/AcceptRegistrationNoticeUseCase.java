package mx.com.fredkorlabs.registration.application.port.in;

public interface AcceptRegistrationNoticeUseCase {
    AcceptRegistrationNoticeResult execute(AcceptRegistrationNoticeCommand command);
}
