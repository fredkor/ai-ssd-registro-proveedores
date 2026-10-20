package mx.com.fredkorlabs.registration.domain;

import mx.com.fredkorlabs.registration.domain.exception.InvalidWebsiteException;
import mx.com.fredkorlabs.registration.domain.model.Website;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WebsiteTest {

    @Test
    void US08_CA013_httpsUrlWithHost_isAccepted() {
        Website website = new Website("https://empresa.com");
        assertEquals("https://empresa.com", website.value());
    }

    @Test
    void US08_CA013_httpUrlWithHost_isAccepted() {
        Website website = new Website("http://empresa.com");
        assertEquals("http://empresa.com", website.value());
    }

    @Test
    void US08_CA013_urlWithoutProtocol_isRejected() {
        assertThrows(InvalidWebsiteException.class, () -> new Website("empresa.com"));
    }

    @Test
    void US08_CA013_httpsWithoutHost_isRejected() {
        assertThrows(InvalidWebsiteException.class, () -> new Website("https://"));
    }

    @Test
    void US08_CA013_ftpUrl_isRejected() {
        assertThrows(InvalidWebsiteException.class, () -> new Website("ftp://empresa.com"));
    }

    @Test
    void US08_CA013_javascriptScheme_isRejected() {
        assertThrows(InvalidWebsiteException.class, () -> new Website("javascript:alert(1)"));
    }

    @Test
    void US08_CA013_urlExceeding2048Characters_isRejected() {
        String longUrl = "https://empresa.com/" + "a".repeat(2030);
        assertTrue(longUrl.length() > 2048);
        assertThrows(InvalidWebsiteException.class, () -> new Website(longUrl));
    }

    @Test
    void US08_CA013_urlWith2048Characters_isAccepted() {
        String path = "a".repeat(2048 - "https://e.co/".length());
        String url = "https://e.co/" + path;
        assertEquals(2048, url.length());
        assertDoesNotThrow(() -> new Website(url));
    }
}
