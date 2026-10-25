<!-- Generado con /documentar desde specs/ y backend/src/. No editar a mano: cambia la spec y regenera. -->


# `getSubsectors`

**GET `/api/v1/registration-catalogs/sectors/{sectorId}/subsectors`** — Consultar subsectores de un sector.

- Contrato: [api.md](../api.md#getsubsectors).
- Secuencia: [diagramas.md](../diagramas.md#65-getsubsectors).
- Trazabilidad: [traceability.md §2](../../../specs/traceability.md#2-operaciones-de-la-primera-etapa).

## Historias y requerimientos

[RF-REG-05](../../../specs/requirements/functional.md), [US08](../../../specs/user-stories/US08-capturar-datos-generales.md).

## Criterios de aceptación

| Criterio | Reglas o fuente | Verificación prevista | Tipo (spec_sync) | Pruebas | Clases de prueba |
| :--- | :--- | :--- | :--- | :--- | :--- |
| US08 CA-023 | US08 CA-023; ADR-0020 | Catálogos, web e interfaz: subsectores del sector en la ruta. | backend | 3 | GeneralDataCatalogAdapterTest, GetSubsectorsIntegrationTest, GetSubsectorsServiceTest |

Criterios cuya operación principal es esta, según [traceability.md §3](../../../specs/traceability.md#3-cobertura-de-criterios-de-aceptación). Conteo de [`docs/sync-report.md`](../../sync-report.md).

## Caso de uso

| Elemento | Clase |
| :--- | :--- |
| Puerto de entrada | [`GetSubsectorsUseCase.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/in/GetSubsectorsUseCase.java) |
| Entrada | [`GetSubsectorsQuery.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/in/GetSubsectorsQuery.java) |
| Resultado | [`CatalogOptionsResult.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/in/CatalogOptionsResult.java) |
| Decorador transaccional | Ninguno: se registra el servicio directo ([arquitectura §7](../../../specs/architecture/architecture.md#7-coordinación-y-transacciones)) |
| Servicio | [`GetSubsectorsService.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/service/GetSubsectorsService.java) |

## Invariantes

INV-21; ADR-0020. Detalle en [dominio.md](../dominio.md#invariantes).

## Archivos por capa

| Capa | Archivos |
| :--- | :--- |
| Web | [`RegistrationCatalogsController.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/web/RegistrationCatalogsController.java) implementa `RegistrationCatalogsApi` (generada desde el contrato) |
| Web (token) | [`SecurityConfiguration.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/configuration/SecurityConfiguration.java), [`RegistrationTokenPrincipal.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/web/RegistrationTokenPrincipal.java) |
| Errores | [`GlobalExceptionHandler.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/web/GlobalExceptionHandler.java) |
| Aplicación | [`GetSubsectorsService.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/service/GetSubsectorsService.java) |
| Dominio | — |
| Puertos de salida | [`GeneralDataCatalogPort.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/out/GeneralDataCatalogPort.java) |
| Adaptadores de salida | [`GeneralDataCatalogAdapter.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/out/catalog/GeneralDataCatalogAdapter.java) |

## Pruebas

| Clase de prueba | Ubicación |
| :--- | :--- |
| `GeneralDataCatalogAdapterTest` | [mx/com/fredkorlabs/registration/adapter/out/catalog/GeneralDataCatalogAdapterTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/adapter/out/catalog/GeneralDataCatalogAdapterTest.java) |
| `GetSubsectorsIntegrationTest` | [mx/com/fredkorlabs/registration/GetSubsectorsIntegrationTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/GetSubsectorsIntegrationTest.java) |
| `GetSubsectorsServiceTest` | [mx/com/fredkorlabs/registration/application/service/GetSubsectorsServiceTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/application/service/GetSubsectorsServiceTest.java) |

Incluye las clases nombradas como el caso de uso o el controlador y las que verifican sus criterios.
