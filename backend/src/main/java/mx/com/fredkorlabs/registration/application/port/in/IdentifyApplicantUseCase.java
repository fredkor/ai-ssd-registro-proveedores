package mx.com.fredkorlabs.registration.application.port.in;

public interface IdentifyApplicantUseCase {
    IdentifyApplicantResult execute(IdentifyApplicantCommand command);
}
