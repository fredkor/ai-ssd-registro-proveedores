# Contrato API — Registro de proveedores de Fredkor Labs

## Estado y alcance

[supplier-registration-openapi.yaml](supplier-registration-openapi.yaml) es el contrato
OpenAPI 3.0.3, versión 0.2.0: 22 operaciones y 39 esquemas. Declara
`x-implementation-ready: true`: está habilitado para generación e implementación.
Cubre la primera etapa, sin captura de Contactos ni finalización.

## Operaciones

| Caso de uso | Operación HTTP | operationId |
| :--- | :--- | :--- |
| Consultar aviso | GET `/api/v1/registration-notices/current` | `getRegistrationNotice` |
| Aceptar aviso | POST `/api/v1/registration-notice-acceptances` | `acceptRegistrationNotice` |
| Procesar Firma Electrónica | POST `/api/v1/applicant-identifications` | `identifyApplicant` |
| Consultar existencia | GET `/api/v1/supplier-registrations/existence` | `checkRegistrationExistence` |
| Crear expediente | POST `/api/v1/supplier-registrations` | `startSupplierRegistration` |
| Reanudar | POST `/api/v1/supplier-registrations/{registrationId}/resume` | `resumeSupplierRegistration` |
| Guardar Datos Generales | PUT `/api/v1/supplier-registrations/{registrationId}/general-data` | `saveGeneralData` |
| Guardar CUCoP | PUT `/api/v1/supplier-registrations/{registrationId}/cucop` | `saveCucopParticipation` |
| Cargar cédula | POST `/api/v1/supplier-registrations/{registrationId}/fiscal-document` | `uploadFiscalDocument` |
| Eliminar cédula | DELETE `/api/v1/supplier-registrations/{registrationId}/fiscal-document` | `deleteFiscalDocument` |
| Descargar cédula | GET `/api/v1/supplier-registrations/{registrationId}/fiscal-document` | `downloadFiscalDocument` |
| Consultar catálogo: Régimen | GET `/api/v1/registration-catalogs/tax-regimes` | `getTaxRegimes` |
| Consultar catálogo: País | GET `/api/v1/registration-catalogs/countries` | `getCountries` |
| Consultar catálogo: Titularidad jurídica | GET `/api/v1/registration-catalogs/legal-ownerships` | `getLegalOwnerships` |
| Consultar catálogo: Sector | GET `/api/v1/registration-catalogs/sectors` | `getSectors` |
| Consultar catálogo: Subsector | GET `/api/v1/registration-catalogs/sectors/{sectorId}/subsectors` | `getSubsectors` |
| Consultar catálogo: Giro | GET `/api/v1/registration-catalogs/business-activities` | `getBusinessActivities` |
| Consultar catálogo: Estratificación | GET `/api/v1/registration-catalogs/stratifications` | `getStratifications` |
| Consultar catálogo: Actividad preponderante | GET `/api/v1/registration-catalogs/primary-activities` | `getPrimaryActivities` |
| Consultar catálogo: Número de trabajadores | GET `/api/v1/registration-catalogs/worker-count-ranges` | `getWorkerCountRanges` |
| Consultar catálogo: Ventas anuales | GET `/api/v1/registration-catalogs/annual-sales-ranges` | `getAnnualSalesRanges` |
| Consultar catálogo CUCoP | GET `/api/v1/registration-catalogs/cucop` | `getCucopCatalog` |

No hay endpoints de navegación ni eliminación de teléfonos. DELETE de cédula desasocia, marca metadatos y actualiza progreso en una transacción de BD, sin tocar disco. El proceso diario hace el borrado físico.
PUT guarda una sección completa; identidad y progreso se resuelven y validan en backend.

## Fuentes y reglas de frontera

- [ADR-0020](../adr/0020-shared-catalogos-por-recurso.md): un recurso por catálogo de Datos Generales; Titularidad filtrada en backend.

- [Guía API](API-GUIDELINES.md): recursos, formatos, errores y generación.
- [Integración de firma](signature-integration.md) y [ADR-0016](../adr/0016-backend-verificacion-firma-sha256-rsa.md): evidencia y extracción de identidad.
- [ADR-0015](../adr/0015-shared-token-de-registro.md): JWT firmado, contexto por token en la misma BD, sesiones concurrentes y correlación de aceptación.
- [ADR-0009](../adr/0009-shared-null-explicito-en-requests.md): propiedades requeridas, null y normalización previa a validación.
- [ADR-0010](../adr/0010-backend-almacenamiento-documental-en-volumen.md): carga y asociación inmediatas, eliminación lógica transaccional y borrado físico diario de no asociados.
- [Catálogos](../domain/catalogs.md): filtros, relaciones y códigos. La selección no se valida solo por su tipo en el DTO.
- [RF](../requirements/functional.md) y [HU](../user-stories/): reglas y criterios; no se duplican aquí.

## Validación y preparación

API-03, API-05 y API-07 están resueltas. Eliminar es una transacción de BD que desasocia y marca metadatos; el proceso diario elimina físicamente y reintenta usando esa tabla. La referencia heredada API-04 se retira: no define una decisión abierta independiente.

Estado verificado el 2026-10-20:

1. El contrato declara `x-contract-status: READY`, `x-implementation-ready: true` y sin
   decisiones pendientes; `python3 tools/spec_lint.py --require-ready` da OK.
2. Maven (`backend/pom.xml`) genera interfaces y DTO con openapi-generator 7.22.0, que valida
   el contrato al generar; el backend compila y sus pruebas pasan con ese código generado.
3. Null, propiedades omitidas y desconocidas y restricciones condicionales no quedan garantizadas
   por las anotaciones generadas: cada operación las verifica con pruebas web al implementarse
   (ADR-0009).

Los detalles de preparación están en [stack](../architecture/tech-stack.md), y la cobertura
prevista en [trazabilidad](../traceability.md).
