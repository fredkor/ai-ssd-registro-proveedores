package mx.com.fredkorlabs.registration.application.port.out;

import mx.com.fredkorlabs.registration.domain.model.CucopSpecificItem;

import java.util.Collection;
import java.util.List;

/** The whole CUCoP catalog in a single query (RF-REG-08); search and expansion happen in the frontend. */
public interface CucopCatalogPort {

    CucopCatalog findCatalog();

    /** The specific items (level 4) among the ids; ids of other levels or outside the catalog are absent. */
    List<CucopSpecificItem> findSpecificItems(Collection<Integer> ids);
}
