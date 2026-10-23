package mx.com.fredkorlabs.registration.application.port.out;

import mx.com.fredkorlabs.registration.domain.model.LegalOwnershipOption;

import java.util.List;

/** One query per General Data catalog, so each one can be cached on its own later (ADR-0020). */
public interface GeneralDataCatalogPort {

    /** Every country, ordered by id. */
    List<CountryRecord> findCountries();

    /** True when the code belongs to the country catalog (exact comparison). */
    boolean countryExists(String countryCode);

    /** Legal ownership options of the country plus NOT_DEF, in no particular order. */
    List<LegalOwnershipOption> findLegalOwnershipCandidates(String countryCode);

    /** Every sector, ordered by id. */
    List<CatalogRecord> findSectors();

    /** Subsectors of the sector, ordered by id; empty for an unknown sector. */
    List<CatalogRecord> findSubsectors(int sectorId);

    /** Every business activity (Giro), ordered by id. */
    List<CatalogRecord> findBusinessActivities();

    /** Active stratifications only, ordered by id. */
    List<CatalogRecord> findActiveStratifications();

    /** Every primary activity (Actividad preponderante), ordered by id. */
    List<CatalogRecord> findPrimaryActivities();

    /** Every worker count range (Número de trabajadores), ordered by id. */
    List<CatalogRecord> findWorkerCountRanges();
}
