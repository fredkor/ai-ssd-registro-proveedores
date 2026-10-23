package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.GetLegalOwnershipsQuery;
import mx.com.fredkorlabs.registration.application.port.in.GetLegalOwnershipsResult;
import mx.com.fredkorlabs.registration.application.port.in.GetLegalOwnershipsUseCase;
import mx.com.fredkorlabs.registration.application.port.in.InvalidQueryParameterException;
import mx.com.fredkorlabs.registration.application.port.out.GeneralDataCatalogPort;
import mx.com.fredkorlabs.registration.domain.model.LegalOwnershipOption;
import mx.com.fredkorlabs.registration.domain.model.Origin;
import mx.com.fredkorlabs.registration.domain.model.TaxRegime;

public class GetLegalOwnershipsService implements GetLegalOwnershipsUseCase {

    private final GeneralDataCatalogPort generalDataCatalog;

    public GetLegalOwnershipsService(GeneralDataCatalogPort generalDataCatalog) {
        this.generalDataCatalog = generalDataCatalog;
    }

    @Override
    public GetLegalOwnershipsResult execute(GetLegalOwnershipsQuery query) {
        Origin origin = Origin.valueOf(query.origin());
        TaxRegime regime = query.taxRegime() == null ? null : TaxRegime.valueOf(query.taxRegime());
        // ADR-0020: each combination of filters identifies a single cacheable result.
        if (origin == Origin.NATIONAL && regime == null) {
            throw new InvalidQueryParameterException("taxRegime", "is required when origin is NATIONAL");
        }
        if (origin == Origin.FOREIGN && regime != null) {
            throw new InvalidQueryParameterException("taxRegime", "is not allowed when origin is FOREIGN");
        }
        if (!generalDataCatalog.countryExists(query.countryCode())) {
            throw new InvalidQueryParameterException("countryCode", "is not in the country catalog");
        }

        return new GetLegalOwnershipsResult(LegalOwnershipOption.applicable(
                generalDataCatalog.findLegalOwnershipCandidates(query.countryCode()), origin, query.countryCode(), regime)
            .stream()
            .map(option -> new GetLegalOwnershipsResult.LegalOwnershipItem(option.id(), option.key(),
                option.description(), option.countryCode(), option.regime() == null ? null : option.regime().name()))
            .toList());
    }
}
