package mx.com.fredkorlabs.registration.configuration;

import mx.com.fredkorlabs.registration.adapter.in.transaction.TransactionalStartSupplierRegistrationUseCase;
import mx.com.fredkorlabs.registration.application.port.in.CheckRegistrationExistenceUseCase;
import mx.com.fredkorlabs.registration.application.port.in.StartSupplierRegistrationUseCase;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationContextPort;
import mx.com.fredkorlabs.registration.application.port.out.SupplierRegistrationPersistencePort;
import mx.com.fredkorlabs.registration.application.service.CheckRegistrationExistenceService;
import mx.com.fredkorlabs.registration.application.service.StartSupplierRegistrationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SupplierRegistrationConfiguration {

    // Read-only lookup: registered directly, without a transactional decorator.
    @Bean
    public CheckRegistrationExistenceUseCase checkRegistrationExistenceUseCase(
            SupplierRegistrationPersistencePort supplierRegistrationPersistence) {
        return new CheckRegistrationExistenceService(supplierRegistrationPersistence);
    }

    @Bean
    public StartSupplierRegistrationUseCase startSupplierRegistrationUseCase(
            RegistrationContextPort registrationContext,
            SupplierRegistrationPersistencePort supplierRegistrationPersistence) {
        return new TransactionalStartSupplierRegistrationUseCase(new StartSupplierRegistrationService(
            registrationContext, supplierRegistrationPersistence));
    }
}
