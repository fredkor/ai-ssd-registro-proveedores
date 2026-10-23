package mx.com.fredkorlabs.registration.domain.model;

import mx.com.fredkorlabs.registration.domain.exception.DocumentFormatNotSupportedException;
import mx.com.fredkorlabs.registration.domain.exception.DocumentTooLargeException;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;

/**
 * INV-27: the fiscal document is a single PDF of at most 5 MiB. The extension is compared ignoring case
 * and the content must start with the PDF signature; the declared MIME type is not enough (ADR-0010).
 */
public final class FiscalDocumentFormat {

    public static final long MAX_SIZE_BYTES = 5_242_880L;
    public static final String EXTENSION = "pdf";
    public static final String MEDIA_TYPE = "application/pdf";

    private static final byte[] PDF_SIGNATURE = "%PDF-".getBytes(StandardCharsets.US_ASCII);

    private FiscalDocumentFormat() {
    }

    /** Size first (413), then extension and content (415). */
    public static void validate(String originalName, long sizeBytes, byte[] content) {
        if (sizeBytes > MAX_SIZE_BYTES) {
            throw new DocumentTooLargeException("Document exceeds the maximum size of 5242880 bytes");
        }
        if (originalName == null || !originalName.toLowerCase(Locale.ROOT).endsWith("." + EXTENSION)) {
            throw new DocumentFormatNotSupportedException("Document must have the .pdf extension");
        }
        if (content == null || content.length < PDF_SIGNATURE.length
                || !Arrays.equals(content, 0, PDF_SIGNATURE.length, PDF_SIGNATURE, 0, PDF_SIGNATURE.length)) {
            throw new DocumentFormatNotSupportedException("Document content is not a PDF");
        }
    }
}
