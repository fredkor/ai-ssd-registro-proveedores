package mx.com.fredkorlabs.registration.adapter.in.web;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FiscalDocumentContentDispositionTest {

    @Test
    void US10_CA005_asciiName_isSentAsIs() {
        assertEquals("attachment; filename=\"cedula-fiscal.pdf\"; filename*=UTF-8''cedula-fiscal.pdf",
            FiscalDocumentsController.attachment("cedula-fiscal.pdf"));
    }

    @Test
    void US10_CA005_nonAsciiName_keepsTheExactNameInFilenameStar() {
        assertEquals("attachment; filename=\"C_dula Fiscal.pdf\"; filename*=UTF-8''C%C3%A9dula%20Fiscal.pdf",
            FiscalDocumentsController.attachment("Cédula Fiscal.pdf"));
    }

    @Test
    void ADR0019_quotesAndBackslashes_cannotBreakTheHeader() {
        assertEquals("attachment; filename=\"a_b_c.pdf\"; filename*=UTF-8''a%22b%5Cc.pdf",
            FiscalDocumentsController.attachment("a\"b\\c.pdf"));
    }
}
