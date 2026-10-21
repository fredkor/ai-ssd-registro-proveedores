package mx.com.fredkorlabs.registration.domain;

import mx.com.fredkorlabs.registration.domain.exception.InvalidPhoneNumberException;
import mx.com.fredkorlabs.registration.domain.model.PhoneNumber;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PhoneNumberTest {

    @Test
    void US09_CA003_phoneWith10Digits_isAccepted() {
        PhoneNumber phone = new PhoneNumber("5512345678", null);
        assertEquals("5512345678", phone.number());
    }

    @Test
    void US09_CA003_phoneWithNineDigits_isRejected() {
        assertThrows(InvalidPhoneNumberException.class, () -> new PhoneNumber("551234567", null));
    }

    @Test
    void US09_CA003_phoneWithNonDigitCharacter_isRejected() {
        assertThrows(InvalidPhoneNumberException.class, () -> new PhoneNumber("551234567a", null));
    }

    @Test
    void US09_CA003_phoneWithElevenDigits_isRejected() {
        assertThrows(InvalidPhoneNumberException.class, () -> new PhoneNumber("55123456789", null));
    }

    @Test
    void US09_CA005_extensionWith5Characters_isAccepted() {
        PhoneNumber phone = new PhoneNumber("5512345678", "12345");
        assertEquals("12345", phone.extension());
    }

    @Test
    void US09_CA005_extensionWith6Characters_isRejected() {
        assertThrows(InvalidPhoneNumberException.class, () -> new PhoneNumber("5512345678", "123456"));
    }

    @Test
    void US09_CA006_nullExtension_isAccepted() {
        PhoneNumber phone = new PhoneNumber("5512345678", null);
        assertNull(phone.extension());
    }
}
