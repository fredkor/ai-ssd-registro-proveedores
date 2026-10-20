# ADR-0004: Errores del registro y representación HTTP

- **Estado:** Aceptado
- **Ámbito:** Compartido
- **Fecha:** 2026-09-26
- **Actualización:** 2026-10-18
- **Specs relacionadas:** RF-REG-01 a RF-REG-08; US01 a US11; INV-01 a INV-39; ADR-0007.

## Contexto

El cliente debe distinguir datos inválidos, sello rechazado, expediente inexistente,
acceso denegado, conflictos y fallos técnicos sin recibir detalles de infraestructura.

## Decisión

Las respuestas de error del backend usarán `ErrorResponse { code, message, details }`.
El código y HTTP constituyen el contrato; mensajes y detalles informativos estarán en inglés.

| HTTP | Código | Situación | Reglas o fuente |
| :--- | :--- | :--- | :--- |
| 400 | `VALIDATION_ERROR` | Entrada mal formada, campos requeridos ausentes, longitudes o formatos inválidos, valores de catálogo no válidos o sección que no satisface sus datos obligatorios. | INV-01, INV-02, INV-14 a INV-26, INV-31, INV-34. |
| 401 | `REGISTRATION_TOKEN_INVALID` | Token ausente, inválido, vencido o invalidado por nueva sesión del mismo RFC. | ADR-0015; contexto de aplicación. |
| 403 | `REGISTRATION_ACCESS_DENIED` | La identificación aceptada no corresponde al RFC del expediente solicitado. | INV-12; US05 CA-004. |
| 404 | `REGISTRATION_NOT_FOUND` | Una operación requiere un expediente que no existe. | Casos de uso sobre expediente. |
| 404 | `FISCAL_DOCUMENT_NOT_FOUND` | Se solicita descargar o eliminar una cédula que no está asociada al expediente. | RF-REG-05; US10. |
| 409 | `DUPLICATE_RFC` | Se intenta crear otro expediente para un RFC existente, incluso ante concurrencia. | INV-09, INV-10. |
| 409 | `INVALID_STATE_TRANSITION` | Estado incompatible con la operación, incluida reanudación de REGISTERED o acceso a CUCoP sin Datos Generales guardados. | INV-13, INV-30. |
| 409 | `FISCAL_DOCUMENT_ALREADY_EXISTS` | Se intenta asociar otra cédula cuando ya existe una. | INV-27. |
| 413 | `DOCUMENT_TOO_LARGE` | El archivo supera el máximo permitido de 5 MiB (5,242,880 bytes). | INV-27. |
| 415 | `DOCUMENT_FORMAT_NOT_SUPPORTED` | La extensión no es .pdf (sin distinguir mayúsculas/minúsculas) o el contenido no corresponde a PDF; el MIME declarado no basta. | INV-27. |
| 422 | `SIGNATURE_VERIFICATION_FAILED` | El sello recibido no se verifica con el contenido y certificado del solicitante. | INV-04. |
| 422 | `REGISTRATION_INCOMPLETE` | Finalización futura con secciones pendientes, inválidas o inconsistentes. | INV-38; fuera de la primera etapa. |
| 500 | `INTERNAL_ERROR` | Fallo técnico inesperado de consulta, persistencia, verificación o archivo que impide completar la operación. | ADR-0007. |

- La ausencia o formato inválido de datos necesarios para verificar una firma corresponde
  a VALIDATION_ERROR. La ausencia del atributo de sujeto 2.5.4.45, un primer segmento vacío o de longitud distinta de 12 o 13 también corresponde a VALIDATION_ERROR. La ausencia de atributos de nombre con OID 2.5.4.3, 2.5.4.41 o 2.5.4.10, o el valor vacío del primero coincidente en el orden del certificado, también produce VALIDATION_ERROR. Una discrepancia entre el RFC recibido y ese primer segmento separado por `/`, o una verificación criptográfica negativa, corresponde a SIGNATURE_VERIFICATION_FAILED;
  una excepción técnica que impide verificar corresponde a INTERNAL_ERROR. Todos impiden continuar.
- Los errores del componente externo por vigencia, OCSP, cancelación o indisponibilidad se
  procesan en su integración de interfaz; no se presentan como respuestas HTTP del backend
  si este no fue invocado. No se incorporan servicios de OCSP del backend.
- Un resultado normal de consulta que indica ausencia de expediente no es 404 ni un fallo.
  Detectar REGISTERED durante esa consulta puede ser un resultado normal; intentar una
  operación prohibida sobre él es un conflicto.
- Los errores de infraestructura no son excepciones de dominio. El dominio expresa reglas
  incumplidas; aplicación y adaptadores expresan verificación, acceso y fallos técnicos.
  El adaptador web traduce esas categorías sin forzar una excepción de dominio por cada fila.
- El token de registro se rige por ADR-0015: Bearer JWT, 60 minutos, invalidación al emitir
  una nueva sesión para el mismo RFC. La lista de códigos aplicables se declara por operación.
- No se incluyen rutas físicas, contraseñas, llave privada, certificados completos ni trazas
  en mensajes de error.

## Alternativas consideradas

| Alternativa | A favor | En contra |
| :--- | :--- | :--- |
| Todos los rechazos como 400 | Reduce categorías. | Confunde conflictos, acceso, archivos y reglas. |
| Respuestas de infraestructura sin traducción | Menos mapeos. | Rompe el formato común y expone detalles técnicos. |

## Consecuencias y verificación

Se prueban HTTP y code por operación y el tratamiento de fallos sin efectos exitosos falsos.
Las pruebas de aplicación verifican que las categorías impiden avanzar, crear o completar
cuando corresponde. Este ADR define la traducción técnica; no crea acciones de negocio adicionales.
