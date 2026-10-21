package mx.com.fredkorlabs.registration.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RSA key pair of the registration JWT (ADR-0015). PEM files come from configured locations;
 * without them an ephemeral pair is generated, except under the {@code prod} profile (ADR-0018).
 */
@Configuration
@EnableConfigurationProperties(RegistrationJwtProperties.class)
public class JwtKeyConfiguration {

    private static final Logger log = LoggerFactory.getLogger(JwtKeyConfiguration.class);

    @Bean
    public KeyPair registrationJwtKeyPair(RegistrationJwtProperties properties, Environment environment,
                                          ResourceLoader resourceLoader) throws IOException, GeneralSecurityException {
        if (properties.hasKeyLocations()) {
            KeyFactory rsa = KeyFactory.getInstance("RSA");
            RSAPublicKey publicKey = (RSAPublicKey) rsa.generatePublic(
                new X509EncodedKeySpec(readPem(resourceLoader, properties.publicKeyLocation(), "PUBLIC KEY")));
            RSAPrivateKey privateKey = (RSAPrivateKey) rsa.generatePrivate(
                new PKCS8EncodedKeySpec(readPem(resourceLoader, properties.privateKeyLocation(), "PRIVATE KEY")));
            return new KeyPair(publicKey, privateKey);
        }
        if (environment.acceptsProfiles(Profiles.of("prod"))) {
            throw new IllegalStateException(
                "registration.jwt.public-key-location and private-key-location are required with the prod profile");
        }
        log.warn("No registration JWT keys configured; using an ephemeral RSA key pair. Tokens become invalid on restart.");
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    private static byte[] readPem(ResourceLoader resourceLoader, String location, String type) throws IOException {
        if (location == null || location.isBlank()) {
            throw new IllegalStateException("Both registration JWT key locations must be configured");
        }
        try (InputStream input = resourceLoader.getResource(location).getInputStream()) {
            String pem = new String(input.readAllBytes(), StandardCharsets.US_ASCII);
            String body = pem
                .replace("-----BEGIN " + type + "-----", "")
                .replace("-----END " + type + "-----", "")
                .replaceAll("\\s", "");
            return Base64.getDecoder().decode(body);
        }
    }
}
