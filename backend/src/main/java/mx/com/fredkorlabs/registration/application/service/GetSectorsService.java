package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.CatalogOptionsResult;
import mx.com.fredkorlabs.registration.application.port.in.GetSectorsUseCase;
import mx.com.fredkorlabs.registration.application.port.out.GeneralDataCatalogPort;

public class GetSectorsService implements GetSectorsUseCase {

    private final GeneralDataCatalogPort generalDataCatalog;

    public GetSectorsService(GeneralDataCatalogPort generalDataCatalog) {
        this.generalDataCatalog = generalDataCatalog;
    }

    @Override
    public CatalogOptionsResult execute() {
        return new CatalogOptionsResult(generalDataCatalog.findSectors().stream()
            .map(sector -> new CatalogOptionsResult.CatalogOption(sector.id(), sector.description()))
            .toList());
    }
}
