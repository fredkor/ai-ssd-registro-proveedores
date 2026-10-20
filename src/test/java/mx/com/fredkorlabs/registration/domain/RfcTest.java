package mx.com.fredkorlabs.registration.domain;

import mx.com.fredkorlabs.registration.domain.exception.InvalidRfcException;
import mx.com.fredkorlabs.registration.domain.model.Rfc;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RfcTest {

    @Test
    void US01_CA011_rfcWith12Characters_isAccepted() {
        Rfc rfc = new Rfc("XAXX01010100");
        assertEquals("XAXX01010100", rfc.value());
    }

    @Test
    void US01_CA011_rfcWith13Characters_isAccepted() {
        Rfc rfc = new Rfc("XAXX010101000");
        assertEquals("XAXX010101000", rfc.value());
    }

    @Test
    void US01_CA010_rfcWith11Characters_isRejected() {
        assertThrows(InvalidRfcException.class, () -> new Rfc("XAXX0101010"));
    }

    @Test
    void US01_CA010_rfcWith14Characters_isRejected() {
        assertThrows(InvalidRfcException.class, () -> new Rfc("XAXX0101010001"));
    }

    @Test
    void US01_CA010_nullRfc_isRejected() {
        assertThrows(InvalidRfcException.class, () -> new Rfc(null));
    }
}
