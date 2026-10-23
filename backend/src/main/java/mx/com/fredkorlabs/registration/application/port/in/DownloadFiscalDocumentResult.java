package mx.com.fredkorlabs.registration.application.port.in;

/** Content of the associated fiscal document with its original name; never its physical location. */
public record DownloadFiscalDocumentResult(String originalName, String mediaType, byte[] content) {}
