package mx.com.fredkorlabs.registration.domain.model;

import mx.com.fredkorlabs.registration.domain.exception.InvalidNationalityException;

public record Nationality(String value) {
    public Nationality {
        if (value == null || value.isBlank()) {
            throw new InvalidNationalityException("Nationality cannot be blank");
        }
        if (value.length() > 250) {
            throw new InvalidNationalityException("Nationality must not exceed 250 characters");
        }
    }
}
