package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.SaveCucopParticipationCommand;
import mx.com.fredkorlabs.registration.application.port.in.SaveCucopParticipationUseCase;
import mx.com.fredkorlabs.registration.application.port.in.SectionSaveResult;
import mx.com.fredkorlabs.registration.application.port.out.CucopCatalogPort;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationContextPort;
import mx.com.fredkorlabs.registration.application.port.out.SupplierRegistrationPersistencePort;
import mx.com.fredkorlabs.registration.domain.model.CucopParticipation;
import mx.com.fredkorlabs.registration.domain.model.CucopSpecificItem;
import mx.com.fredkorlabs.registration.domain.model.ParticipationType;
import mx.com.fredkorlabs.registration.domain.model.RegistrationSection;
import mx.com.fredkorlabs.registration.domain.model.SectionStatus;
import mx.com.fredkorlabs.registration.domain.model.SupplierRegistration;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SaveCucopParticipationService implements SaveCucopParticipationUseCase {

    private final RegistrationContextPort registrationContext;
    private final SupplierRegistrationPersistencePort supplierRegistrationPersistence;
    private final CucopCatalogPort cucopCatalog;

    public SaveCucopParticipationService(RegistrationContextPort registrationContext,
                                         SupplierRegistrationPersistencePort supplierRegistrationPersistence,
                                         CucopCatalogPort cucopCatalog) {
        this.registrationContext = registrationContext;
        this.supplierRegistrationPersistence = supplierRegistrationPersistence;
        this.cucopCatalog = cucopCatalog;
    }

    @Override
    public SectionSaveResult execute(SaveCucopParticipationCommand command) {
        // Locked for the whole save, like every section save (design §7.2).
        SupplierRegistration registration = OwnedRegistration.lock(registrationContext, supplierRegistrationPersistence,
            command.registrationSessionId(), command.registrationId()).stored().registration();
        registration.ensureCucopParticipationCanBeSaved();

        Map<Integer, CucopSpecificItem> catalogItems = cucopCatalog.findSpecificItems(
                command.specificItemIds() == null ? List.of()
                    : command.specificItemIds().stream().filter(Objects::nonNull).distinct().toList())
            .stream().collect(Collectors.toMap(CucopSpecificItem::id, Function.identity()));
        CucopParticipation participation = CucopParticipation.validate(
            command.participationTypes() == null ? null : command.participationTypes().stream()
                .map(type -> type == null ? null : ParticipationType.valueOf(type))
                .toList(),
            command.specificItemIds(), catalogItems);
        registration.saveCucopParticipation(participation);
        supplierRegistrationPersistence.saveCucopParticipation(registration);

        // CONTACTS is the continuation point; its capture is out of scope in this stage (RF-REG-08).
        return new SectionSaveResult(registration.id().value(), RegistrationSection.CUCOP.name(),
            SectionStatus.COMPLETED.name(), RegistrationSection.CONTACTS.name());
    }
}
