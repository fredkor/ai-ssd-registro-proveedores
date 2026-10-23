package mx.com.fredkorlabs.registration.testsupport;

import mx.com.fredkorlabs.registration.application.port.out.NoticeAcceptanceReference;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationStatusView;
import mx.com.fredkorlabs.registration.application.port.out.SignatureEvidenceReference;
import mx.com.fredkorlabs.registration.application.port.out.StoredRegistration;
import mx.com.fredkorlabs.registration.application.port.out.SupplierRegistrationPersistencePort;
import mx.com.fredkorlabs.registration.domain.model.Rfc;
import mx.com.fredkorlabs.registration.domain.model.StoredDocumentReference;
import mx.com.fredkorlabs.registration.domain.model.SupplierRegistration;
import mx.com.fredkorlabs.registration.domain.model.SupplierRegistrationId;

import java.util.Optional;

/** Persistence test double whose operations all fail; each test overrides only what its use case calls. */
public class UnusedSupplierRegistrationPersistence implements SupplierRegistrationPersistencePort {

    @Override
    public Optional<RegistrationStatusView> findStatusByRfc(Rfc rfc) {
        throw unused();
    }

    @Override
    public void insert(SupplierRegistration registration, SignatureEvidenceReference initialEvidence,
                       NoticeAcceptanceReference acceptance) {
        throw unused();
    }

    @Override
    public Optional<StoredRegistration> findForUpdate(SupplierRegistrationId id) {
        throw unused();
    }

    @Override
    public void saveResume(SupplierRegistration registration, SignatureEvidenceReference newEvidence,
                           NoticeAcceptanceReference newAcceptance) {
        throw unused();
    }

    @Override
    public void saveFiscalDocumentAssociation(SupplierRegistrationId registrationId, StoredDocumentReference document) {
        throw unused();
    }

    private static UnsupportedOperationException unused() {
        return new UnsupportedOperationException("Not used by this use case");
    }
}
