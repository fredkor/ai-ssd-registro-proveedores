package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.CatalogOptionsResult;
import mx.com.fredkorlabs.registration.application.port.in.GetPrimaryActivitiesUseCase;
import mx.com.fredkorlabs.registration.application.port.out.GeneralDataCatalogPort;

public class GetPrimaryActivitiesService implements GetPrimaryActivitiesUseCase {

    private final GeneralDataCatalogPort generalDataCatalog;

    public GetPrimaryActivitiesService(GeneralDataCatalogPort generalDataCatalog) {
        this.generalDataCatalog = generalDataCatalog;
    }

    @Override
    public CatalogOptionsResult execute() {
        return new CatalogOptionsResult(generalDataCatalog.findPrimaryActivities().stream()
            .map(activity -> new CatalogOptionsResult.CatalogOption(activity.id(), activity.description()))
            .toList());
    }
}
