package mx.com.fredkorlabs.registration.adapter.out.signature;

import mx.com.fredkorlabs.registration.application.port.out.ApplicantSignatureVerificationPort;
import mx.com.fredkorlabs.registration.application.port.out.SignatureCheck;
import mx.com.fredkorlabs.registration.domain.model.CertificateSubjectAttribute;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;

/** SHA256withRSA (RSASSA-PKCS1-v1_5) over the UTF-8 bytes of the signed content, as in ADR-0016. */
@Component
public class JdkApplicantSignatureVerificationAdapter implements ApplicantSignatureVerificationPort {

    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    @Override
    public SignatureCheck verify(byte[] certificate, String signedContent, byte[] signature) {
        X509Certificate x509;
        List<CertificateSubjectAttribute> subject;
        try {
            x509 = (X509Certificate) CertificateFactory.getInstance("X.509")
                .generateCertificate(new ByteArrayInputStream(certificate));
            subject = X500SubjectReader.read(x509.getSubjectX500Principal().getEncoded());
        } catch (CertificateException | IllegalArgumentException e) {
            return SignatureCheck.unreadableCertificate();
        }
        return new SignatureCheck(sealMatches(x509, signedContent, signature), subject);
    }

    private SignatureCheck.Status sealMatches(X509Certificate certificate, String signedContent, byte[] signature) {
        try {
            Signature verifier = Signature.getInstance(SIGNATURE_ALGORITHM);
            verifier.initVerify(certificate.getPublicKey());
            verifier.update(signedContent.getBytes(StandardCharsets.UTF_8));
            return verifier.verify(signature) ? SignatureCheck.Status.VALID : SignatureCheck.Status.INVALID;
        } catch (InvalidKeyException | SignatureException e) {
            // Non-RSA key or malformed seal bytes: the seal cannot match this certificate.
            return SignatureCheck.Status.INVALID;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA256withRSA is not available", e);
        }
    }
}
