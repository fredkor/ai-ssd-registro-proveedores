package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.SupplierRegistrationResult;
import mx.com.fredkorlabs.registration.application.port.out.NoticeAcceptanceReference;
import mx.com.fredkorlabs.registration.domain.model.ApplicantIdentity;
import mx.com.fredkorlabs.registration.domain.model.SupplierRegistration;

/** Projection shared by the use cases that return the registration. */
final class SupplierRegistrationResults {

    private SupplierRegistrationResults() {
    }

    static SupplierRegistrationResult of(SupplierRegistration registration,
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
