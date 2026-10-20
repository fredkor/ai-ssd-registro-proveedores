# Integración de Firma Electrónica del solicitante

El componente COMP-EFIRMA-001 comprueba vigencia y OCSP y firma `Temporal`.
La interfaz comprueba disponibilidad y estado listo y procesa éxito, error o cancelación.
El backend conserva el resultado aceptado completo antes de permitir crear o reanudar.

## Representación y mapeo de evidencia

Los nombres externos y tipos se definen en `SignatureResultRequest` del
[contrato](supplier-registration-openapi.yaml). Todas sus propiedades deben enviarse;
las opcionales sin valor se representan con null conforme a ADR-0009.
La proyección de evidencia conserva los nombres internos siguientes:

| Dato de negocio | Nombre técnico en el modelo | Propiedad del componente |
| :--- | :--- | :--- |
| Resultado | `successful` | `resultado` |
| Mensaje | `message` | `mensaje` |
| Contenido firmado | `signedContent` | `cadenaOriginal` |
| RFC | `rfc` | `rfc` |
| CURP | `curp` | `curp` |
| Nombre completo / Razón social | `legalName` | `razonSocial` |
| Sello digital | `signature` | `cadenaFirma` |
| Número de serie del certificado | `certificateSerialNumber` | `numeroSerie` |
| Indicador de vigencia | `certificateIsCurrent` | `isVigente` |
| Fecha de vigencia | `certificateValidityDate` | `fechaVigencia` |
| Certificado Base64 | `certificate` | `certificado` |
| Certificado PEM | `certificatePem` | `certificadoPem` |
| Tipo de certificado | `certificateType` | `tipoCertificado` |
| Origen del componente | `componentOrigin` | `origen` |
| Resultado íntegro | `completeResult` | Todas las propiedades entregadas por el componente. |

`legalName` en esta proyección conserva `razonSocial` recibido; es distinto del nombre
verificado de `ApplicantIdentity`, extraído del certificado. No sobrescribir uno con otro.
`completeResult` preserva todas las propiedades originales aceptadas, sin llave privada ni
contraseña. El adaptador serializa la evidencia sin introducir Jackson en dominio.

## Verificación y extracción

El [ADR-0016](../adr/0016-backend-verificacion-firma-sha256-rsa.md) define certificado
Base64/PEM, UTF-8, RSA/SHA-256, extracción de RFC y nombre y errores.
Las fechas se transmiten como date-time ISO 8601; los números conservan el tipo del componente.
No se agregan validaciones obligatorias de CURP, número de serie o vigencia en backend.
El origen de la evidencia no sustituye el origen seleccionado en Datos Generales.

## Contexto y comprobación

El [ADR-0015](../adr/0015-shared-token-de-registro.md) define emisión del token e invalidación.
Los [criterios US01](../user-stories/US01-identificacion-mediante-efirma.md) y la
[trazabilidad](../traceability.md) definen la verificación prevista; no representan pruebas ejecutadas.
