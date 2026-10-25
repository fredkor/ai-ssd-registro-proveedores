<!-- Generado con /documentar desde specs/ y backend/src/. No editar a mano: cambia la spec y regenera. -->


# `deleteFiscalDocument`

**DELETE `/api/v1/supplier-registrations/{registrationId}/fiscal-document`** — Eliminar cédula fiscal asociada.

- Contrato: [api.md](../api.md#deletefiscaldocument).
- Secuencia: [diagramas.md](../diagramas.md#53-deletefiscaldocument).
- Trazabilidad: [traceability.md §2](../../../specs/traceability.md#2-operaciones-de-la-primera-etapa).

## Historias y requerimientos

[RF-REG-05](../../../specs/requirements/functional.md), [US10](../../../specs/user-stories/US10-gestionar-cedula-fiscal.md).

## Criterios de aceptación

| Criterio | Reglas o fuente | Verificación prevista | Tipo (spec_sync) | Pruebas | Clases de prueba |
| :--- | :--- | :--- | :--- | :--- | :--- |
| US10 CA-006 | INV-29 | Confirmación; desasociación, marca no asociado y progreso en transacción sin disco; fallo hace rollback. | backend | 4 | DeleteFiscalDocumentIntegrationTest, DeleteFiscalDocumentRollbackTest, DeleteFiscalDocumentServiceTest, SupplierRegistrationPersistenceAdapterTest |
| US10 CA-007 | US10 CA-007 | Eliminar exitosamente deja Datos Generales PENDING; fallo conserva progreso; CUCoP intacto. | backend | 3 | DeleteFiscalDocumentIntegrationTest, DeleteFiscalDocumentServiceTest, SupplierRegistrationTest |
| US10 CA-011 | INV-29; ADR-0010 | Archivo desasociado inaccesible desde expediente; metadatos retenidos para limpieza, asociados excluidos. | backend | 3 | DeleteFiscalDocumentIntegrationTest, DownloadFiscalDocumentIntegrationTest, DownloadFiscalDocumentServiceTest |

Criterios cuya operación principal es esta, según [traceability.md §3](../../../specs/traceability.md#3-cobertura-de-criterios-de-aceptación). Conteo de [`docs/sync-report.md`](../../sync-report.md).

## Caso de uso

| Elemento | Clase |
| :--- | :--- |
| Puerto de entrada | [`DeleteFiscalDocumentUseCase.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/in/DeleteFiscalDocumentUseCase.java) |
| Entrada | [`DeleteFiscalDocumentCommand.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/in/DeleteFiscalDocumentCommand.java) |
| Resultado | `void` |
| Decorador transaccional | [`TransactionalDeleteFiscalDocumentUseCase.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/transaction/TransactionalDeleteFiscalDocumentUseCase.java) |
| Servicio | [`DeleteFiscalDocumentService.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/service/DeleteFiscalDocumentService.java) |

## Invariantes

INV-27 a INV-29. Detalle en [dominio.md](../dominio.md#invariantes).

## Archivos por capa

| Capa | Archivos |
| :--- | :--- |
| Web | [`FiscalDocumentsController.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/web/FiscalDocumentsController.java) implementa `FiscalDocumentsApi` (generada desde el contrato) |
| Web (token) | [`SecurityConfiguration.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/configuration/SecurityConfiguration.java), [`RegistrationTokenPrincipal.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/web/RegistrationTokenPrincipal.java) |
| Errores | [`GlobalExceptionHandler.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/web/GlobalExceptionHandler.java) |
| Transacción | [`TransactionalDeleteFiscalDocumentUseCase.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/transaction/TransactionalDeleteFiscalDocumentUseCase.java) |
| Aplicación | [`DeleteFiscalDocumentService.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/service/DeleteFiscalDocumentService.java), [`OwnedRegistration.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/service/OwnedRegistration.java) |
| Dominio | [`FiscalIdentificationDocument.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/domain/model/FiscalIdentificationDocument.java), [`SupplierRegistration.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/domain/model/SupplierRegistration.java) |
| Puertos de salida | [`RegistrationContextPort.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/out/RegistrationContextPort.java), [`SupplierRegistrationPersistencePort.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/out/SupplierRegistrationPersistencePort.java), [`FileMetadataPort.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/out/FileMetadataPort.java) |
| Adaptadores de salida | [`RegistrationContextAdapter.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/out/persistence/RegistrationContextAdapter.java), [`SupplierRegistrationPersistenceAdapter.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/out/persistence/SupplierRegistrationPersistenceAdapter.java), [`FileMetadataPersistenceAdapter.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/out/persistence/FileMetadataPersistenceAdapter.java) |

## Pruebas

| Clase de prueba | Ubicación |
| :--- | :--- |
| `DeleteFiscalDocumentIntegrationTest` | [mx/com/fredkorlabs/registration/DeleteFiscalDocumentIntegrationTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/DeleteFiscalDocumentIntegrationTest.java) |
| `DeleteFiscalDocumentRollbackTest` | [mx/com/fredkorlabs/registration/DeleteFiscalDocumentRollbackTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/DeleteFiscalDocumentRollbackTest.java) |
| `DeleteFiscalDocumentServiceTest` | [mx/com/fredkorlabs/registration/application/service/DeleteFiscalDocumentServiceTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/application/service/DeleteFiscalDocumentServiceTest.java) |
| `DownloadFiscalDocumentIntegrationTest` | [mx/com/fredkorlabs/registration/DownloadFiscalDocumentIntegrationTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/DownloadFiscalDocumentIntegrationTest.java) |
| `DownloadFiscalDocumentServiceTest` | [mx/com/fredkorlabs/registration/application/service/DownloadFiscalDocumentServiceTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/application/service/DownloadFiscalDocumentServiceTest.java) |
| `SupplierRegistrationPersistenceAdapterTest` | [mx/com/fredkorlabs/registration/adapter/out/persistence/SupplierRegistrationPersistenceAdapterTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/adapter/out/persistence/SupplierRegistrationPersistenceAdapterTest.java) |
| `SupplierRegistrationTest` | [mx/com/fredkorlabs/registration/domain/SupplierRegistrationTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/domain/SupplierRegistrationTest.java) |

Incluye las clases nombradas como el caso de uso o el controlador y las que verifican sus criterios.
