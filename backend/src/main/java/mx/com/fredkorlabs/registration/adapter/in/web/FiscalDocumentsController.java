package mx.com.fredkorlabs.registration.adapter.in.web;

import mx.com.fredkorlabs.registration.adapter.in.web.generated.FiscalDocumentsApi;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.FiscalDocumentResponse;
import mx.com.fredkorlabs.registration.application.port.in.UploadFiscalDocumentCommand;
import mx.com.fredkorlabs.registration.application.port.in.UploadFiscalDocumentResult;
import mx.com.fredkorlabs.registration.application.port.in.UploadFiscalDocumentUseCase;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;

@RestController
public class FiscalDocumentsController implements FiscalDocumentsApi {

    private static final Set<String> UPLOAD_PARTS = Set.of("file");

    private final UploadFiscalDocumentUseCase uploadFiscalDocument;

    public FiscalDocumentsController(UploadFiscalDocumentUseCase uploadFiscalDocument) {
        this.uploadFiscalDocument = uploadFiscalDocument;
    }

    @Override
    public ResponseEntity<FiscalDocumentResponse> uploadFiscalDocument(UUID registrationId, MultipartFile file) {
        MultipartParts.requireOnly(UPLOAD_PARTS);
        UploadFiscalDocumentResult result = uploadFiscalDocument.execute(new UploadFiscalDocumentCommand(
            registrationId, RegistrationTokenPrincipal.currentTokenId(), file.getOriginalFilename(), bytesOf(file)));
        return ResponseEntity.ok(new FiscalDocumentResponse(result.id(), result.originalName(),
            FiscalDocumentResponse.ExtensionEnum.fromValue(result.extension()), result.sizeBytes(),
            result.uploadedAt().atOffset(ZoneOffset.UTC)));
    }

    @Override
    public ResponseEntity<Resource> downloadFiscalDocument(UUID registrationId) {
        throw new UnsupportedOperationException("Not implemented in this iteration");
    }

    @Override
    public ResponseEntity<Void> deleteFiscalDocument(UUID registrationId) {
        throw new UnsupportedOperationException("Not implemented in this iteration");
    }

    private static byte[] bytesOf(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Uploaded document could not be read", e);
        }
    }
}
