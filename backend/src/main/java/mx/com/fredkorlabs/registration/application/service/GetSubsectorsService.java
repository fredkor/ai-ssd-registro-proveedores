package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.CatalogOptionsResult;
import mx.com.fredkorlabs.registration.application.port.in.GetSubsectorsQuery;
import mx.com.fredkorlabs.registration.application.port.in.GetSubsectorsUseCase;
import mx.com.fredkorlabs.registration.application.port.out.GeneralDataCatalogPort;

public class GetSubsectorsService implements GetSubsectorsUseCase {

    private final GeneralDataCatalogPort generalDataCatalog;

    public GetSubsectorsService(GeneralDataCatalogPort generalDataCatalog) {
        this.generalDataCatalog = generalDataCatalog;
    }

    // ADR-0020: an unknown sector is not an error; it simply has no subsectors.
    @Override
    public CatalogOptionsResult execute(GetSubsectorsQuery query) {
        return new CatalogOptionsResult(generalDataCatalog.findSubsectors(query.sectorId()).stream()
            .map(subsector -> new CatalogOptionsResult.CatalogOption(subsector.id(), subsector.description()))
            .toList());
    }
}
