<!-- Generado con /documentar desde specs/ y backend/src/. No editar a mano: cambia la spec y regenera. -->


# `checkRegistrationExistence`

**GET `/api/v1/supplier-registrations/existence`** — Consultar existencia para la identidad aceptada.

- Contrato: [api.md](../api.md#checkregistrationexistence).
- Secuencia: [diagramas.md](../diagramas.md#41-checkregistrationexistence).
- Trazabilidad: [traceability.md §2](../../../specs/traceability.md#2-operaciones-de-la-primera-etapa).

## Historias y requerimientos

[RF-REG-02](../../../specs/requirements/functional.md), [US02](../../../specs/user-stories/US02-verificar-existencia-del-registro.md).

## Criterios de aceptación

| Criterio | Reglas o fuente | Verificación prevista | Tipo (spec_sync) | Pruebas | Clases de prueba |
| :--- | :--- | :--- | :--- | :--- | :--- |
| US02 CA-001 | INV-09 | Aplicación y web. | backend | 4 | CheckRegistrationExistenceIntegrationTest, CheckRegistrationExistenceServiceTest, ResponsePropertyInclusionTest, SupplierRegistrationPersistenceAdapterTest |
| US02 CA-002 | INV-09 | Aplicación y web. | backend | 3 | CheckRegistrationExistenceIntegrationTest, CheckRegistrationExistenceServiceTest, SupplierRegistrationPersistenceAdapterTest |
| US02 CA-003 | INV-09, INV-13 | Aplicación y web. | backend | 3 | CheckRegistrationExistenceIntegrationTest, CheckRegistrationExistenceServiceTest, SupplierRegistrationPersistenceAdapterTest |
| US02 CA-004 | INV-09 | Aplicación y web. | backend | 2 | CheckRegistrationExistenceServiceTest, SupplierRegistrationsControllerTest |
| US02 CA-005 | INV-11 | Aplicación y web. | backend | 3 | CheckRegistrationExistenceIntegrationTest, CheckRegistrationExistenceServiceTest, SupplierRegistrationPersistenceAdapterTest |

Criterios cuya operación principal es esta, según [traceability.md §3](../../../specs/traceability.md#3-cobertura-de-criterios-de-aceptación). Conteo de [`docs/sync-report.md`](../../sync-report.md).

## Caso de uso

| Elemento | Clase |
| :--- | :--- |
| Puerto de entrada | [`CheckRegistrationExistenceUseCase.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/in/CheckRegistrationExistenceUseCase.java) |
| Entrada | [`CheckRegistrationExistenceQuery.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/in/CheckRegistrationExistenceQuery.java) |
| Resultado | [`CheckRegistrationExistenceResult.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/in/CheckRegistrationExistenceResult.java) |
| Decorador transaccional | Ninguno: se registra el servicio directo ([arquitectura §7](../../../specs/architecture/architecture.md#7-coordinación-y-transacciones)) |
| Servicio | [`CheckRegistrationExistenceService.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/service/CheckRegistrationExistenceService.java) |

## Invariantes

INV-09, INV-11, INV-13. Detalle en [dominio.md](../dominio.md#invariantes).

## Archivos por capa

| Capa | Archivos |
| :--- | :--- |
| Web | [`SupplierRegistrationsController.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/web/SupplierRegistrationsController.java) implementa `SupplierRegistrationsApi` (generada desde el contrato) |
| Web (token) | [`SecurityConfiguration.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/configuration/SecurityConfiguration.java), [`RegistrationTokenPrincipal.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/web/RegistrationTokenPrincipal.java) |
| Errores | [`GlobalExceptionHandler.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/web/GlobalExceptionHandler.java) |
| Aplicación | [`CheckRegistrationExistenceService.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/service/CheckRegistrationExistenceService.java) |
| Dominio | [`Rfc.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/domain/model/Rfc.java) |
| Puertos de salida | [`SupplierRegistrationPersistencePort.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/out/SupplierRegistrationPersistencePort.java) |
| Adaptadores de salida | [`SupplierRegistrationPersistenceAdapter.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/out/persistence/SupplierRegistrationPersistenceAdapter.java) |

## Pruebas

| Clase de prueba | Ubicación |
| :--- | :--- |
| `CheckRegistrationExistenceIntegrationTest` | [mx/com/fredkorlabs/registration/CheckRegistrationExistenceIntegrationTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/CheckRegistrationExistenceIntegrationTest.java) |
| `CheckRegistrationExistenceServiceTest` | [mx/com/fredkorlabs/registration/application/service/CheckRegistrationExistenceServiceTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/application/service/CheckRegistrationExistenceServiceTest.java) |
| `ResponsePropertyInclusionTest` | [mx/com/fredkorlabs/registration/configuration/ResponsePropertyInclusionTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/configuration/ResponsePropertyInclusionTest.java) |
| `SupplierRegistrationPersistenceAdapterTest` | [mx/com/fredkorlabs/registration/adapter/out/persistence/SupplierRegistrationPersistenceAdapterTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/adapter/out/persistence/SupplierRegistrationPersistenceAdapterTest.java) |
| `SupplierRegistrationsControllerTest` | [mx/com/fredkorlabs/registration/adapter/in/web/SupplierRegistrationsControllerTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/adapter/in/web/SupplierRegistrationsControllerTest.java) |

Incluye las clases nombradas como el caso de uso o el controlador y las que verifican sus criterios.
