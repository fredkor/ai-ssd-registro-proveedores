package mx.com.fredkorlabs.registration.application.port.out;

import mx.com.fredkorlabs.registration.domain.model.RegistrationStatus;
import mx.com.fredkorlabs.registration.domain.model.SupplierRegistrationId;

import java.util.Objects;

/** Identifier and status of a registration, without rebuilding the whole aggregate. */
public record RegistrationStatusView(SupplierRegistrationId id, RegistrationStatus status) {
    public RegistrationStatusView {
        Objects.requireNonNull(id, "Registration id is required");
        Objects.requireNonNull(status, "Registration status is required");
    }
}
