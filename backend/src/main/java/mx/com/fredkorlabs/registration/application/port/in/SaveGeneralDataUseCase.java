package mx.com.fredkorlabs.registration.application.port.in;

public interface SaveGeneralDataUseCase {
    SectionSaveResult execute(SaveGeneralDataCommand command);
}
