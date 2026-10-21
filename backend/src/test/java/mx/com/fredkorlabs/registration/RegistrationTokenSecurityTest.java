package mx.com.fredkorlabs.registration;

import mx.com.fredkorlabs.registration.configuration.RegistrationJwtProperties;
import mx.com.fredkorlabs.registration.testsupport.PostgresTestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static mx.com.fredkorlabs.registration.testsupport.SignatureFixtures.identifyRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** ADR-0015 token validation on a protected route (its controller is not implemented yet). */
@Import(PostgresTestcontainersConfiguration.class)
@SpringBootTest(classes = SupplierRegistrationApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class RegistrationTokenSecurityTest {

    private static final String PROTECTED = "/api/v1/supplier-registrations/existence";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private RegistrationJwtProperties properties;

    private String issuedToken() throws Exception {
        String acceptance = JsonMapper.shared().readTree(mockMvc.perform(post("/api/v1/registration-notice-acceptances")
                .contentType(MediaType.APPLICATION_JSON).content("{\"version\":\"1.0\"}"))
            .andReturn().getResponse().getContentAsString()).get("acceptanceId").asString();
        String body = mockMvc.perform(post("/api/v1/applicant-identifications").contentType(MediaType.APPLICATION_JSON)
                .content(identifyRequest(acceptance, "legal-entity", "AAA010101AAA")))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return JsonMapper.shared().readTree(body).get("registrationToken").asString();
    }

    private String signedToken(String issuer, String audience, Instant issuedAt, Instant expiresAt) {
        JwtClaimsSet claims = JwtClaimsSet.builder().subject("AAA010101AAA").id(UUID.randomUUID().toString())
            .issuer(issuer).audience(List.of(audience)).issuedAt(issuedAt).expiresAt(expiresAt).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(SignatureAlgorithm.RS256).build(), claims))
            .getTokenValue();
    }

    private ResultActions callProtected(String token) throws Exception {
        return mockMvc.perform(get(PROTECTED).header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
    }

    private static void assertTokenInvalid(ResultActions result) throws Exception {
        result.andExpect(status().isUnauthorized())
            .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
            .andExpect(jsonPath("$.code").value("REGISTRATION_TOKEN_INVALID"))
            .andExpect(jsonPath("$.details").isEmpty());
    }

    @Test
    void US01_CA026_missingToken_returns401() throws Exception {
        assertTokenInvalid(mockMvc.perform(get(PROTECTED)));
    }

    @Test
    void US01_CA017_issuedToken_authenticatesProtectedRoute() throws Exception {
        int status = callProtected(issuedToken()).andReturn().getResponse().getStatus();

        assertNotEquals(401, status);
    }

    @Test
    void US01_CA026_tamperedToken_returns401() throws Exception {
        String token = issuedToken();
        String[] parts = token.split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8)
            .replace("AAA010101AAA", "BBB010101BBB");
        String tampered = parts[0] + "." + Base64.getUrlEncoder().withoutPadding()
            .encodeToString(payload.getBytes(StandardCharsets.UTF_8)) + "." + parts[2];

        assertTokenInvalid(callProtected(tampered));
    }

    @Test
    void US01_CA026_unsignedTokenWithAlgNone_returns401() throws Exception {
        String header = Base64.getUrlEncoder().withoutPadding().encodeToString("{\"alg\":\"none\"}".getBytes());
        String token = issuedToken();
        assertTokenInvalid(callProtected(header + "." + token.split("\\.")[1] + "."));
    }

    @Test
    void US01_CA018_expiredToken_returns401() throws Exception {
        Instant now = Instant.now();
        assertTokenInvalid(callProtected(signedToken(properties.issuer(), properties.audience(),
            now.minusSeconds(7200), now.minusSeconds(3600))));
    }

    @Test
    void US01_CA026_wrongIssuerOrAudience_returns401() throws Exception {
        Instant now = Instant.now();
        assertTokenInvalid(callProtected(signedToken("other-issuer", properties.audience(), now, now.plusSeconds(600))));
        assertTokenInvalid(callProtected(signedToken(properties.issuer(), "other-audience", now, now.plusSeconds(600))));
    }

    @Test
    void US01_CA027_validSignatureWithoutStoredContext_returns401() throws Exception {
        Instant now = Instant.now();
        assertTokenInvalid(callProtected(signedToken(properties.issuer(), properties.audience(),
            now, now.plusSeconds(600))));
    }

    @Test
    void US03_CA006_publicOperations_doNotRequireToken() throws Exception {
        mockMvc.perform(get("/api/v1/registration-notices/current")).andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/registration-notice-acceptances")
            .contentType(MediaType.APPLICATION_JSON).content("{\"version\":\"1.0\"}")).andExpect(status().isOk());
    }
}
