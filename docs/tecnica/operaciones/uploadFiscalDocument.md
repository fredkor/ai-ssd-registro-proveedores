<!-- Generado con /documentar desde specs/ y backend/src/. No editar a mano: cambia la spec y regenera. -->


# `uploadFiscalDocument`

**POST `/api/v1/supplier-registrations/{registrationId}/fiscal-document`** — Cargar la única cédula fiscal.

- Contrato: [api.md](../api.md#uploadfiscaldocument).
- Secuencia: [diagramas.md](../diagramas.md#51-uploadfiscaldocument).
- Trazabilidad: [traceability.md §2](../../../specs/traceability.md#2-operaciones-de-la-primera-etapa).

## Historias y requerimientos

[RF-REG-05](../../../specs/requirements/functional.md), [US10](../../../specs/user-stories/US10-gestionar-cedula-fiscal.md).

## Criterios de aceptación

| Criterio | Reglas o fuente | Verificación prevista | Tipo (spec_sync) | Pruebas | Clases de prueba |
| :--- | :--- | :--- | :--- | :--- | :--- |
| US10 CA-001 | US10 CA-001; INV-28 | Carga con asociación inmediata, UUID, descarga y visualización sin Siguiente. | backend | 5 | FiscalDocumentFormatTest, SupplierRegistrationTest, UploadFiscalDocumentIntegrationTest, UploadFiscalDocumentServiceTest, VolumeFileStorageAdapterTest |
| US10 CA-002 | INV-27 | Aplicación y web. | backend | 2 | FiscalDocumentFormatTest, UploadFiscalDocumentIntegrationTest |
| US10 CA-004 | INV-27 | Persistencia y concurrencia: máximo una cédula asociada. | backend | 4 | SupplierRegistrationPersistenceAdapterTest, SupplierRegistrationTest, UploadFiscalDocumentIntegrationTest, UploadFiscalDocumentServiceTest |
| US10 CA-005 | INV-28 | Archivo disponible tras volver o reanudar aunque no se pulse Siguiente. | backend | 6 | DownloadFiscalDocumentIntegrationTest, DownloadFiscalDocumentServiceTest, FiscalDocumentContentDispositionTest, SupplierRegistrationPersistenceAdapterTest, UploadFiscalDocumentIntegrationTest, VolumeFileStorageAdapterTest |
| US10 CA-008 | INV-27; ADR-0010, ADR-0004 | Documento y web: PDF de 5,242,880 bytes permitido, un byte adicional rechazado; variantes de mayúsculas en extensión. | backend | 4 | FiscalDocumentFormatTest, UploadFiscalDocumentIntegrationTest, UploadFiscalDocumentServerTest, UploadFiscalDocumentServiceTest |
| US10 CA-009 | INV-27; ADR-0010, ADR-0004 | Documento y web: contenido no PDF renombrado y MIME declarado no evitan HTTP 415. | backend | 3 | FiscalDocumentFormatTest, UploadFiscalDocumentIntegrationTest, UploadFiscalDocumentServiceTest |

Criterios cuya operación principal es esta, según [traceability.md §3](../../../specs/traceability.md#3-cobertura-de-criterios-de-aceptación). Conteo de [`docs/sync-report.md`](../../sync-report.md).

## Caso de uso

| Elemento | Clase |
| :--- | :--- |
| Puerto de entrada | [`UploadFiscalDocumentUseCase.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/in/UploadFiscalDocumentUseCase.java) |
| Entrada | [`UploadFiscalDocumentCommand.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/in/UploadFiscalDocumentCommand.java) |
| Resultado | [`UploadFiscalDocumentResult.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/in/UploadFiscalDocumentResult.java) |
| Decorador transaccional | [`TransactionalUploadFiscalDocumentUseCase.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/transaction/TransactionalUploadFiscalDocumentUseCase.java) |
| Servicio | [`UploadFiscalDocumentService.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/service/UploadFiscalDocumentService.java) |

## Invariantes

INV-27, INV-28; ADR-0010. Detalle en [dominio.md](../dominio.md#invariantes).

## Archivos por capa

| Capa | Archivos |
| :--- | :--- |
| Web | [`FiscalDocumentsController.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/web/FiscalDocumentsController.java) implementa `FiscalDocumentsApi` (generada desde el contrato) |
| Web (token) | [`SecurityConfiguration.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/configuration/SecurityConfiguration.java), [`RegistrationTokenPrincipal.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/web/RegistrationTokenPrincipal.java) |
| Errores | [`GlobalExceptionHandler.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/web/GlobalExceptionHandler.java) |
| Transacción | [`TransactionalUploadFiscalDocumentUseCase.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/transaction/TransactionalUploadFiscalDocumentUseCase.java) |
| Aplicación | [`UploadFiscalDocumentService.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/service/UploadFiscalDocumentService.java), [`OwnedRegistration.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/service/OwnedRegistration.java) |
| Dominio | [`FiscalDocumentFormat.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/domain/model/FiscalDocumentFormat.java), [`FiscalIdentificationDocument.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/domain/model/FiscalIdentificationDocument.java), [`StoredDocumentReference.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/domain/model/StoredDocumentReference.java), [`SupplierRegistration.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/domain/model/SupplierRegistration.java) |
| Puertos de salida | [`RegistrationContextPort.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/out/RegistrationContextPort.java), [`SupplierRegistrationPersistencePort.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/out/SupplierRegistrationPersistencePort.java), [`FileStoragePort.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/out/FileStoragePort.java), [`FileMetadataPort.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/out/FileMetadataPort.java) |
| Adaptadores de salida | [`RegistrationContextAdapter.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/out/persistence/RegistrationContextAdapter.java), [`SupplierRegistrationPersistenceAdapter.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/out/persistence/SupplierRegistrationPersistenceAdapter.java), [`VolumeFileStorageAdapter.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/out/document/VolumeFileStorageAdapter.java), [`FileMetadataPersistenceAdapter.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/out/persistence/FileMetadataPersistenceAdapter.java) |
| Reloj | [`ClockConfiguration.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/configuration/ClockConfiguration.java) (UTC) |

## Pruebas

| Clase de prueba | Ubicación |
| :--- | :--- |
| `DownloadFiscalDocumentIntegrationTest` | [mx/com/fredkorlabs/registration/DownloadFiscalDocumentIntegrationTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/DownloadFiscalDocumentIntegrationTest.java) |
| `DownloadFiscalDocumentServiceTest` | [mx/com/fredkorlabs/registration/application/service/DownloadFiscalDocumentServiceTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/application/service/DownloadFiscalDocumentServiceTest.java) |
| `FiscalDocumentContentDispositionTest` | [mx/com/fredkorlabs/registration/adapter/in/web/FiscalDocumentContentDispositionTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/adapter/in/web/FiscalDocumentContentDispositionTest.java) |
| `FiscalDocumentFormatTest` | [mx/com/fredkorlabs/registration/domain/FiscalDocumentFormatTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/domain/FiscalDocumentFormatTest.java) |
| `SupplierRegistrationPersistenceAdapterTest` | [mx/com/fredkorlabs/registration/adapter/out/persistence/SupplierRegistrationPersistenceAdapterTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/adapter/out/persistence/SupplierRegistrationPersistenceAdapterTest.java) |
| `SupplierRegistrationTest` | [mx/com/fredkorlabs/registration/domain/SupplierRegistrationTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/domain/SupplierRegistrationTest.java) |
| `UploadFiscalDocumentIntegrationTest` | [mx/com/fredkorlabs/registration/UploadFiscalDocumentIntegrationTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/UploadFiscalDocumentIntegrationTest.java) |
| `UploadFiscalDocumentRollbackTest` | [mx/com/fredkorlabs/registration/UploadFiscalDocumentRollbackTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/UploadFiscalDocumentRollbackTest.java) |
| `UploadFiscalDocumentServerTest` | [mx/com/fredkorlabs/registration/UploadFiscalDocumentServerTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/UploadFiscalDocumentServerTest.java) |
| `UploadFiscalDocumentServiceTest` | [mx/com/fredkorlabs/registration/application/service/UploadFiscalDocumentServiceTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/application/service/UploadFiscalDocumentServiceTest.java) |
| `VolumeFileStorageAdapterTest` | [mx/com/fredkorlabs/registration/adapter/out/document/VolumeFileStorageAdapterTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/adapter/out/document/VolumeFileStorageAdapterTest.java) |

Incluye las clases nombradas como el caso de uso o el controlador y las que verifican sus criterios.
