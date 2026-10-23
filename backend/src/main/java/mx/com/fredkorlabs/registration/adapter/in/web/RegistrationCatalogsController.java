package mx.com.fredkorlabs.registration.adapter.in.web;

import mx.com.fredkorlabs.registration.adapter.in.web.generated.RegistrationCatalogsApi;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.CatalogOptionListResponse;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.CountryOptionListResponse;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.CountryOptionResponse;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.CucopCatalogResponse;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.LegalOwnershipOptionListResponse;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.Origin;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.TaxRegime;
import mx.com.fredkorlabs.registration.application.port.in.GetCountriesUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegistrationCatalogsController implements RegistrationCatalogsApi {

    private final GetCountriesUseCase getCountries;

    public RegistrationCatalogsController(GetCountriesUseCase getCountries) {
        this.getCountries = getCountries;
    }

    @Override
    public ResponseEntity<CountryOptionListResponse> getCountries() {
        return ResponseEntity.ok(new CountryOptionListResponse(getCountries.execute().items().stream()
            .map(country -> new CountryOptionResponse(country.id(), country.description(), country.countryCode()))
            .toList()));
    }

    @Override
    public ResponseEntity<LegalOwnershipOptionListResponse> getLegalOwnerships(Origin origin, String countryCode,
                                                                               TaxRegime taxRegime) {
        throw new UnsupportedOperationException("Not implemented in this iteration");
    }

    @Override
    public ResponseEntity<CatalogOptionListResponse> getSectors() {
        throw new UnsupportedOperationException("Not implemented in this iteration");
    }

    @Override
    public ResponseEntity<CatalogOptionListResponse> getSubsectors(Integer sectorId) {
        throw new UnsupportedOperationException("Not implemented in this iteration");
    }

    @Override
    public ResponseEntity<CatalogOptionListResponse> getBusinessActivities() {
        throw new UnsupportedOperationException("Not implemented in this iteration");
    }

    @Override
    public ResponseEntity<CatalogOptionListResponse> getStratifications() {
        throw new UnsupportedOperationException("Not implemented in this iteration");
    }

    @Override
    public ResponseEntity<CatalogOptionListResponse> getPrimaryActivities() {
        throw new UnsupportedOperationException("Not implemented in this iteration");
    }

    @Override
    public ResponseEntity<CatalogOptionListResponse> getWorkerCountRanges() {
        throw new UnsupportedOperationException("Not implemented in this iteration");
    }

    @Override
    public ResponseEntity<CatalogOptionListResponse> getAnnualSalesRanges() {
        throw new UnsupportedOperationException("Not implemented in this iteration");
    }

    @Override
    public ResponseEntity<CucopCatalogResponse> getCucopCatalog() {
        throw new UnsupportedOperationException("Not implemented in this iteration");
    }
}
