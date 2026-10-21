package mx.com.fredkorlabs.registration.domain.model;

import mx.com.fredkorlabs.registration.domain.exception.InvalidForeignFiscalCodeException;

public record ForeignFiscalCode(String value) {
    public ForeignFiscalCode {
        if (value == null || value.isBlank()) {
            throw new InvalidForeignFiscalCodeException("Foreign fiscal code cannot be blank");
        }
        if (value.length() > 20) {
            throw new InvalidForeignFiscalCodeException(
                "Foreign fiscal code must not exceed 20 characters, got: " + value.length());
        }
    }
}
