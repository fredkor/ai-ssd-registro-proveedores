package mx.com.fredkorlabs.registration.application.port.out;

import mx.com.fredkorlabs.registration.domain.model.ApplicantIdentity;

import java.time.Instant;

public interface RegistrationContextPort {

    /**
     * Signs a registration JWT and stores its context (ADR-0015). Throws
     * {@link NoticeAcceptanceAlreadyUsedException} when another token already consumed the acceptance.
     */
    String issue(ApplicantIdentity identity, SignatureEvidenceReference evidence,
                 NoticeAcceptanceReference acceptance, Instant issuedAt, Instant expiresAt);

    /** True when the token's context exists for that RFC and has not expired at {@code now}. */
    boolean isActive(String jti, String rfc, Instant now);
}
