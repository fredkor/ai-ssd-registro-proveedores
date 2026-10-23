package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.GetCountriesResult;
import mx.com.fredkorlabs.registration.application.port.in.GetCountriesUseCase;
import mx.com.fredkorlabs.registration.application.port.out.GeneralDataCatalogPort;

public class GetCountriesService implements GetCountriesUseCase {

    private final GeneralDataCatalogPort generalDataCatalog;

    public GetCountriesService(GeneralDataCatalogPort generalDataCatalog) {
        this.generalDataCatalog = generalDataCatalog;
    }

    @Override
    public GetCountriesResult execute() {
        return new GetCountriesResult(generalDataCatalog.findCountries().stream()
            .map(country -> new GetCountriesResult.CountryItem(country.id(), country.name(), country.code()))
            .toList());
    }
}
