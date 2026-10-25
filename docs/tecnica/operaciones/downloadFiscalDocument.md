<!-- Generado con /documentar desde specs/ y backend/src/. No editar a mano: cambia la spec y regenera. -->


# `downloadFiscalDocument`

**GET `/api/v1/supplier-registrations/{registrationId}/fiscal-document`** — Descargar cédula fiscal asociada.

- Contrato: [api.md](../api.md#downloadfiscaldocument).
- Secuencia: [diagramas.md](../diagramas.md#52-downloadfiscaldocument).
- Trazabilidad: [traceability.md §2](../../../specs/traceability.md#2-operaciones-de-la-primera-etapa).

## Historias y requerimientos

[RF-REG-05](../../../specs/requirements/functional.md), [US10](../../../specs/user-stories/US10-gestionar-cedula-fiscal.md).

## Criterios de aceptación

| Criterio | Reglas o fuente | Verificación prevista | Tipo (spec_sync) | Pruebas | Clases de prueba |
| :--- | :--- | :--- | :--- | :--- | :--- |
| US10 CA-011 | INV-29; ADR-0010 | Archivo desasociado inaccesible desde expediente; metadatos retenidos para limpieza, asociados excluidos. | backend | 3 | DeleteFiscalDocumentIntegrationTest, DownloadFiscalDocumentIntegrationTest, DownloadFiscalDocumentServiceTest |

Criterios cuya operación principal es esta, según [traceability.md §3](../../../specs/traceability.md#3-cobertura-de-criterios-de-aceptación). Conteo de [`docs/sync-report.md`](../../sync-report.md).

## Caso de uso

| Elemento | Clase |
| :--- | :--- |
| Puerto de entrada | [`DownloadFiscalDocumentUseCase.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/in/DownloadFiscalDocumentUseCase.java) |
| Entrada | [`DownloadFiscalDocumentQuery.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/in/DownloadFiscalDocumentQuery.java) |
| Resultado | [`DownloadFiscalDocumentResult.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/in/DownloadFiscalDocumentResult.java) |
| Decorador transaccional | [`TransactionalDownloadFiscalDocumentUseCase.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/transaction/TransactionalDownloadFiscalDocumentUseCase.java) |
| Servicio | [`DownloadFiscalDocumentService.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/service/DownloadFiscalDocumentService.java) |

## Invariantes

INV-12, INV-27, INV-28; ADR-0010. Detalle en [dominio.md](../dominio.md#invariantes).

## Archivos por capa

| Capa | Archivos |
| :--- | :--- |
| Web | [`FiscalDocumentsController.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/web/FiscalDocumentsController.java) implementa `FiscalDocumentsApi` (generada desde el contrato) |
| Web (token) | [`SecurityConfiguration.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/configuration/SecurityConfiguration.java), [`RegistrationTokenPrincipal.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/web/RegistrationTokenPrincipal.java) |
| Errores | [`GlobalExceptionHandler.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/web/GlobalExceptionHandler.java) |
| Transacción | [`TransactionalDownloadFiscalDocumentUseCase.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/transaction/TransactionalDownloadFiscalDocumentUseCase.java) |
| Aplicación | [`DownloadFiscalDocumentService.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/service/DownloadFiscalDocumentService.java), [`OwnedRegistration.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/service/OwnedRegistration.java) |
| Dominio | — |
| Puertos de salida | [`RegistrationContextPort.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/out/RegistrationContextPort.java), [`SupplierRegistrationPersistencePort.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/out/SupplierRegistrationPersistencePort.java), [`FileMetadataPort.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/out/FileMetadataPort.java), [`FileStoragePort.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/out/FileStoragePort.java) |
| Adaptadores de salida | [`RegistrationContextAdapter.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/out/persistence/RegistrationContextAdapter.java), [`SupplierRegistrationPersistenceAdapter.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/out/persistence/SupplierRegistrationPersistenceAdapter.java), [`FileMetadataPersistenceAdapter.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/out/persistence/FileMetadataPersistenceAdapter.java), [`VolumeFileStorageAdapter.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/out/document/VolumeFileStorageAdapter.java) |

## Pruebas

| Clase de prueba | Ubicación |
| :--- | :--- |
| `DeleteFiscalDocumentIntegrationTest` | [mx/com/fredkorlabs/registration/DeleteFiscalDocumentIntegrationTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/DeleteFiscalDocumentIntegrationTest.java) |
| `DownloadFiscalDocumentIntegrationTest` | [mx/com/fredkorlabs/registration/DownloadFiscalDocumentIntegrationTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/DownloadFiscalDocumentIntegrationTest.java) |
| `DownloadFiscalDocumentServiceTest` | [mx/com/fredkorlabs/registration/application/service/DownloadFiscalDocumentServiceTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/application/service/DownloadFiscalDocumentServiceTest.java) |

Incluye las clases nombradas como el caso de uso o el controlador y las que verifican sus criterios.
