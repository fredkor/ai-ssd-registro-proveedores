package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.UploadFiscalDocumentCommand;
import mx.com.fredkorlabs.registration.application.port.in.UploadFiscalDocumentResult;
import mx.com.fredkorlabs.registration.application.port.in.UploadFiscalDocumentUseCase;
import mx.com.fredkorlabs.registration.application.port.out.FileMetadataPort;
import mx.com.fredkorlabs.registration.application.port.out.FileStoragePort;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationContextPort;
import mx.com.fredkorlabs.registration.application.port.out.StoredFileMetadata;
import mx.com.fredkorlabs.registration.application.port.out.SupplierRegistrationPersistencePort;
import mx.com.fredkorlabs.registration.domain.model.FiscalDocumentFormat;
import mx.com.fredkorlabs.registration.domain.model.FiscalIdentificationDocument;
import mx.com.fredkorlabs.registration.domain.model.StoredDocumentReference;
import mx.com.fredkorlabs.registration.domain.model.SupplierRegistration;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class UploadFiscalDocumentService implements UploadFiscalDocumentUseCase {

    // ADR-0010: {yyyy}/{MM}/{dd}/{rfc}/{uuid}.{extension} under the volume root, with the UTC upload date.
    private static final DateTimeFormatter DATE_DIRECTORIES =
        DateTimeFormatter.ofPattern("yyyy/MM/dd").withZone(ZoneOffset.UTC);

    private final RegistrationContextPort registrationContext;
    private final SupplierRegistrationPersistencePort supplierRegistrationPersistence;
    private final FileStoragePort fileStorage;
    private final FileMetadataPort fileMetadata;
    private final Clock clock;

    public UploadFiscalDocumentService(RegistrationContextPort registrationContext,
                                       SupplierRegistrationPersistencePort supplierRegistrationPersistence,
                                       FileStoragePort fileStorage, FileMetadataPort fileMetadata, Clock clock) {
        this.registrationContext = registrationContext;
        this.supplierRegistrationPersistence = supplierRegistrationPersistence;
        this.fileStorage = fileStorage;
        this.fileMetadata = fileMetadata;
        this.clock = clock;
    }

    @Override
    public UploadFiscalDocumentResult execute(UploadFiscalDocumentCommand command) {
        SupplierRegistration registration = OwnedRegistration.lock(registrationContext, supplierRegistrationPersistence,
            command.registrationSessionId(), command.registrationId()).stored().registration();
        // 409 (state, existing document) before 413/415, so nothing is written for a registration that cannot take it.
        registration.ensureFiscalDocumentCanBeAssociated();
        byte[] content = command.content();
        FiscalDocumentFormat.validate(command.originalName(), content.length, content);

        UUID fileId = UUID.randomUUID();
        Instant uploadedAt = clock.instant().truncatedTo(ChronoUnit.MICROS);
        String relativePath = DATE_DIRECTORIES.format(uploadedAt) + "/" + registration.applicant().rfc().value()
            + "/" + fileId + "." + FiscalDocumentFormat.EXTENSION;

        // Content first, then metadata and association in the same transaction (ADR-0010).
        fileStorage.store(relativePath, content);
        fileMetadata.register(new StoredFileMetadata(fileId, registration.id(), command.originalName(), relativePath,
            FiscalDocumentFormat.EXTENSION, FiscalDocumentFormat.MEDIA_TYPE, content.length, uploadedAt, true));
        StoredDocumentReference reference = new StoredDocumentReference(fileId);
        registration.associateFiscalDocument(
            new FiscalIdentificationDocument(registration.id(), command.originalName(), reference));
        supplierRegistrationPersistence.saveFiscalDocumentAssociation(registration.id(), reference);

        return new UploadFiscalDocumentResult(fileId, command.originalName(), FiscalDocumentFormat.EXTENSION,
            content.length, uploadedAt);
    }
}
