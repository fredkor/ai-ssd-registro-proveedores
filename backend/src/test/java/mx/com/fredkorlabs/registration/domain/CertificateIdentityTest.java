package mx.com.fredkorlabs.registration.domain;

import mx.com.fredkorlabs.registration.domain.exception.ApplicantRfcMismatchException;
import mx.com.fredkorlabs.registration.domain.exception.InvalidCertificateIdentityException;
import mx.com.fredkorlabs.registration.domain.model.ApplicantIdentity;
import mx.com.fredkorlabs.registration.domain.model.CertificateIdentity;
import mx.com.fredkorlabs.registration.domain.model.CertificateSubjectAttribute;
import mx.com.fredkorlabs.registration.domain.model.TaxRegime;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CertificateIdentityTest {

    private static CertificateSubjectAttribute attr(String oid, String value) {
        return new CertificateSubjectAttribute(oid, value);
    }

    @Test
    void US01_CA020_singleRfcAttribute_isHolderRfc() {
        CertificateIdentity identity = CertificateIdentity.from(List.of(
            attr("2.5.4.3", "EMPRESA SA"), attr("2.5.4.45", "AAA010101AAA")));

        assertEquals("AAA010101AAA", identity.holderRfc());
    }

    @Test
    void US01_CA020_rfcWithRepresentative_takesOnlyFirstSegment() {
        CertificateIdentity identity = CertificateIdentity.from(List.of(
            attr("2.5.4.3", "EMPRESA SA"), attr("2.5.4.45", "AAA010101AAA/HEGT7610034S2")));

        assertEquals("AAA010101AAA", identity.holderRfc());
    }

    @Test
    void US01_CA020_representativeRfc_isNotAnAlternativeMatch() {
        CertificateIdentity identity = CertificateIdentity.from(List.of(
            attr("2.5.4.3", "EMPRESA SA"), attr("2.5.4.45", "AAA010101AAA/HEGT7610034S2")));

        assertThrows(ApplicantRfcMismatchException.class, () -> identity.matching("HEGT7610034S2"));
    }

    @Test
    void US01_CA021_missingRfcAttribute_isRejected() {
        assertThrows(InvalidCertificateIdentityException.class,
            () -> CertificateIdentity.from(List.of(attr("2.5.4.3", "EMPRESA SA"))));
    }

    @Test
    void US01_CA021_emptyFirstRfcSegment_isRejected() {
        assertThrows(InvalidCertificateIdentityException.class, () -> CertificateIdentity.from(List.of(
            attr("2.5.4.3", "EMPRESA SA"), attr("2.5.4.45", "/HEGT7610034S2"))));
    }

    @Test
    void US01_CA021_firstRfcSegmentWithInvalidLength_isRejected() {
        assertThrows(InvalidCertificateIdentityException.class, () -> CertificateIdentity.from(List.of(
            attr("2.5.4.3", "EMPRESA SA"), attr("2.5.4.45", "AAA0101"))));
    }

    @Test
    void US01_CA021_firstRfcSegmentIsNotTrimmed() {
        assertThrows(InvalidCertificateIdentityException.class, () -> CertificateIdentity.from(List.of(
            attr("2.5.4.3", "EMPRESA SA"), attr("2.5.4.45", "AAA010101AAA  / HEGT7610034S2"))));
    }

    @Test
    void US01_CA022_receivedRfcDifferentFromHolder_isRejected() {
        CertificateIdentity identity = CertificateIdentity.from(List.of(
            attr("2.5.4.3", "EMPRESA SA"), attr("2.5.4.45", "AAA010101AAA")));

        assertThrows(ApplicantRfcMismatchException.class, () -> identity.matching("BBB010101BBB"));
    }

    @Test
    void US01_CA022_rfcComparisonIsCaseSensitive() {
        CertificateIdentity identity = CertificateIdentity.from(List.of(
            attr("2.5.4.3", "EMPRESA SA"), attr("2.5.4.45", "AAA010101AAA")));

        assertThrows(ApplicantRfcMismatchException.class, () -> identity.matching("aaa010101aaa"));
    }

    @Test
    void US01_CA023_legalName_isFirstMatchingAttributeInCertificateOrder() {
        CertificateIdentity identity = CertificateIdentity.from(List.of(
            attr("2.5.4.6", "MX"), attr("2.5.4.41", "NOMBRE 41"), attr("2.5.4.3", "NOMBRE 3"),
            attr("2.5.4.10", "ORGANIZACION 10"), attr("2.5.4.45", "AAA010101AAA")));

        assertEquals("NOMBRE 41", identity.legalName());
    }

    @Test
    void US01_CA023_organizationFirst_isUsedWithoutPriorityOrConcatenation() {
        CertificateIdentity identity = CertificateIdentity.from(List.of(
            attr("2.5.4.10", "ORGANIZACION 10"), attr("2.5.4.3", "NOMBRE 3"), attr("2.5.4.45", "AAA010101AAA")));

        assertEquals("ORGANIZACION 10", identity.legalName());
    }

    @Test
    void US01_CA024_identityUsesCertificateNameAndDerivedValues() {
        ApplicantIdentity identity = CertificateIdentity.from(List.of(
            attr("2.5.4.3", "NOMBRE CERTIFICADO"), attr("2.5.4.45", "HEGT7610034S2"))).matching("HEGT7610034S2");

        assertEquals("NOMBRE CERTIFICADO", identity.legalName());
        assertEquals(TaxRegime.INDIVIDUAL, identity.taxRegime());
        assertEquals("hegt7610034s2", identity.username().value());
    }

    @Test
    void US01_CA025_missingNameAttribute_isRejected() {
        assertThrows(InvalidCertificateIdentityException.class, () -> CertificateIdentity.from(List.of(
            attr("2.5.4.6", "MX"), attr("2.5.4.45", "AAA010101AAA"))));
    }

    @Test
    void US01_CA025_emptyFirstNameAttribute_isRejectedWithoutUsingLaterValue() {
        assertThrows(InvalidCertificateIdentityException.class, () -> CertificateIdentity.from(List.of(
            attr("2.5.4.3", ""), attr("2.5.4.41", "NOMBRE POSTERIOR"), attr("2.5.4.45", "AAA010101AAA"))));
    }
}
