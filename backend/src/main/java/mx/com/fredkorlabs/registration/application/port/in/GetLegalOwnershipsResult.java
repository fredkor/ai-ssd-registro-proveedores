package mx.com.fredkorlabs.registration.application.port.in;

import java.util.List;

/** Applicable options in ascending key order; {@code countryCode} and {@code taxRegime} may be null. */
public record GetLegalOwnershipsResult(List<LegalOwnershipItem> items) {

    public GetLegalOwnershipsResult {
        items = List.copyOf(items);
    }

    public record LegalOwnershipItem(int id, String key, String description, String countryCode, String taxRegime) {}
}
