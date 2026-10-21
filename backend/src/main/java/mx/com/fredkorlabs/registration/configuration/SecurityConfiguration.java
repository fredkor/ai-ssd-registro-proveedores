package mx.com.fredkorlabs.registration.configuration;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import mx.com.fredkorlabs.registration.adapter.in.web.RegistrationTokenAuthenticationEntryPoint;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationContextPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Clock;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * ADR-0015: stateless API, Bearer JWT (RS256) required except for the notice, the acceptance,
 * the identification and the API documentation.
 */
@Configuration
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain registrationSecurityFilterChain(
            HttpSecurity http, JwtDecoder jwtDecoder,
            RegistrationTokenAuthenticationEntryPoint entryPoint) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(requests -> requests
                .requestMatchers(HttpMethod.GET, "/api/v1/registration-notices/current").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/registration-notice-acceptances").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/applicant-identifications").permitAll()
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
                    "/supplier-registration-openapi.yaml", "/error").permitAll()
                .anyRequest().authenticated())
            .oauth2ResourceServer(resourceServer -> resourceServer
                .jwt(jwt -> jwt.decoder(jwtDecoder))
                .authenticationEntryPoint(entryPoint))
            .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(entryPoint));
        return http.build();
    }

    @Bean
    public JwtEncoder registrationJwtEncoder(KeyPair registrationJwtKeyPair) {
        RSAKey key = new RSAKey.Builder((RSAPublicKey) registrationJwtKeyPair.getPublic())
            .privateKey((RSAPrivateKey) registrationJwtKeyPair.getPrivate())
            .build();
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(key)));
    }

    @Bean
    public JwtDecoder registrationJwtDecoder(KeyPair registrationJwtKeyPair, RegistrationJwtProperties properties,
                                             RegistrationContextPort registrationContext, Clock clock) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
            .withPublicKey((RSAPublicKey) registrationJwtKeyPair.getPublic())
            .signatureAlgorithm(SignatureAlgorithm.RS256)
            .build();
        // exp is strict (no clock skew): from exp onwards the token is no longer valid.
        JwtTimestampValidator timestamps = new JwtTimestampValidator(Duration.ZERO);
        timestamps.setClock(clock);
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(List.of(
            timestamps,
            new JwtIssuerValidator(properties.issuer()),
            new JwtClaimValidator<Collection<String>>(JwtClaimNames.AUD,
                audience -> audience != null && audience.contains(properties.audience())),
            requiredClaims(),
            activeContext(registrationContext, clock))));
        return decoder;
    }

    private static OAuth2TokenValidator<Jwt> requiredClaims() {
        return jwt -> List.of(JwtClaimNames.SUB, JwtClaimNames.JTI, JwtClaimNames.IAT, JwtClaimNames.EXP)
            .stream().allMatch(claim -> jwt.getClaims().get(claim) != null)
            ? OAuth2TokenValidatorResult.success()
            : OAuth2TokenValidatorResult.failure(invalidToken("Missing required claim"));
    }

    private static OAuth2TokenValidator<Jwt> activeContext(RegistrationContextPort registrationContext, Clock clock) {
        return jwt -> registrationContext.isActive(Objects.toString(jwt.getId(), ""),
                Objects.toString(jwt.getSubject(), ""), clock.instant())
            ? OAuth2TokenValidatorResult.success()
            : OAuth2TokenValidatorResult.failure(invalidToken("No active registration context"));
    }

    private static OAuth2Error invalidToken(String description) {
        return new OAuth2Error("invalid_token", description, null);
    }
}
