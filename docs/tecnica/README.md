<!-- Generado con /documentar desde specs/ y backend/src/. No editar a mano: cambia la spec y regenera. -->

# Documentación técnica — Registro de proveedores

Documentación derivada de [`specs/`](../../specs/) y del código de `backend/src/`. Explica y enlaza las
especificaciones; ante cualquier diferencia prevalecen las specs y el
[contrato OpenAPI](../../specs/api/supplier-registration-openapi.yaml). Se regenera con `/documentar`.

## Propósito

Según la [visión](../../specs/vision.md) (§1), el sistema permite que personas físicas y morales inicien y
continúen su registro como proveedores en la Plataforma de Compras de Fredkor Labs, mediante identificación con
e.firma y captura progresiva de su información. La primera etapa (§3) cubre aviso, identificación, existencia,
inicio y reanudación del expediente, Datos Generales con teléfonos y cédula fiscal, y CUCoP; termina en el punto de
continuación hacia Contactos. La finalización y las secciones posteriores quedan fuera (§4).

## Índice

| Documento | Contenido |
| :--- | :--- |
| [arquitectura.md](arquitectura.md) | Capas, regla de dependencia, modelos por frontera, paquetes con sus clases y flujo de una petición. |
| [diagramas.md](diagramas.md) | Componentes y un diagrama de secuencia por operación (lo mantiene `/diagramas`). |
| [api.md](api.md) | Las 22 operaciones del contrato: método, ruta, seguridad, petición, respuestas y códigos de error. |
| [operaciones/](operaciones/) | Una ficha por `operationId`: historias, criterios, caso de uso, invariantes, archivos por capa y pruebas. |
| [dominio.md](dominio.md) | Términos y su clase, estados, transiciones e invariantes INV-01 a INV-39 con las clases que las citan. |
| [pruebas.md](pruebas.md) | Estrategia por capa y cobertura de criterios. |
| [decisiones.md](decisiones.md) | Un renglón por ADR con su estado. |

Operaciones:

- [`getRegistrationNotice`](operaciones/getRegistrationNotice.md) — GET `/api/v1/registration-notices/current`
- [`acceptRegistrationNotice`](operaciones/acceptRegistrationNotice.md) — POST `/api/v1/registration-notice-acceptances`
- [`identifyApplicant`](operaciones/identifyApplicant.md) — POST `/api/v1/applicant-identifications`
- [`checkRegistrationExistence`](operaciones/checkRegistrationExistence.md) — GET `/api/v1/supplier-registrations/existence`
- [`startSupplierRegistration`](operaciones/startSupplierRegistration.md) — POST `/api/v1/supplier-registrations`
- [`resumeSupplierRegistration`](operaciones/resumeSupplierRegistration.md) — POST `/api/v1/supplier-registrations/{registrationId}/resume`
- [`saveGeneralData`](operaciones/saveGeneralData.md) — PUT `/api/v1/supplier-registrations/{registrationId}/general-data`
- [`saveCucopParticipation`](operaciones/saveCucopParticipation.md) — PUT `/api/v1/supplier-registrations/{registrationId}/cucop`
- [`uploadFiscalDocument`](operaciones/uploadFiscalDocument.md) — POST `/api/v1/supplier-registrations/{registrationId}/fiscal-document`
- [`downloadFiscalDocument`](operaciones/downloadFiscalDocument.md) — GET `/api/v1/supplier-registrations/{registrationId}/fiscal-document`
- [`deleteFiscalDocument`](operaciones/deleteFiscalDocument.md) — DELETE `/api/v1/supplier-registrations/{registrationId}/fiscal-document`
- [`getTaxRegimes`](operaciones/getTaxRegimes.md) — GET `/api/v1/registration-catalogs/tax-regimes`
- [`getCountries`](operaciones/getCountries.md) — GET `/api/v1/registration-catalogs/countries`
- [`getLegalOwnerships`](operaciones/getLegalOwnerships.md) — GET `/api/v1/registration-catalogs/legal-ownerships`
- [`getSectors`](operaciones/getSectors.md) — GET `/api/v1/registration-catalogs/sectors`
- [`getSubsectors`](operaciones/getSubsectors.md) — GET `/api/v1/registration-catalogs/sectors/{sectorId}/subsectors`
- [`getBusinessActivities`](operaciones/getBusinessActivities.md) — GET `/api/v1/registration-catalogs/business-activities`
- [`getStratifications`](operaciones/getStratifications.md) — GET `/api/v1/registration-catalogs/stratifications`
- [`getPrimaryActivities`](operaciones/getPrimaryActivities.md) — GET `/api/v1/registration-catalogs/primary-activities`
- [`getWorkerCountRanges`](operaciones/getWorkerCountRanges.md) — GET `/api/v1/registration-catalogs/worker-count-ranges`
- [`getAnnualSalesRanges`](operaciones/getAnnualSalesRanges.md) — GET `/api/v1/registration-catalogs/annual-sales-ranges`
- [`getCucopCatalog`](operaciones/getCucopCatalog.md) — GET `/api/v1/registration-catalogs/cucop`

