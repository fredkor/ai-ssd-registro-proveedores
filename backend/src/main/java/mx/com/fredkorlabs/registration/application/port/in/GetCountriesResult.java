package mx.com.fredkorlabs.registration.application.port.in;

import java.util.List;

/** Countries ordered by id (ADR-0020); {@code description} is the Spanish name. */
public record GetCountriesResult(List<CountryItem> items) {

    public GetCountriesResult {
        items = List.copyOf(items);
    }

    public record CountryItem(int id, String description, String countryCode) {}
}
