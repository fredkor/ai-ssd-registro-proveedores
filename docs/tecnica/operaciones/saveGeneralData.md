<!-- Generado con /documentar desde specs/ y backend/src/. No editar a mano: cambia la spec y regenera. -->


# `saveGeneralData`

**PUT `/api/v1/supplier-registrations/{registrationId}/general-data`** — Validar y guardar Datos Generales completos.

- Contrato: [api.md](../api.md#savegeneraldata).
- Secuencia: [diagramas.md](../diagramas.md#44-savegeneraldata).
- Trazabilidad: [traceability.md §2](../../../specs/traceability.md#2-operaciones-de-la-primera-etapa).

## Historias y requerimientos

[RF-REG-05](../../../specs/requirements/functional.md), [US06](../../../specs/user-stories/US06-navegar-y-guardar-secciones.md), [US08](../../../specs/user-stories/US08-capturar-datos-generales.md), [US09](../../../specs/user-stories/US09-capturar-telefonos-del-proveedor.md), [US10](../../../specs/user-stories/US10-gestionar-cedula-fiscal.md).

## Criterios de aceptación

| Criterio | Reglas o fuente | Verificación prevista | Tipo (spec_sync) | Pruebas | Clases de prueba |
| :--- | :--- | :--- | :--- | :--- | :--- |
| US06 CA-006 | INV-37 | Dominio, aplicación y persistencia: CUCoP conserva selecciones y progreso intactos. | backend | 3 | SaveCucopParticipationIntegrationTest, SaveGeneralDataIntegrationTest, SupplierRegistrationTest |
| US08 CA-003 | INV-20; ADR-0020 | Catálogos, aplicación y web: filtro en backend por país y régimen nacionales; orden por clave. | backend | 6 | GeneralDataCatalogAdapterTest, GeneralDataTest, GetLegalOwnershipsIntegrationTest, GetLegalOwnershipsServiceTest, LegalOwnershipOptionTest, SaveGeneralDataIntegrationTest |
| US08 CA-004 | US08 CA-004 | Dominio, aplicación y web. | backend | 2 | GeneralDataTest, SaveGeneralDataIntegrationTest |
| US08 CA-006 | INV-34 | Dominio, aplicación y web. | backend | 4 | GeneralDataTest, SaveGeneralDataIntegrationTest, SaveGeneralDataServiceTest, SupplierRegistrationTest |
| US08 CA-007 | INV-34 | Dominio, aplicación y web. | backend | 3 | GeneralDataTest, SaveGeneralDataIntegrationTest, SaveGeneralDataServiceTest |
| US08 CA-008 | INV-23; ADR-0020 | Catálogos, dominio y web: solo registros activos; inactivo rechazado al guardar. | backend | 3 | GeneralDataCatalogAdapterTest, GeneralDataTest, SaveGeneralDataIntegrationTest |
| US08 CA-013 | INV-24; ADR-0009 | Dominio, aplicación y web: URL absoluta http/https con host; rechazar falta de protocolo/host, otros esquemas y longitud mayor a 2048 tras recorte; vacío a null, sin acceso a red. | backend | 3 | GeneralDataTest, SaveGeneralDataIntegrationTest, WebsiteTest |
| US08 CA-014 | INV-20; ADR-0020 | Catálogos, aplicación e interfaz: país o NOT_DEF extranjero sin filtro por régimen; NOT_DEF con y sin opciones del país; respaldo vacío y orden por clave. | backend | 6 | GeneralDataCatalogAdapterTest, GeneralDataTest, GetLegalOwnershipsIntegrationTest, GetLegalOwnershipsServiceTest, LegalOwnershipOptionTest, ResponsePropertyInclusionTest |
| US08 CA-018 | INV-19 | Dominio, aplicación y web. | backend | 3 | ForeignFiscalCodeTest, GeneralDataTest, SaveGeneralDataIntegrationTest |
| US08 CA-021 | ADR-0009; RF-REG-05 | Dominio, aplicación y web. | backend | 2 | GeneralDataTest, SaveGeneralDataIntegrationTest |
| US08 CA-024 | US08 CA-024 | Dominio, aplicación y web. | backend | 1 | SaveGeneralDataIntegrationTest |
| US08 CA-025 | ADR-0009; RF-REG-05 | Web, aplicación y dominio: recorte previo a validación y espacios interiores conservados. | backend | 2 | EditableTextFieldsTest, SaveGeneralDataIntegrationTest |
| US08 CA-026 | ADR-0009; RF-REG-05 | Web, aplicación y persistencia: obligatorio vacío rechazado, opcional vacío guardado como null. | backend | 3 | EditableTextFieldsTest, GeneralDataTest, SaveGeneralDataIntegrationTest |
| US08 CA-027 | ADR-0009; INV-06, INV-11 | Aplicación: identidad y evidencia PKI sin normalización. | backend | 1 | EditableTextFieldsTest |
| US09 CA-003 | INV-26 | Dominio, aplicación y web. | backend | 3 | GeneralDataTest, PhoneNumberTest, SaveGeneralDataIntegrationTest |
| US09 CA-004 | INV-26 | Dominio, aplicación y web. | backend | 2 | GeneralDataTest, SaveGeneralDataIntegrationTest |
| US09 CA-005 | INV-26 | Dominio y web. | backend | 1 | PhoneNumberTest |
| US09 CA-006 | ADR-0009; RF-REG-05 | Dominio y web. | backend | 2 | GeneralDataTest, PhoneNumberTest |
| US09 CA-007 | US09 CA-007; INV-26, INV-34 | Interfaz y aplicación: botón Eliminar por fila y persistencia de colección con Siguiente. | backend | 1 | SupplierRegistrationPersistenceAdapterTest |
| US09 CA-008 | INV-26; US09 CA-008 | Interfaz, dominio y web: eliminar última fila impide avanzar sin un teléfono válido. | backend | 1 | SaveGeneralDataIntegrationTest |
| US09 CA-009 | INV-34; US09 CA-009 | Aplicación y persistencia: salir o fallar sin guardar conserva teléfonos anteriores. | backend | 1 | SaveGeneralDataIntegrationTest |
| US10 CA-003 | INV-27 | Dominio y aplicación. | backend | 3 | SaveGeneralDataIntegrationTest, SaveGeneralDataServiceTest, SupplierRegistrationTest |
| US10 CA-007 | US10 CA-007 | Eliminar exitosamente deja Datos Generales PENDING; fallo conserva progreso; CUCoP intacto. | backend | 3 | DeleteFiscalDocumentIntegrationTest, DeleteFiscalDocumentServiceTest, SupplierRegistrationTest |
| US10 CA-010 | INV-27, INV-28; US10 CA-010 | Validar asociación existente sin UUID editable; fallo de sección conserva archivo cargado. | backend | 1 | SaveGeneralDataIntegrationTest |

Criterios cuya operación principal es esta, según [traceability.md §3](../../../specs/traceability.md#3-cobertura-de-criterios-de-aceptación). Conteo de [`docs/sync-report.md`](../../sync-report.md).

## Caso de uso

| Elemento | Clase |
| :--- | :--- |
| Puerto de entrada | [`SaveGeneralDataUseCase.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/in/SaveGeneralDataUseCase.java) |
| Entrada | [`SaveGeneralDataCommand.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/in/SaveGeneralDataCommand.java) |
| Resultado | [`SectionSaveResult.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/in/SectionSaveResult.java) |
| Decorador transaccional | [`TransactionalSaveGeneralDataUseCase.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/transaction/TransactionalSaveGeneralDataUseCase.java) |
| Servicio | [`SaveGeneralDataService.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/service/SaveGeneralDataService.java) |

## Invariantes

INV-14 a INV-28, INV-34, INV-37. Detalle en [dominio.md](../dominio.md#invariantes).

## Archivos por capa

| Capa | Archivos |
| :--- | :--- |
| Web | [`SupplierRegistrationsController.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/web/SupplierRegistrationsController.java) implementa `SupplierRegistrationsApi` (generada desde el contrato) |
| Web (token) | [`SecurityConfiguration.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/configuration/SecurityConfiguration.java), [`RegistrationTokenPrincipal.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/web/RegistrationTokenPrincipal.java) |
| Errores | [`GlobalExceptionHandler.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/web/GlobalExceptionHandler.java) |
| Transacción | [`TransactionalSaveGeneralDataUseCase.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/transaction/TransactionalSaveGeneralDataUseCase.java) |
| Aplicación | [`SaveGeneralDataService.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/service/SaveGeneralDataService.java), [`OwnedRegistration.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/service/OwnedRegistration.java) |
| Dominio | [`GeneralData.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/domain/model/GeneralData.java), [`GeneralDataInput.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/domain/model/GeneralDataInput.java), [`Origin.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/domain/model/Origin.java), [`RegistrationSection.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/domain/model/RegistrationSection.java), [`SectionStatus.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/domain/model/SectionStatus.java), [`SupplierRegistration.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/domain/model/SupplierRegistration.java) |
| Puertos de salida | [`RegistrationContextPort.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/out/RegistrationContextPort.java), [`SupplierRegistrationPersistencePort.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/out/SupplierRegistrationPersistencePort.java), [`GeneralDataCatalogPort.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/out/GeneralDataCatalogPort.java) |
| Adaptadores de salida | [`RegistrationContextAdapter.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/out/persistence/RegistrationContextAdapter.java), [`SupplierRegistrationPersistenceAdapter.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/out/persistence/SupplierRegistrationPersistenceAdapter.java), [`GeneralDataCatalogAdapter.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/out/catalog/GeneralDataCatalogAdapter.java) |

## Pruebas

| Clase de prueba | Ubicación |
| :--- | :--- |
| `DeleteFiscalDocumentIntegrationTest` | [mx/com/fredkorlabs/registration/DeleteFiscalDocumentIntegrationTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/DeleteFiscalDocumentIntegrationTest.java) |
| `DeleteFiscalDocumentServiceTest` | [mx/com/fredkorlabs/registration/application/service/DeleteFiscalDocumentServiceTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/application/service/DeleteFiscalDocumentServiceTest.java) |
| `EditableTextFieldsTest` | [mx/com/fredkorlabs/registration/adapter/in/web/EditableTextFieldsTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/adapter/in/web/EditableTextFieldsTest.java) |
| `ForeignFiscalCodeTest` | [mx/com/fredkorlabs/registration/domain/ForeignFiscalCodeTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/domain/ForeignFiscalCodeTest.java) |
| `GeneralDataCatalogAdapterTest` | [mx/com/fredkorlabs/registration/adapter/out/catalog/GeneralDataCatalogAdapterTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/adapter/out/catalog/GeneralDataCatalogAdapterTest.java) |
| `GeneralDataTest` | [mx/com/fredkorlabs/registration/domain/GeneralDataTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/domain/GeneralDataTest.java) |
| `GetLegalOwnershipsIntegrationTest` | [mx/com/fredkorlabs/registration/GetLegalOwnershipsIntegrationTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/GetLegalOwnershipsIntegrationTest.java) |
| `GetLegalOwnershipsServiceTest` | [mx/com/fredkorlabs/registration/application/service/GetLegalOwnershipsServiceTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/application/service/GetLegalOwnershipsServiceTest.java) |
| `LegalOwnershipOptionTest` | [mx/com/fredkorlabs/registration/domain/LegalOwnershipOptionTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/domain/LegalOwnershipOptionTest.java) |
| `PhoneNumberTest` | [mx/com/fredkorlabs/registration/domain/PhoneNumberTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/domain/PhoneNumberTest.java) |
| `ResponsePropertyInclusionTest` | [mx/com/fredkorlabs/registration/configuration/ResponsePropertyInclusionTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/configuration/ResponsePropertyInclusionTest.java) |
| `SaveCucopParticipationIntegrationTest` | [mx/com/fredkorlabs/registration/SaveCucopParticipationIntegrationTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/SaveCucopParticipationIntegrationTest.java) |
| `SaveGeneralDataIntegrationTest` | [mx/com/fredkorlabs/registration/SaveGeneralDataIntegrationTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/SaveGeneralDataIntegrationTest.java) |
| `SaveGeneralDataRollbackTest` | [mx/com/fredkorlabs/registration/SaveGeneralDataRollbackTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/SaveGeneralDataRollbackTest.java) |
| `SaveGeneralDataServiceTest` | [mx/com/fredkorlabs/registration/application/service/SaveGeneralDataServiceTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/application/service/SaveGeneralDataServiceTest.java) |
| `SupplierRegistrationPersistenceAdapterTest` | [mx/com/fredkorlabs/registration/adapter/out/persistence/SupplierRegistrationPersistenceAdapterTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/adapter/out/persistence/SupplierRegistrationPersistenceAdapterTest.java) |
| `SupplierRegistrationTest` | [mx/com/fredkorlabs/registration/domain/SupplierRegistrationTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/domain/SupplierRegistrationTest.java) |
| `SupplierRegistrationsControllerTest` | [mx/com/fredkorlabs/registration/adapter/in/web/SupplierRegistrationsControllerTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/adapter/in/web/SupplierRegistrationsControllerTest.java) |
| `WebsiteTest` | [mx/com/fredkorlabs/registration/domain/WebsiteTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/domain/WebsiteTest.java) |

Incluye las clases nombradas como el caso de uso o el controlador y las que verifican sus criterios.
