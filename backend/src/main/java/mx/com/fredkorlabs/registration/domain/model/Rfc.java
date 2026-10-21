package mx.com.fredkorlabs.registration.domain.model;

import mx.com.fredkorlabs.registration.domain.exception.InvalidRfcException;

public record Rfc(String value) {
    public Rfc {
        if (value == null || (value.length() != 12 && value.length() != 13)) {
            throw new InvalidRfcException(
                "RFC must be 12 or 13 characters, got: " + (value == null ? "null" : value.length()));
        }
    }
}
