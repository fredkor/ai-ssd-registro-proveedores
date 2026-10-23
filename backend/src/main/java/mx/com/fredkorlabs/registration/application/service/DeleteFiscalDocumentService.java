package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.DeleteFiscalDocumentCommand;
import mx.com.fredkorlabs.registration.application.port.in.DeleteFiscalDocumentUseCase;
import mx.com.fredkorlabs.registration.application.port.in.FiscalDocumentNotFoundException;
import mx.com.fredkorlabs.registration.application.port.out.FileMetadataPort;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationContextPort;
import mx.com.fredkorlabs.registration.application.port.out.SupplierRegistrationPersistencePort;
import mx.com.fredkorlabs.registration.domain.model.FiscalIdentificationDocument;
import mx.com.fredkorlabs.registration.domain.model.SupplierRegistration;

public class DeleteFiscalDocumentService implements DeleteFiscalDocumentUseCase {

    private final RegistrationContextPort registrationContext;
    private final SupplierRegistrationPersistencePort supplierRegistrationPersistence;
    private final FileMetadataPort fileMetadata;

    public DeleteFiscalDocumentService(RegistrationContextPort registrationContext,
                                       SupplierRegistrationPersistencePort supplierRegistrationPersistence,
                                       FileMetadataPort fileMetadata) {
        this.registrationContext = registrationContext;
        this.supplierRegistrationPersistence = supplierRegistrationPersistence;
        this.fileMetadata = fileMetadata;
    }

    @Override
    public void execute(DeleteFiscalDocumentCommand command) {
        SupplierRegistration registration = OwnedRegistration.lock(registrationContext, supplierRegistrationPersistence,
            command.registrationSessionId(), command.registrationId()).stored().registration();
        registration.ensureFiscalDocumentCanBeDissociated();
        FiscalIdentificationDocument document = registration.associatedDocument();
        if (document == null) {
            throw new FiscalDocumentNotFoundException("No fiscal document is associated with the registration");
        }

        // One database transaction, no file content touched: the daily cleanup deletes it later (ADR-0010).
        registration.dissociateFiscalDocument();
        supplierRegistrationPersistence.saveFiscalDocumentDissociation(registration);
        fileMetadata.markNotAssociated(document.storedFile().value());
    }
}
