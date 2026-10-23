package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.CatalogOptionsResult;
import mx.com.fredkorlabs.registration.application.port.in.GetWorkerCountRangesUseCase;
import mx.com.fredkorlabs.registration.application.port.out.GeneralDataCatalogPort;

public class GetWorkerCountRangesService implements GetWorkerCountRangesUseCase {

    private final GeneralDataCatalogPort generalDataCatalog;

    public GetWorkerCountRangesService(GeneralDataCatalogPort generalDataCatalog) {
        this.generalDataCatalog = generalDataCatalog;
    }

    @Override
    public CatalogOptionsResult execute() {
        return new CatalogOptionsResult(generalDataCatalog.findWorkerCountRanges().stream()
            .map(range -> new CatalogOptionsResult.CatalogOption(range.id(), range.description()))
            .toList());
    }
}
