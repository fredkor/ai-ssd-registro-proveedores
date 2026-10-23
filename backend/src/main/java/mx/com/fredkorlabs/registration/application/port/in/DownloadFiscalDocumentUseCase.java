package mx.com.fredkorlabs.registration.application.port.in;

public interface DownloadFiscalDocumentUseCase {
    DownloadFiscalDocumentResult execute(DownloadFiscalDocumentQuery query);
}
