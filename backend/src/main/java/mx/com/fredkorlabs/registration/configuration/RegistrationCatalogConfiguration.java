package mx.com.fredkorlabs.registration.configuration;

import mx.com.fredkorlabs.registration.application.port.in.GetCountriesUseCase;
import mx.com.fredkorlabs.registration.application.port.in.GetLegalOwnershipsUseCase;
import mx.com.fredkorlabs.registration.application.port.out.GeneralDataCatalogPort;
import mx.com.fredkorlabs.registration.application.service.GetCountriesService;
import mx.com.fredkorlabs.registration.application.service.GetLegalOwnershipsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RegistrationCatalogConfiguration {

    // Read-only catalog queries: registered directly, without a transactional decorator.
    @Bean
    public GetCountriesUseCase getCountriesUseCase(GeneralDataCatalogPort generalDataCatalog) {
        return new GetCountriesService(generalDataCatalog);
    }

    @Bean
    public GetLegalOwnershipsUseCase getLegalOwnershipsUseCase(GeneralDataCatalogPort generalDataCatalog) {
        return new GetLegalOwnershipsService(generalDataCatalog);
    }
}
