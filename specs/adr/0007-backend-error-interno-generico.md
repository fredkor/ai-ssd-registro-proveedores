# ADR-0007: Error interno genérico sin detalles de infraestructura

- **Estado:** Aceptado
- **Ámbito:** Backend
- **Fecha:** 2026-09-26
- **Actualización:** 2026-10-21
- **Specs relacionadas:** ADR-0004; `architecture/architecture.md`; borrador del contrato API de registro.

## Contexto

Los fallos inesperados deben impedir el avance sin exponer infraestructura ni alterar el
formato común de errores del registro.

## Decisión

- Las excepciones inesperadas del backend responden HTTP 500 con Problem Details (ADR-0019):
  `code: INTERNAL_ERROR`, `title: "Internal error"`, `detail: "Unexpected error"` y `errors: []`.
- La respuesta no incluye mensajes técnicos, trazas, rutas del volumen, credenciales ni
  contenido completo de firma o certificado.
- Se registra el fallo con nivel ERROR para diagnóstico, sin volcar llave privada, contraseña
  ni el resultado completo del componente de Firma Electrónica del solicitante.
- Se declara INTERNAL_ERROR en el catálogo de errores del contrato; cada operación que pueda
  producirlo deberá declarar su respuesta correspondiente.
- Es una categoría de infraestructura, no una excepción de dominio. Su traducción no implica
  éxito de creación, guardado, verificación o eliminación.
- El detalle de compensación entre archivo y metadatos se rige por ADR-0010.

## Alternativas consideradas

| Alternativa | A favor | En contra |
| :--- | :--- | :--- |
| Respuesta por defecto de la infraestructura | Menor implementación. | Formato variable y riesgo de exponer detalles. |
| Responder con el mensaje original de la excepción | Facilita diagnóstico desde cliente. | Expone información interna. |

## Consecuencias y verificación

Una prueba web provocará un fallo inesperado y verificará HTTP, code, mensaje genérico y
ausencia de detalles internos. Las pruebas de aplicación comprobarán que el fallo no marca
la operación como completada.
