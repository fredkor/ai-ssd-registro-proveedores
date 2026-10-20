# Convenciones del Proyecto

## Idioma
- **Castellano:** visión, requerimientos, historias de usuario, ADR y descripciones del contrato.
- **Inglés:** todo lo que se convierte en código: rutas, operationId, campos, enums, clases, métodos y pruebas.

## Códigos de dominio
- Estados y catálogos se escriben en inglés, MAYÚSCULAS_CON_GUION_BAJO (ej. `DRAFT`, `ELECTRONICS`).
- Cada código tiene su equivalente en castellano documentado en `specs/domain/`.
- Ningún código puede usarse en el contrato o en el código si no existe primero en `specs/domain/`.