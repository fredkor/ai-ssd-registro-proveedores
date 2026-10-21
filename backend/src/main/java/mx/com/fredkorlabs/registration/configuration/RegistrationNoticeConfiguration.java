package mx.com.fredkorlabs.registration.configuration;

import mx.com.fredkorlabs.registration.adapter.in.transaction.TransactionalAcceptRegistrationNoticeUseCase;
import mx.com.fredkorlabs.registration.application.port.in.AcceptRegistrationNoticeUseCase;
import mx.com.fredkorlabs.registration.application.port.in.GetRegistrationNoticeUseCase;
import mx.com.fredkorlabs.registration.application.port.out.NoticeAcceptancePersistencePort;
import mx.com.fredkorlabs.registration.application.service.AcceptRegistrationNoticeService;
import mx.com.fredkorlabs.registration.application.service.GetRegistrationNoticeService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class RegistrationNoticeConfiguration {

    @Bean
    public GetRegistrationNoticeUseCase getRegistrationNoticeUseCase() {
        return new GetRegistrationNoticeService();
    }

    @Bean
    public AcceptRegistrationNoticeUseCase acceptRegistrationNoticeUseCase(
            NoticeAcceptancePersistencePort noticeAcceptancePersistence, Clock clock) {
        return new TransactionalAcceptRegistrationNoticeUseCase(
            new AcceptRegistrationNoticeService(noticeAcceptancePersistence, clock));
    }
}
