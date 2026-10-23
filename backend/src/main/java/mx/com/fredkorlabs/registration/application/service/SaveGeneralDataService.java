package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.SaveGeneralDataCommand;
import mx.com.fredkorlabs.registration.application.port.in.SaveGeneralDataUseCase;
import mx.com.fredkorlabs.registration.application.port.in.SectionSaveResult;
import mx.com.fredkorlabs.registration.application.port.out.GeneralDataCatalogPort;
import mx.com.fredkorlabs.registration.application.port.out.GeneralDataSelection;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationContextPort;
import mx.com.fredkorlabs.registration.application.port.out.SupplierRegistrationPersistencePort;
import mx.com.fredkorlabs.registration.domain.model.GeneralData;
import mx.com.fredkorlabs.registration.domain.model.GeneralDataInput;
import mx.com.fredkorlabs.registration.domain.model.Origin;
import mx.com.fredkorlabs.registration.domain.model.RegistrationSection;
import mx.com.fredkorlabs.registration.domain.model.SectionStatus;
import mx.com.fredkorlabs.registration.domain.model.SupplierRegistration;

public class SaveGeneralDataService implements SaveGeneralDataUseCase {

    private final RegistrationContextPort registrationContext;
    private final SupplierRegistrationPersistencePort supplierRegistrationPersistence;
    private final GeneralDataCatalogPort generalDataCatalog;

    public SaveGeneralDataService(RegistrationContextPort registrationContext,
                                  SupplierRegistrationPersistencePort supplierRegistrationPersistence,
                                  GeneralDataCatalogPort generalDataCatalog) {
        this.registrationContext = registrationContext;
        this.supplierRegistrationPersistence = supplierRegistrationPersistence;
        this.generalDataCatalog = generalDataCatalog;
    }

    @Override
    public SectionSaveResult execute(SaveGeneralDataCommand command) {
        // Locked for the whole save, so a concurrent fiscal document deletion is serialized (design §7.2).
        SupplierRegistration registration = OwnedRegistration.lock(registrationContext, supplierRegistrationPersistence,
            command.registrationSessionId(), command.registrationId()).stored().registration();
        registration.ensureGeneralDataCanBeSaved();

        GeneralData data = GeneralData.validate(input(command), registration.applicant().taxRegime(),
            generalDataCatalog.resolveGeneralDataReferences(new GeneralDataSelection(command.countryId(),
                command.legalOwnershipId(), command.sectorId(), command.subsectorId(), command.businessActivityId(),
                command.stratificationId(), command.primaryActivityId(), command.workerCountRangeId(),
                command.annualSalesRangeId())));
        // The fiscal document comes from its persisted association, never from the request (US10 CA-010).
        registration.saveGeneralData(data);
        supplierRegistrationPersistence.saveGeneralData(registration);

        // CONTACTS follows CUCoP and is out of scope in this stage; General Data always continues to CUCoP.
        return new SectionSaveResult(registration.id().value(), RegistrationSection.GENERAL_DATA.name(),
            SectionStatus.COMPLETED.name(), RegistrationSection.CUCOP.name());
    }

    private static GeneralDataInput input(SaveGeneralDataCommand command) {
        return new GeneralDataInput(command.origin() == null ? null : Origin.valueOf(command.origin()),
            command.countryId(), command.nationality(), command.foreignFiscalCode(), command.legalOwnershipId(),
            command.sectorId(), command.subsectorId(), command.businessActivityId(), command.stratificationId(),
            command.primaryActivityId(), command.workerCountRangeId(), command.annualSalesRangeId(), command.website(),
            command.phones() == null ? null : command.phones().stream()
                .map(phone -> phone == null ? null : new GeneralDataInput.PhoneInput(phone.number(), phone.extension()))
                .toList());
    }
}
