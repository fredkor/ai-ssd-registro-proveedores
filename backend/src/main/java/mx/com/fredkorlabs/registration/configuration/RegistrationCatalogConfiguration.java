package mx.com.fredkorlabs.registration.configuration;

import mx.com.fredkorlabs.registration.application.port.in.GetAnnualSalesRangesUseCase;
import mx.com.fredkorlabs.registration.application.port.in.GetBusinessActivitiesUseCase;
import mx.com.fredkorlabs.registration.application.port.in.GetCountriesUseCase;
import mx.com.fredkorlabs.registration.application.port.in.GetCucopCatalogUseCase;
import mx.com.fredkorlabs.registration.application.port.in.GetLegalOwnershipsUseCase;
import mx.com.fredkorlabs.registration.application.port.in.GetPrimaryActivitiesUseCase;
import mx.com.fredkorlabs.registration.application.port.in.GetSectorsUseCase;
import mx.com.fredkorlabs.registration.application.port.in.GetStratificationsUseCase;
import mx.com.fredkorlabs.registration.application.port.in.GetSubsectorsUseCase;
import mx.com.fredkorlabs.registration.application.port.in.GetWorkerCountRangesUseCase;
import mx.com.fredkorlabs.registration.application.port.out.CucopCatalogPort;
import mx.com.fredkorlabs.registration.application.port.out.GeneralDataCatalogPort;
import mx.com.fredkorlabs.registration.application.service.GetAnnualSalesRangesService;
import mx.com.fredkorlabs.registration.application.service.GetBusinessActivitiesService;
import mx.com.fredkorlabs.registration.application.service.GetCountriesService;
import mx.com.fredkorlabs.registration.application.service.GetCucopCatalogService;
import mx.com.fredkorlabs.registration.application.service.GetLegalOwnershipsService;
import mx.com.fredkorlabs.registration.application.service.GetPrimaryActivitiesService;
import mx.com.fredkorlabs.registration.application.service.GetSectorsService;
import mx.com.fredkorlabs.registration.application.service.GetStratificationsService;
import mx.com.fredkorlabs.registration.application.service.GetSubsectorsService;
import mx.com.fredkorlabs.registration.application.service.GetWorkerCountRangesService;
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

    @Bean
    public GetSectorsUseCase getSectorsUseCase(GeneralDataCatalogPort generalDataCatalog) {
        return new GetSectorsService(generalDataCatalog);
    }

    @Bean
    public GetSubsectorsUseCase getSubsectorsUseCase(GeneralDataCatalogPort generalDataCatalog) {
        return new GetSubsectorsService(generalDataCatalog);
    }

    @Bean
    public GetBusinessActivitiesUseCase getBusinessActivitiesUseCase(GeneralDataCatalogPort generalDataCatalog) {
        return new GetBusinessActivitiesService(generalDataCatalog);
    }

    @Bean
    public GetStratificationsUseCase getStratificationsUseCase(GeneralDataCatalogPort generalDataCatalog) {
        return new GetStratificationsService(generalDataCatalog);
    }

    @Bean
    public GetPrimaryActivitiesUseCase getPrimaryActivitiesUseCase(GeneralDataCatalogPort generalDataCatalog) {
        return new GetPrimaryActivitiesService(generalDataCatalog);
    }

    @Bean
    public GetWorkerCountRangesUseCase getWorkerCountRangesUseCase(GeneralDataCatalogPort generalDataCatalog) {
        return new GetWorkerCountRangesService(generalDataCatalog);
    }

    @Bean
    public GetAnnualSalesRangesUseCase getAnnualSalesRangesUseCase(GeneralDataCatalogPort generalDataCatalog) {
        return new GetAnnualSalesRangesService(generalDataCatalog);
    }

    @Bean
    public GetCucopCatalogUseCase getCucopCatalogUseCase(CucopCatalogPort cucopCatalog) {
        return new GetCucopCatalogService(cucopCatalog);
    }
}
