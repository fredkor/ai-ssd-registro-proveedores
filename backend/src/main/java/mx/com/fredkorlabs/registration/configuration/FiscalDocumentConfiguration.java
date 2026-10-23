package mx.com.fredkorlabs.registration.configuration;

import mx.com.fredkorlabs.registration.adapter.in.transaction.TransactionalUploadFiscalDocumentUseCase;
import mx.com.fredkorlabs.registration.adapter.out.document.VolumeFileStorageAdapter;
import mx.com.fredkorlabs.registration.application.port.in.UploadFiscalDocumentUseCase;
import mx.com.fredkorlabs.registration.application.port.out.FileMetadataPort;
import mx.com.fredkorlabs.registration.application.port.out.FileStoragePort;
import mx.com.fredkorlabs.registration.application.port.out.RegistrationContextPort;
import mx.com.fredkorlabs.registration.application.port.out.SupplierRegistrationPersistencePort;
import mx.com.fredkorlabs.registration.application.service.UploadFiscalDocumentService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.time.Clock;

@Configuration
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
}
