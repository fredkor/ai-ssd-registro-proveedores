package mx.com.fredkorlabs.registration.application.port.out;

/** The whole CUCoP catalog in a single query (RF-REG-08); search and expansion happen in the frontend. */
public interface CucopCatalogPort {

    CucopCatalog findCatalog();
}
