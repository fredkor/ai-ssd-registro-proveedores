package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.CatalogOptionsResult;
import mx.com.fredkorlabs.registration.application.port.in.GetStratificationsUseCase;
import mx.com.fredkorlabs.registration.application.port.out.GeneralDataCatalogPort;

public class GetStratificationsService implements GetStratificationsUseCase {

    private final GeneralDataCatalogPort generalDataCatalog;

    public GetStratificationsService(GeneralDataCatalogPort generalDataCatalog) {
        this.generalDataCatalog = generalDataCatalog;
    }

    // Only active entries, with no association or filter by tax regime (domain/catalogs.md §8, ADR-0020).
    @Override
    public CatalogOptionsResult execute() {
        return new CatalogOptionsResult(generalDataCatalog.findActiveStratifications().stream()
            .map(stratification -> new CatalogOptionsResult.CatalogOption(stratification.id(),
                stratification.description()))
            .toList());
    }
}
