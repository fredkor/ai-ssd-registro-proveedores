package mx.com.fredkorlabs.registration.configuration;

import mx.com.fredkorlabs.registration.adapter.in.transaction.TransactionalResumeSupplierRegistrationUseCase;
import mx.com.fredkorlabs.registration.adapter.in.transaction.TransactionalSaveCucopParticipationUseCase;
import mx.com.fredkorlabs.registration.adapter.in.transaction.TransactionalSaveGeneralDataUseCase;
import mx.com.fredkorlabs.registration.adapter.in.transaction.TransactionalStartSupplierRegistrationUseCase;
import mx.com.fredkorlabs.registration.application.port.in.CheckRegistrationExistenceUseCase;
import mx.com.fredkorlabs.registration.application.port.in.ResumeSupplierRegistrationUseCase;
import mx.com.fredkorlabs.registration.application.port.in.SaveCucopParticipationUseCase;
import mx.com.fredkorlabs.registration.application.port.in.SaveGeneralDataUseCase;
import mx.com.fredkorlabs.registration.application.port.in.StartSupplierRegistrationUseCase;
import mx.com.fredkorlabs.registration.application.port.out.CucopCatalogPort;
import mx.com.fredkorlabs.registration.application.port.out.GeneralDataCatalogPort;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationContextPort;
import mx.com.fredkorlabs.registration.application.port.out.SupplierRegistrationPersistencePort;
import mx.com.fredkorlabs.registration.application.service.CheckRegistrationExistenceService;
import mx.com.fredkorlabs.registration.application.service.ResumeSupplierRegistrationService;
import mx.com.fredkorlabs.registration.application.service.SaveCucopParticipationService;
import mx.com.fredkorlabs.registration.application.service.SaveGeneralDataService;
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

    @Bean
    public ResumeSupplierRegistrationUseCase resumeSupplierRegistrationUseCase(
            RegistrationContextPort registrationContext,
            SupplierRegistrationPersistencePort supplierRegistrationPersistence) {
        return new TransactionalResumeSupplierRegistrationUseCase(new ResumeSupplierRegistrationService(
            registrationContext, supplierRegistrationPersistence));
    }

    @Bean
    public SaveGeneralDataUseCase saveGeneralDataUseCase(
            RegistrationContextPort registrationContext,
            SupplierRegistrationPersistencePort supplierRegistrationPersistence,
            GeneralDataCatalogPort generalDataCatalog) {
        return new TransactionalSaveGeneralDataUseCase(new SaveGeneralDataService(
            registrationContext, supplierRegistrationPersistence, generalDataCatalog));
    }

    @Bean
    public SaveCucopParticipationUseCase saveCucopParticipationUseCase(
            RegistrationContextPort registrationContext,
            SupplierRegistrationPersistencePort supplierRegistrationPersistence,
            CucopCatalogPort cucopCatalog) {
        return new TransactionalSaveCucopParticipationUseCase(new SaveCucopParticipationService(
            registrationContext, supplierRegistrationPersistence, cucopCatalog));
    }
}
