package mx.com.fredkorlabs.registration.configuration;

import mx.com.fredkorlabs.registration.application.port.in.CheckRegistrationExistenceUseCase;
import mx.com.fredkorlabs.registration.application.port.out.SupplierRegistrationPersistencePort;
import mx.com.fredkorlabs.registration.application.service.CheckRegistrationExistenceService;
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
}
