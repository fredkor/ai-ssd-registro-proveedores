package mx.com.fredkorlabs.registration.configuration;

import mx.com.fredkorlabs.registration.adapter.in.transaction.TransactionalCleanUpUnassociatedFilesUseCase;
import mx.com.fredkorlabs.registration.adapter.in.transaction.TransactionalDeleteFiscalDocumentUseCase;
import mx.com.fredkorlabs.registration.adapter.in.transaction.TransactionalDownloadFiscalDocumentUseCase;
import mx.com.fredkorlabs.registration.adapter.in.transaction.TransactionalUploadFiscalDocumentUseCase;
import mx.com.fredkorlabs.registration.adapter.out.document.VolumeFileStorageAdapter;
import mx.com.fredkorlabs.registration.application.port.in.CleanUpUnassociatedFilesUseCase;
import mx.com.fredkorlabs.registration.application.port.in.DeleteFiscalDocumentUseCase;
import mx.com.fredkorlabs.registration.application.port.in.DownloadFiscalDocumentUseCase;
import mx.com.fredkorlabs.registration.application.port.in.UploadFiscalDocumentUseCase;
import mx.com.fredkorlabs.registration.application.port.out.FileMetadataPort;
import mx.com.fredkorlabs.registration.application.port.out.FileStoragePort;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationContextPort;
import mx.com.fredkorlabs.registration.application.port.out.SupplierRegistrationPersistencePort;
import mx.com.fredkorlabs.registration.application.service.CleanUpUnassociatedFilesService;
import mx.com.fredkorlabs.registration.application.service.DeleteFiscalDocumentService;
import mx.com.fredkorlabs.registration.application.service.DownloadFiscalDocumentService;
import mx.com.fredkorlabs.registration.application.service.UploadFiscalDocumentService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.nio.file.Path;
import java.time.Clock;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(RegistrationDocumentsProperties.class)
public class FiscalDocumentConfiguration {

    @Bean
    public FileStoragePort fileStoragePort(RegistrationDocumentsProperties properties) {
        return new VolumeFileStorageAdapter(Path.of(properties.rootLocation()));
    }

    @Bean
    public UploadFiscalDocumentUseCase uploadFiscalDocumentUseCase(
            RegistrationContextPort registrationContext,
            SupplierRegistrationPersistencePort supplierRegistrationPersistence,
            FileStoragePort fileStorage, FileMetadataPort fileMetadata, Clock clock) {
        return new TransactionalUploadFiscalDocumentUseCase(new UploadFiscalDocumentService(
            registrationContext, supplierRegistrationPersistence, fileStorage, fileMetadata, clock));
    }

    @Bean
    public DownloadFiscalDocumentUseCase downloadFiscalDocumentUseCase(
            RegistrationContextPort registrationContext,
            SupplierRegistrationPersistencePort supplierRegistrationPersistence,
            FileMetadataPort fileMetadata, FileStoragePort fileStorage) {
        return new TransactionalDownloadFiscalDocumentUseCase(new DownloadFiscalDocumentService(
            registrationContext, supplierRegistrationPersistence, fileMetadata, fileStorage));
    }

    @Bean
    public DeleteFiscalDocumentUseCase deleteFiscalDocumentUseCase(
            RegistrationContextPort registrationContext,
            SupplierRegistrationPersistencePort supplierRegistrationPersistence,
            FileMetadataPort fileMetadata) {
        return new TransactionalDeleteFiscalDocumentUseCase(new DeleteFiscalDocumentService(
            registrationContext, supplierRegistrationPersistence, fileMetadata));
    }

    /** ADR-0010: one transaction per daily run; DailyFileCleanupScheduler triggers it. */
    @Bean
    public CleanUpUnassociatedFilesUseCase cleanUpUnassociatedFilesUseCase(
            FileMetadataPort fileMetadata, FileStoragePort fileStorage, Clock clock) {
        return new TransactionalCleanUpUnassociatedFilesUseCase(new CleanUpUnassociatedFilesService(
            fileMetadata, fileStorage, clock));
    }
}
