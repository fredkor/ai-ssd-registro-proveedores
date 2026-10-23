package mx.com.fredkorlabs.registration.domain.model;

/**
 * Catalog facts the application resolved for a General Data submission (domain model §5.2): {@code countryCode},
 * {@code legalOwnership} and {@code subsectorSectorId} are null when the selected id is not in its catalog.
 */
public record GeneralDataCatalogContext(
    String countryCode,
    LegalOwnershipOption legalOwnership,
    boolean sectorExists,
    Integer subsectorSectorId,
    boolean businessActivityExists,
    boolean stratificationActive,
    boolean primaryActivityExists,
    boolean workerCountRangeExists,
    boolean annualSalesRangeExists
) {}
