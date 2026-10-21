package mx.com.fredkorlabs.registration.adapter.out.signature;

import mx.com.fredkorlabs.registration.application.port.out.SignatureCheck;
import mx.com.fredkorlabs.registration.domain.model.CertificateSubjectAttribute;
import org.junit.jupiter.api.Test;

import java.util.List;

import static mx.com.fredkorlabs.registration.testsupport.SignatureFixtures.certificate;
import static mx.com.fredkorlabs.registration.testsupport.SignatureFixtures.signature;
import static org.junit.jupiter.api.Assertions.*;

/** ADR-0016 interoperability: seals and certificates produced by node-forge. */
class JdkApplicantSignatureVerificationAdapterTest {

    private final JdkApplicantSignatureVerificationAdapter adapter = new JdkApplicantSignatureVerificationAdapter();

    @Test
    void US01_CA014_nodeForgeSealOverTemporal_isValid() {
        SignatureCheck check = adapter.verify(certificate("legal-entity"), "Temporal", signature("legal-entity"));

        assertEquals(SignatureCheck.Status.VALID, check.status());
    }

    @Test
    void US01_CA014_subjectAttributes_keepCertificateOrderAndOids() {
        SignatureCheck check = adapter.verify(certificate("name-41-before-3"), "Temporal", signature("name-41-before-3"));

        assertEquals(List.of(
            new CertificateSubjectAttribute("2.5.4.41", "NOMBRE DEL OID 41"),
            new CertificateSubjectAttribute("2.5.4.3", "NOMBRE DEL OID 3"),
            new CertificateSubjectAttribute("2.5.4.45", "AAA010101AAA")), check.subject());
    }

    @Test
    void US01_CA014_utf8SubjectValues_areDecoded() {
        SignatureCheck check = adapter.verify(certificate("individual"), "Temporal", signature("individual"));

        assertEquals(new CertificateSubjectAttribute("2.5.4.3", "JOSÉ PÉREZ NÚÑEZ"), check.subject().getFirst());
    }

    @Test
    void US01_CA015_alteredSignedContent_isInvalid() {
        SignatureCheck check = adapter.verify(certificate("legal-entity"), "temporal", signature("legal-entity"));

        assertEquals(SignatureCheck.Status.INVALID, check.status());
    }

    @Test
    void US01_CA015_alteredSeal_isInvalid() {
        byte[] seal = signature("legal-entity");
        seal[10] ^= 0x01;

        assertEquals(SignatureCheck.Status.INVALID,
            adapter.verify(certificate("legal-entity"), "Temporal", seal).status());
    }

    @Test
    void US01_CA015_sealFromAnotherCertificate_isInvalid() {
        assertEquals(SignatureCheck.Status.INVALID,
            adapter.verify(certificate("legal-entity"), "Temporal", signature("individual")).status());
    }

    @Test
    void US01_CA015_truncatedSeal_isInvalid() {
        assertEquals(SignatureCheck.Status.INVALID,
            adapter.verify(certificate("legal-entity"), "Temporal", new byte[] {1, 2, 3}).status());
    }

    @Test
    void US01_CA015_malformedCertificate_isUnreadable() {
        SignatureCheck check = adapter.verify(new byte[] {0x30, 0x03, 0x01}, "Temporal", signature("legal-entity"));

        assertEquals(SignatureCheck.Status.UNREADABLE_CERTIFICATE, check.status());
        assertTrue(check.subject().isEmpty());
    }
}
