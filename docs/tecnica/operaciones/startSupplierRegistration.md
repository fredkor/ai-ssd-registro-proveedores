<!-- Generado con /documentar desde specs/ y backend/src/. No editar a mano: cambia la spec y regenera. -->


# `startSupplierRegistration`

**POST `/api/v1/supplier-registrations`** — Crear expediente de registro.

- Contrato: [api.md](../api.md#startsupplierregistration).
- Secuencia: [diagramas.md](../diagramas.md#42-startsupplierregistration).
- Trazabilidad: [traceability.md §2](../../../specs/traceability.md#2-operaciones-de-la-primera-etapa).

## Historias y requerimientos

[RF-REG-04](../../../specs/requirements/functional.md), [US04](../../../specs/user-stories/US04-crear-expediente-de-registro.md).

## Criterios de aceptación

| Criterio | Reglas o fuente | Verificación prevista | Tipo (spec_sync) | Pruebas | Clases de prueba |
| :--- | :--- | :--- | :--- | :--- | :--- |
| US03 CA-005 | INV-08 | Aplicación y persistencia. | backend | 4 | StartSupplierRegistrationIntegrationTest, StartSupplierRegistrationServiceTest, SupplierRegistrationPersistenceAdapterTest, SupplierRegistrationTest |
| US04 CA-001 | INV-10 | Dominio, aplicación y persistencia. | backend | 4 | StartSupplierRegistrationIntegrationTest, StartSupplierRegistrationServiceTest, SupplierRegistrationPersistenceAdapterTest, SupplierRegistrationTest |
| US04 CA-002 | INV-36 | Dominio, aplicación y persistencia. | backend | 4 | StartSupplierRegistrationIntegrationTest, StartSupplierRegistrationServiceTest, SupplierRegistrationPersistenceAdapterTest, SupplierRegistrationTest |
| US04 CA-003 | INV-09 | Dominio, aplicación y persistencia. | backend | 3 | StartSupplierRegistrationIntegrationTest, StartSupplierRegistrationServiceTest, SupplierRegistrationPersistenceAdapterTest |
| US04 CA-004 | INV-10 | Dominio, aplicación y persistencia. | backend | 2 | StartSupplierRegistrationServiceTest, SupplierRegistrationsControllerTest |
| US04 CA-005 | INV-10 | Dominio, aplicación y persistencia. | backend | 3 | StartSupplierRegistrationIntegrationTest, StartSupplierRegistrationServiceTest, SupplierRegistrationTest |
| US04 CA-006 | INV-10 | Dominio, aplicación y persistencia. | backend | 4 | IdentificationPersistenceAdapterTest, StartSupplierRegistrationIntegrationTest, StartSupplierRegistrationServiceTest, SupplierRegistrationTest |

Criterios cuya operación principal es esta, según [traceability.md §3](../../../specs/traceability.md#3-cobertura-de-criterios-de-aceptación). Conteo de [`docs/sync-report.md`](../../sync-report.md).

## Caso de uso

| Elemento | Clase |
| :--- | :--- |
| Puerto de entrada | [`StartSupplierRegistrationUseCase.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/in/StartSupplierRegistrationUseCase.java) |
| Entrada | [`StartSupplierRegistrationCommand.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/in/StartSupplierRegistrationCommand.java) |
| Resultado | [`SupplierRegistrationResult.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/in/SupplierRegistrationResult.java) |
| Decorador transaccional | [`TransactionalStartSupplierRegistrationUseCase.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/transaction/TransactionalStartSupplierRegistrationUseCase.java) |
| Servicio | [`StartSupplierRegistrationService.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/service/StartSupplierRegistrationService.java) |

## Invariantes

INV-07 a INV-11, INV-36. Detalle en [dominio.md](../dominio.md#invariantes).

## Archivos por capa

| Capa | Archivos |
| :--- | :--- |
| Web | [`SupplierRegistrationsController.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/web/SupplierRegistrationsController.java) implementa `SupplierRegistrationsApi` (generada desde el contrato) |
| Web (token) | [`SecurityConfiguration.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/configuration/SecurityConfiguration.java), [`RegistrationTokenPrincipal.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/web/RegistrationTokenPrincipal.java) |
| Errores | [`GlobalExceptionHandler.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/web/GlobalExceptionHandler.java) |
| Transacción | [`TransactionalStartSupplierRegistrationUseCase.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/transaction/TransactionalStartSupplierRegistrationUseCase.java) |
| Aplicación | [`StartSupplierRegistrationService.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/service/StartSupplierRegistrationService.java) |
| Dominio | [`ApplicantIdentity.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/domain/model/ApplicantIdentity.java), [`SupplierRegistration.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/domain/model/SupplierRegistration.java), [`SupplierRegistrationId.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/domain/model/SupplierRegistrationId.java) |
| Puertos de salida | [`RegistrationContextPort.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/out/RegistrationContextPort.java), [`SupplierRegistrationPersistencePort.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/out/SupplierRegistrationPersistencePort.java) |
| Adaptadores de salida | [`RegistrationContextAdapter.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/out/persistence/RegistrationContextAdapter.java), [`SupplierRegistrationPersistenceAdapter.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/out/persistence/SupplierRegistrationPersistenceAdapter.java) |

## Pruebas

| Clase de prueba | Ubicación |
| :--- | :--- |
| `IdentificationPersistenceAdapterTest` | [mx/com/fredkorlabs/registration/adapter/out/persistence/IdentificationPersistenceAdapterTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/adapter/out/persistence/IdentificationPersistenceAdapterTest.java) |
| `StartSupplierRegistrationIntegrationTest` | [mx/com/fredkorlabs/registration/StartSupplierRegistrationIntegrationTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/StartSupplierRegistrationIntegrationTest.java) |
| `StartSupplierRegistrationServiceTest` | [mx/com/fredkorlabs/registration/application/service/StartSupplierRegistrationServiceTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/application/service/StartSupplierRegistrationServiceTest.java) |
| `SupplierRegistrationPersistenceAdapterTest` | [mx/com/fredkorlabs/registration/adapter/out/persistence/SupplierRegistrationPersistenceAdapterTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/adapter/out/persistence/SupplierRegistrationPersistenceAdapterTest.java) |
| `SupplierRegistrationTest` | [mx/com/fredkorlabs/registration/domain/SupplierRegistrationTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/domain/SupplierRegistrationTest.java) |
| `SupplierRegistrationsControllerTest` | [mx/com/fredkorlabs/registration/adapter/in/web/SupplierRegistrationsControllerTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/adapter/in/web/SupplierRegistrationsControllerTest.java) |

Incluye las clases nombradas como el caso de uso o el controlador y las que verifican sus criterios.
