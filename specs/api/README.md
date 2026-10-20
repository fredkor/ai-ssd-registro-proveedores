# Contrato API — Registro de proveedores de Fredkor Labs

## Estado y alcance

[supplier-registration-openapi.yaml](supplier-registration-openapi.yaml) es el borrador
OpenAPI 3.0.3, versión 0.1.0: 13 operaciones y 35 esquemas. Mantiene
`x-implementation-ready: false`; todavía no está habilitado para generación o implementación.
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
| Consultar catálogos generales | GET `/api/v1/registration-catalogs/general-data` | `getGeneralDataCatalogs` |
| Consultar catálogo CUCoP | GET `/api/v1/registration-catalogs/cucop` | `getCucopCatalog` |

No hay endpoints de navegación ni eliminación de teléfonos. DELETE de cédula desasocia, marca metadatos y actualiza progreso en una transacción de BD, sin tocar disco. El proceso diario hace el borrado físico.
PUT guarda una sección completa; identidad y progreso se resuelven y validan en backend.

## Fuentes y reglas de frontera

- [Guía API](API-GUIDELINES.md): recursos, formatos, errores y generación.
- [Integración de firma](signature-integration.md) y [ADR-0016](../adr/0016-backend-verificacion-firma-sha256-rsa.md): evidencia y extracción de identidad.
- [ADR-0015](../adr/0015-shared-token-de-registro.md): JWT firmado, contexto vigente por RFC en la misma BD y correlación de aceptación.
- [ADR-0009](../adr/0009-shared-null-explicito-en-requests.md): propiedades requeridas, null y normalización previa a validación.
- [ADR-0010](../adr/0010-backend-almacenamiento-documental-en-volumen.md): carga y asociación inmediatas, eliminación lógica transaccional y borrado físico diario de no asociados.
- [Catálogos](../domain/catalogs.md): filtros, relaciones y códigos. La selección no se valida solo por su tipo en el DTO.
- [RF](../requirements/functional.md) y [HU](../user-stories/): reglas y criterios; no se duplican aquí.

## Validación y preparación pendiente

API-03, API-05 y API-07 están resueltas. Eliminar es una transacción de BD que desasocia y marca metadatos; el proceso diario elimina físicamente y reintenta usando esa tabla. La referencia heredada API-04 se retira: no define una decisión abierta independiente.
Esto no declara que el contrato esté listo para generar.

Antes de habilitarlo se requiere:

1. Completar la revisión cruzada de respuestas, ejemplos, restricciones condicionales y
   normalización con los RF/HU/ADR vigentes.
2. Ejecutar el [validador documental](../../tools/README.md), ya adaptado al registro. Maven
   apunta al contrato y a mx.com.fredkorlabs.registration; falta comprobar la generación.
3. Ejecutar validación OpenAPI y generación con la configuración alineada, comprobando
   especialmente null, propiedades omitidas y desconocidas y restricciones condicionales.
4. Verificar comportamiento mediante pruebas de las capas cuando exista implementación.

El parseo YAML y las comprobaciones documentales actuales no sustituyen esos controles.
La validación con swagger-parser anotada el 2026-10-19 corresponde a una revisión anterior;
no se presenta como validación de las modificaciones posteriores ni de la generación actual.
Los detalles de preparación están en [stack](../architecture/tech-stack.md), y la cobertura
prevista en [trazabilidad](../traceability.md).
