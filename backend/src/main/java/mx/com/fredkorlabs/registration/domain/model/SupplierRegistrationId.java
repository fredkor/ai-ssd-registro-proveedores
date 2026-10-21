package mx.com.fredkorlabs.registration.domain.model;

import java.util.UUID;

public record SupplierRegistrationId(UUID value) {
    public SupplierRegistrationId {
        if (value == null) throw new IllegalArgumentException("Registration ID cannot be null");
    }
}
