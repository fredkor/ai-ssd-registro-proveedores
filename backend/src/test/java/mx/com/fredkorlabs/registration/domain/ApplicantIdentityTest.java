package mx.com.fredkorlabs.registration.domain;

import mx.com.fredkorlabs.registration.domain.model.ApplicantIdentity;
import mx.com.fredkorlabs.registration.domain.model.Rfc;
import mx.com.fredkorlabs.registration.domain.model.TaxRegime;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class ApplicantIdentityTest {

    @Test
    void US08_CA011_rfcWith12Characters_derivesLegalEntityRegime() {
        ApplicantIdentity identity = new ApplicantIdentity(new Rfc("XAXX01010100"), "Corp SA");
        assertEquals(TaxRegime.LEGAL_ENTITY, identity.taxRegime());
        assertEquals(1, identity.taxRegime().catalogId());
    }

    @Test
    void US08_CA012_rfcWith13Characters_derivesIndividualRegime() {
        ApplicantIdentity identity = new ApplicantIdentity(new Rfc("XAXX010101000"), "Juan Pérez");
        assertEquals(TaxRegime.INDIVIDUAL, identity.taxRegime());
        assertEquals(2, identity.taxRegime().catalogId());
    }

    @Test
    void US08_CA019_username_isRfcInLowercase() {
        ApplicantIdentity identity = new ApplicantIdentity(new Rfc("XAXX01010100"), "Corp SA");
        assertEquals("xaxx01010100", identity.username().value());
    }

    @Test
    void US08_CA019_username_isTheSameUnderAnyDefaultLocale() {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr"));
            ApplicantIdentity identity = new ApplicantIdentity(new Rfc("IIAX010101AB1"), "Ignacio Ibarra");

            assertEquals("iiax010101ab1", identity.username().value());
        } finally {
            Locale.setDefault(previous);
        }
    }
}
