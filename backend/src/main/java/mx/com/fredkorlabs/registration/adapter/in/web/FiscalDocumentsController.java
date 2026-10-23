package mx.com.fredkorlabs.registration.adapter.in.web;

import mx.com.fredkorlabs.registration.adapter.in.web.generated.FiscalDocumentsApi;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.FiscalDocumentResponse;
import mx.com.fredkorlabs.registration.application.port.in.DeleteFiscalDocumentCommand;
import mx.com.fredkorlabs.registration.application.port.in.DeleteFiscalDocumentUseCase;
import mx.com.fredkorlabs.registration.application.port.in.DownloadFiscalDocumentQuery;
import mx.com.fredkorlabs.registration.application.port.in.DownloadFiscalDocumentResult;
import mx.com.fredkorlabs.registration.application.port.in.DownloadFiscalDocumentUseCase;
import mx.com.fredkorlabs.registration.application.port.in.UploadFiscalDocumentCommand;
import mx.com.fredkorlabs.registration.application.port.in.UploadFiscalDocumentResult;
import mx.com.fredkorlabs.registration.application.port.in.UploadFiscalDocumentUseCase;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;

@RestController
public class FiscalDocumentsController implements FiscalDocumentsApi {

    private static final Set<String> UPLOAD_PARTS = Set.of("file");

    private final UploadFiscalDocumentUseCase uploadFiscalDocument;
    private final DownloadFiscalDocumentUseCase downloadFiscalDocument;
    private final DeleteFiscalDocumentUseCase deleteFiscalDocument;

    public FiscalDocumentsController(UploadFiscalDocumentUseCase uploadFiscalDocument,
                                     DownloadFiscalDocumentUseCase downloadFiscalDocument,
                                     DeleteFiscalDocumentUseCase deleteFiscalDocument) {
        this.uploadFiscalDocument = uploadFiscalDocument;
        this.downloadFiscalDocument = downloadFiscalDocument;
        this.deleteFiscalDocument = deleteFiscalDocument;
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
        DownloadFiscalDocumentResult result = downloadFiscalDocument.execute(
            new DownloadFiscalDocumentQuery(registrationId, RegistrationTokenPrincipal.currentTokenId()));
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(result.mediaType()))
            .contentLength(result.content().length)
            .header(HttpHeaders.CONTENT_DISPOSITION, attachment(result.originalName()))
            .body(new ByteArrayResource(result.content()));
    }

    @Override
    public ResponseEntity<Void> deleteFiscalDocument(UUID registrationId) {
        deleteFiscalDocument.execute(
            new DeleteFiscalDocumentCommand(registrationId, RegistrationTokenPrincipal.currentTokenId()));
        return ResponseEntity.noContent().build();
    }

    /**
     * RFC 6266: an ASCII {@code filename} fallback (other characters become {@code _}) and the exact name in
     * {@code filename*} as UTF-8 (RFC 5987). Only the original name is sent, never the volume path.
     */
    static String attachment(String originalName) {
        StringBuilder fallback = new StringBuilder();
        originalName.codePoints().forEach(c ->
            fallback.appendCodePoint(c < 0x20 || c > 0x7e || c == '"' || c == '\\' ? '_' : c));
        StringBuilder encoded = new StringBuilder();
        for (byte b : originalName.getBytes(StandardCharsets.UTF_8)) {
            int c = b & 0xff;
            if (Character.isLetterOrDigit(c) && c < 0x80 || "!#$&+-.^_`|~".indexOf(c) >= 0) {
                encoded.append((char) c);
            } else {
                encoded.append('%').append(String.format("%02X", c));
            }
        }
        return "attachment; filename=\"" + fallback + "\"; filename*=UTF-8''" + encoded;
    }

    private static byte[] bytesOf(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Uploaded document could not be read", e);
        }
    }
}
