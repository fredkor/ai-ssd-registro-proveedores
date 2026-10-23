package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.CatalogOptionsResult;
import mx.com.fredkorlabs.registration.application.port.in.GetBusinessActivitiesUseCase;
import mx.com.fredkorlabs.registration.application.port.out.GeneralDataCatalogPort;

public class GetBusinessActivitiesService implements GetBusinessActivitiesUseCase {

    private final GeneralDataCatalogPort generalDataCatalog;

    public GetBusinessActivitiesService(GeneralDataCatalogPort generalDataCatalog) {
        this.generalDataCatalog = generalDataCatalog;
    }

    @Override
    public CatalogOptionsResult execute() {
        return new CatalogOptionsResult(generalDataCatalog.findBusinessActivities().stream()
            .map(activity -> new CatalogOptionsResult.CatalogOption(activity.id(), activity.description()))
            .toList());
    }
}
