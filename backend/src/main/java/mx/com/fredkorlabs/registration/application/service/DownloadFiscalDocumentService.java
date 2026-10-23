package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.DownloadFiscalDocumentQuery;
import mx.com.fredkorlabs.registration.application.port.in.DownloadFiscalDocumentResult;
import mx.com.fredkorlabs.registration.application.port.in.DownloadFiscalDocumentUseCase;
import mx.com.fredkorlabs.registration.application.port.in.FiscalDocumentNotFoundException;
import mx.com.fredkorlabs.registration.application.port.out.FileMetadataPort;
import mx.com.fredkorlabs.registration.application.port.out.FileStoragePort;
import mx.com.fredkorlabs.registration.application.port.out.FiscalDocumentView;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationContextPort;
import mx.com.fredkorlabs.registration.application.port.out.StoredFileMetadata;
import mx.com.fredkorlabs.registration.application.port.out.SupplierRegistrationPersistencePort;

public class DownloadFiscalDocumentService implements DownloadFiscalDocumentUseCase {

    private final RegistrationContextPort registrationContext;
    private final SupplierRegistrationPersistencePort supplierRegistrationPersistence;
    private final FileMetadataPort fileMetadata;
    private final FileStoragePort fileStorage;

    public DownloadFiscalDocumentService(RegistrationContextPort registrationContext,
                                         SupplierRegistrationPersistencePort supplierRegistrationPersistence,
                                         FileMetadataPort fileMetadata, FileStoragePort fileStorage) {
        this.registrationContext = registrationContext;
        this.supplierRegistrationPersistence = supplierRegistrationPersistence;
        this.fileMetadata = fileMetadata;
        this.fileStorage = fileStorage;
    }

    @Override
    public DownloadFiscalDocumentResult execute(DownloadFiscalDocumentQuery query) {
        FiscalDocumentView document = OwnedRegistration.read(registrationContext, supplierRegistrationPersistence,
                query.registrationSessionId(), query.registrationId()).stored().fiscalDocument();
        // Only the active association counts: a dissociated file is not part of the registration (US10 CA-011).
        if (document == null) {
            throw new FiscalDocumentNotFoundException("No fiscal document is associated with the registration");
        }
        StoredFileMetadata metadata = fileMetadata.find(document.id())
            .orElseThrow(() -> new IllegalStateException("Associated fiscal document has no metadata"));
        return new DownloadFiscalDocumentResult(metadata.originalName(), metadata.mediaType(),
            fileStorage.read(metadata.relativePath()));
    }
}
