package mx.com.fredkorlabs.registration.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** ADR-0015: issuer, audience and PEM key locations of the registration JWT. */
@ConfigurationProperties(prefix = "registration.jwt")
public record RegistrationJwtProperties(
    String issuer,
    String audience,
    String publicKeyLocation,
    String privateKeyLocation
) {
    boolean hasKeyLocations() {
        return hasText(publicKeyLocation) || hasText(privateKeyLocation);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
