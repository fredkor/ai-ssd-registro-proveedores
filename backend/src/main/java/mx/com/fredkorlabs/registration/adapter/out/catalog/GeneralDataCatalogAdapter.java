package mx.com.fredkorlabs.registration.adapter.out.catalog;

import mx.com.fredkorlabs.registration.application.port.out.CountryRecord;
import mx.com.fredkorlabs.registration.application.port.out.GeneralDataCatalogPort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GeneralDataCatalogAdapter implements GeneralDataCatalogPort {

    private final CountryJpaRepository countries;

    public GeneralDataCatalogAdapter(CountryJpaRepository countries) {
        this.countries = countries;
    }

    @Override
    public List<CountryRecord> findCountries() {
        return countries.findAllByOrderByIdAsc().stream()
            .map(country -> new CountryRecord(country.getId(), country.getCode(), country.getName()))
            .toList();
    }
}
