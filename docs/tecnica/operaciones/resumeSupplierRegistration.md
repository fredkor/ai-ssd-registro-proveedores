<!-- Generado con /documentar desde specs/ y backend/src/. No editar a mano: cambia la spec y regenera. -->


# `resumeSupplierRegistration`

**POST `/api/v1/supplier-registrations/{registrationId}/resume`** — Reanudar expediente incompleto.

- Contrato: [api.md](../api.md#resumesupplierregistration).
- Secuencia: [diagramas.md](../diagramas.md#43-resumesupplierregistration).
- Trazabilidad: [traceability.md §2](../../../specs/traceability.md#2-operaciones-de-la-primera-etapa).

## Historias y requerimientos

[RF-REG-06](../../../specs/requirements/functional.md), [US05](../../../specs/user-stories/US05-reanudar-expediente-incompleto.md).

## Criterios de aceptación

| Criterio | Reglas o fuente | Verificación prevista | Tipo (spec_sync) | Pruebas | Clases de prueba |
| :--- | :--- | :--- | :--- | :--- | :--- |
| US03 CA-007 | INV-07, INV-08; ADR-0015 | Aplicación y persistencia: aviso fijo, aceptación expresa y sustitución por la última al reanudar sin historial. | backend | 8 | AcceptRegistrationNoticeServiceTest, IdentificationPersistenceAdapterTest, IdentifyApplicantIntegrationTest, IdentifyApplicantServiceTest, NoticeAcceptancePersistenceAdapterTest, NoticeAcceptanceTest, ResumeSupplierRegistrationIntegrationTest, ResumeSupplierRegistrationServiceTest |
| US03 CA-008 | INV-08; ADR-0015 | Aplicación y persistencia: fallo conserva aceptación asociada anterior e historial de evidencias de firma intacto. | backend | 1 | ResumeSupplierRegistrationRollbackTest |
| US05 CA-001 | INV-12 | Aplicación, persistencia y web. | backend | 3 | ResumeSupplierRegistrationIntegrationTest, ResumeSupplierRegistrationServiceTest, SupplierRegistrationPersistenceAdapterTest |
| US05 CA-002 | INV-36 | Aplicación, persistencia y web. | backend | 1 | SaveCucopParticipationIntegrationTest |
| US05 CA-003 | INV-36 | Aplicación, persistencia y web. | backend | 4 | ResumeSupplierRegistrationIntegrationTest, ResumeSupplierRegistrationServiceTest, SupplierRegistrationPersistenceAdapterTest, SupplierRegistrationTest |
| US05 CA-004 | INV-12 | Aplicación, persistencia y web. | backend | 2 | ResumeSupplierRegistrationIntegrationTest, ResumeSupplierRegistrationServiceTest |
| US05 CA-005 | INV-12 | Aplicación, persistencia y web. | backend | 1 | ResumeSupplierRegistrationServiceTest |
| US05 CA-006 | INV-13 | Aplicación, persistencia y web. | backend | 3 | ResumeSupplierRegistrationIntegrationTest, ResumeSupplierRegistrationServiceTest, SupplierRegistrationTest |
| US05 CA-007 | INV-36 | Interfaz: abrir siempre Datos Generales; la respuesta no indica sección y el backend conserva datos y progreso (US05 CA-003). | interfaz / externa / futura | 0 | — |
| US05 CA-008 | INV-12 | Aplicación, persistencia y web. | backend | 2 | ResumeSupplierRegistrationIntegrationTest, ResumeSupplierRegistrationServiceTest |
| US05 CA-009 | INV-11, INV-12; US05 CA-009 | Aplicación, persistencia y web: actualizar nombre del certificado sin cambiar UUID, datos ni progreso. | backend | 3 | ResumeSupplierRegistrationIntegrationTest, ResumeSupplierRegistrationServiceTest, SupplierRegistrationTest |
| US05 CA-010 | INV-06; US05 CA-010 | Aplicación y persistencia: historial de evidencias sin sobrescritura. | backend | 4 | ResumeSupplierRegistrationIntegrationTest, ResumeSupplierRegistrationServiceTest, SupplierRegistrationPersistenceAdapterTest, SupplierRegistrationTest |
| US05 CA-011 | INV-12; US05 CA-011 | Aplicación y persistencia: rollback del nombre y vínculo histórico; evidencia aceptada conservada. | backend | 2 | ResumeSupplierRegistrationRollbackTest, ResumeSupplierRegistrationServiceTest |
| US06 CA-005 | US06 CA-005 | Interfaz y aplicación. | backend | 2 | SaveGeneralDataIntegrationTest, SupplierRegistrationPersistenceAdapterTest |
| US09 CA-009 | INV-34; US09 CA-009 | Aplicación y persistencia: salir o fallar sin guardar conserva teléfonos anteriores. | backend | 1 | SaveGeneralDataIntegrationTest |
| US10 CA-005 | INV-28 | Archivo disponible tras volver o reanudar aunque no se pulse Siguiente. | backend | 6 | DownloadFiscalDocumentIntegrationTest, DownloadFiscalDocumentServiceTest, FiscalDocumentContentDispositionTest, SupplierRegistrationPersistenceAdapterTest, UploadFiscalDocumentIntegrationTest, VolumeFileStorageAdapterTest |
| US10 CA-011 | INV-29; ADR-0010 | Archivo desasociado inaccesible desde expediente; metadatos retenidos para limpieza, asociados excluidos. | backend | 3 | DeleteFiscalDocumentIntegrationTest, DownloadFiscalDocumentIntegrationTest, DownloadFiscalDocumentServiceTest |

Criterios cuya operación principal es esta, según [traceability.md §3](../../../specs/traceability.md#3-cobertura-de-criterios-de-aceptación). Conteo de [`docs/sync-report.md`](../../sync-report.md).

## Caso de uso

| Elemento | Clase |
| :--- | :--- |
| Puerto de entrada | [`ResumeSupplierRegistrationUseCase.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/in/ResumeSupplierRegistrationUseCase.java) |
| Entrada | [`ResumeSupplierRegistrationCommand.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/in/ResumeSupplierRegistrationCommand.java) |
| Resultado | [`SupplierRegistrationResult.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/in/SupplierRegistrationResult.java) |
| Decorador transaccional | [`TransactionalResumeSupplierRegistrationUseCase.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/transaction/TransactionalResumeSupplierRegistrationUseCase.java) |
| Servicio | [`ResumeSupplierRegistrationService.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/service/ResumeSupplierRegistrationService.java) |

## Invariantes

INV-12, INV-13, INV-28, INV-36. Detalle en [dominio.md](../dominio.md#invariantes).

## Archivos por capa

| Capa | Archivos |
| :--- | :--- |
| Web | [`SupplierRegistrationsController.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/web/SupplierRegistrationsController.java) implementa `SupplierRegistrationsApi` (generada desde el contrato) |
| Web (token) | [`SecurityConfiguration.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/configuration/SecurityConfiguration.java), [`RegistrationTokenPrincipal.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/web/RegistrationTokenPrincipal.java) |
| Errores | [`GlobalExceptionHandler.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/web/GlobalExceptionHandler.java) |
| Transacción | [`TransactionalResumeSupplierRegistrationUseCase.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/transaction/TransactionalResumeSupplierRegistrationUseCase.java) |
| Aplicación | [`ResumeSupplierRegistrationService.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/service/ResumeSupplierRegistrationService.java), [`OwnedRegistration.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/service/OwnedRegistration.java) |
| Dominio | [`SupplierRegistration.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/domain/model/SupplierRegistration.java) |
| Puertos de salida | [`RegistrationContextPort.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/out/RegistrationContextPort.java), [`SupplierRegistrationPersistencePort.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/out/SupplierRegistrationPersistencePort.java) |
| Adaptadores de salida | [`RegistrationContextAdapter.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/out/persistence/RegistrationContextAdapter.java), [`SupplierRegistrationPersistenceAdapter.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/out/persistence/SupplierRegistrationPersistenceAdapter.java) |

## Pruebas

| Clase de prueba | Ubicación |
| :--- | :--- |
| `AcceptRegistrationNoticeServiceTest` | [mx/com/fredkorlabs/registration/application/service/AcceptRegistrationNoticeServiceTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/application/service/AcceptRegistrationNoticeServiceTest.java) |
| `DeleteFiscalDocumentIntegrationTest` | [mx/com/fredkorlabs/registration/DeleteFiscalDocumentIntegrationTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/DeleteFiscalDocumentIntegrationTest.java) |
| `DownloadFiscalDocumentIntegrationTest` | [mx/com/fredkorlabs/registration/DownloadFiscalDocumentIntegrationTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/DownloadFiscalDocumentIntegrationTest.java) |
| `DownloadFiscalDocumentServiceTest` | [mx/com/fredkorlabs/registration/application/service/DownloadFiscalDocumentServiceTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/application/service/DownloadFiscalDocumentServiceTest.java) |
| `FiscalDocumentContentDispositionTest` | [mx/com/fredkorlabs/registration/adapter/in/web/FiscalDocumentContentDispositionTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/adapter/in/web/FiscalDocumentContentDispositionTest.java) |
| `IdentificationPersistenceAdapterTest` | [mx/com/fredkorlabs/registration/adapter/out/persistence/IdentificationPersistenceAdapterTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/adapter/out/persistence/IdentificationPersistenceAdapterTest.java) |
| `IdentifyApplicantIntegrationTest` | [mx/com/fredkorlabs/registration/IdentifyApplicantIntegrationTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/IdentifyApplicantIntegrationTest.java) |
| `IdentifyApplicantServiceTest` | [mx/com/fredkorlabs/registration/application/service/IdentifyApplicantServiceTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/application/service/IdentifyApplicantServiceTest.java) |
| `NoticeAcceptancePersistenceAdapterTest` | [mx/com/fredkorlabs/registration/adapter/out/persistence/NoticeAcceptancePersistenceAdapterTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/adapter/out/persistence/NoticeAcceptancePersistenceAdapterTest.java) |
| `NoticeAcceptanceTest` | [mx/com/fredkorlabs/registration/domain/NoticeAcceptanceTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/domain/NoticeAcceptanceTest.java) |
| `ResumeSupplierRegistrationIntegrationTest` | [mx/com/fredkorlabs/registration/ResumeSupplierRegistrationIntegrationTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/ResumeSupplierRegistrationIntegrationTest.java) |
| `ResumeSupplierRegistrationRollbackTest` | [mx/com/fredkorlabs/registration/ResumeSupplierRegistrationRollbackTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/ResumeSupplierRegistrationRollbackTest.java) |
| `ResumeSupplierRegistrationServiceTest` | [mx/com/fredkorlabs/registration/application/service/ResumeSupplierRegistrationServiceTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/application/service/ResumeSupplierRegistrationServiceTest.java) |
| `SaveCucopParticipationIntegrationTest` | [mx/com/fredkorlabs/registration/SaveCucopParticipationIntegrationTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/SaveCucopParticipationIntegrationTest.java) |
| `SaveGeneralDataIntegrationTest` | [mx/com/fredkorlabs/registration/SaveGeneralDataIntegrationTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/SaveGeneralDataIntegrationTest.java) |
| `SupplierRegistrationPersistenceAdapterTest` | [mx/com/fredkorlabs/registration/adapter/out/persistence/SupplierRegistrationPersistenceAdapterTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/adapter/out/persistence/SupplierRegistrationPersistenceAdapterTest.java) |
| `SupplierRegistrationTest` | [mx/com/fredkorlabs/registration/domain/SupplierRegistrationTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/domain/SupplierRegistrationTest.java) |
| `SupplierRegistrationsControllerTest` | [mx/com/fredkorlabs/registration/adapter/in/web/SupplierRegistrationsControllerTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/adapter/in/web/SupplierRegistrationsControllerTest.java) |
| `UploadFiscalDocumentIntegrationTest` | [mx/com/fredkorlabs/registration/UploadFiscalDocumentIntegrationTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/UploadFiscalDocumentIntegrationTest.java) |
| `VolumeFileStorageAdapterTest` | [mx/com/fredkorlabs/registration/adapter/out/document/VolumeFileStorageAdapterTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/adapter/out/document/VolumeFileStorageAdapterTest.java) |

Incluye las clases nombradas como el caso de uso o el controlador y las que verifican sus criterios.
