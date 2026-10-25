<!-- Generado con /documentar desde specs/ y backend/src/. No editar a mano: cambia la spec y regenera. -->


# API del registro

Resumen navegable del [contrato OpenAPI](../../specs/api/supplier-registration-openapi.yaml), que es la fuente de verdad (`info.version` 0.2.1, estado READY). Los códigos `code` y su HTTP siguen [ADR-0004](../../specs/adr/0004-shared-codigos-de-error.md); el cuerpo de error es Problem Details ([ADR-0019](../../specs/adr/0019-shared-problem-details-rfc-9457.md)), `application/problem+json`. Swagger UI muestra el mismo contrato ([ADR-0008](../../specs/adr/0008-backend-swagger-ui-con-contrato.md)).

## Convenciones

- **Seguridad.** Las operaciones marcadas con *Bearer* exigen `Authorization: Bearer <registrationToken>` (esquema `RegistrationToken`, [ADR-0015](../../specs/adr/0015-shared-token-de-registro.md)). Sin token válido responden 401 `REGISTRATION_TOKEN_INVALID`.
- **Ejemplos.** Todos salen del contrato. Las peticiones multipart, la descarga del PDF y el 204 no tienen cuerpo JSON. Algunas listas de catálogo muestran solo parte de las filas.
- **Null y propiedades desconocidas** según [ADR-0009](../../specs/adr/0009-shared-null-explicito-en-requests.md).

## Error: `ProblemResponse`

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `type` | string | Sí | — |
| `title` | string | Sí | — |
| `status` | integer | Sí | ≥ 400, ≤ 599 |
| `detail` | string | Sí | — |
| `instance` | string | Sí | — |
| `code` | `ErrorCode` (`VALIDATION_ERROR`, `REGISTRATION_TOKEN_INVALID`, `REGISTRATION_ACCESS_DENIED`, `REGISTRATION_NOT_FOUND`, `FISCAL_DOCUMENT_NOT_FOUND`, `RESOURCE_NOT_FOUND`, `METHOD_NOT_ALLOWED`, `NOT_ACCEPTABLE`, `DUPLICATE_RFC`, `INVALID_STATE_TRANSITION`, `FISCAL_DOCUMENT_ALREADY_EXISTS`, `DOCUMENT_TOO_LARGE`, `DOCUMENT_FORMAT_NOT_SUPPORTED`, `UNSUPPORTED_MEDIA_TYPE`, `SIGNATURE_VERIFICATION_FAILED`, `REGISTRATION_INCOMPLETE`, `INTERNAL_ERROR`) | Sí | — |
| `errors` | array de `ProblemFieldErrorResponse` | Sí | — |

No admite propiedades no definidas.


Cada error de campo (`errors[]`) es un `ProblemFieldErrorResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `pointer` | string | No | — |
| `parameter` | string | No | — |
| `detail` | string | Sí | — |

No admite propiedades no definidas.


### Ejemplos de error

Cada operación enlaza estas respuestas por su HTTP; los ejemplos son los del contrato.

**400 `VALIDATION_ERROR` (`ValidationError`)**

```json
{
  "type": "urn:fredkorlabs:registration:problem:validation-error",
  "title": "Invalid request",
  "status": 400,
  "detail": "General Data does not satisfy its rules",
  "instance": "/api/v1/supplier-registrations/f42d5ae0-9203-465d-ac1a-f96e205e9529/general-data",
  "code": "VALIDATION_ERROR",
  "errors": [
    {
      "pointer": "/phones",
      "detail": "must contain at least one phone for legal entities"
    }
  ]
}
```

**403 `REGISTRATION_ACCESS_DENIED` (`AccessDenied`)**

```json
{
  "type": "urn:fredkorlabs:registration:problem:registration-access-denied",
  "title": "Registration access denied",
  "status": 403,
  "detail": "Registration belongs to another applicant",
  "instance": "/api/v1/supplier-registrations/f42d5ae0-9203-465d-ac1a-f96e205e9529/resume",
  "code": "REGISTRATION_ACCESS_DENIED",
  "errors": []
}
```

**404 `REGISTRATION_NOT_FOUND` (`RegistrationNotFound`)**

```json
{
  "type": "urn:fredkorlabs:registration:problem:registration-not-found",
  "title": "Registration not found",
  "status": 404,
  "detail": "Registration does not exist",
  "instance": "/api/v1/supplier-registrations/f42d5ae0-9203-465d-ac1a-f96e205e9529/resume",
  "code": "REGISTRATION_NOT_FOUND",
  "errors": []
}
```

**404 `REGISTRATION_NOT_FOUND` (`DocumentNotFound`, expediente inexistente)**

```json
{
  "type": "urn:fredkorlabs:registration:problem:registration-not-found",
  "title": "Registration not found",
  "status": 404,
  "detail": "Registration does not exist",
  "instance": "/api/v1/supplier-registrations/f42d5ae0-9203-465d-ac1a-f96e205e9529/fiscal-document",
  "code": "REGISTRATION_NOT_FOUND",
  "errors": []
}
```

**404 `FISCAL_DOCUMENT_NOT_FOUND` (`DocumentNotFound`, expediente sin cédula asociada)**

```json
{
  "type": "urn:fredkorlabs:registration:problem:fiscal-document-not-found",
  "title": "Fiscal document not found",
  "status": 404,
  "detail": "No fiscal document is associated with the registration",
  "instance": "/api/v1/supplier-registrations/f42d5ae0-9203-465d-ac1a-f96e205e9529/fiscal-document",
  "code": "FISCAL_DOCUMENT_NOT_FOUND",
  "errors": []
}
```

**409 `DUPLICATE_RFC` (`DuplicateRfc`)**

```json
{
  "type": "urn:fredkorlabs:registration:problem:duplicate-rfc",
  "title": "Duplicate RFC",
  "status": 409,
  "detail": "A registration already exists for this RFC",
  "instance": "/api/v1/supplier-registrations",
  "code": "DUPLICATE_RFC",
  "errors": []
}
```

**409 `INVALID_STATE_TRANSITION` (`StateConflict`)**

```json
{
  "type": "urn:fredkorlabs:registration:problem:invalid-state-transition",
  "title": "Invalid state transition",
  "status": 409,
  "detail": "Operation requires registration in IN_PROGRESS status, got: REGISTERED",
  "instance": "/api/v1/supplier-registrations/f42d5ae0-9203-465d-ac1a-f96e205e9529/resume",
  "code": "INVALID_STATE_TRANSITION",
  "errors": []
}
```

**409 `INVALID_STATE_TRANSITION` (`DocumentConflict`, expediente que no está en captura)**

```json
{
  "type": "urn:fredkorlabs:registration:problem:invalid-state-transition",
  "title": "Invalid state transition",
  "status": 409,
  "detail": "Operation requires registration in IN_PROGRESS status, got: REGISTERED",
  "instance": "/api/v1/supplier-registrations/f42d5ae0-9203-465d-ac1a-f96e205e9529/fiscal-document",
  "code": "INVALID_STATE_TRANSITION",
  "errors": []
}
```

**409 `FISCAL_DOCUMENT_ALREADY_EXISTS` (`DocumentConflict`, ya hay una cédula asociada)**

```json
{
  "type": "urn:fredkorlabs:registration:problem:fiscal-document-already-exists",
  "title": "Fiscal document already exists",
  "status": 409,
  "detail": "A fiscal document is already associated",
  "instance": "/api/v1/supplier-registrations/f42d5ae0-9203-465d-ac1a-f96e205e9529/fiscal-document",
  "code": "FISCAL_DOCUMENT_ALREADY_EXISTS",
  "errors": []
}
```

**413 `DOCUMENT_TOO_LARGE` (`DocumentTooLarge`)**

```json
{
  "type": "urn:fredkorlabs:registration:problem:document-too-large",
  "title": "Document too large",
  "status": 413,
  "detail": "Document exceeds the maximum size of 5242880 bytes",
  "instance": "/api/v1/supplier-registrations/f42d5ae0-9203-465d-ac1a-f96e205e9529/fiscal-document",
  "code": "DOCUMENT_TOO_LARGE",
  "errors": []
}
```

**415 `DOCUMENT_FORMAT_NOT_SUPPORTED` (`UnsupportedDocument`)**

```json
{
  "type": "urn:fredkorlabs:registration:problem:document-format-not-supported",
  "title": "Document format not supported",
  "status": 415,
  "detail": "Document content is not a PDF",
  "instance": "/api/v1/supplier-registrations/f42d5ae0-9203-465d-ac1a-f96e205e9529/fiscal-document",
  "code": "DOCUMENT_FORMAT_NOT_SUPPORTED",
  "errors": []
}
```

**422 `SIGNATURE_VERIFICATION_FAILED` (`SignatureVerificationFailed`)**

```json
{
  "type": "urn:fredkorlabs:registration:problem:signature-verification-failed",
  "title": "Signature verification failed",
  "status": 422,
  "detail": "Seal does not match the signed content and certificate",
  "instance": "/api/v1/applicant-identifications",
  "code": "SIGNATURE_VERIFICATION_FAILED",
  "errors": []
}
```

**500 `INTERNAL_ERROR` (`InternalError`)**

```json
{
  "type": "urn:fredkorlabs:registration:problem:internal-error",
  "title": "Internal error",
  "status": 500,
  "detail": "Unexpected error",
  "instance": "/api/v1/applicant-identifications",
  "code": "INTERNAL_ERROR",
  "errors": []
}
```

**401 `REGISTRATION_TOKEN_INVALID` (`InvalidRegistrationToken`)**

```json
{
  "type": "urn:fredkorlabs:registration:problem:registration-token-invalid",
  "title": "Invalid registration token",
  "status": 401,
  "detail": "Registration token is missing or invalid",
  "instance": "/api/v1/supplier-registrations/existence",
  "code": "REGISTRATION_TOKEN_INVALID",
  "errors": []
}
```

## Índice

| operationId | Método y ruta | Seguridad |
| :--- | :--- | :--- |
| [`getRegistrationNotice`](#getregistrationnotice) | GET `/api/v1/registration-notices/current` | Pública |
| [`acceptRegistrationNotice`](#acceptregistrationnotice) | POST `/api/v1/registration-notice-acceptances` | Pública |
| [`identifyApplicant`](#identifyapplicant) | POST `/api/v1/applicant-identifications` | Pública |
| [`checkRegistrationExistence`](#checkregistrationexistence) | GET `/api/v1/supplier-registrations/existence` | Bearer |
| [`startSupplierRegistration`](#startsupplierregistration) | POST `/api/v1/supplier-registrations` | Bearer |
| [`resumeSupplierRegistration`](#resumesupplierregistration) | POST `/api/v1/supplier-registrations/{registrationId}/resume` | Bearer |
| [`saveGeneralData`](#savegeneraldata) | PUT `/api/v1/supplier-registrations/{registrationId}/general-data` | Bearer |
| [`saveCucopParticipation`](#savecucopparticipation) | PUT `/api/v1/supplier-registrations/{registrationId}/cucop` | Bearer |
| [`uploadFiscalDocument`](#uploadfiscaldocument) | POST `/api/v1/supplier-registrations/{registrationId}/fiscal-document` | Bearer |
| [`downloadFiscalDocument`](#downloadfiscaldocument) | GET `/api/v1/supplier-registrations/{registrationId}/fiscal-document` | Bearer |
| [`deleteFiscalDocument`](#deletefiscaldocument) | DELETE `/api/v1/supplier-registrations/{registrationId}/fiscal-document` | Bearer |
| [`getTaxRegimes`](#gettaxregimes) | GET `/api/v1/registration-catalogs/tax-regimes` | Bearer |
| [`getCountries`](#getcountries) | GET `/api/v1/registration-catalogs/countries` | Bearer |
| [`getLegalOwnerships`](#getlegalownerships) | GET `/api/v1/registration-catalogs/legal-ownerships` | Bearer |
| [`getSectors`](#getsectors) | GET `/api/v1/registration-catalogs/sectors` | Bearer |
| [`getSubsectors`](#getsubsectors) | GET `/api/v1/registration-catalogs/sectors/{sectorId}/subsectors` | Bearer |
| [`getBusinessActivities`](#getbusinessactivities) | GET `/api/v1/registration-catalogs/business-activities` | Bearer |
| [`getStratifications`](#getstratifications) | GET `/api/v1/registration-catalogs/stratifications` | Bearer |
| [`getPrimaryActivities`](#getprimaryactivities) | GET `/api/v1/registration-catalogs/primary-activities` | Bearer |
| [`getWorkerCountRanges`](#getworkercountranges) | GET `/api/v1/registration-catalogs/worker-count-ranges` | Bearer |
| [`getAnnualSalesRanges`](#getannualsalesranges) | GET `/api/v1/registration-catalogs/annual-sales-ranges` | Bearer |
| [`getCucopCatalog`](#getcucopcatalog) | GET `/api/v1/registration-catalogs/cucop` | Bearer |

## RegistrationNotices

### getRegistrationNotice

**GET `/api/v1/registration-notices/current`** — Consultar Política de Aviso vigente.

- **Seguridad:** pública, sin token.
- **Caso de uso:** `GetRegistrationNoticeUseCase`. Fuentes: RF-REG-03, US03.
- **Detalle:** [operaciones/getRegistrationNotice.md](operaciones/getRegistrationNotice.md) · [diagrama de secuencia](diagramas.md#21-getregistrationnotice).

Presentar antes de ejecutar la firma inicial. Versión 1.0.

**Respuestas**

| HTTP | Descripción | Cuerpo | `code` |
| :--- | :--- | :--- | :--- |
| 200 | Operación completada correctamente. | `RegistrationNoticeResponse` | — |
| 500 | Fallo técnico; respuesta genérica sin detalles internos. | `ProblemResponse` | `INTERNAL_ERROR` |

Ejemplo del contrato (200, `RegistrationNoticeResponse`):

```json
{
  "version": "1.0",
  "text": "Al registrarse en el Sistema de Compras de \"Fredkor Labs\" MANIFIESTA SU CONFORMIDAD Y ACEPTACIÓN para:\n\nQue los actos pertinentes al sistema, desde su inicio hasta su conclusión, se realicen a través de medios de comunicación electrónica usando su correo electrónico;\n\nQue se dé por notificado de las actuaciones electrónicas que emita la empresa, en el mismo día en que consulta el Sistema de Compras de Fredkor Labs, y\n\nQue en el supuesto de que por causas imputables a la empresa se encuentre imposibilitado para consultar el sistema, o abrir los documentos electrónicos que contengan la información depositada en dicho sistema, en los días quince y último de cada mes o bien, el día hábil siguiente si alguno de éstos fuera inhábil, lo hará del conocimiento de la empresa a más tardar dentro de los tres días hábiles siguientes a aquél en que ocurra dicho impedimento, por medios de comunicación electrónica."
}
```

`RegistrationNoticeResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `version` | string (`1.0`) | Sí | — |
| `text` | string | Sí | — |

