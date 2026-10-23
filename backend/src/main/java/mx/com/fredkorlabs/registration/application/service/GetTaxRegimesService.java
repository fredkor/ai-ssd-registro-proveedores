package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.CatalogOptionsResult;
import mx.com.fredkorlabs.registration.application.port.in.GetTaxRegimesUseCase;
import mx.com.fredkorlabs.registration.application.port.out.GeneralDataCatalogPort;

public class GetTaxRegimesService implements GetTaxRegimesUseCase {

    private final GeneralDataCatalogPort generalDataCatalog;

    public GetTaxRegimesService(GeneralDataCatalogPort generalDataCatalog) {
        this.generalDataCatalog = generalDataCatalog;
    }

    @Override
    public CatalogOptionsResult execute() {
        return new CatalogOptionsResult(generalDataCatalog.findTaxRegimes().stream()
            .map(regime -> new CatalogOptionsResult.CatalogOption(regime.id(), regime.description()))
            .toList());
    }
}
