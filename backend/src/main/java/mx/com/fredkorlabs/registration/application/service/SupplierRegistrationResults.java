package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.SupplierRegistrationResult;
import mx.com.fredkorlabs.registration.application.port.out.FiscalDocumentView;
import mx.com.fredkorlabs.registration.application.port.out.NoticeAcceptanceReference;
import mx.com.fredkorlabs.registration.domain.model.ApplicantIdentity;
import mx.com.fredkorlabs.registration.domain.model.CucopParticipation;
import mx.com.fredkorlabs.registration.domain.model.CucopSpecificItem;
import mx.com.fredkorlabs.registration.domain.model.GeneralData;
import mx.com.fredkorlabs.registration.domain.model.SupplierRegistration;

import java.util.Comparator;

/** Projection shared by the use cases that return the registration. */
final class SupplierRegistrationResults {

    private SupplierRegistrationResults() {
    }

    static SupplierRegistrationResult of(SupplierRegistration registration,
                                         NoticeAcceptanceReference acceptanceReference) {
        return of(registration, acceptanceReference, null);
    }

    static SupplierRegistrationResult of(SupplierRegistration registration,
                                         NoticeAcceptanceReference acceptanceReference,
                                         FiscalDocumentView fiscalDocument) {
        ApplicantIdentity identity = registration.applicant();
        return new SupplierRegistrationResult(
            registration.id().value(),
            registration.status().name(),
            identity.rfc().value(),
            identity.legalName(),
            identity.taxRegime().name(),
            identity.taxRegime().catalogId(),
            identity.username().value(),
            registration.latestAcceptance().version(),
            registration.latestAcceptance().acceptedAt(),
            acceptanceReference.value(),
            registration.progress().stream()
                .map(progress -> new SupplierRegistrationResult.SectionProgressItem(
                    progress.section().name(), progress.status().name()))
                .toList(),
            fiscalDocument == null ? null : new SupplierRegistrationResult.FiscalDocumentItem(fiscalDocument.id(),
                fiscalDocument.originalName(), fiscalDocument.extension(), fiscalDocument.sizeBytes(),
                fiscalDocument.uploadedAt()),
            generalDataOf(registration.generalData()),
            cucopParticipationOf(registration.cucopParticipation()));
    }

    private static SupplierRegistrationResult.CucopParticipationItem cucopParticipationOf(
            CucopParticipation participation) {
        if (participation == null) {
            return null;
        }
        return new SupplierRegistrationResult.CucopParticipationItem(
            participation.participationTypes().stream().sorted().map(Enum::name).toList(),
            participation.specificItems().stream()
                .sorted(Comparator.comparingInt(CucopSpecificItem::id))
                .map(item -> new SupplierRegistrationResult.SpecificItem(item.id(), item.key(), item.description(),
                    item.level(), item.genericItemId()))
                .toList());
    }

    private static SupplierRegistrationResult.GeneralDataItem generalDataOf(GeneralData data) {
        if (data == null) {
            return null;
        }
        return new SupplierRegistrationResult.GeneralDataItem(data.origin().name(), data.countryId(),
            data.nationality().value(), data.foreignFiscalCode() == null ? null : data.foreignFiscalCode().value(),
            data.legalOwnershipId(), data.sectorId(), data.subsectorId(), data.businessActivityId(),
            data.stratificationId(), data.primaryActivityId(), data.workerCountRangeId(), data.annualSalesRangeId(),
            data.website() == null ? null : data.website().value(),
            data.phones() == null ? null : data.phones().stream()
                .map(phone -> new SupplierRegistrationResult.PhoneItem(phone.number(), phone.extension()))
                .toList());
    }
}
