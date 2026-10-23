package mx.com.fredkorlabs.registration.adapter.in.web;

import mx.com.fredkorlabs.registration.adapter.in.web.generated.RegistrationCatalogsApi;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.CatalogOptionListResponse;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.CatalogOptionResponse;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.CountryOptionListResponse;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.CountryOptionResponse;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.CucopCatalogResponse;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.CucopTreeNodeResponse;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.LegalOwnershipOptionListResponse;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.LegalOwnershipOptionResponse;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.Origin;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.TaxRegime;
import mx.com.fredkorlabs.registration.application.port.in.CatalogOptionsResult;
import mx.com.fredkorlabs.registration.application.port.in.GetAnnualSalesRangesUseCase;
import mx.com.fredkorlabs.registration.application.port.in.GetBusinessActivitiesUseCase;
import mx.com.fredkorlabs.registration.application.port.in.GetCountriesUseCase;
import mx.com.fredkorlabs.registration.application.port.in.GetCucopCatalogResult.CucopNode;
import mx.com.fredkorlabs.registration.application.port.in.GetCucopCatalogUseCase;
import mx.com.fredkorlabs.registration.application.port.in.GetLegalOwnershipsQuery;
import mx.com.fredkorlabs.registration.application.port.in.GetLegalOwnershipsUseCase;
import mx.com.fredkorlabs.registration.application.port.in.GetPrimaryActivitiesUseCase;
import mx.com.fredkorlabs.registration.application.port.in.GetSectorsUseCase;
import mx.com.fredkorlabs.registration.application.port.in.GetStratificationsUseCase;
import mx.com.fredkorlabs.registration.application.port.in.GetSubsectorsQuery;
import mx.com.fredkorlabs.registration.application.port.in.GetSubsectorsUseCase;
import mx.com.fredkorlabs.registration.application.port.in.GetTaxRegimesUseCase;
import mx.com.fredkorlabs.registration.application.port.in.GetWorkerCountRangesUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
public class RegistrationCatalogsController implements RegistrationCatalogsApi {

    private static final Set<String> LEGAL_OWNERSHIP_PARAMETERS = Set.of("origin", "countryCode", "taxRegime");

    private final GetTaxRegimesUseCase getTaxRegimes;
    private final GetCountriesUseCase getCountries;
    private final GetLegalOwnershipsUseCase getLegalOwnerships;
    private final GetSectorsUseCase getSectors;
    private final GetSubsectorsUseCase getSubsectors;
    private final GetBusinessActivitiesUseCase getBusinessActivities;
    private final GetStratificationsUseCase getStratifications;
    private final GetPrimaryActivitiesUseCase getPrimaryActivities;
    private final GetWorkerCountRangesUseCase getWorkerCountRanges;
    private final GetAnnualSalesRangesUseCase getAnnualSalesRanges;
    private final GetCucopCatalogUseCase getCucopCatalog;

    public RegistrationCatalogsController(GetTaxRegimesUseCase getTaxRegimes,
                                          GetCountriesUseCase getCountries,
                                          GetLegalOwnershipsUseCase getLegalOwnerships,
                                          GetSectorsUseCase getSectors,
                                          GetSubsectorsUseCase getSubsectors,
                                          GetBusinessActivitiesUseCase getBusinessActivities,
                                          GetStratificationsUseCase getStratifications,
                                          GetPrimaryActivitiesUseCase getPrimaryActivities,
                                          GetWorkerCountRangesUseCase getWorkerCountRanges,
                                          GetAnnualSalesRangesUseCase getAnnualSalesRanges,
                                          GetCucopCatalogUseCase getCucopCatalog) {
        this.getTaxRegimes = getTaxRegimes;
        this.getCountries = getCountries;
        this.getLegalOwnerships = getLegalOwnerships;
        this.getSectors = getSectors;
        this.getSubsectors = getSubsectors;
        this.getBusinessActivities = getBusinessActivities;
        this.getStratifications = getStratifications;
        this.getPrimaryActivities = getPrimaryActivities;
        this.getWorkerCountRanges = getWorkerCountRanges;
        this.getAnnualSalesRanges = getAnnualSalesRanges;
        this.getCucopCatalog = getCucopCatalog;
    }

    @Override
    public ResponseEntity<CatalogOptionListResponse> getTaxRegimes() {
        return ResponseEntity.ok(toCatalogOptions(getTaxRegimes.execute()));
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
        QueryParameters.requireOnly(LEGAL_OWNERSHIP_PARAMETERS);
        return ResponseEntity.ok(new LegalOwnershipOptionListResponse(getLegalOwnerships.execute(
                new GetLegalOwnershipsQuery(origin.getValue(), countryCode,
                    taxRegime == null ? null : taxRegime.getValue()))
            .items().stream()
            .map(option -> {
                LegalOwnershipOptionResponse response = new LegalOwnershipOptionResponse(
                    option.key(), option.id(), option.description(), option.countryCode());
                return option.taxRegime() == null ? response : response.taxRegime(TaxRegime.fromValue(option.taxRegime()));
            })
            .toList()));
    }

    @Override
    public ResponseEntity<CatalogOptionListResponse> getSectors() {
        return ResponseEntity.ok(toCatalogOptions(getSectors.execute()));
    }

    @Override
    public ResponseEntity<CatalogOptionListResponse> getSubsectors(Integer sectorId) {
        return ResponseEntity.ok(toCatalogOptions(getSubsectors.execute(new GetSubsectorsQuery(sectorId))));
    }

    @Override
    public ResponseEntity<CatalogOptionListResponse> getBusinessActivities() {
        return ResponseEntity.ok(toCatalogOptions(getBusinessActivities.execute()));
    }

    @Override
    public ResponseEntity<CatalogOptionListResponse> getStratifications() {
        return ResponseEntity.ok(toCatalogOptions(getStratifications.execute()));
    }

    @Override
    public ResponseEntity<CatalogOptionListResponse> getPrimaryActivities() {
        return ResponseEntity.ok(toCatalogOptions(getPrimaryActivities.execute()));
    }

    @Override
    public ResponseEntity<CatalogOptionListResponse> getWorkerCountRanges() {
        return ResponseEntity.ok(toCatalogOptions(getWorkerCountRanges.execute()));
    }

    @Override
    public ResponseEntity<CatalogOptionListResponse> getAnnualSalesRanges() {
        return ResponseEntity.ok(toCatalogOptions(getAnnualSalesRanges.execute()));
    }

    @Override
    public ResponseEntity<CucopCatalogResponse> getCucopCatalog() {
        return ResponseEntity.ok(new CucopCatalogResponse(getCucopCatalog.execute().items().stream()
            .map(RegistrationCatalogsController::toTreeNode)
            .toList()));
    }

    private static CatalogOptionListResponse toCatalogOptions(CatalogOptionsResult result) {
        return new CatalogOptionListResponse(result.items().stream()
            .map(option -> new CatalogOptionResponse(option.id(), option.description()))
            .toList());
    }

    private static CucopTreeNodeResponse toTreeNode(CucopNode node) {
        return new CucopTreeNodeResponse(node.id(), node.key(), node.description(),
            CucopTreeNodeResponse.LevelEnum.fromValue(node.level()),
            node.items().stream().map(RegistrationCatalogsController::toTreeNode).toList());
    }
}
