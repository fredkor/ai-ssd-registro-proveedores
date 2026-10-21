package mx.com.fredkorlabs.registration.testsupport;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Base64;

/** node-forge interoperability fixtures (src/test/resources/signature/generate-fixtures.cjs). */
public final class SignatureFixtures {

    private static final JsonNode FIXTURES = load();

    private SignatureFixtures() {
    }

    public static String certificateBase64(String variant) { return text(variant, "certificado"); }

    public static String certificatePem(String variant) { return text(variant, "certificadoPem"); }

    public static String signatureBase64(String variant) { return text(variant, "cadenaFirma"); }

    public static byte[] certificate(String variant) { return Base64.getDecoder().decode(certificateBase64(variant)); }

    public static byte[] signature(String variant) { return Base64.getDecoder().decode(signatureBase64(variant)); }

    /** Full identifyApplicant request body in the component's exact property names. */
    public static String identifyRequest(String acceptanceId, String variant, String rfc) {
        return """
            {"noticeAcceptanceId":"%s","signatureResult":{"resultado":true,"fechaVigencia":"2030-01-01T00:00:00Z",\
            "mensaje":"OK","cadenaOriginal":"Temporal","certificado":"%s","cadenaFirma":"%s","rfc":"%s",\
            "curp":null,"razonSocial":"RAZON SOCIAL PKI","numeroSerie":30001000000500003416,"tipoCertificado":1,\
            "certificadoPem":%s,"isVigente":true,"origen":"COMPONENTE"}}"""
            .formatted(acceptanceId, certificateBase64(variant), signatureBase64(variant), rfc,
                JsonMapper.shared().writeValueAsString(certificatePem(variant)));
    }

    private static String text(String variant, String property) {
        JsonNode node = FIXTURES.get(variant);
        if (node == null) throw new IllegalArgumentException("Unknown fixture " + variant);
        return node.get(property).asString();
    }

    private static JsonNode load() {
        try (InputStream input = SignatureFixtures.class.getResourceAsStream("/signature/fixtures.json")) {
            return JsonMapper.shared().readTree(input);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
