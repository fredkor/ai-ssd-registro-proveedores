package mx.com.fredkorlabs.registration.application.port.out;

import mx.com.fredkorlabs.registration.domain.model.SupplierRegistration;

import java.util.Objects;
import java.util.Set;

/** A reconstituted registration with the store references of its acceptance and evidence history. */
public record StoredRegistration(
    SupplierRegistration registration,
    NoticeAcceptanceReference acceptanceReference,
    Set<SignatureEvidenceReference> evidenceReferences
) {
    public StoredRegistration {
        Objects.requireNonNull(registration, "Registration is required");
        Objects.requireNonNull(acceptanceReference, "Acceptance reference is required");
        evidenceReferences = Set.copyOf(evidenceReferences);
    }
}
