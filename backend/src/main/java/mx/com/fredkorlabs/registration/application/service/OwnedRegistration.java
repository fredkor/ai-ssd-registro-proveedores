package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.RegistrationAccessDeniedException;
import mx.com.fredkorlabs.registration.application.port.in.RegistrationNotFoundException;
import mx.com.fredkorlabs.registration.application.port.in.RegistrationSessionUnavailableException;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationContextPort;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationSession;
import mx.com.fredkorlabs.registration.application.port.out.StoredRegistration;
import mx.com.fredkorlabs.registration.application.port.out.SupplierRegistrationPersistencePort;
import mx.com.fredkorlabs.registration.domain.model.SupplierRegistrationId;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/** A registration that belongs to the token's applicant, with the token's session. */
record OwnedRegistration(RegistrationSession session, StoredRegistration stored) {

    /**
     * Resolves the token's context (ADR-0015), locks the registration (design §7.2) and checks that its RFC is
     * the token's one (INV-12): 401, 404 and 403 in that order.
     */
    static OwnedRegistration lock(RegistrationContextPort registrationContext,
                                  SupplierRegistrationPersistencePort supplierRegistrationPersistence,
                                  String registrationSessionId, UUID registrationId) {
        return owned(registrationContext, registrationSessionId,
            () -> supplierRegistrationPersistence.findForUpdate(new SupplierRegistrationId(registrationId)));
    }

    /** Same checks as {@link #lock} without locking the row, for read-only operations. */
    static OwnedRegistration read(RegistrationContextPort registrationContext,
                                  SupplierRegistrationPersistencePort supplierRegistrationPersistence,
                                  String registrationSessionId, UUID registrationId) {
        return owned(registrationContext, registrationSessionId,
            () -> supplierRegistrationPersistence.find(new SupplierRegistrationId(registrationId)));
    }

    private static OwnedRegistration owned(RegistrationContextPort registrationContext, String registrationSessionId,
                                           Supplier<Optional<StoredRegistration>> lookup) {
        RegistrationSession session = registrationContext.resolve(registrationSessionId)
            .orElseThrow(() -> new RegistrationSessionUnavailableException("Registration token has no context"));
        StoredRegistration stored = lookup.get()
            .orElseThrow(() -> new RegistrationNotFoundException("Registration does not exist"));
        if (!stored.registration().applicant().rfc().equals(session.identity().rfc())) {
            throw new RegistrationAccessDeniedException("Registration belongs to another applicant");
        }
        return new OwnedRegistration(session, stored);
    }
}
