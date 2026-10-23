package mx.com.fredkorlabs.registration.domain;

import mx.com.fredkorlabs.registration.domain.exception.DocumentFormatNotSupportedException;
import mx.com.fredkorlabs.registration.domain.exception.DocumentTooLargeException;
import mx.com.fredkorlabs.registration.domain.model.FiscalDocumentFormat;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class FiscalDocumentFormatTest {

    private static final byte[] PDF = "%PDF-1.7\n%%EOF".getBytes(StandardCharsets.US_ASCII);

    @Test
    void US10_CA001_pdfWithSignature_isAccepted() {
        assertDoesNotThrow(() -> FiscalDocumentFormat.validate("cedula.pdf", PDF.length, PDF));
    }

    @Test
    void US10_CA008_exactMaximumSize_isAcceptedAndOneMoreByteIsTooLarge() {
        assertDoesNotThrow(() -> FiscalDocumentFormat.validate("cedula.pdf", 5_242_880L, PDF));
        assertThrows(DocumentTooLargeException.class,
            () -> FiscalDocumentFormat.validate("cedula.pdf", 5_242_881L, PDF));
    }

    @Test
    void US10_CA008_extension_isComparedIgnoringCase() {
        assertDoesNotThrow(() -> FiscalDocumentFormat.validate("CEDULA.PDF", PDF.length, PDF));
        assertDoesNotThrow(() -> FiscalDocumentFormat.validate("cedula.Pdf", PDF.length, PDF));
    }

    @Test
    void US10_CA002_otherExtension_isNotSupported() {
        assertThrows(DocumentFormatNotSupportedException.class,
            () -> FiscalDocumentFormat.validate("cedula.txt", PDF.length, PDF));
        assertThrows(DocumentFormatNotSupportedException.class,
            () -> FiscalDocumentFormat.validate("cedula", PDF.length, PDF));
        assertThrows(DocumentFormatNotSupportedException.class,
            () -> FiscalDocumentFormat.validate(null, PDF.length, PDF));
    }

    @Test
    void US10_CA009_contentWithoutPdfSignature_isNotSupported() {
        byte[] text = "plain text".getBytes(StandardCharsets.US_ASCII);
        assertThrows(DocumentFormatNotSupportedException.class,
            () -> FiscalDocumentFormat.validate("cedula.pdf", text.length, text));
        assertThrows(DocumentFormatNotSupportedException.class,
            () -> FiscalDocumentFormat.validate("cedula.pdf", 0, new byte[0]));
        byte[] shifted = " %PDF-1.7".getBytes(StandardCharsets.US_ASCII);
        assertThrows(DocumentFormatNotSupportedException.class,
            () -> FiscalDocumentFormat.validate("cedula.pdf", shifted.length, shifted));
    }

    @Test
    void US10_CA002_tooLarge_isReportedBeforeFormat() {
        assertThrows(DocumentTooLargeException.class,
            () -> FiscalDocumentFormat.validate("cedula.txt", 5_242_881L, new byte[0]));
    }
}
