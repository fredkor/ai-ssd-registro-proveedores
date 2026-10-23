package mx.com.fredkorlabs.registration.application.port.out;

import mx.com.fredkorlabs.registration.domain.model.Rfc;
import mx.com.fredkorlabs.registration.domain.model.SupplierRegistration;

import java.util.Optional;

public interface SupplierRegistrationPersistencePort {

    /** Empty only when no registration exists for the RFC; query failures propagate (INV-09). */
    Optional<RegistrationStatusView> findStatusByRfc(Rfc rfc);

    /**
     * Inserts a new registration with its progress and its initial evidence in the evidence history.
     * Throws {@link DuplicateRegistrationException} when the RFC, acceptance or evidence is already taken.
     */
    void insert(SupplierRegistration registration, SignatureEvidenceReference initialEvidence,
                NoticeAcceptanceReference acceptance);
}
