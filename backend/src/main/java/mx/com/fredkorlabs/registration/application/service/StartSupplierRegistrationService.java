package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.RegistrationAlreadyExistsException;
import mx.com.fredkorlabs.registration.application.port.in.RegistrationSessionUnavailableException;
import mx.com.fredkorlabs.registration.application.port.in.StartSupplierRegistrationCommand;
import mx.com.fredkorlabs.registration.application.port.in.StartSupplierRegistrationUseCase;
import mx.com.fredkorlabs.registration.application.port.in.SupplierRegistrationResult;
import mx.com.fredkorlabs.registration.application.port.out.DuplicateRegistrationException;
import mx.com.fredkorlabs.registration.application.port.out.NoticeAcceptanceReference;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationContextPort;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationSession;
import mx.com.fredkorlabs.registration.application.port.out.SupplierRegistrationPersistencePort;
import mx.com.fredkorlabs.registration.domain.model.ApplicantIdentity;
import mx.com.fredkorlabs.registration.domain.model.SupplierRegistration;
import mx.com.fredkorlabs.registration.domain.model.SupplierRegistrationId;

import java.util.UUID;

public class StartSupplierRegistrationService implements StartSupplierRegistrationUseCase {

    private static final String DUPLICATE_DETAIL = "A registration already exists for this RFC";

    private final RegistrationContextPort registrationContext;
    private final SupplierRegistrationPersistencePort supplierRegistrationPersistence;

    public StartSupplierRegistrationService(RegistrationContextPort registrationContext,
                                            SupplierRegistrationPersistencePort supplierRegistrationPersistence) {
        this.registrationContext = registrationContext;
        this.supplierRegistrationPersistence = supplierRegistrationPersistence;
    }

    @Override
    public SupplierRegistrationResult execute(StartSupplierRegistrationCommand command) {
        // Identity, evidence and acceptance come only from the token's context (ADR-0015).
        RegistrationSession session = registrationContext.resolve(command.registrationSessionId())
            .orElseThrow(() -> new RegistrationSessionUnavailableException("Registration token has no context"));
        ApplicantIdentity identity = session.identity();

        // INV-09/INV-10: non-existence must be confirmed; a lookup failure propagates, it is not absence.
        if (supplierRegistrationPersistence.findStatusByRfc(identity.rfc()).isPresent()) {
            throw new RegistrationAlreadyExistsException(DUPLICATE_DETAIL);
        }

        SupplierRegistration registration = SupplierRegistration.start(
            new SupplierRegistrationId(UUID.randomUUID()), identity, session.acceptance(), session.evidence());
        try {
            supplierRegistrationPersistence.insert(registration, session.evidenceReference(),
                session.acceptanceReference());
        } catch (DuplicateRegistrationException e) {
            // A concurrent request created it after the lookup; the unique RFC constraint decides.
            throw new RegistrationAlreadyExistsException(DUPLICATE_DETAIL, e);
        }
        return toResult(registration, session.acceptanceReference());
    }

    private static SupplierRegistrationResult toResult(SupplierRegistration registration,
                                                       NoticeAcceptanceReference acceptanceReference) {
        ApplicantIdentity identity = registration.applicant();
        return new SupplierRegistrationResult(
            registration.id().value(),
            registration.status().name(),
            identity.rfc().value(),
            identity.legalName(),
            identity.taxRegime().name(),
            identity.username().value(),
            registration.latestAcceptance().version(),
            registration.latestAcceptance().acceptedAt(),
            acceptanceReference.value(),
            registration.progress().stream()
                .map(progress -> new SupplierRegistrationResult.SectionProgressItem(
                    progress.section().name(), progress.status().name()))
                .toList());
    }
}
