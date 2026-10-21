package mx.com.fredkorlabs.registration.application.port.in;

/**
 * ResultadoFirma received for identification: the original JSON kept intact plus the projections
 * needed to verify the seal (architecture §4).
 */
public record SignatureResultInput(
    String completeResult,
    boolean successful,
    String signedContent,
    byte[] certificate,
    byte[] signature,
    String rfc,
    String legalName
) {}
