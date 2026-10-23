package mx.com.fredkorlabs.registration.application.port.in;

import java.util.List;

/** Options of a General Data catalog with id and description, ordered by id (ADR-0020). */
public record CatalogOptionsResult(List<CatalogOption> items) {

    public CatalogOptionsResult {
        items = List.copyOf(items);
    }

    public record CatalogOption(int id, String description) {}
}
