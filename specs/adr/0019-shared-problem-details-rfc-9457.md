# ADR-0019: Errores como Problem Details (RFC 9457)

- **Estado:** Aceptado
- **Ámbito:** Compartido
- **Fecha:** 2026-10-21
- **Actualización:** 2026-10-21 (errores de protocolo y miembro `parameter`)
- **Specs relacionadas:** ADR-0004, ADR-0007, ADR-0009, ADR-0015; contrato OpenAPI; `api/API-GUIDELINES.md`.

## Contexto

`ErrorResponse { code, message, details }` (ADR-0004) no tiene una forma estándar ni indica qué
campo provocó un error de validación: un 400 "Malformed request body" con `details: []` obliga al
cliente a adivinar. RFC 9457 define un formato estándar de errores HTTP con miembros de extensión.

## Decisión

- Todas las respuestas de error (4xx y 5xx) de la API del registro usan Problem Details de
  RFC 9457 con media type `application/problem+json`. Se aplica a todas las operaciones,
  incluidos los rechazos de seguridad (401) y el error interno genérico (500). Sustituye a
  `ErrorResponse`.
- Miembros, todos presentes en cada respuesta:

| Miembro | Contenido |
| :--- | :--- |
| `type` | URN del tipo de problema: `urn:fredkorlabs:registration:problem:<código en kebab-case>`. |
| `title` | Resumen fijo del tipo, en inglés (tabla siguiente). No varía entre ocurrencias. |
| `status` | Código HTTP, igual al de la respuesta. |
| `detail` | Explicación de esta ocurrencia, en inglés, sin datos internos ni valores sensibles. |
| `instance` | Ruta de la petición (por ejemplo, `/api/v1/applicant-identifications`). |
| `code` | Extensión: código de negocio de ADR-0004 (enum `ErrorCode`). Es el que usa el cliente para decidir. |
| `errors` | Extensión: lista de errores por campo; vacía cuando el problema no se atribuye a campos. |

- Cada elemento de `errors` tiene `detail`, una descripción breve en inglés que no repite el
  valor recibido, y exactamente uno de estos miembros:
  - `pointer`: JSON Pointer (RFC 6901) al lugar del cuerpo de la petición que falló (por ejemplo,
    `/signatureResult/cadenaOriginal`).
  - `parameter`: nombre del parámetro de ruta, query o parte multipart que falló (por ejemplo,
    `registrationId`), porque un JSON Pointer solo aplica al cuerpo. Ejemplos: `is required`,
  `must not be null`, `must be one of [Temporal]`, `has an invalid format`, `is not a defined property`.
- Correspondencia entre código, tipo, título y estado (los códigos y estados de ADR-0004 no cambian):

| `code` | `type` (sufijo del URN) | `title` | `status` |
| :--- | :--- | :--- | :--- |
| VALIDATION_ERROR | `validation-error` | Invalid request | 400 |
| REGISTRATION_TOKEN_INVALID | `registration-token-invalid` | Invalid registration token | 401 |
| REGISTRATION_ACCESS_DENIED | `registration-access-denied` | Registration access denied | 403 |
| REGISTRATION_NOT_FOUND | `registration-not-found` | Registration not found | 404 |
| FISCAL_DOCUMENT_NOT_FOUND | `fiscal-document-not-found` | Fiscal document not found | 404 |
| RESOURCE_NOT_FOUND | `resource-not-found` | Resource not found | 404 |
| METHOD_NOT_ALLOWED | `method-not-allowed` | Method not allowed | 405 |
| NOT_ACCEPTABLE | `not-acceptable` | Not acceptable | 406 |
| DUPLICATE_RFC | `duplicate-rfc` | Duplicate RFC | 409 |
| INVALID_STATE_TRANSITION | `invalid-state-transition` | Invalid state transition | 409 |
| FISCAL_DOCUMENT_ALREADY_EXISTS | `fiscal-document-already-exists` | Fiscal document already exists | 409 |
| DOCUMENT_TOO_LARGE | `document-too-large` | Document too large | 413 |
| DOCUMENT_FORMAT_NOT_SUPPORTED | `document-format-not-supported` | Document format not supported | 415 |
| UNSUPPORTED_MEDIA_TYPE | `unsupported-media-type` | Unsupported media type | 415 |
| SIGNATURE_VERIFICATION_FAILED | `signature-verification-failed` | Signature verification failed | 422 |
| REGISTRATION_INCOMPLETE | `registration-incomplete` | Registration incomplete | 422 |
| INTERNAL_ERROR | `internal-error` | Internal error | 500 |

- Errores de entrada (VALIDATION_ERROR): propiedad omitida, null en campo obligatorio, propiedad
  no definida, tipo incorrecto, valor fuera del enum y restricciones de longitud o formato
  incluyen su `pointer`. Un cuerpo vacío o que no es JSON válido responde con `errors` vacío y un
  `detail` que lo indica. Las reglas de negocio que no dependen de un campo concreto (por ejemplo,
  certificado sin atributo de RFC o aceptación ya usada) usan `detail` y `errors` vacío.
- Errores propios del framework (2026-10-21): ruta inexistente, método no permitido (con cabecera
  `Allow`), `Accept` no soportado y `Content-Type` no soportado usan los códigos de protocolo de
  ADR-0004. Un parámetro de ruta o query ausente o con tipo inválido, o una parte multipart
  ausente, es VALIDATION_ERROR con `parameter`. Un multipart que excede el límite es
  DOCUMENT_TOO_LARGE. Los fallos técnicos fuera del controlador (por ejemplo, dentro del filtro de
  seguridad) también responden con Problem Details mediante el controlador de errores de la aplicación.
- INTERNAL_ERROR mantiene ADR-0007: `detail` es `Unexpected error`, `errors` vacío, sin trazas,
  rutas físicas ni contenido de firma o certificado. El fallo se registra con nivel ERROR.
- REGISTRATION_TOKEN_INVALID conserva la cabecera `WWW-Authenticate: Bearer` (ADR-0015).
- El contrato define el esquema `ProblemResponse` y `ProblemFieldErrorResponse` y declara
  `application/problem+json` en todas las respuestas de error. El backend construye los errores
  con `org.springframework.http.ProblemDetail`, la implementación de RFC 9457 de Spring, con
  `code` y `errors` como propiedades de extensión; no se escriben DTO web a mano.

## Alternativas consideradas

| Alternativa | A favor | En contra |
| :--- | :--- | :--- |
| Problem Details con `code` y `errors[]` | Estándar, autodescriptivo y con el campo exacto que falló. | Cambia el formato de error del contrato y del frontend; opción elegida. |
| Mantener `ErrorResponse` y llenar `details` | Cambio mínimo. | Formato propio sin estructura por campo. |
| Problem Details sin `code` | Más cercano al mínimo de la RFC. | El cliente tendría que interpretar URNs para decidir. |

## Consecuencias y verificación

- ADR-0004 conserva la tabla de códigos y estados; su representación HTTP pasa a este ADR.
  ADR-0007 conserva su regla de error genérico con este formato.
- `spec_lint` verifica que toda respuesta 4xx/5xx use `application/problem+json` con `ProblemResponse`.
- Las pruebas web verifican `Content-Type`, todos los miembros, la correspondencia de la tabla y
  los `pointer` de los errores de entrada.
- El frontend debe leer `code` y `errors` en lugar de `message` y `details`.
