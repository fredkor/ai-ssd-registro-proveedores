package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.CatalogOptionsResult;
import mx.com.fredkorlabs.registration.application.port.in.GetAnnualSalesRangesUseCase;
import mx.com.fredkorlabs.registration.application.port.out.GeneralDataCatalogPort;

public class GetAnnualSalesRangesService implements GetAnnualSalesRangesUseCase {

    private final GeneralDataCatalogPort generalDataCatalog;

    public GetAnnualSalesRangesService(GeneralDataCatalogPort generalDataCatalog) {
        this.generalDataCatalog = generalDataCatalog;
    }

    @Override
    public CatalogOptionsResult execute() {
        return new CatalogOptionsResult(generalDataCatalog.findAnnualSalesRanges().stream()
            .map(range -> new CatalogOptionsResult.CatalogOption(range.id(), range.description()))
            .toList());
    }
}
