package mx.com.fredkorlabs.registration.application.port.in;

public interface UploadFiscalDocumentUseCase {
    UploadFiscalDocumentResult execute(UploadFiscalDocumentCommand command);
}
