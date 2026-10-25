<!-- Generado con /documentar desde specs/ y backend/src/. No editar a mano: cambia la spec y regenera. -->


# `saveCucopParticipation`

**PUT `/api/v1/supplier-registrations/{registrationId}/cucop`** — Validar y guardar participación CUCoP.

- Contrato: [api.md](../api.md#savecucopparticipation).
- Secuencia: [diagramas.md](../diagramas.md#45-savecucopparticipation).
- Trazabilidad: [traceability.md §2](../../../specs/traceability.md#2-operaciones-de-la-primera-etapa).

## Historias y requerimientos

[RF-REG-08](../../../specs/requirements/functional.md), [RF-REG-05](../../../specs/requirements/functional.md), [US11](../../../specs/user-stories/US11-capturar-participacion-y-partidas-cucop.md), [US06](../../../specs/user-stories/US06-navegar-y-guardar-secciones.md).

## Criterios de aceptación

| Criterio | Reglas o fuente | Verificación prevista | Tipo (spec_sync) | Pruebas | Clases de prueba |
| :--- | :--- | :--- | :--- | :--- | :--- |
| US11 CA-001 | INV-30 | Interfaz (acceso a la sección) y aplicación (el guardado exige Datos Generales guardados). `getCucopCatalog` no recibe expediente. | backend | 3 | SaveCucopParticipationIntegrationTest, SaveCucopParticipationServiceTest, SupplierRegistrationTest |
| US11 CA-004 | INV-31 | Interfaz y dominio. | backend | 2 | CucopParticipationTest, SaveCucopParticipationIntegrationTest |
| US11 CA-011 | INV-31, INV-34 | Dominio, aplicación y persistencia. | backend | 5 | CucopParticipationTest, SaveCucopParticipationIntegrationTest, SaveCucopParticipationServiceTest, SupplierRegistrationPersistenceAdapterTest, SupplierRegistrationTest |
| US11 CA-012 | INV-34 | Aplicación y web. | backend | 5 | CucopCatalogAdapterTest, CucopParticipationTest, SaveCucopParticipationIntegrationTest, SaveCucopParticipationRollbackTest, SaveCucopParticipationServiceTest |

Criterios cuya operación principal es esta, según [traceability.md §3](../../../specs/traceability.md#3-cobertura-de-criterios-de-aceptación). Conteo de [`docs/sync-report.md`](../../sync-report.md).

## Caso de uso

| Elemento | Clase |
| :--- | :--- |
| Puerto de entrada | [`SaveCucopParticipationUseCase.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/in/SaveCucopParticipationUseCase.java) |
| Entrada | [`SaveCucopParticipationCommand.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/in/SaveCucopParticipationCommand.java) |
| Resultado | [`SectionSaveResult.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/in/SectionSaveResult.java) |
| Decorador transaccional | [`TransactionalSaveCucopParticipationUseCase.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/transaction/TransactionalSaveCucopParticipationUseCase.java) |
| Servicio | [`SaveCucopParticipationService.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/service/SaveCucopParticipationService.java) |

## Invariantes

INV-30 a INV-34, INV-37. Detalle en [dominio.md](../dominio.md#invariantes).

## Archivos por capa

| Capa | Archivos |
| :--- | :--- |
| Web | [`SupplierRegistrationsController.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/web/SupplierRegistrationsController.java) implementa `SupplierRegistrationsApi` (generada desde el contrato) |
| Web (token) | [`SecurityConfiguration.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/configuration/SecurityConfiguration.java), [`RegistrationTokenPrincipal.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/web/RegistrationTokenPrincipal.java) |
| Errores | [`GlobalExceptionHandler.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/web/GlobalExceptionHandler.java) |
| Transacción | [`TransactionalSaveCucopParticipationUseCase.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/transaction/TransactionalSaveCucopParticipationUseCase.java) |
| Aplicación | [`SaveCucopParticipationService.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/service/SaveCucopParticipationService.java), [`OwnedRegistration.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/service/OwnedRegistration.java) |
| Dominio | [`CucopParticipation.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/domain/model/CucopParticipation.java), [`CucopSpecificItem.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/domain/model/CucopSpecificItem.java), [`ParticipationType.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/domain/model/ParticipationType.java), [`RegistrationSection.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/domain/model/RegistrationSection.java), [`SectionStatus.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/domain/model/SectionStatus.java), [`SupplierRegistration.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/domain/model/SupplierRegistration.java) |
| Puertos de salida | [`RegistrationContextPort.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/out/RegistrationContextPort.java), [`SupplierRegistrationPersistencePort.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/out/SupplierRegistrationPersistencePort.java), [`CucopCatalogPort.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/out/CucopCatalogPort.java) |
| Adaptadores de salida | [`RegistrationContextAdapter.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/out/persistence/RegistrationContextAdapter.java), [`SupplierRegistrationPersistenceAdapter.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/out/persistence/SupplierRegistrationPersistenceAdapter.java), [`CucopCatalogAdapter.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/out/catalog/CucopCatalogAdapter.java) |

## Pruebas

| Clase de prueba | Ubicación |
| :--- | :--- |
| `CucopCatalogAdapterTest` | [mx/com/fredkorlabs/registration/adapter/out/catalog/CucopCatalogAdapterTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/adapter/out/catalog/CucopCatalogAdapterTest.java) |
| `CucopParticipationTest` | [mx/com/fredkorlabs/registration/domain/CucopParticipationTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/domain/CucopParticipationTest.java) |
| `SaveCucopParticipationIntegrationTest` | [mx/com/fredkorlabs/registration/SaveCucopParticipationIntegrationTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/SaveCucopParticipationIntegrationTest.java) |
| `SaveCucopParticipationRollbackTest` | [mx/com/fredkorlabs/registration/SaveCucopParticipationRollbackTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/SaveCucopParticipationRollbackTest.java) |
| `SaveCucopParticipationServiceTest` | [mx/com/fredkorlabs/registration/application/service/SaveCucopParticipationServiceTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/application/service/SaveCucopParticipationServiceTest.java) |
| `SupplierRegistrationPersistenceAdapterTest` | [mx/com/fredkorlabs/registration/adapter/out/persistence/SupplierRegistrationPersistenceAdapterTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/adapter/out/persistence/SupplierRegistrationPersistenceAdapterTest.java) |
| `SupplierRegistrationTest` | [mx/com/fredkorlabs/registration/domain/SupplierRegistrationTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/domain/SupplierRegistrationTest.java) |
| `SupplierRegistrationsControllerTest` | [mx/com/fredkorlabs/registration/adapter/in/web/SupplierRegistrationsControllerTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/adapter/in/web/SupplierRegistrationsControllerTest.java) |

Incluye las clases nombradas como el caso de uso o el controlador y las que verifican sus criterios.
