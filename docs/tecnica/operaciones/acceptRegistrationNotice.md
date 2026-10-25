<!-- Generado con /documentar desde specs/ y backend/src/. No editar a mano: cambia la spec y regenera. -->


# `acceptRegistrationNotice`

**POST `/api/v1/registration-notice-acceptances`** — Registrar aceptación expresa del aviso.

- Contrato: [api.md](../api.md#acceptregistrationnotice).
- Secuencia: [diagramas.md](../diagramas.md#22-acceptregistrationnotice).
- Trazabilidad: [traceability.md §2](../../../specs/traceability.md#2-operaciones-de-la-primera-etapa).

## Historias y requerimientos

[RF-REG-03](../../../specs/requirements/functional.md), [US03](../../../specs/user-stories/US03-aceptar-politicas.md).

## Criterios de aceptación

| Criterio | Reglas o fuente | Verificación prevista | Tipo (spec_sync) | Pruebas | Clases de prueba |
| :--- | :--- | :--- | :--- | :--- | :--- |
| US03 CA-003 | INV-07, INV-08 | Aplicación y persistencia. | backend | 5 | AcceptRegistrationNoticeIntegrationTest, AcceptRegistrationNoticeServiceTest, NoticeAcceptancePersistenceAdapterTest, NoticeAcceptanceTest, RegistrationNoticesControllerTest |
| US03 CA-004 | INV-07 | Aplicación y web. | backend | 7 | AcceptRegistrationNoticeIntegrationTest, AcceptRegistrationNoticeServiceTest, IdentificationPersistenceAdapterTest, IdentifyApplicantIntegrationTest, IdentifyApplicantServiceTest, ProblemDetailsTest, RegistrationNoticesControllerTest |
| US03 CA-007 | INV-07, INV-08; ADR-0015 | Aplicación y persistencia: aviso fijo, aceptación expresa y sustitución por la última al reanudar sin historial. | backend | 8 | AcceptRegistrationNoticeServiceTest, IdentificationPersistenceAdapterTest, IdentifyApplicantIntegrationTest, IdentifyApplicantServiceTest, NoticeAcceptancePersistenceAdapterTest, NoticeAcceptanceTest, ResumeSupplierRegistrationIntegrationTest, ResumeSupplierRegistrationServiceTest |

Criterios cuya operación principal es esta, según [traceability.md §3](../../../specs/traceability.md#3-cobertura-de-criterios-de-aceptación). Conteo de [`docs/sync-report.md`](../../sync-report.md).

## Caso de uso

| Elemento | Clase |
| :--- | :--- |
| Puerto de entrada | [`AcceptRegistrationNoticeUseCase.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/in/AcceptRegistrationNoticeUseCase.java) |
| Entrada | [`AcceptRegistrationNoticeCommand.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/in/AcceptRegistrationNoticeCommand.java) |
| Resultado | [`AcceptRegistrationNoticeResult.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/in/AcceptRegistrationNoticeResult.java) |
| Decorador transaccional | [`TransactionalAcceptRegistrationNoticeUseCase.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/transaction/TransactionalAcceptRegistrationNoticeUseCase.java) |
| Servicio | [`AcceptRegistrationNoticeService.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/service/AcceptRegistrationNoticeService.java) |

## Invariantes

INV-07, INV-08. Detalle en [dominio.md](../dominio.md#invariantes).

## Archivos por capa

| Capa | Archivos |
| :--- | :--- |
| Web | [`RegistrationNoticesController.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/web/RegistrationNoticesController.java) implementa `RegistrationNoticesApi` (generada desde el contrato) |
| Errores | [`GlobalExceptionHandler.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/web/GlobalExceptionHandler.java) |
| Transacción | [`TransactionalAcceptRegistrationNoticeUseCase.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/transaction/TransactionalAcceptRegistrationNoticeUseCase.java) |
| Aplicación | [`AcceptRegistrationNoticeService.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/service/AcceptRegistrationNoticeService.java) |
| Dominio | [`NoticeAcceptance.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/domain/model/NoticeAcceptance.java) |
| Puertos de salida | [`NoticeAcceptancePersistencePort.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/out/NoticeAcceptancePersistencePort.java) |
| Adaptadores de salida | [`NoticeAcceptancePersistenceAdapter.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/out/persistence/NoticeAcceptancePersistenceAdapter.java) |
| Reloj | [`ClockConfiguration.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/configuration/ClockConfiguration.java) (UTC) |

## Pruebas

| Clase de prueba | Ubicación |
| :--- | :--- |
| `AcceptRegistrationNoticeIntegrationTest` | [mx/com/fredkorlabs/registration/AcceptRegistrationNoticeIntegrationTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/AcceptRegistrationNoticeIntegrationTest.java) |
| `AcceptRegistrationNoticeServiceTest` | [mx/com/fredkorlabs/registration/application/service/AcceptRegistrationNoticeServiceTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/application/service/AcceptRegistrationNoticeServiceTest.java) |
| `IdentificationPersistenceAdapterTest` | [mx/com/fredkorlabs/registration/adapter/out/persistence/IdentificationPersistenceAdapterTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/adapter/out/persistence/IdentificationPersistenceAdapterTest.java) |
| `IdentifyApplicantIntegrationTest` | [mx/com/fredkorlabs/registration/IdentifyApplicantIntegrationTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/IdentifyApplicantIntegrationTest.java) |
| `IdentifyApplicantServiceTest` | [mx/com/fredkorlabs/registration/application/service/IdentifyApplicantServiceTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/application/service/IdentifyApplicantServiceTest.java) |
| `NoticeAcceptancePersistenceAdapterTest` | [mx/com/fredkorlabs/registration/adapter/out/persistence/NoticeAcceptancePersistenceAdapterTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/adapter/out/persistence/NoticeAcceptancePersistenceAdapterTest.java) |
| `NoticeAcceptanceTest` | [mx/com/fredkorlabs/registration/domain/NoticeAcceptanceTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/domain/NoticeAcceptanceTest.java) |
| `ProblemDetailsTest` | [mx/com/fredkorlabs/registration/adapter/in/web/ProblemDetailsTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/adapter/in/web/ProblemDetailsTest.java) |
| `RegistrationNoticesControllerTest` | [mx/com/fredkorlabs/registration/adapter/in/web/RegistrationNoticesControllerTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/adapter/in/web/RegistrationNoticesControllerTest.java) |
| `ResumeSupplierRegistrationIntegrationTest` | [mx/com/fredkorlabs/registration/ResumeSupplierRegistrationIntegrationTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/ResumeSupplierRegistrationIntegrationTest.java) |
| `ResumeSupplierRegistrationServiceTest` | [mx/com/fredkorlabs/registration/application/service/ResumeSupplierRegistrationServiceTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/application/service/ResumeSupplierRegistrationServiceTest.java) |

Incluye las clases nombradas como el caso de uso o el controlador y las que verifican sus criterios.
