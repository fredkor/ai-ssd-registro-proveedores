package mx.com.fredkorlabs.registration.testsupport;

import mx.com.fredkorlabs.registration.application.port.out.CucopCatalog;
import mx.com.fredkorlabs.registration.application.port.out.CucopCatalogPort;
import mx.com.fredkorlabs.registration.domain.model.CucopSpecificItem;

import java.util.Collection;
import java.util.List;

/** CUCoP catalog test double whose queries all fail; each test overrides only the queries its use case makes. */
public class UnusedCucopCatalog implements CucopCatalogPort {

    @Override
    public CucopCatalog findCatalog() {
        throw unused();
    }

    @Override
    public List<CucopSpecificItem> findSpecificItems(Collection<Integer> ids) {
        throw unused();
    }

    private static UnsupportedOperationException unused() {
        return new UnsupportedOperationException("Not used by this use case");
    }
}
