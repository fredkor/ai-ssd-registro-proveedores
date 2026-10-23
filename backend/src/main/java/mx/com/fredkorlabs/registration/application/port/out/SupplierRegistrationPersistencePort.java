package mx.com.fredkorlabs.registration.application.port.out;

import mx.com.fredkorlabs.registration.domain.model.Rfc;
import mx.com.fredkorlabs.registration.domain.model.StoredDocumentReference;
import mx.com.fredkorlabs.registration.domain.model.SupplierRegistration;
import mx.com.fredkorlabs.registration.domain.model.SupplierRegistrationId;

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

    /** Reconstitutes the registration and locks its row until the transaction ends (design §7.2). */
    Optional<StoredRegistration> findForUpdate(SupplierRegistrationId id);

    /** Reconstitutes the registration without locking it, for read-only operations. */
    Optional<StoredRegistration> find(SupplierRegistrationId id);

    /** Stores a resumption: verified legal name, latest acceptance and the new evidence in the history. */
    void saveResume(SupplierRegistration registration, SignatureEvidenceReference newEvidence,
                    NoticeAcceptanceReference newAcceptance);

    /** Stores the active fiscal document reference of the registration (at most one, design §4). */
    void saveFiscalDocumentAssociation(SupplierRegistrationId registrationId, StoredDocumentReference document);

    /** Removes the active fiscal document reference and stores the resulting section progress (INV-29). */
    void saveFiscalDocumentDissociation(SupplierRegistration registration);
}
