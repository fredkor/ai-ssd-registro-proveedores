package mx.com.fredkorlabs.registration.adapter.out.persistence;

import mx.com.fredkorlabs.registration.application.port.out.NoticeAcceptanceAlreadyUsedException;
import mx.com.fredkorlabs.registration.application.port.out.NoticeAcceptanceReference;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationContextPort;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationSession;
import mx.com.fredkorlabs.registration.application.port.out.SignatureEvidenceReference;
import mx.com.fredkorlabs.registration.domain.model.ApplicantIdentity;
import mx.com.fredkorlabs.registration.domain.model.ApplicantSignatureEvidence;
import mx.com.fredkorlabs.registration.domain.model.NoticeAcceptance;
import mx.com.fredkorlabs.registration.domain.model.Rfc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Signs the registration JWT and stores its context in the registration database (ADR-0015). */
public class RegistrationContextAdapter implements RegistrationContextPort {

    private final RegistrationContextJpaRepository contexts;
    private final ApplicantSignatureEvidenceJpaRepository evidences;
    private final NoticeAcceptanceJpaRepository acceptances;
    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final String audience;

    public RegistrationContextAdapter(RegistrationContextJpaRepository contexts,
                                      ApplicantSignatureEvidenceJpaRepository evidences,
                                      NoticeAcceptanceJpaRepository acceptances,
                                      JwtEncoder jwtEncoder, String issuer, String audience) {
        this.contexts = contexts;
        this.evidences = evidences;
        this.acceptances = acceptances;
        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.audience = audience;
    }

    @Override
    public String issue(ApplicantIdentity identity, SignatureEvidenceReference evidence,
                        NoticeAcceptanceReference acceptance, Instant issuedAt, Instant expiresAt) {
        UUID jti = UUID.randomUUID();
        String rfc = identity.rfc().value();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .subject(rfc)
            .id(jti.toString())
            .issuedAt(issuedAt)
            .expiresAt(expiresAt)
            .issuer(issuer)
            .audience(List.of(audience))
            .build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(
            JwsHeader.with(SignatureAlgorithm.RS256).build(), claims)).getTokenValue();
        try {
            contexts.saveAndFlush(new RegistrationContextJpaEntity(jti, rfc, issuedAt, expiresAt,
                evidences.getReferenceById(UUID.fromString(evidence.value())),
                acceptances.getReferenceById(UUID.fromString(acceptance.value()))));
        } catch (DataIntegrityViolationException e) {
            throw new NoticeAcceptanceAlreadyUsedException("Notice acceptance already backs another token", e);
        }
        return token;
    }

    @Override
    public boolean isActive(String jti, String rfc, Instant now) {
        return findContext(jti)
            .filter(context -> context.getRfc().equals(rfc))
            .filter(context -> now.isBefore(context.getExpiresAt()))
            .isPresent();
    }

    @Override
    public Optional<RegistrationSession> resolve(String jti) {
        return findContext(jti).map(context -> {
            ApplicantSignatureEvidenceJpaEntity evidence = context.getSignatureEvidence();
            NoticeAcceptanceJpaEntity acceptance = context.getNoticeAcceptance();
            return new RegistrationSession(
                new ApplicantIdentity(new Rfc(context.getRfc()), evidence.getVerifiedLegalName()),
                new ApplicantSignatureEvidence(evidence.getCompleteResult()),
                new SignatureEvidenceReference(evidence.getId().toString()),
                new NoticeAcceptance(acceptance.getVersion(), acceptance.getAcceptedAt()),
                new NoticeAcceptanceReference(acceptance.getId().toString()));
        });
    }

    private Optional<RegistrationContextJpaEntity> findContext(String jti) {
        UUID id;
        try {
            id = UUID.fromString(jti);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
        return contexts.findById(id);
    }
}