## Compilar, probar y ejecutar

Resumen de [CLAUDE.md](../../CLAUDE.md) y del [README raíz](../../README.md#cómo-ejecutar-el-backend), que tiene
el detalle. Stack: [tech-stack.md](../../specs/architecture/tech-stack.md) (Java 25, Spring Boot 4.1,
PostgreSQL 17 con Flyway).

| Acción | Comando | Desde |
| :--- | :--- | :--- |
| Validar specs y contrato | `python3 tools/spec_lint.py --require-ready` | raíz |
| Specs contra código y pruebas | `python3 tools/spec_sync.py` (`--md docs/sync-report.md` para el informe) | raíz |
| Regenerar interfaces y DTO desde el contrato | `./mvnw clean generate-sources` | `backend/` |
| Compilar y probar (requiere Docker) | `./mvnw test` | `backend/` |
| Ejecutar la aplicación | `./mvnw spring-boot:run` | `backend/` |

- **Docker es obligatorio.** Las pruebas levantan un PostgreSQL 17 desechable con Testcontainers
  ([ADR-0018](../../specs/adr/0018-backend-postgresql-en-pruebas-y-desarrollo.md)); en local la base también
  corre en un contenedor (README raíz, paso 1).
- **Variables de entorno** (`backend/src/main/resources/application.yaml`):

| Variable | Uso |
| :--- | :--- |
| `REGISTRATION_DB_URL`, `REGISTRATION_DB_USERNAME`, `REGISTRATION_DB_PASSWORD` | Conexión a PostgreSQL 17 (ADR-0017). |
| `REGISTRATION_DOCUMENTS_ROOT` | Raíz del volumen documental; por omisión `/repository` (ADR-0010). |
| `REGISTRATION_JWT_PRIVATE_KEY_LOCATION`, `REGISTRATION_JWT_PUBLIC_KEY_LOCATION` | Llaves RSA en PEM. Sin ellas se genera un par efímero; con el perfil `prod` son obligatorias (ADR-0015). |

- **Swagger UI** con el contrato autoritativo ([ADR-0008](../../specs/adr/0008-backend-swagger-ui-con-contrato.md)):
  `http://localhost:8080/swagger-ui/index.html`. La API queda en `http://localhost:8080/api/v1`.
- **Postman** ([ADR-0021](../../specs/adr/0021-backend-colecciones-postman.md)), complementario a `./mvnw test`:
  [`registro-proveedores-contrato`](../../postman/registro-proveedores-contrato.postman_collection.json) (las 22
  operaciones) y [`registro-proveedores-flujo`](../../postman/registro-proveedores-flujo.postman_collection.json)
  (ciclo de vida completo con aserciones; usa [`postman/fixtures/`](../../postman/fixtures/)).

## Divergencias detectadas

Diferencias encontradas al generar esta documentación. No se documentan como reglas.

| ID | Tema | Detalle | Resolución propuesta |
| :--- | :--- | :--- | :--- |
| DOC-20261025-01 | Contrato sin ejemplos | `/documentar` pide un ejemplo de petición y respuesta tomado del contrato por operación, pero `supplier-registration-openapi.yaml` solo define ejemplos de `RegistrationExistenceResponse`, `SupplierRegistrationResponse`, `SectionSaveResponse`, `FiscalDocumentResponse`, del 500 y de algunos campos de `ProblemResponse` y `RegistrationNoticeResponse`. [api.md](api.md) muestra esos ejemplos y, para el resto, la estructura del esquema, sin inventar valores. | Si se quieren ejemplos completos, agregarlos al contrato con `/cambio-spec` (`contract:`) y regenerar. |
| DOC-20261025-02 | "Dónde se garantiza" de INV-02, INV-04 e INV-08 omite el dominio | `domain-model.md` §3 sitúa INV-02 en aplicación, INV-04 en la integración criptográfica e INV-08 en aplicación y persistencia. En el código, parte de esas reglas vive en el dominio: `CertificateIdentity.from` extrae RFC y nombre del titular y `CertificateIdentity.matching` rechaza un RFC distinto (`ApplicantRfcMismatchException`), y el constructor de `NoticeAcceptance` exige la versión 1.0 (`InvalidNoticeVersionException`). Es el mismo caso que SYNC-20261025-07 (INV-28 e INV-29). [dominio.md](dominio.md#invariantes) lista esas clases sin cambiar la regla. | Hallazgo para `/sincronizar`: agregar la parte de dominio a las tres INV (`design:`, requiere aprobación). |
