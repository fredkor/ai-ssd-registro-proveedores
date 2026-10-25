package mx.com.fredkorlabs.registration.application.port.in;

public interface SaveCucopParticipationUseCase {
    SectionSaveResult execute(SaveCucopParticipationCommand command);
}
