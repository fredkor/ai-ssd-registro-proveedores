package mx.com.fredkorlabs.registration.adapter.in.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * RFC of the authenticated registration token. Its {@code sub} was already checked against the stored
 * context (ADR-0015), so it is the RFC of the accepted identification, never a client-supplied filter.
 */
final class RegistrationTokenPrincipal {

    private RegistrationTokenPrincipal() {
    }

    static String currentRfc() {
        return currentToken().getSubject();
    }

    /** jti of the token, which identifies its stored context (ADR-0015). */
    static String currentTokenId() {
        return currentToken().getId();
    }

    private static Jwt currentToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken token)) {
            throw new IllegalStateException("Protected operation reached without a registration token");
        }
        return token.getToken();
    }
}
