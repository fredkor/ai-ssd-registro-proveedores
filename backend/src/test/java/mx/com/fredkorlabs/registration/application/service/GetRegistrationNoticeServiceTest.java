package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.GetRegistrationNoticeResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GetRegistrationNoticeServiceTest {

    private final GetRegistrationNoticeService service = new GetRegistrationNoticeService();

    @Test
    void US03_CA006_getRegistrationNotice_returnsVersion1_0() {
        GetRegistrationNoticeResult result = service.execute();
        assertEquals("1.0", result.version());
    }

    @Test
    void US03_CA006_getRegistrationNotice_returnsExpectedNoticeText() {
        GetRegistrationNoticeResult result = service.execute();
        assertFalse(result.text().isBlank());
        assertTrue(result.text().startsWith("Al registrarse en el Sistema de Compras de"));
        assertTrue(result.text().contains("Fredkor Labs"));
    }
}
