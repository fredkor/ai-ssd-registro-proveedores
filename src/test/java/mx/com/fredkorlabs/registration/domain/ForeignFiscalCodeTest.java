package mx.com.fredkorlabs.registration.domain;

import mx.com.fredkorlabs.registration.domain.exception.InvalidForeignFiscalCodeException;
import mx.com.fredkorlabs.registration.domain.model.ForeignFiscalCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ForeignFiscalCodeTest {

    @Test
    void US08_CA018_codeWith20Characters_isAccepted() {
        ForeignFiscalCode code = new ForeignFiscalCode("12345678901234567890");
        assertEquals(20, code.value().length());
    }

    @Test
    void US08_CA018_codeWith1Character_isAccepted() {
        ForeignFiscalCode code = new ForeignFiscalCode("A");
        assertEquals("A", code.value());
    }

    @Test
    void US08_CA018_codeWith21Characters_isRejected() {
        assertThrows(InvalidForeignFiscalCodeException.class,
            () -> new ForeignFiscalCode("123456789012345678901"));
    }
}