No admite propiedades no definidas.

### acceptRegistrationNotice

**POST `/api/v1/registration-notice-acceptances`** — Registrar aceptación expresa del aviso.

- **Seguridad:** pública, sin token.
- **Caso de uso:** `AcceptRegistrationNoticeUseCase`. Fuentes: RF-REG-03, US03.
- **Detalle:** [operaciones/acceptRegistrationNotice.md](operaciones/acceptRegistrationNotice.md) · [diagrama de secuencia](diagramas.md#22-acceptregistrationnotice).

La aceptación no crea expediente. Hora del backend. Si falla, no habilita firma.

**Petición** (`application/json`)

`AcceptRegistrationNoticeRequest`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `version` | string (`1.0`) | Sí | — |

No admite propiedades no definidas.

Ejemplo del contrato:

```json
{
  "version": "1.0"
}
```

**Respuestas**

| HTTP | Descripción | Cuerpo | `code` |
| :--- | :--- | :--- | :--- |
| 200 | Operación completada correctamente. | `NoticeAcceptanceResponse` | — |
| 400 | Datos de entrada inválidos. | `ProblemResponse` | `VALIDATION_ERROR` |
| 500 | Fallo técnico; respuesta genérica sin detalles internos. | `ProblemResponse` | `INTERNAL_ERROR` |

Ejemplo del contrato (200, `NoticeAcceptanceResponse`):

```json
{
  "version": "1.0",
  "acceptedAt": "2026-10-19T17:55:00Z",
  "acceptanceId": "example-acceptance"
}
```

`NoticeAcceptanceResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `version` | string (`1.0`) | Sí | — |
| `acceptedAt` | string (date-time) | Sí | — |
| `acceptanceId` | string | Sí | mín. 1 |

No admite propiedades no definidas.

## ApplicantIdentifications

### identifyApplicant

**POST `/api/v1/applicant-identifications`** — Procesar Firma Electrónica del solicitante.

- **Seguridad:** pública, sin token.
- **Caso de uso:** `IdentifyApplicantUseCase`. Fuentes: RF-REG-01, RF-REG-03, US01, US03.
- **Detalle:** [operaciones/identifyApplicant.md](operaciones/identifyApplicant.md) · [diagrama de secuencia](diagramas.md#31-identifyapplicant).

Requiere aceptación del aviso anterior a firmar. Verifica sello contra contenido y certificado y conserva íntegro el resultado aceptado. El componente externo, no este endpoint, comprueba vigencia y OCSP. Sin credenciales privadas. Usa noticeAcceptanceId para comprobar aceptación previa. Si es inexistente, con formato inválido o ya usada en una identificación exitosa produce VALIDATION_ERROR. Un resultado=false no habilita identificación. Solo después de verificar y conservar el resultado emite registrationToken. El token vence a los 60 minutos y no invalida otros tokens vigentes del mismo RFC. El sello es RSA PKCS#1 v1.5 con SHA-256 sobre los bytes UTF-8 de cadenaOriginal; el backend verifica con SHA256withRSA según ADR-0016.

**Petición** (`application/json`)

`IdentifyApplicantRequest`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `noticeAcceptanceId` | string | Sí | mín. 1 |
| `signatureResult` | `SignatureResultRequest` | Sí | — |

No admite propiedades no definidas.

`SignatureResultRequest`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `resultado` | boolean (`True`) | Sí | — |
| `fechaVigencia` | string (date-time) | Sí | — |
| `mensaje` | string | Sí | — |
| `cadenaOriginal` | string (`Temporal`) | Sí | — |
| `certificado` | string (byte) | Sí | mín. 1 |
| `cadenaFirma` | string (byte) | Sí | mín. 1 |
| `rfc` | `Rfc` (string) | Sí | — |
| `curp` | string | Sí | admite null |
| `razonSocial` | string | Sí | mín. 1 |
| `numeroSerie` | number | Sí | — |
| `tipoCertificado` | number | Sí | — |
| `certificadoPem` | string | Sí | mín. 1 |
| `isVigente` | boolean | Sí | — |
| `origen` | string | Sí | — |

No admite propiedades no definidas.

Ejemplo del contrato:

```json
{
  "noticeAcceptanceId": "example-acceptance",
  "signatureResult": {
    "resultado": true,
    "fechaVigencia": "2030-01-01T00:00:00Z",
    "mensaje": "OK",
    "cadenaOriginal": "Temporal",
    "certificado": "MIIDJTCCAg2gAwIBAgIBATANBgkqhkiG9w0BAQsFADAeMRwwGgYDVQQDDBNBVVRPUklEQUQgREUgUFJVRUJBMB4XDTI2MDEwMTAwMDAwMFoXDTMwMDEwMTAwMDAwMFowgY0xITAfBgNVBAMMGEVNUFJFU0EgRUpFTVBMTyBTQSBERSBDVjEhMB8GA1UEKQwYRU1QUkVTQSBFSkVNUExPIFNBIERFIENWMSEwHwYDVQQKDBhFTVBSRVNBIEVKRU1QTE8gU0EgREUgQ1YxCzAJBgNVBAYMAk1YMRUwEwYDVQQtDAxBQUEwMTAxMDFBQUEwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQC+GFx6x6dup/z5b/Fg/EmrkOLpO3ERfbhDYGamBML7c+sfyRCKjxtJnGx+C3UhVkB92XqgAtGzSf5EkQCH0NCKMsvDSFBUk1PUAl/MX6QrLGnMzlemricivG7A9OOAGWwGSsugytAaFrbAtbKGRPKAyHoLTQmYsKDyc4HbX51OwEMwa77HvkRBDylIPD0hHCN+V+3lT7/Biu3LCc1utMbxyIa49NrIBs3a6LCtvuGCaA+6uyDVba2dfsHqA+vgTDgV+E+1bPmb9j7A3/mFeCBAHYOLjaHLAQa2TKvbpUD3hOcdP3Qnn8xPjUp9CvVIntZLSy+CPv9mmhksDL2drBtTAgMBAAEwDQYJKoZIhvcNAQELBQADggEBACRV7742zVVFYN1uCksMsQU3Xq9Lyw3OF1zg92IxmxqwogBNbuQmakt8ppL8sEnlqJL/XrUgRFyH/HlI/7XUAIT3uW/pXRqmoUoVgNclOpvyqUh5XHdGI80ulQBBmCjR+lIiwx4oC0Oyx1aW+ppoDmEex5pslmlkuJ7Gi+X//nrMtI2PYvadC8p+2f5y8zQnjPi84DoX/bmJr8yfwmi0B0q6pMOl7mvUtOsnlHg/G2iu2QtmuwRN60N0z8UfnOWUIOHIk90/9QV3XvCFeOBaQ7/g4MTWTqeIhlVA6DgHqZRZvWDn9bkkEoju+hhsCO4y/MnCO2wxLIQZEC6jq/iTsSM=",
    "cadenaFirma": "a7OmRR4Wpx2PIP3P0yVakjNMGUps33lTjfGzXznoA5AjmDGfk/SYqHXK3MT9JYkFphxjghbon1Cs2D6qr6UBSurxHW1qnMix5GpAU9Ijwwa18wtcNuqsCmIgoUssq4WUHRS/0TMag8WA+FN98Df5/YtGM7Xm8x0ttaXElVqyGFF7kdIJZfBQc155h5wZ/Yd9U+Fk1olGYhbwxIRCjxmsAdJMgw6Rj3/TkWlvLloJQwFzWLZ9++yzpewZ0btf2AlnMNfhZyPz6DdpTw6NU4PI/hOeTHY4A5kdCKsMUJsmchy0CMj1KEoNrgVrZ86o5fMFsr6Q5srrEKPL0O+NpqMRGQ==",
    "rfc": "AAA010101AAA",
    "curp": null,
    "razonSocial": "RAZON SOCIAL PKI",
    "numeroSerie": 30001000000500003416,
    "tipoCertificado": 1,
    "certificadoPem": "-----BEGIN CERTIFICATE-----\r\nMIIDJTCCAg2gAwIBAgIBATANBgkqhkiG9w0BAQsFADAeMRwwGgYDVQQDDBNBVVRP\r\nUklEQUQgREUgUFJVRUJBMB4XDTI2MDEwMTAwMDAwMFoXDTMwMDEwMTAwMDAwMFow\r\ngY0xITAfBgNVBAMMGEVNUFJFU0EgRUpFTVBMTyBTQSBERSBDVjEhMB8GA1UEKQwY\r\nRU1QUkVTQSBFSkVNUExPIFNBIERFIENWMSEwHwYDVQQKDBhFTVBSRVNBIEVKRU1Q\r\nTE8gU0EgREUgQ1YxCzAJBgNVBAYMAk1YMRUwEwYDVQQtDAxBQUEwMTAxMDFBQUEw\r\nggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQC+GFx6x6dup/z5b/Fg/Emr\r\nkOLpO3ERfbhDYGamBML7c+sfyRCKjxtJnGx+C3UhVkB92XqgAtGzSf5EkQCH0NCK\r\nMsvDSFBUk1PUAl/MX6QrLGnMzlemricivG7A9OOAGWwGSsugytAaFrbAtbKGRPKA\r\nyHoLTQmYsKDyc4HbX51OwEMwa77HvkRBDylIPD0hHCN+V+3lT7/Biu3LCc1utMbx\r\nyIa49NrIBs3a6LCtvuGCaA+6uyDVba2dfsHqA+vgTDgV+E+1bPmb9j7A3/mFeCBA\r\nHYOLjaHLAQa2TKvbpUD3hOcdP3Qnn8xPjUp9CvVIntZLSy+CPv9mmhksDL2drBtT\r\nAgMBAAEwDQYJKoZIhvcNAQELBQADggEBACRV7742zVVFYN1uCksMsQU3Xq9Lyw3O\r\nF1zg92IxmxqwogBNbuQmakt8ppL8sEnlqJL/XrUgRFyH/HlI/7XUAIT3uW/pXRqm\r\noUoVgNclOpvyqUh5XHdGI80ulQBBmCjR+lIiwx4oC0Oyx1aW+ppoDmEex5pslmlk\r\nuJ7Gi+X//nrMtI2PYvadC8p+2f5y8zQnjPi84DoX/bmJr8yfwmi0B0q6pMOl7mvU\r\ntOsnlHg/G2iu2QtmuwRN60N0z8UfnOWUIOHIk90/9QV3XvCFeOBaQ7/g4MTWTqeI\r\nhlVA6DgHqZRZvWDn9bkkEoju+hhsCO4y/MnCO2wxLIQZEC6jq/iTsSM=\r\n-----END CERTIFICATE-----\r\n",
    "isVigente": true,
    "origen": "COMPONENTE"
  }
}
```

**Respuestas**

| HTTP | Descripción | Cuerpo | `code` |
| :--- | :--- | :--- | :--- |
| 200 | Operación completada correctamente. | `ApplicantIdentificationResponse` | — |
| 400 | Datos de entrada inválidos. | `ProblemResponse` | `VALIDATION_ERROR` |
| 422 | El sello no corresponde a contenido y certificado. | `ProblemResponse` | `SIGNATURE_VERIFICATION_FAILED` |
| 500 | Fallo técnico; respuesta genérica sin detalles internos. | `ProblemResponse` | `INTERNAL_ERROR` |

Ejemplo del contrato (200, `ApplicantIdentificationResponse`):

```json
{
  "identity": {
    "rfc": "AAA010101AAA",
    "legalName": "EMPRESA EJEMPLO SA DE CV",
    "taxRegime": "LEGAL_ENTITY",
    "taxRegimeId": 1,
    "username": "aaa010101aaa"
  },
  "registrationToken": "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJBQUEwMTAxMDFBQUEiLCJqdGkiOiIzZjFjMmI5ZS02ZDRhLTRmMGUtOWI4YS0yYzdkNWUxZjBhMzQiLCJpYXQiOjE3OTI0MzI1NjAsImV4cCI6MTc5MjQzNjE2MCwiaXNzIjoic3VwcGxpZXItcmVnaXN0cmF0aW9uIiwiYXVkIjoic3VwcGxpZXItcmVnaXN0cmF0aW9uLWFwaSJ9.firma-RS256-de-ejemplo",
  "tokenExpiresAt": "2026-10-19T18:56:00Z"
}
```

`ApplicantIdentificationResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `identity` | `ApplicantIdentityResponse` | Sí | — |
| `registrationToken` | `RegistrationToken` (string) | Sí | — |
| `tokenExpiresAt` | string (date-time) | Sí | — |

No admite propiedades no definidas.

`ApplicantIdentityResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `rfc` | `Rfc` (string) | Sí | — |
| `legalName` | string | Sí | mín. 1 |
| `taxRegime` | `TaxRegime` (`LEGAL_ENTITY`, `INDIVIDUAL`) | Sí | — |
| `taxRegimeId` | `CatalogId` (integer) | Sí | — |
| `username` | string | Sí | mín. 12, máx. 13 |

No admite propiedades no definidas.

## SupplierRegistrations

### checkRegistrationExistence

**GET `/api/v1/supplier-registrations/existence`** — Consultar existencia para la identidad aceptada.

- **Seguridad:** Bearer (`RegistrationToken`).
- **Caso de uso:** `CheckRegistrationExistenceUseCase`. Fuentes: RF-REG-02, US02.
- **Detalle:** [operaciones/checkRegistrationExistence.md](operaciones/checkRegistrationExistence.md) · [diagrama de secuencia](diagramas.md#41-checkregistrationexistence).

No recibe RFC como filtro libre. La ausencia normal retorna exists=false; un error no se convierte en inexistencia.

**Respuestas**

| HTTP | Descripción | Cuerpo | `code` |
| :--- | :--- | :--- | :--- |
| 200 | Operación completada correctamente. | `RegistrationExistenceResponse` | — |
| 401 | Token de registro ausente, inválido, vencido a los 60 minutos o sin contexto en el backend. | `ProblemResponse` | `REGISTRATION_TOKEN_INVALID` |
| 500 | Fallo técnico; respuesta genérica sin detalles internos. | `ProblemResponse` | `INTERNAL_ERROR` |

Ejemplo del contrato (200, `RegistrationExistenceResponse`):

```json
{
  "exists": false
}
```

`RegistrationExistenceResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `exists` | boolean | Sí | — |
| `registrationId` | `RegistrationId` (string, uuid) | No | — |
| `status` | `RegistrationStatus` (`IN_PROGRESS`, `REGISTERED`) | No | — |

No admite propiedades no definidas.

### startSupplierRegistration

**POST `/api/v1/supplier-registrations`** — Crear expediente de registro.

- **Seguridad:** Bearer (`RegistrationToken`).
- **Caso de uso:** `StartSupplierRegistrationUseCase`. Fuentes: RF-REG-04, US04.
- **Transición:** `- -> IN_PROGRESS`.
- **Detalle:** [operaciones/startSupplierRegistration.md](operaciones/startSupplierRegistration.md) · [diagrama de secuencia](diagramas.md#42-startsupplierregistration).

Crea UUID en IN_PROGRESS solo tras aceptación, identificación exitosa e inexistencia comprobada. Impide duplicidad concurrente. Abre Datos Generales. La identidad y aceptación se resuelven exclusivamente desde el token; no requiere cuerpo de petición.

**Respuestas**

| HTTP | Descripción | Cuerpo | `code` |
| :--- | :--- | :--- | :--- |
| 201 | Operación completada correctamente. | `SupplierRegistrationResponse` | — |
| 401 | Token de registro ausente, inválido, vencido a los 60 minutos o sin contexto en el backend. | `ProblemResponse` | `REGISTRATION_TOKEN_INVALID` |
| 409 | Ya existe un expediente para el RFC. | `ProblemResponse` | `DUPLICATE_RFC` |
| 500 | Fallo técnico; respuesta genérica sin detalles internos. | `ProblemResponse` | `INTERNAL_ERROR` |

Ejemplo del contrato (201, `SupplierRegistrationResponse`):

```json
{
  "id": "f42d5ae0-9203-465d-ac1a-f96e205e9529",
  "status": "IN_PROGRESS",
  "identity": {
    "rfc": "AAA010101AAA",
    "legalName": "EMPRESA EJEMPLO SA DE CV",
    "taxRegime": "LEGAL_ENTITY",
    "taxRegimeId": 1,
    "username": "aaa010101aaa"
  },
  "noticeAcceptance": {
    "version": "1.0",
    "acceptedAt": "2026-10-19T17:55:00Z",
    "acceptanceId": "example-acceptance"
  },
  "progress": [
    {
      "section": "GENERAL_DATA",
      "status": "PENDING"
    },
    {
      "section": "CUCOP",
      "status": "PENDING"
    },
    {
      "section": "CONTACTS",
      "status": "PENDING"
    },
    {
      "section": "FISCAL_ADDRESS",
      "status": "PENDING"
    },
    {
      "section": "COVERAGE",
      "status": "PENDING"
    },
    {
      "section": "ELECTRONIC_SIGNATURE",
      "status": "PENDING"
    }
  ]
}
```

`SupplierRegistrationResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `id` | `RegistrationId` (string, uuid) | Sí | — |
| `status` | `RegistrationStatus` (`IN_PROGRESS`, `REGISTERED`) | Sí | — |
| `identity` | `ApplicantIdentityResponse` | Sí | — |
| `noticeAcceptance` | `NoticeAcceptanceResponse` | Sí | — |
| `generalData` | `GeneralDataResponse` | No | — |
| `fiscalDocument` | `FiscalDocumentResponse` | No | — |
| `cucopParticipation` | `CucopParticipationResponse` | No | — |
| `progress` | array de `SectionProgressResponse` | Sí | — |

No admite propiedades no definidas.

`ApplicantIdentityResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `rfc` | `Rfc` (string) | Sí | — |
| `legalName` | string | Sí | mín. 1 |
| `taxRegime` | `TaxRegime` (`LEGAL_ENTITY`, `INDIVIDUAL`) | Sí | — |
| `taxRegimeId` | `CatalogId` (integer) | Sí | — |
| `username` | string | Sí | mín. 12, máx. 13 |

No admite propiedades no definidas.

`NoticeAcceptanceResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `version` | string (`1.0`) | Sí | — |
| `acceptedAt` | string (date-time) | Sí | — |
| `acceptanceId` | string | Sí | mín. 1 |

No admite propiedades no definidas.

`GeneralDataResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `origin` | `Origin` (`NATIONAL`, `FOREIGN`) | Sí | — |
| `countryId` | `CatalogId` (integer) | Sí | — |
| `nationality` | string | Sí | mín. 1, máx. 250 |
| `foreignFiscalCode` | string | No | mín. 1, máx. 20, admite null |
| `legalOwnershipId` | `CatalogId` (integer) | Sí | — |
| `sectorId` | integer | No | admite null |
| `subsectorId` | integer | No | admite null |
| `businessActivityId` | `CatalogId` (integer) | Sí | — |
| `stratificationId` | `CatalogId` (integer) | Sí | — |
| `primaryActivityId` | `CatalogId` (integer) | Sí | — |
| `workerCountRangeId` | `CatalogId` (integer) | Sí | — |
| `annualSalesRangeId` | `CatalogId` (integer) | Sí | — |
| `website` | string (uri) | No | máx. 2048, admite null |
| `phones` | array de `PhoneRequest` | No | admite null |

No admite propiedades no definidas.

`PhoneRequest`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `number` | string | Sí | patrón `^[0-9]{10}$` |
| `extension` | string | Sí | máx. 5, admite null |

No admite propiedades no definidas.

`FiscalDocumentResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `id` | string (uuid) | Sí | — |
| `originalName` | string | Sí | — |
| `extension` | string (`pdf`) | Sí | — |
| `sizeBytes` | integer (int64) | Sí | ≥ 0, ≤ 5242880 |
| `uploadedAt` | string (date-time) | Sí | — |

No admite propiedades no definidas.

`CucopParticipationResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `participationTypes` | array de `ParticipationType` (`GOODS_AND_SERVICES_SUPPLIER`, `PUBLIC_WORKS_CONTRACTOR`) | Sí | mín. 1 elementos, sin repetidos |
| `specificItems` | array de `CucopNodeResponse` | Sí | mín. 1 elementos |

No admite propiedades no definidas.

`CucopNodeResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `id` | `CatalogId` (integer) | Sí | — |
| `key` | string | Sí | — |
| `description` | string | Sí | — |
| `level` | integer (`1`, `2`, `3`, `4`) | Sí | — |
| `parentId` | `CatalogId` (integer) | No | — |

No admite propiedades no definidas.

`SectionProgressResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `section` | `RegistrationSection` (`GENERAL_DATA`, `CUCOP`, `CONTACTS`, `FISCAL_ADDRESS`, `COVERAGE`, `ELECTRONIC_SIGNATURE`) | Sí | — |
| `status` | `SectionStatus` (`PENDING`, `COMPLETED`, `REQUIRES_REVIEW`) | Sí | — |

No admite propiedades no definidas.

### resumeSupplierRegistration

**POST `/api/v1/supplier-registrations/{registrationId}/resume`** — Reanudar expediente incompleto.

- **Seguridad:** Bearer (`RegistrationToken`).
- **Caso de uso:** `ResumeSupplierRegistrationUseCase`. Fuentes: RF-REG-06, US05.
- **Detalle:** [operaciones/resumeSupplierRegistration.md](operaciones/resumeSupplierRegistration.md) · [diagrama de secuencia](diagramas.md#43-resumesupplierregistration).

Exige nueva identificación del mismo RFC. Recupera el mismo expediente y siempre abre Datos Generales; no sobrescribe datos ante un fallo. REGISTERED no puede reanudarse como borrador. La identidad y aceptación se resuelven exclusivamente desde el token; no requiere cuerpo de petición. Al reanudar se actualiza legalName desde el nuevo certificado verificado del mismo RFC y se vincula históricamente su evidencia sin sobrescribir las anteriores; ambas acciones se confirman en una transacción. Se conservan UUID, datos capturados, documentos confirmados y progreso, incluido CUCoP. Un fallo revierte la actualización del nombre y vínculo histórico sin eliminar la evidencia conservada al identificar. Si la evidencia del token ya está vinculada al expediente (identificación ya aplicada al crearlo o reanudarlo, p. ej. un reintento), responde 200 con el estado actual sin modificarlo.

**Parámetros**

| Nombre | En | Tipo | Obligatorio | Ejemplo | Descripción |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `registrationId` | path | `RegistrationId` (string, uuid) | Sí | `f42d5ae0-9203-465d-ac1a-f96e205e9529` | — |

**Respuestas**

| HTTP | Descripción | Cuerpo | `code` |
| :--- | :--- | :--- | :--- |
| 200 | Operación completada correctamente. | `SupplierRegistrationResponse` | — |
| 400 | Datos de entrada inválidos. | `ProblemResponse` | `VALIDATION_ERROR` |
| 401 | Token de registro ausente, inválido, vencido a los 60 minutos o sin contexto en el backend. | `ProblemResponse` | `REGISTRATION_TOKEN_INVALID` |
| 403 | El contexto aceptado no corresponde al expediente. | `ProblemResponse` | `REGISTRATION_ACCESS_DENIED` |
| 404 | Expediente inexistente. | `ProblemResponse` | `REGISTRATION_NOT_FOUND` |
| 409 | Estado incompatible con la operación. | `ProblemResponse` | `INVALID_STATE_TRANSITION` |
| 500 | Fallo técnico; respuesta genérica sin detalles internos. | `ProblemResponse` | `INTERNAL_ERROR` |

Ejemplo del contrato (200, `SupplierRegistrationResponse`):

```json
{
  "id": "f42d5ae0-9203-465d-ac1a-f96e205e9529",
  "status": "IN_PROGRESS",
  "identity": {
    "rfc": "AAA010101AAA",
    "legalName": "EMPRESA EJEMPLO SA DE CV",
    "taxRegime": "LEGAL_ENTITY",
    "taxRegimeId": 1,
    "username": "aaa010101aaa"
  },
  "noticeAcceptance": {
    "version": "1.0",
    "acceptedAt": "2026-10-19T17:55:00Z",
    "acceptanceId": "example-acceptance"
  },
  "progress": [
    {
      "section": "GENERAL_DATA",
      "status": "PENDING"
    },
    {
      "section": "CUCOP",
      "status": "PENDING"
    },
    {
      "section": "CONTACTS",
      "status": "PENDING"
    },
    {
      "section": "FISCAL_ADDRESS",
      "status": "PENDING"
    },
    {
      "section": "COVERAGE",
      "status": "PENDING"
    },
    {
      "section": "ELECTRONIC_SIGNATURE",
      "status": "PENDING"
    }
  ]
}
```

`SupplierRegistrationResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `id` | `RegistrationId` (string, uuid) | Sí | — |
| `status` | `RegistrationStatus` (`IN_PROGRESS`, `REGISTERED`) | Sí | — |
| `identity` | `ApplicantIdentityResponse` | Sí | — |
| `noticeAcceptance` | `NoticeAcceptanceResponse` | Sí | — |
| `generalData` | `GeneralDataResponse` | No | — |
| `fiscalDocument` | `FiscalDocumentResponse` | No | — |
| `cucopParticipation` | `CucopParticipationResponse` | No | — |
| `progress` | array de `SectionProgressResponse` | Sí | — |

No admite propiedades no definidas.

`ApplicantIdentityResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `rfc` | `Rfc` (string) | Sí | — |
| `legalName` | string | Sí | mín. 1 |
| `taxRegime` | `TaxRegime` (`LEGAL_ENTITY`, `INDIVIDUAL`) | Sí | — |
| `taxRegimeId` | `CatalogId` (integer) | Sí | — |
| `username` | string | Sí | mín. 12, máx. 13 |

No admite propiedades no definidas.

`NoticeAcceptanceResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `version` | string (`1.0`) | Sí | — |
| `acceptedAt` | string (date-time) | Sí | — |
| `acceptanceId` | string | Sí | mín. 1 |

No admite propiedades no definidas.

`GeneralDataResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `origin` | `Origin` (`NATIONAL`, `FOREIGN`) | Sí | — |
| `countryId` | `CatalogId` (integer) | Sí | — |
| `nationality` | string | Sí | mín. 1, máx. 250 |
| `foreignFiscalCode` | string | No | mín. 1, máx. 20, admite null |
| `legalOwnershipId` | `CatalogId` (integer) | Sí | — |
| `sectorId` | integer | No | admite null |
| `subsectorId` | integer | No | admite null |
| `businessActivityId` | `CatalogId` (integer) | Sí | — |
| `stratificationId` | `CatalogId` (integer) | Sí | — |
| `primaryActivityId` | `CatalogId` (integer) | Sí | — |
| `workerCountRangeId` | `CatalogId` (integer) | Sí | — |
| `annualSalesRangeId` | `CatalogId` (integer) | Sí | — |
| `website` | string (uri) | No | máx. 2048, admite null |
| `phones` | array de `PhoneRequest` | No | admite null |

No admite propiedades no definidas.

`PhoneRequest`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `number` | string | Sí | patrón `^[0-9]{10}$` |
| `extension` | string | Sí | máx. 5, admite null |

No admite propiedades no definidas.

`FiscalDocumentResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `id` | string (uuid) | Sí | — |
| `originalName` | string | Sí | — |
| `extension` | string (`pdf`) | Sí | — |
| `sizeBytes` | integer (int64) | Sí | ≥ 0, ≤ 5242880 |
| `uploadedAt` | string (date-time) | Sí | — |

No admite propiedades no definidas.

`CucopParticipationResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `participationTypes` | array de `ParticipationType` (`GOODS_AND_SERVICES_SUPPLIER`, `PUBLIC_WORKS_CONTRACTOR`) | Sí | mín. 1 elementos, sin repetidos |
| `specificItems` | array de `CucopNodeResponse` | Sí | mín. 1 elementos |

No admite propiedades no definidas.

`CucopNodeResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `id` | `CatalogId` (integer) | Sí | — |
| `key` | string | Sí | — |
| `description` | string | Sí | — |
| `level` | integer (`1`, `2`, `3`, `4`) | Sí | — |
| `parentId` | `CatalogId` (integer) | No | — |

No admite propiedades no definidas.

`SectionProgressResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `section` | `RegistrationSection` (`GENERAL_DATA`, `CUCOP`, `CONTACTS`, `FISCAL_ADDRESS`, `COVERAGE`, `ELECTRONIC_SIGNATURE`) | Sí | — |
| `status` | `SectionStatus` (`PENDING`, `COMPLETED`, `REQUIRES_REVIEW`) | Sí | — |

No admite propiedades no definidas.

### saveGeneralData

**PUT `/api/v1/supplier-registrations/{registrationId}/general-data`** — Validar y guardar Datos Generales completos.

- **Seguridad:** Bearer (`RegistrationToken`).
- **Caso de uso:** `SaveGeneralDataUseCase`. Fuentes: RF-REG-05, US06, US08, US09, US10.
- **Detalle:** [operaciones/saveGeneralData.md](operaciones/saveGeneralData.md) · [diagrama de secuencia](diagramas.md#44-savegeneraldata).

Invocado por Siguiente. Valida datos, teléfonos y cédula previamente asociada, y guarda datos y progreso en una transacción. No cambia estado global ni permite editar identidad. Atrás no invoca esta operación.

**Parámetros**

| Nombre | En | Tipo | Obligatorio | Ejemplo | Descripción |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `registrationId` | path | `RegistrationId` (string, uuid) | Sí | `f42d5ae0-9203-465d-ac1a-f96e205e9529` | — |

**Petición** (`application/json`)

`GeneralDataRequest`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `origin` | `Origin` (`NATIONAL`, `FOREIGN`) | Sí | — |
| `countryId` | `CatalogId` (integer) | Sí | — |
| `nationality` | string | Sí | mín. 1, máx. 250 |
| `foreignFiscalCode` | string | Sí | mín. 1, máx. 20, admite null |
| `legalOwnershipId` | `CatalogId` (integer) | Sí | — |
| `sectorId` | integer | Sí | admite null |
| `subsectorId` | integer | Sí | admite null |
| `businessActivityId` | `CatalogId` (integer) | Sí | — |
| `stratificationId` | `CatalogId` (integer) | Sí | — |
| `primaryActivityId` | `CatalogId` (integer) | Sí | — |
| `workerCountRangeId` | `CatalogId` (integer) | Sí | — |
| `annualSalesRangeId` | `CatalogId` (integer) | Sí | — |
| `website` | string (uri) | Sí | máx. 2048, admite null |
| `phones` | array de `PhoneRequest` | Sí | admite null |

No admite propiedades no definidas.

`PhoneRequest`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `number` | string | Sí | patrón `^[0-9]{10}$` |
| `extension` | string | Sí | máx. 5, admite null |

No admite propiedades no definidas.

Ejemplo del contrato:

```json
{
  "origin": "NATIONAL",
  "countryId": 151,
  "nationality": "Mexicana",
  "foreignFiscalCode": null,
  "legalOwnershipId": 1,
  "sectorId": 5,
  "subsectorId": 21,
  "businessActivityId": 1,
  "stratificationId": 2,
  "primaryActivityId": 3,
  "workerCountRangeId": 4,
  "annualSalesRangeId": 2,
  "website": "https://empresa.com",
  "phones": [
    {
      "number": "5512345678",
      "extension": "12"
    },
    {
      "number": "0012345678",
      "extension": null
    }
  ]
}
```

**Respuestas**

| HTTP | Descripción | Cuerpo | `code` |
| :--- | :--- | :--- | :--- |
| 200 | Operación completada correctamente. | `SectionSaveResponse` | — |
| 400 | Datos de entrada inválidos. | `ProblemResponse` | `VALIDATION_ERROR` |
| 401 | Token de registro ausente, inválido, vencido a los 60 minutos o sin contexto en el backend. | `ProblemResponse` | `REGISTRATION_TOKEN_INVALID` |
| 403 | El contexto aceptado no corresponde al expediente. | `ProblemResponse` | `REGISTRATION_ACCESS_DENIED` |
| 404 | Expediente inexistente. | `ProblemResponse` | `REGISTRATION_NOT_FOUND` |
| 409 | Estado incompatible con la operación. | `ProblemResponse` | `INVALID_STATE_TRANSITION` |
| 500 | Fallo técnico; respuesta genérica sin detalles internos. | `ProblemResponse` | `INTERNAL_ERROR` |

Ejemplo del contrato (200, `SectionSaveResponse`):

```json
{
  "registrationId": "f42d5ae0-9203-465d-ac1a-f96e205e9529",
  "section": "GENERAL_DATA",
  "status": "COMPLETED",
  "nextSection": "CUCOP"
}
```

`SectionSaveResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `registrationId` | `RegistrationId` (string, uuid) | Sí | — |
| `section` | `RegistrationSection` (`GENERAL_DATA`, `CUCOP`, `CONTACTS`, `FISCAL_ADDRESS`, `COVERAGE`, `ELECTRONIC_SIGNATURE`) | Sí | — |
| `status` | string (`COMPLETED`) | Sí | — |
| `nextSection` | `RegistrationSection` (`GENERAL_DATA`, `CUCOP`, `CONTACTS`, `FISCAL_ADDRESS`, `COVERAGE`, `ELECTRONIC_SIGNATURE`) | Sí | — |

No admite propiedades no definidas.

### saveCucopParticipation

**PUT `/api/v1/supplier-registrations/{registrationId}/cucop`** — Validar y guardar participación CUCoP.

- **Seguridad:** Bearer (`RegistrationToken`).
- **Caso de uso:** `SaveCucopParticipationUseCase`. Fuentes: RF-REG-08, RF-REG-05, US11, US06.
- **Detalle:** [operaciones/saveCucopParticipation.md](operaciones/saveCucopParticipation.md) · [diagrama de secuencia](diagramas.md#45-savecucopparticipation).

Requiere Datos Generales en COMPLETED (con PENDING, incluida la cédula eliminada, responde 409 INVALID_STATE_TRANSITION), uno o ambos tipos y al menos una específica, sin repetidos (400 con el pointer del elemento repetido). Resuelve catálogo y guarda sección y progreso atómicamente. Continúa hacia Contactos. Atrás, selección y Deshacer no guardan.

**Parámetros**

| Nombre | En | Tipo | Obligatorio | Ejemplo | Descripción |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `registrationId` | path | `RegistrationId` (string, uuid) | Sí | `f42d5ae0-9203-465d-ac1a-f96e205e9529` | — |

**Petición** (`application/json`)

`CucopParticipationRequest`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `participationTypes` | array de `ParticipationType` (`GOODS_AND_SERVICES_SUPPLIER`, `PUBLIC_WORKS_CONTRACTOR`) | Sí | mín. 1 elementos, sin repetidos |
| `specificItemIds` | array de `CatalogId` (integer) | Sí | mín. 1 elementos, sin repetidos |

No admite propiedades no definidas.

Ejemplo del contrato:

```json
{
  "participationTypes": [
    "GOODS_AND_SERVICES_SUPPLIER",
    "PUBLIC_WORKS_CONTRACTOR"
  ],
  "specificItemIds": [
    1,
    2
  ]
}
```

**Respuestas**

| HTTP | Descripción | Cuerpo | `code` |
| :--- | :--- | :--- | :--- |
| 200 | Operación completada correctamente. | `SectionSaveResponse` | — |
| 400 | Datos de entrada inválidos. | `ProblemResponse` | `VALIDATION_ERROR` |
| 401 | Token de registro ausente, inválido, vencido a los 60 minutos o sin contexto en el backend. | `ProblemResponse` | `REGISTRATION_TOKEN_INVALID` |
| 403 | El contexto aceptado no corresponde al expediente. | `ProblemResponse` | `REGISTRATION_ACCESS_DENIED` |
| 404 | Expediente inexistente. | `ProblemResponse` | `REGISTRATION_NOT_FOUND` |
| 409 | Estado incompatible con la operación. | `ProblemResponse` | `INVALID_STATE_TRANSITION` |
| 500 | Fallo técnico; respuesta genérica sin detalles internos. | `ProblemResponse` | `INTERNAL_ERROR` |

Ejemplo del contrato (200, `SectionSaveResponse`):

```json
{
  "registrationId": "f42d5ae0-9203-465d-ac1a-f96e205e9529",
  "section": "GENERAL_DATA",
  "status": "COMPLETED",
  "nextSection": "CUCOP"
}
```

`SectionSaveResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `registrationId` | `RegistrationId` (string, uuid) | Sí | — |
| `section` | `RegistrationSection` (`GENERAL_DATA`, `CUCOP`, `CONTACTS`, `FISCAL_ADDRESS`, `COVERAGE`, `ELECTRONIC_SIGNATURE`) | Sí | — |
| `status` | string (`COMPLETED`) | Sí | — |
| `nextSection` | `RegistrationSection` (`GENERAL_DATA`, `CUCOP`, `CONTACTS`, `FISCAL_ADDRESS`, `COVERAGE`, `ELECTRONIC_SIGNATURE`) | Sí | — |

No admite propiedades no definidas.

## FiscalDocuments

### uploadFiscalDocument

**POST `/api/v1/supplier-registrations/{registrationId}/fiscal-document`** — Cargar la única cédula fiscal.

- **Seguridad:** Bearer (`RegistrationToken`).
- **Caso de uso:** `UploadFiscalDocumentUseCase`. Fuentes: RF-REG-05, US10.
- **Detalle:** [operaciones/uploadFiscalDocument.md](operaciones/uploadFiscalDocument.md) · [diagrama de secuencia](diagramas.md#51-uploadfiscaldocument).

Guarda y asocia inmediatamente el PDF de máximo 5 MiB (5,242,880 bytes) al expediente y devuelve UUID y metadatos; disponible al regresar o reanudar sin Siguiente. No completa Datos Generales. No informa éxito parcial; seguir ADR-0010.

**Parámetros**

| Nombre | En | Tipo | Obligatorio | Ejemplo | Descripción |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `registrationId` | path | `RegistrationId` (string, uuid) | Sí | `f42d5ae0-9203-465d-ac1a-f96e205e9529` | — |

**Petición** (`multipart/form-data`)

| Parte | Tipo | Obligatoria | Descripción |
| :--- | :--- | :--- | :--- |
| `file` | string (binary) | Sí | Un único PDF, máximo 5 MiB (5,242,880 bytes) inclusive sobre el archivo. Extensión .pdf sin distinguir mayúsculas/minúsculas y contenido PDF comprobado en servidor por su firma (primeros bytes %PDF-); no basta el nombre ni el MIME declarado. No se interpreta maxLength como bytes. Una parte no definida o repetida responde VALIDATION_ERROR con parameter. |

**Respuestas**

| HTTP | Descripción | Cuerpo | `code` |
| :--- | :--- | :--- | :--- |
| 200 | Operación completada correctamente. | `FiscalDocumentResponse` | — |
| 400 | Datos de entrada inválidos. | `ProblemResponse` | `VALIDATION_ERROR` |
| 401 | Token de registro ausente, inválido, vencido a los 60 minutos o sin contexto en el backend. | `ProblemResponse` | `REGISTRATION_TOKEN_INVALID` |
| 403 | El contexto aceptado no corresponde al expediente. | `ProblemResponse` | `REGISTRATION_ACCESS_DENIED` |
| 404 | Expediente inexistente. | `ProblemResponse` | `REGISTRATION_NOT_FOUND` |
| 409 | Estado incompatible o ya existe una cédula. | `ProblemResponse` | `INVALID_STATE_TRANSITION`, `FISCAL_DOCUMENT_ALREADY_EXISTS` |
| 413 | El archivo supera el máximo de 5 MiB (5,242,880 bytes); el máximo exacto está permitido. | `ProblemResponse` | `DOCUMENT_TOO_LARGE` |
| 415 | Se requiere extensión .pdf sin distinguir mayúsculas/minúsculas y contenido PDF comprobado en servidor por su firma (primeros bytes %PDF-); extensión o contenido incorrectos producen DOCUMENT_FORMAT_NOT_SUPPORTED. | `ProblemResponse` | `DOCUMENT_FORMAT_NOT_SUPPORTED` |
| 500 | Fallo técnico; respuesta genérica sin detalles internos. | `ProblemResponse` | `INTERNAL_ERROR` |

Ejemplo del contrato (200, `FiscalDocumentResponse`):

```json
{
  "id": "8bd04b95-88ae-4dd8-bd19-27229040225e",
  "originalName": "cedula-fiscal.pdf",
  "extension": "pdf",
  "sizeBytes": 5242880,
  "uploadedAt": "2026-10-19T18:00:00Z"
}
```

`FiscalDocumentResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `id` | string (uuid) | Sí | — |
| `originalName` | string | Sí | — |
| `extension` | string (`pdf`) | Sí | — |
| `sizeBytes` | integer (int64) | Sí | ≥ 0, ≤ 5242880 |
| `uploadedAt` | string (date-time) | Sí | — |

No admite propiedades no definidas.

### downloadFiscalDocument

**GET `/api/v1/supplier-registrations/{registrationId}/fiscal-document`** — Descargar cédula fiscal asociada.

- **Seguridad:** Bearer (`RegistrationToken`).
- **Caso de uso:** `DownloadFiscalDocumentUseCase`. Fuentes: RF-REG-05, US10.
- **Detalle:** [operaciones/downloadFiscalDocument.md](operaciones/downloadFiscalDocument.md) · [diagrama de secuencia](diagramas.md#52-downloadfiscaldocument).

Solo para el expediente correspondiente a la identificación aceptada. Presenta nombre original sin exponer ruta física.

**Parámetros**

| Nombre | En | Tipo | Obligatorio | Ejemplo | Descripción |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `registrationId` | path | `RegistrationId` (string, uuid) | Sí | `f42d5ae0-9203-465d-ac1a-f96e205e9529` | — |

**Respuestas**

| HTTP | Descripción | Cuerpo | `code` |
| :--- | :--- | :--- | :--- |
| 200 | Contenido del PDF asociado. | `string (binary)` | — |
| 400 | Datos de entrada inválidos. | `ProblemResponse` | `VALIDATION_ERROR` |
| 401 | Token de registro ausente, inválido, vencido a los 60 minutos o sin contexto en el backend. | `ProblemResponse` | `REGISTRATION_TOKEN_INVALID` |
| 403 | El contexto aceptado no corresponde al expediente. | `ProblemResponse` | `REGISTRATION_ACCESS_DENIED` |
| 404 | Expediente o cédula fiscal inexistentes. | `ProblemResponse` | `REGISTRATION_NOT_FOUND`, `FISCAL_DOCUMENT_NOT_FOUND` |
| 500 | Fallo técnico; respuesta genérica sin detalles internos. | `ProblemResponse` | `INTERNAL_ERROR` |

### deleteFiscalDocument

**DELETE `/api/v1/supplier-registrations/{registrationId}/fiscal-document`** — Eliminar cédula fiscal asociada.

- **Seguridad:** Bearer (`RegistrationToken`).
- **Caso de uso:** `DeleteFiscalDocumentUseCase`. Fuentes: RF-REG-05, US10.
- **Detalle:** [operaciones/deleteFiscalDocument.md](operaciones/deleteFiscalDocument.md) · [diagrama de secuencia](diagramas.md#53-deletefiscaldocument).

Requiere advertencia y confirmación. En una transacción de BD retira la referencia del expediente, marca associated=false en metadatos y deja Datos Generales PENDING sin afectar CUCoP. No borra ni mueve contenido físico y no espera Siguiente. Fallo hace rollback de referencia, marca y progreso; éxito solo después del commit. Los metadatos se conservan y el proceso diario borra físicamente los no asociados elegibles. El documento desasociado no se muestra ni descarga desde el expediente.

**Parámetros**

| Nombre | En | Tipo | Obligatorio | Ejemplo | Descripción |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `registrationId` | path | `RegistrationId` (string, uuid) | Sí | `f42d5ae0-9203-465d-ac1a-f96e205e9529` | — |

**Respuestas**

| HTTP | Descripción | Cuerpo | `code` |
| :--- | :--- | :--- | :--- |
| 204 | Desasociación, marca de metadatos y progreso confirmados en BD; el borrado físico queda al proceso diario. | — | — |
| 400 | Datos de entrada inválidos. | `ProblemResponse` | `VALIDATION_ERROR` |
| 401 | Token de registro ausente, inválido, vencido a los 60 minutos o sin contexto en el backend. | `ProblemResponse` | `REGISTRATION_TOKEN_INVALID` |
| 403 | El contexto aceptado no corresponde al expediente. | `ProblemResponse` | `REGISTRATION_ACCESS_DENIED` |
| 404 | Expediente o cédula fiscal inexistentes. | `ProblemResponse` | `REGISTRATION_NOT_FOUND`, `FISCAL_DOCUMENT_NOT_FOUND` |
| 409 | Estado incompatible con la operación. | `ProblemResponse` | `INVALID_STATE_TRANSITION` |
| 500 | Fallo técnico; respuesta genérica sin detalles internos. | `ProblemResponse` | `INTERNAL_ERROR` |

## RegistrationCatalogs

### getTaxRegimes

**GET `/api/v1/registration-catalogs/tax-regimes`** — Consultar catálogo de régimen.

- **Seguridad:** Bearer (`RegistrationToken`).
- **Caso de uso:** `GetTaxRegimesUseCase`. Fuentes: RF-REG-05, US08.
- **Detalle:** [operaciones/getTaxRegimes.md](operaciones/getTaxRegimes.md) · [diagrama de secuencia](diagramas.md#61-gettaxregimes).

Catálogo completo en items, ordenado por id, sin paginación (ADR-0020). El frontend muestra la descripción del registro cuyo id es identity.taxRegimeId.

**Respuestas**

| HTTP | Descripción | Cuerpo | `code` |
| :--- | :--- | :--- | :--- |
| 200 | Operación completada correctamente. | `CatalogOptionListResponse` | — |
| 401 | Token de registro ausente, inválido, vencido a los 60 minutos o sin contexto en el backend. | `ProblemResponse` | `REGISTRATION_TOKEN_INVALID` |
| 500 | Fallo técnico; respuesta genérica sin detalles internos. | `ProblemResponse` | `INTERNAL_ERROR` |

Ejemplo del contrato (200, `CatalogOptionListResponse`):

```json
{
  "items": [
    {
      "id": 1,
      "description": "PERSONA MORAL"
    },
    {
      "id": 2,
      "description": "PERSONA FÍSICA"
    }
  ]
}
```

`CatalogOptionListResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `items` | array de `CatalogOptionResponse` | Sí | — |

No admite propiedades no definidas.

`CatalogOptionResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `id` | `CatalogId` (integer) | Sí | — |
| `description` | string | Sí | — |

No admite propiedades no definidas.

### getCountries

**GET `/api/v1/registration-catalogs/countries`** — Consultar catálogo de países.

- **Seguridad:** Bearer (`RegistrationToken`).
- **Caso de uso:** `GetCountriesUseCase`. Fuentes: RF-REG-05, US08.
- **Detalle:** [operaciones/getCountries.md](operaciones/getCountries.md) · [diagrama de secuencia](diagramas.md#62-getcountries).

Catálogo completo en items, ordenado por id, sin paginación (ADR-0020).

**Respuestas**

| HTTP | Descripción | Cuerpo | `code` |
| :--- | :--- | :--- | :--- |
| 200 | Operación completada correctamente. | `CountryOptionListResponse` | — |
| 401 | Token de registro ausente, inválido, vencido a los 60 minutos o sin contexto en el backend. | `ProblemResponse` | `REGISTRATION_TOKEN_INVALID` |
| 500 | Fallo técnico; respuesta genérica sin detalles internos. | `ProblemResponse` | `INTERNAL_ERROR` |

Ejemplo del contrato (200, `CountryOptionListResponse`):

```json
{
  "items": [
    {
      "id": 1,
      "description": "AFGANISTÁN",
      "countryCode": "AF"
    },
    {
      "id": 2,
      "description": "ISLAS ÅLAND",
      "countryCode": "AX"
    },
    {
      "id": 3,
      "description": "ALBANIA",
      "countryCode": "AL"
    },
    {
      "id": 151,
      "description": "MÉXICO",
      "countryCode": "MX"
    }
  ]
}
```

`CountryOptionListResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `items` | array de `CountryOptionResponse` | Sí | — |

No admite propiedades no definidas.

`CountryOptionResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `id` | `CatalogId` (integer) | Sí | — |
| `description` | string | Sí | — |
| `countryCode` | string | Sí | — |

No admite propiedades no definidas.

### getLegalOwnerships

**GET `/api/v1/registration-catalogs/legal-ownerships`** — Consultar titularidad jurídica aplicable.

- **Seguridad:** Bearer (`RegistrationToken`).
- **Caso de uso:** `GetLegalOwnershipsUseCase`. Fuentes: RF-REG-05, US08.
- **Detalle:** [operaciones/getLegalOwnerships.md](operaciones/getLegalOwnerships.md) · [diagrama de secuencia](diagramas.md#63-getlegalownerships).

Titularidad jurídica filtrada en backend según domain/catalogs.md (ADR-0020). NATIONAL devuelve los registros del país y del régimen; FOREIGN, los del país más NOT_DEF, sin filtro de régimen. taxRegime es obligatorio con NATIONAL y no se permite con FOREIGN. Orden ascendente por key. Un parámetro ausente, no definido, repetido o inválido responde VALIDATION_ERROR con parameter.

**Parámetros**

| Nombre | En | Tipo | Obligatorio | Ejemplo | Descripción |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `origin` | query | `Origin` (`NATIONAL`, `FOREIGN`) | Sí | `NATIONAL` | Origen seleccionado en Datos Generales. |
| `countryCode` | query | string | Sí | `MX` | Clave del país seleccionado (countryCode del catálogo de países). Una clave que no existe en el catálogo (comparación exacta) responde VALIDATION_ERROR con parameter countryCode. |
| `taxRegime` | query | `TaxRegime` (`LEGAL_ENTITY`, `INDIVIDUAL`) | No | `LEGAL_ENTITY` | Obligatorio con origin=NATIONAL; no se permite con origin=FOREIGN. |

**Respuestas**

| HTTP | Descripción | Cuerpo | `code` |
| :--- | :--- | :--- | :--- |
| 200 | Operación completada correctamente. | `LegalOwnershipOptionListResponse` | — |
| 400 | Datos de entrada inválidos. | `ProblemResponse` | `VALIDATION_ERROR` |
| 401 | Token de registro ausente, inválido, vencido a los 60 minutos o sin contexto en el backend. | `ProblemResponse` | `REGISTRATION_TOKEN_INVALID` |
| 500 | Fallo técnico; respuesta genérica sin detalles internos. | `ProblemResponse` | `INTERNAL_ERROR` |

Ejemplo del contrato (200, `LegalOwnershipOptionListResponse`):

```json
{
  "items": [
    {
      "key": "MX00001",
      "id": 1,
      "description": "1.- SOCIEDAD ANÓNIMA (S.A.)",
      "countryCode": "MX",
      "taxRegime": "LEGAL_ENTITY"
    },
    {
      "key": "MX00002",
      "id": 2,
      "description": "1.1.- SOCIEDAD ANÓNIMA DE CAPITAL VARIABLE ( S.A. DE C.V.)",
      "countryCode": "MX",
      "taxRegime": "LEGAL_ENTITY"
    },
    {
      "key": "MX00003",
      "id": 3,
      "description": "2.- SOCIEDAD DE RESPONSABILIDAD LIMITADA (S. DE R.L.)",
      "countryCode": "MX",
      "taxRegime": "LEGAL_ENTITY"
    }
  ]
}
```

`LegalOwnershipOptionListResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `items` | array de `LegalOwnershipOptionResponse` | Sí | — |

No admite propiedades no definidas.

`LegalOwnershipOptionResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `key` | string | Sí | — |
| `id` | `CatalogId` (integer) | Sí | — |
| `description` | string | Sí | — |
| `countryCode` | string | Sí | admite null |
| `taxRegime` | `TaxRegime` (`LEGAL_ENTITY`, `INDIVIDUAL`) | No | — |

No admite propiedades no definidas.

### getSectors

**GET `/api/v1/registration-catalogs/sectors`** — Consultar catálogo de sectores.

- **Seguridad:** Bearer (`RegistrationToken`).
- **Caso de uso:** `GetSectorsUseCase`. Fuentes: RF-REG-05, US08.
- **Detalle:** [operaciones/getSectors.md](operaciones/getSectors.md) · [diagrama de secuencia](diagramas.md#64-getsectors).

Catálogo completo en items, ordenado por id, sin paginación (ADR-0020).

**Respuestas**

| HTTP | Descripción | Cuerpo | `code` |
| :--- | :--- | :--- | :--- |
| 200 | Operación completada correctamente. | `CatalogOptionListResponse` | — |
| 401 | Token de registro ausente, inválido, vencido a los 60 minutos o sin contexto en el backend. | `ProblemResponse` | `REGISTRATION_TOKEN_INVALID` |
| 500 | Fallo técnico; respuesta genérica sin detalles internos. | `ProblemResponse` | `INTERNAL_ERROR` |

Ejemplo del contrato (200, `CatalogOptionListResponse`):

```json
{
  "items": [
    {
      "id": 1,
      "description": "11 AGRICULTURA, CRÍA Y EXPLOTACIÓN DE ANIMALES, APROVECHAMIENTO FORESTAL, PESCA Y CAZA"
    },
    {
      "id": 2,
      "description": "21 MINERÍA"
    },
    {
      "id": 3,
      "description": "22 GENERACIÓN, TRANSMISIÓN, DISTRIBUCIÓN Y COMERCIALIZACIÓN DE ENERGÍA ELÉCTRICA, SUMINISTRO DE AGUA Y DE GAS NATURAL POR DUCTOS AL CONSUMIDOR FINAL"
    },
    {
      "id": 4,
      "description": "23 CONSTRUCCIÓN"
    },
    {
      "id": 5,
      "description": "31-33 INDUSTRIAS MANUFACTURERAS"
    }
  ]
}
```

`CatalogOptionListResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `items` | array de `CatalogOptionResponse` | Sí | — |

No admite propiedades no definidas.

`CatalogOptionResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `id` | `CatalogId` (integer) | Sí | — |
| `description` | string | Sí | — |

No admite propiedades no definidas.

### getSubsectors

**GET `/api/v1/registration-catalogs/sectors/{sectorId}/subsectors`** — Consultar subsectores de un sector.

- **Seguridad:** Bearer (`RegistrationToken`).
- **Caso de uso:** `GetSubsectorsUseCase`. Fuentes: RF-REG-05, US08.
- **Detalle:** [operaciones/getSubsectors.md](operaciones/getSubsectors.md) · [diagrama de secuencia](diagramas.md#65-getsubsectors).

Subsectores del sector indicado, ordenados por id (ADR-0020). Un sector inexistente devuelve items vacío.

**Parámetros**

| Nombre | En | Tipo | Obligatorio | Ejemplo | Descripción |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `sectorId` | path | `CatalogId` (integer) | Sí | `5` | Sector seleccionado. |

**Respuestas**

| HTTP | Descripción | Cuerpo | `code` |
| :--- | :--- | :--- | :--- |
| 200 | Operación completada correctamente. | `CatalogOptionListResponse` | — |
| 400 | Datos de entrada inválidos. | `ProblemResponse` | `VALIDATION_ERROR` |
| 401 | Token de registro ausente, inválido, vencido a los 60 minutos o sin contexto en el backend. | `ProblemResponse` | `REGISTRATION_TOKEN_INVALID` |
| 500 | Fallo técnico; respuesta genérica sin detalles internos. | `ProblemResponse` | `INTERNAL_ERROR` |

Ejemplo del contrato (200, `CatalogOptionListResponse`):

```json
{
  "items": [
    {
      "id": 13,
      "description": "311 INDUSTRIA ALIMENTARIA"
    },
    {
      "id": 14,
      "description": "312 INDUSTRIA DE LAS BEBIDAS Y DEL TABACO"
    },
    {
      "id": 15,
      "description": "313 FABRICACIÓN DE INSUMOS TEXTILES Y ACABADO DE TEXTILES"
    },
    {
      "id": 16,
      "description": "314 FABRICACIÓN DE PRODUCTOS TEXTILES, EXCEPTO PRENDAS DE VESTIR"
    },
    {
      "id": 17,
      "description": "315 FABRICACIÓN DE PRENDAS DE VESTIR"
    }
  ]
}
```

`CatalogOptionListResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `items` | array de `CatalogOptionResponse` | Sí | — |

No admite propiedades no definidas.

`CatalogOptionResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `id` | `CatalogId` (integer) | Sí | — |
| `description` | string | Sí | — |

No admite propiedades no definidas.

### getBusinessActivities

**GET `/api/v1/registration-catalogs/business-activities`** — Consultar catálogo de giros.

- **Seguridad:** Bearer (`RegistrationToken`).
- **Caso de uso:** `GetBusinessActivitiesUseCase`. Fuentes: RF-REG-05, US08.
- **Detalle:** [operaciones/getBusinessActivities.md](operaciones/getBusinessActivities.md) · [diagrama de secuencia](diagramas.md#66-getbusinessactivities).

Catálogo completo en items, ordenado por id, sin paginación (ADR-0020).

**Respuestas**

| HTTP | Descripción | Cuerpo | `code` |
| :--- | :--- | :--- | :--- |
| 200 | Operación completada correctamente. | `CatalogOptionListResponse` | — |
| 401 | Token de registro ausente, inválido, vencido a los 60 minutos o sin contexto en el backend. | `ProblemResponse` | `REGISTRATION_TOKEN_INVALID` |
| 500 | Fallo técnico; respuesta genérica sin detalles internos. | `ProblemResponse` | `INTERNAL_ERROR` |

Ejemplo del contrato (200, `CatalogOptionListResponse`):

```json
{
  "items": [
    {
      "id": 1,
      "description": "INDUSTRIAL"
    },
    {
      "id": 2,
      "description": "COMERCIAL"
    },
    {
      "id": 3,
      "description": "DE SERVICIOS"
    }
  ]
}
```

`CatalogOptionListResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `items` | array de `CatalogOptionResponse` | Sí | — |

No admite propiedades no definidas.

`CatalogOptionResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `id` | `CatalogId` (integer) | Sí | — |
| `description` | string | Sí | — |

No admite propiedades no definidas.

### getStratifications

**GET `/api/v1/registration-catalogs/stratifications`** — Consultar catálogo de estratificación.

- **Seguridad:** Bearer (`RegistrationToken`).
- **Caso de uso:** `GetStratificationsUseCase`. Fuentes: RF-REG-05, US08.
- **Detalle:** [operaciones/getStratifications.md](operaciones/getStratifications.md) · [diagrama de secuencia](diagramas.md#67-getstratifications).

Registros activos de estratificación, ordenados por id, sin asociación ni filtro por régimen (ADR-0020). Un ID inactivo se rechaza al guardar Datos Generales.

**Respuestas**

| HTTP | Descripción | Cuerpo | `code` |
| :--- | :--- | :--- | :--- |
| 200 | Operación completada correctamente. | `CatalogOptionListResponse` | — |
| 401 | Token de registro ausente, inválido, vencido a los 60 minutos o sin contexto en el backend. | `ProblemResponse` | `REGISTRATION_TOKEN_INVALID` |
| 500 | Fallo técnico; respuesta genérica sin detalles internos. | `ProblemResponse` | `INTERNAL_ERROR` |

Ejemplo del contrato (200, `CatalogOptionListResponse`):

```json
{
  "items": [
    {
      "id": 1,
      "description": "COOPERATIVAS"
    },
    {
      "id": 2,
      "description": "GRANDE"
    },
    {
      "id": 3,
      "description": "MEDIANA"
    },
    {
      "id": 4,
      "description": "MICRO"
    },
    {
      "id": 6,
      "description": "PEQUEÑA"
    }
  ]
}
```

`CatalogOptionListResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `items` | array de `CatalogOptionResponse` | Sí | — |

No admite propiedades no definidas.

`CatalogOptionResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `id` | `CatalogId` (integer) | Sí | — |
| `description` | string | Sí | — |

No admite propiedades no definidas.

### getPrimaryActivities

**GET `/api/v1/registration-catalogs/primary-activities`** — Consultar catálogo de actividades preponderantes.

- **Seguridad:** Bearer (`RegistrationToken`).
- **Caso de uso:** `GetPrimaryActivitiesUseCase`. Fuentes: RF-REG-05, US08.
- **Detalle:** [operaciones/getPrimaryActivities.md](operaciones/getPrimaryActivities.md) · [diagrama de secuencia](diagramas.md#68-getprimaryactivities).

Catálogo completo en items, ordenado por id, sin paginación (ADR-0020).

**Respuestas**

| HTTP | Descripción | Cuerpo | `code` |
| :--- | :--- | :--- | :--- |
| 200 | Operación completada correctamente. | `CatalogOptionListResponse` | — |
| 401 | Token de registro ausente, inválido, vencido a los 60 minutos o sin contexto en el backend. | `ProblemResponse` | `REGISTRATION_TOKEN_INVALID` |
| 500 | Fallo técnico; respuesta genérica sin detalles internos. | `ProblemResponse` | `INTERNAL_ERROR` |

Ejemplo del contrato (200, `CatalogOptionListResponse`):

```json
{
  "items": [
    {
      "id": 1,
      "description": "RECURSOS NATURALES"
    },
    {
      "id": 2,
      "description": "CONSTRUCCIÓN"
    },
    {
      "id": 3,
      "description": "FABRICACIÓN PRODUCCIÓN O ELABORACIÓN"
    },
    {
      "id": 4,
      "description": "COMERCIO AL POR MAYOR"
    },
    {
      "id": 5,
      "description": "TRANSPORTE"
    },
    {
      "id": 6,
      "description": "COMUNICACIÓN"
    },
    {
      "id": 7,
      "description": "SERVICIOS"
    },
    {
      "id": 8,
      "description": "COMERCIO AL POR MENOR"
    }
  ]
}
```

`CatalogOptionListResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `items` | array de `CatalogOptionResponse` | Sí | — |

No admite propiedades no definidas.

`CatalogOptionResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `id` | `CatalogId` (integer) | Sí | — |
| `description` | string | Sí | — |

No admite propiedades no definidas.

### getWorkerCountRanges

**GET `/api/v1/registration-catalogs/worker-count-ranges`** — Consultar rangos de número de trabajadores.

- **Seguridad:** Bearer (`RegistrationToken`).
- **Caso de uso:** `GetWorkerCountRangesUseCase`. Fuentes: RF-REG-05, US08.
- **Detalle:** [operaciones/getWorkerCountRanges.md](operaciones/getWorkerCountRanges.md) · [diagrama de secuencia](diagramas.md#69-getworkercountranges).

Catálogo completo en items, ordenado por id, sin paginación (ADR-0020).

**Respuestas**

| HTTP | Descripción | Cuerpo | `code` |
| :--- | :--- | :--- | :--- |
| 200 | Operación completada correctamente. | `CatalogOptionListResponse` | — |
| 401 | Token de registro ausente, inválido, vencido a los 60 minutos o sin contexto en el backend. | `ProblemResponse` | `REGISTRATION_TOKEN_INVALID` |
| 500 | Fallo técnico; respuesta genérica sin detalles internos. | `ProblemResponse` | `INTERNAL_ERROR` |

Ejemplo del contrato (200, `CatalogOptionListResponse`):

```json
{
  "items": [
    {
      "id": 1,
      "description": "0"
    },
    {
      "id": 2,
      "description": "1-10"
    },
    {
      "id": 3,
      "description": "11-30"
    },
    {
      "id": 4,
      "description": "31-50"
    },
    {
      "id": 5,
      "description": "51-100"
    },
    {
      "id": 6,
      "description": "101-251"
    },
    {
      "id": 7,
      "description": "MÁS DE 251"
    }
  ]
}
```

`CatalogOptionListResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `items` | array de `CatalogOptionResponse` | Sí | — |

No admite propiedades no definidas.

`CatalogOptionResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `id` | `CatalogId` (integer) | Sí | — |
| `description` | string | Sí | — |

No admite propiedades no definidas.

### getAnnualSalesRanges

**GET `/api/v1/registration-catalogs/annual-sales-ranges`** — Consultar rangos de ventas anuales.

- **Seguridad:** Bearer (`RegistrationToken`).
- **Caso de uso:** `GetAnnualSalesRangesUseCase`. Fuentes: RF-REG-05, US08.
- **Detalle:** [operaciones/getAnnualSalesRanges.md](operaciones/getAnnualSalesRanges.md) · [diagrama de secuencia](diagramas.md#610-getannualsalesranges).

Catálogo completo en items, ordenado por id, sin paginación (ADR-0020).

**Respuestas**

| HTTP | Descripción | Cuerpo | `code` |
| :--- | :--- | :--- | :--- |
| 200 | Operación completada correctamente. | `CatalogOptionListResponse` | — |
| 401 | Token de registro ausente, inválido, vencido a los 60 minutos o sin contexto en el backend. | `ProblemResponse` | `REGISTRATION_TOKEN_INVALID` |
| 500 | Fallo técnico; respuesta genérica sin detalles internos. | `ProblemResponse` | `INTERNAL_ERROR` |

Ejemplo del contrato (200, `CatalogOptionListResponse`):

```json
{
  "items": [
    {
      "id": 1,
      "description": "$0 - $4 MILLONES"
    },
    {
      "id": 2,
      "description": "$4 - $100 MILLONES"
    },
    {
      "id": 3,
      "description": "$100 - $250 MILLONES"
    },
    {
      "id": 4,
      "description": "MÁS DE $250 MILLONES"
    }
  ]
}
```

`CatalogOptionListResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `items` | array de `CatalogOptionResponse` | Sí | — |

No admite propiedades no definidas.

`CatalogOptionResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `id` | `CatalogId` (integer) | Sí | — |
| `description` | string | Sí | — |

No admite propiedades no definidas.

### getCucopCatalog

**GET `/api/v1/registration-catalogs/cucop`** — Cargar el árbol CUCoP completo.

- **Seguridad:** Bearer (`RegistrationToken`).
- **Caso de uso:** `GetCucopCatalogUseCase`. Fuentes: RF-REG-08, US11.
- **Detalle:** [operaciones/getCucopCatalog.md](operaciones/getCucopCatalog.md) · [diagrama de secuencia](diagramas.md#611-getcucopcatalog).

Devuelve el árbol CUCoP completo ya jerarquizado, en una sola consulta sin paginación. La raíz items contiene los capítulos y cada nodo trae sus hijos en su propio items, hasta las partidas específicas, cuyo items va vacío. Los hermanos se ordenan por key ascendente. El frontend busca por clave o palabra desde el tercer carácter, sin peticiones por expansión ni búsqueda. El volumen aproximado de 300 elementos no es un límite contractual. La tabla visual de seleccionadas pagina 10 filas. Los IDs pueden repetirse entre niveles.

**Respuestas**

| HTTP | Descripción | Cuerpo | `code` |
| :--- | :--- | :--- | :--- |
| 200 | Operación completada correctamente. | `CucopCatalogResponse` | — |
| 401 | Token de registro ausente, inválido, vencido a los 60 minutos o sin contexto en el backend. | `ProblemResponse` | `REGISTRATION_TOKEN_INVALID` |
| 500 | Fallo técnico; respuesta genérica sin detalles internos. | `ProblemResponse` | `INTERNAL_ERROR` |

Ejemplo del contrato (200, `CucopCatalogResponse`):

```json
{
  "items": [
    {
      "id": 1,
      "key": "2000",
      "description": "MATERIALES Y SUMINISTROS",
      "level": 1,
      "items": [
        {
          "id": 1,
          "key": "2100",
          "description": "MATERIALES DE ADMINISTRACION, EMISION DE DOCUMENTOS Y ARTICULOS OFICIALES",
          "level": 2,
          "items": [
            {
              "id": 1,
              "key": "2110",
              "description": "MATERIALES, UTILES Y EQUIPOS MENORES DE OFICINA",
              "level": 3,
              "items": [
                {
                  "id": 1,
                  "key": "21101",
                  "description": "MATERIALES Y ÚTILES DE OFICINA",
                  "level": 4,
                  "items": []
                }
              ]
            },
            {
              "id": 2,
              "key": "2120",
              "description": "MATERIALES Y UTILES DE IMPRESION Y REPRODUCCION",
              "level": 3,
              "items": [
                {
                  "id": 2,
                  "key": "21201",
                  "description": "MATERIALES Y ÚTILES DE IMPRESIÓN Y REPRODUCCIÓN",
                  "level": 4,
                  "items": []
                }
              ]
            }
          ]
        }
      ]
    }
  ]
}
```

`CucopCatalogResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `items` | array de `CucopTreeNodeResponse` | Sí | — |

No admite propiedades no definidas.

`CucopTreeNodeResponse`:

| Campo | Tipo | Obligatorio | Restricciones |
| :--- | :--- | :--- | :--- |
| `id` | `CatalogId` (integer) | Sí | — |
| `key` | string | Sí | — |
| `description` | string | Sí | — |
| `level` | integer (`1`, `2`, `3`, `4`) | Sí | — |
| `items` | array de `CucopTreeNodeResponse` | Sí | — |

No admite propiedades no definidas.
