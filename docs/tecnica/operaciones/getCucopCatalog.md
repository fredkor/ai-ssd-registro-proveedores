<!-- Generado con /documentar desde specs/ y backend/src/. No editar a mano: cambia la spec y regenera. -->


# `getCucopCatalog`

**GET `/api/v1/registration-catalogs/cucop`** — Cargar el árbol CUCoP completo.

- Contrato: [api.md](../api.md#getcucopcatalog).
- Secuencia: [diagramas.md](../diagramas.md#611-getcucopcatalog).
- Trazabilidad: [traceability.md §2](../../../specs/traceability.md#2-operaciones-de-la-primera-etapa).

## Historias y requerimientos

[RF-REG-08](../../../specs/requirements/functional.md), [US11](../../../specs/user-stories/US11-capturar-participacion-y-partidas-cucop.md).

## Criterios de aceptación

| Criterio | Reglas o fuente | Verificación prevista | Tipo (spec_sync) | Pruebas | Clases de prueba |
| :--- | :--- | :--- | :--- | :--- | :--- |
| US11 CA-015 | US11 CA-015 | Web, catálogos e interfaz. | backend | 3 | CucopCatalogAdapterTest, GetCucopCatalogIntegrationTest, GetCucopCatalogServiceTest |

Criterios cuya operación principal es esta, según [traceability.md §3](../../../specs/traceability.md#3-cobertura-de-criterios-de-aceptación). Conteo de [`docs/sync-report.md`](../../sync-report.md).

## Caso de uso

| Elemento | Clase |
| :--- | :--- |
| Puerto de entrada | [`GetCucopCatalogUseCase.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/in/GetCucopCatalogUseCase.java) |
| Entrada | Sin parámetros |
| Resultado | [`GetCucopCatalogResult.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/in/GetCucopCatalogResult.java) |
| Decorador transaccional | Ninguno: se registra el servicio directo ([arquitectura §7](../../../specs/architecture/architecture.md#7-coordinación-y-transacciones)) |
| Servicio | [`GetCucopCatalogService.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/service/GetCucopCatalogService.java) |

## Invariantes

INV-32; RF-REG-08. Detalle en [dominio.md](../dominio.md#invariantes).

## Archivos por capa

| Capa | Archivos |
| :--- | :--- |
| Web | [`RegistrationCatalogsController.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/web/RegistrationCatalogsController.java) implementa `RegistrationCatalogsApi` (generada desde el contrato) |
| Web (token) | [`SecurityConfiguration.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/configuration/SecurityConfiguration.java), [`RegistrationTokenPrincipal.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/web/RegistrationTokenPrincipal.java) |
| Errores | [`GlobalExceptionHandler.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/web/GlobalExceptionHandler.java) |
| Aplicación | [`GetCucopCatalogService.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/service/GetCucopCatalogService.java) |
| Dominio | — |
| Puertos de salida | [`CucopCatalogPort.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/out/CucopCatalogPort.java) |
| Adaptadores de salida | [`CucopCatalogAdapter.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/out/catalog/CucopCatalogAdapter.java) |

## Pruebas

| Clase de prueba | Ubicación |
| :--- | :--- |
| `CucopCatalogAdapterTest` | [mx/com/fredkorlabs/registration/adapter/out/catalog/CucopCatalogAdapterTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/adapter/out/catalog/CucopCatalogAdapterTest.java) |
| `GetCucopCatalogIntegrationTest` | [mx/com/fredkorlabs/registration/GetCucopCatalogIntegrationTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/GetCucopCatalogIntegrationTest.java) |
| `GetCucopCatalogServiceTest` | [mx/com/fredkorlabs/registration/application/service/GetCucopCatalogServiceTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/application/service/GetCucopCatalogServiceTest.java) |

Incluye las clases nombradas como el caso de uso o el controlador y las que verifican sus criterios.
