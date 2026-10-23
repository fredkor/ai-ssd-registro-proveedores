package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.ResumeSupplierRegistrationCommand;
import mx.com.fredkorlabs.registration.application.port.in.ResumeSupplierRegistrationUseCase;
import mx.com.fredkorlabs.registration.application.port.in.SupplierRegistrationResult;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationContextPort;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationSession;
import mx.com.fredkorlabs.registration.application.port.out.StoredRegistration;
import mx.com.fredkorlabs.registration.application.port.out.SupplierRegistrationPersistencePort;
import mx.com.fredkorlabs.registration.domain.model.SupplierRegistration;

public class ResumeSupplierRegistrationService implements ResumeSupplierRegistrationUseCase {

    private final RegistrationContextPort registrationContext;
    private final SupplierRegistrationPersistencePort supplierRegistrationPersistence;

    public ResumeSupplierRegistrationService(RegistrationContextPort registrationContext,
                                             SupplierRegistrationPersistencePort supplierRegistrationPersistence) {
        this.registrationContext = registrationContext;
        this.supplierRegistrationPersistence = supplierRegistrationPersistence;
    }

    @Override
    public SupplierRegistrationResult execute(ResumeSupplierRegistrationCommand command) {
        // Identity, evidence and acceptance come only from the token's context (ADR-0015); INV-12 owner check.
        OwnedRegistration owned = OwnedRegistration.lock(registrationContext, supplierRegistrationPersistence,
            command.registrationSessionId(), command.registrationId());
        RegistrationSession session = owned.session();
        StoredRegistration stored = owned.stored();
        SupplierRegistration registration = stored.registration();

        // INV-13: a REGISTERED registration cannot be resumed as a draft.
        registration.ensureResumable();

        // An identification is applied once: its evidence already in the history means nothing to change.
        if (stored.evidenceReferences().contains(session.evidenceReference())) {
            return SupplierRegistrationResults.of(registration, stored.acceptanceReference(), stored.fiscalDocument());
        }

        registration.resume(session.identity().legalName(), session.acceptance(), session.evidence());
        supplierRegistrationPersistence.saveResume(registration, session.evidenceReference(),
            session.acceptanceReference());
        return SupplierRegistrationResults.of(registration, session.acceptanceReference(), stored.fiscalDocument());
    }
}
