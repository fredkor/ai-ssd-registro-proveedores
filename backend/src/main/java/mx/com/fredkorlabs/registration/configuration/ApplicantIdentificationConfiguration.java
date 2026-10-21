package mx.com.fredkorlabs.registration.configuration;

import mx.com.fredkorlabs.registration.adapter.in.transaction.TransactionalIdentifyApplicantUseCase;
import mx.com.fredkorlabs.registration.adapter.out.persistence.ApplicantSignatureEvidenceJpaRepository;
import mx.com.fredkorlabs.registration.adapter.out.persistence.NoticeAcceptanceJpaRepository;
import mx.com.fredkorlabs.registration.adapter.out.persistence.RegistrationContextAdapter;
import mx.com.fredkorlabs.registration.adapter.out.persistence.RegistrationContextJpaRepository;
import mx.com.fredkorlabs.registration.application.port.in.IdentifyApplicantUseCase;
import mx.com.fredkorlabs.registration.application.port.out.ApplicantSignatureEvidencePersistencePort;
import mx.com.fredkorlabs.registration.application.port.out.ApplicantSignatureVerificationPort;
import mx.com.fredkorlabs.registration.application.port.out.NoticeAcceptancePersistencePort;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationContextPort;
import mx.com.fredkorlabs.registration.application.service.IdentifyApplicantService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import java.time.Clock;

@Configuration
public class ApplicantIdentificationConfiguration {

    @Bean
    public RegistrationContextPort registrationContextPort(RegistrationContextJpaRepository contexts,
                                                           ApplicantSignatureEvidenceJpaRepository evidences,
                                                           NoticeAcceptanceJpaRepository acceptances,
                                                           JwtEncoder registrationJwtEncoder,
                                                           RegistrationJwtProperties properties) {
        return new RegistrationContextAdapter(contexts, evidences, acceptances, registrationJwtEncoder,
            properties.issuer(), properties.audience());
    }

    @Bean
    public IdentifyApplicantUseCase identifyApplicantUseCase(
            NoticeAcceptancePersistencePort noticeAcceptancePersistence,
            ApplicantSignatureVerificationPort signatureVerification,
            ApplicantSignatureEvidencePersistencePort signatureEvidencePersistence,
            RegistrationContextPort registrationContext,
            Clock clock) {
        return new TransactionalIdentifyApplicantUseCase(new IdentifyApplicantService(
            noticeAcceptancePersistence, signatureVerification, signatureEvidencePersistence,
            registrationContext, clock));
    }
}
