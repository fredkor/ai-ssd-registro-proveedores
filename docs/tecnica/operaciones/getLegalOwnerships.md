<!-- Generado con /documentar desde specs/ y backend/src/. No editar a mano: cambia la spec y regenera. -->


# `getLegalOwnerships`

**GET `/api/v1/registration-catalogs/legal-ownerships`** — Consultar titularidad jurídica aplicable.

- Contrato: [api.md](../api.md#getlegalownerships).
- Secuencia: [diagramas.md](../diagramas.md#63-getlegalownerships).
- Trazabilidad: [traceability.md §2](../../../specs/traceability.md#2-operaciones-de-la-primera-etapa).

## Historias y requerimientos

[RF-REG-05](../../../specs/requirements/functional.md), [US08](../../../specs/user-stories/US08-capturar-datos-generales.md).

## Criterios de aceptación

| Criterio | Reglas o fuente | Verificación prevista | Tipo (spec_sync) | Pruebas | Clases de prueba |
| :--- | :--- | :--- | :--- | :--- | :--- |
| US08 CA-003 | INV-20; ADR-0020 | Catálogos, aplicación y web: filtro en backend por país y régimen nacionales; orden por clave. | backend | 6 | GeneralDataCatalogAdapterTest, GeneralDataTest, GetLegalOwnershipsIntegrationTest, GetLegalOwnershipsServiceTest, LegalOwnershipOptionTest, SaveGeneralDataIntegrationTest |
| US08 CA-014 | INV-20; ADR-0020 | Catálogos, aplicación e interfaz: país o NOT_DEF extranjero sin filtro por régimen; NOT_DEF con y sin opciones del país; respaldo vacío y orden por clave. | backend | 6 | GeneralDataCatalogAdapterTest, GeneralDataTest, GetLegalOwnershipsIntegrationTest, GetLegalOwnershipsServiceTest, LegalOwnershipOptionTest, ResponsePropertyInclusionTest |

Criterios cuya operación principal es esta, según [traceability.md §3](../../../specs/traceability.md#3-cobertura-de-criterios-de-aceptación). Conteo de [`docs/sync-report.md`](../../sync-report.md).

## Caso de uso

| Elemento | Clase |
| :--- | :--- |
| Puerto de entrada | [`GetLegalOwnershipsUseCase.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/in/GetLegalOwnershipsUseCase.java) |
| Entrada | [`GetLegalOwnershipsQuery.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/in/GetLegalOwnershipsQuery.java) |
| Resultado | [`GetLegalOwnershipsResult.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/in/GetLegalOwnershipsResult.java) |
| Decorador transaccional | Ninguno: se registra el servicio directo ([arquitectura §7](../../../specs/architecture/architecture.md#7-coordinación-y-transacciones)) |
| Servicio | [`GetLegalOwnershipsService.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/service/GetLegalOwnershipsService.java) |

## Invariantes

INV-20; ADR-0020. Detalle en [dominio.md](../dominio.md#invariantes).

## Archivos por capa

| Capa | Archivos |
| :--- | :--- |
| Web | [`RegistrationCatalogsController.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/web/RegistrationCatalogsController.java) implementa `RegistrationCatalogsApi` (generada desde el contrato) |
| Web (token) | [`SecurityConfiguration.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/configuration/SecurityConfiguration.java), [`RegistrationTokenPrincipal.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/web/RegistrationTokenPrincipal.java) |
| Errores | [`GlobalExceptionHandler.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/web/GlobalExceptionHandler.java) |
| Aplicación | [`GetLegalOwnershipsService.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/service/GetLegalOwnershipsService.java) |
| Dominio | [`LegalOwnershipOption.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/domain/model/LegalOwnershipOption.java), [`Origin.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/domain/model/Origin.java), [`TaxRegime.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/domain/model/TaxRegime.java) |
| Puertos de salida | [`GeneralDataCatalogPort.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/out/GeneralDataCatalogPort.java) |
| Adaptadores de salida | [`GeneralDataCatalogAdapter.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/out/catalog/GeneralDataCatalogAdapter.java) |

## Pruebas

| Clase de prueba | Ubicación |
| :--- | :--- |
| `GeneralDataCatalogAdapterTest` | [mx/com/fredkorlabs/registration/adapter/out/catalog/GeneralDataCatalogAdapterTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/adapter/out/catalog/GeneralDataCatalogAdapterTest.java) |
| `GeneralDataTest` | [mx/com/fredkorlabs/registration/domain/GeneralDataTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/domain/GeneralDataTest.java) |
| `GetLegalOwnershipsIntegrationTest` | [mx/com/fredkorlabs/registration/GetLegalOwnershipsIntegrationTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/GetLegalOwnershipsIntegrationTest.java) |
| `GetLegalOwnershipsServiceTest` | [mx/com/fredkorlabs/registration/application/service/GetLegalOwnershipsServiceTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/application/service/GetLegalOwnershipsServiceTest.java) |
| `LegalOwnershipOptionTest` | [mx/com/fredkorlabs/registration/domain/LegalOwnershipOptionTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/domain/LegalOwnershipOptionTest.java) |
| `ResponsePropertyInclusionTest` | [mx/com/fredkorlabs/registration/configuration/ResponsePropertyInclusionTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/configuration/ResponsePropertyInclusionTest.java) |
| `SaveGeneralDataIntegrationTest` | [mx/com/fredkorlabs/registration/SaveGeneralDataIntegrationTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/SaveGeneralDataIntegrationTest.java) |

Incluye las clases nombradas como el caso de uso o el controlador y las que verifican sus criterios.
