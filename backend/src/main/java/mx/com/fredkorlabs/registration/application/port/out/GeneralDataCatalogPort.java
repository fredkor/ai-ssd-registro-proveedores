package mx.com.fredkorlabs.registration.application.port.out;

import java.util.List;

/** One query per General Data catalog, so each one can be cached on its own later (ADR-0020). */
public interface GeneralDataCatalogPort {

    /** Every country, ordered by id. */
    List<CountryRecord> findCountries();
}
