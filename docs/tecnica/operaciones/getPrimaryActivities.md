<!-- Generado con /documentar desde specs/ y backend/src/. No editar a mano: cambia la spec y regenera. -->


# `getPrimaryActivities`

**GET `/api/v1/registration-catalogs/primary-activities`** — Consultar catálogo de actividades preponderantes.

- Contrato: [api.md](../api.md#getprimaryactivities).
- Secuencia: [diagramas.md](../diagramas.md#68-getprimaryactivities).
- Trazabilidad: [traceability.md §2](../../../specs/traceability.md#2-operaciones-de-la-primera-etapa).

## Historias y requerimientos

[RF-REG-05](../../../specs/requirements/functional.md), [US08](../../../specs/user-stories/US08-capturar-datos-generales.md).

## Criterios de aceptación

| Criterio | Reglas o fuente | Verificación prevista | Tipo (spec_sync) | Pruebas | Clases de prueba |
| :--- | :--- | :--- | :--- | :--- | :--- |
| US08 CA-022 | US08 CA-022; ADR-0020 | Catálogos, web e interfaz: un recurso por catálogo. | backend | 14 | GeneralDataCatalogAdapterTest, GetAnnualSalesRangesIntegrationTest, GetAnnualSalesRangesServiceTest, GetBusinessActivitiesIntegrationTest, GetBusinessActivitiesServiceTest, GetPrimaryActivitiesIntegrationTest, GetPrimaryActivitiesServiceTest, GetSectorsIntegrationTest, GetSectorsServiceTest, GetStratificationsIntegrationTest, GetStratificationsServiceTest, GetTaxRegimesIntegrationTest, GetWorkerCountRangesIntegrationTest, GetWorkerCountRangesServiceTest |

Criterios cuya operación principal es esta, según [traceability.md §3](../../../specs/traceability.md#3-cobertura-de-criterios-de-aceptación). Conteo de [`docs/sync-report.md`](../../sync-report.md).

## Caso de uso

| Elemento | Clase |
| :--- | :--- |
| Puerto de entrada | [`GetPrimaryActivitiesUseCase.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/in/GetPrimaryActivitiesUseCase.java) |
| Entrada | Sin parámetros |
| Resultado | [`CatalogOptionsResult.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/in/CatalogOptionsResult.java) |
| Decorador transaccional | Ninguno: se registra el servicio directo ([arquitectura §7](../../../specs/architecture/architecture.md#7-coordinación-y-transacciones)) |
| Servicio | [`GetPrimaryActivitiesService.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/service/GetPrimaryActivitiesService.java) |

## Invariantes

INV-17; ADR-0020. Detalle en [dominio.md](../dominio.md#invariantes).

## Archivos por capa

| Capa | Archivos |
| :--- | :--- |
| Web | [`RegistrationCatalogsController.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/web/RegistrationCatalogsController.java) implementa `RegistrationCatalogsApi` (generada desde el contrato) |
| Web (token) | [`SecurityConfiguration.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/configuration/SecurityConfiguration.java), [`RegistrationTokenPrincipal.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/web/RegistrationTokenPrincipal.java) |
| Errores | [`GlobalExceptionHandler.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/web/GlobalExceptionHandler.java) |
| Aplicación | [`GetPrimaryActivitiesService.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/service/GetPrimaryActivitiesService.java) |
| Dominio | — |
| Puertos de salida | [`GeneralDataCatalogPort.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/out/GeneralDataCatalogPort.java) |
| Adaptadores de salida | [`GeneralDataCatalogAdapter.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/out/catalog/GeneralDataCatalogAdapter.java) |

## Pruebas

| Clase de prueba | Ubicación |
| :--- | :--- |
| `GeneralDataCatalogAdapterTest` | [mx/com/fredkorlabs/registration/adapter/out/catalog/GeneralDataCatalogAdapterTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/adapter/out/catalog/GeneralDataCatalogAdapterTest.java) |
| `GetAnnualSalesRangesIntegrationTest` | [mx/com/fredkorlabs/registration/GetAnnualSalesRangesIntegrationTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/GetAnnualSalesRangesIntegrationTest.java) |
| `GetAnnualSalesRangesServiceTest` | [mx/com/fredkorlabs/registration/application/service/GetAnnualSalesRangesServiceTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/application/service/GetAnnualSalesRangesServiceTest.java) |
| `GetBusinessActivitiesIntegrationTest` | [mx/com/fredkorlabs/registration/GetBusinessActivitiesIntegrationTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/GetBusinessActivitiesIntegrationTest.java) |
| `GetBusinessActivitiesServiceTest` | [mx/com/fredkorlabs/registration/application/service/GetBusinessActivitiesServiceTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/application/service/GetBusinessActivitiesServiceTest.java) |
| `GetPrimaryActivitiesIntegrationTest` | [mx/com/fredkorlabs/registration/GetPrimaryActivitiesIntegrationTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/GetPrimaryActivitiesIntegrationTest.java) |
| `GetPrimaryActivitiesServiceTest` | [mx/com/fredkorlabs/registration/application/service/GetPrimaryActivitiesServiceTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/application/service/GetPrimaryActivitiesServiceTest.java) |
| `GetSectorsIntegrationTest` | [mx/com/fredkorlabs/registration/GetSectorsIntegrationTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/GetSectorsIntegrationTest.java) |
| `GetSectorsServiceTest` | [mx/com/fredkorlabs/registration/application/service/GetSectorsServiceTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/application/service/GetSectorsServiceTest.java) |
| `GetStratificationsIntegrationTest` | [mx/com/fredkorlabs/registration/GetStratificationsIntegrationTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/GetStratificationsIntegrationTest.java) |
| `GetStratificationsServiceTest` | [mx/com/fredkorlabs/registration/application/service/GetStratificationsServiceTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/application/service/GetStratificationsServiceTest.java) |
| `GetTaxRegimesIntegrationTest` | [mx/com/fredkorlabs/registration/GetTaxRegimesIntegrationTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/GetTaxRegimesIntegrationTest.java) |
| `GetWorkerCountRangesIntegrationTest` | [mx/com/fredkorlabs/registration/GetWorkerCountRangesIntegrationTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/GetWorkerCountRangesIntegrationTest.java) |
| `GetWorkerCountRangesServiceTest` | [mx/com/fredkorlabs/registration/application/service/GetWorkerCountRangesServiceTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/application/service/GetWorkerCountRangesServiceTest.java) |

Incluye las clases nombradas como el caso de uso o el controlador y las que verifican sus criterios.
