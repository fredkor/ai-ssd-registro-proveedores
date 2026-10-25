# ADR-0008: Swagger UI con contrato autoritativo del registro

- **Estado:** Aceptado
- **Ámbito:** Backend
- **Fecha:** 2026-09-27
- **Actualización:** 2026-10-18; 2026-10-25 (estado del contrato)
- **Specs relacionadas:** `architecture/architecture.md`; `architecture/tech-stack.md`; contrato OpenAPI del registro.

## Contexto

La API debe documentarse desde las especificaciones y no deducirse del código de manera
que sustituya las reglas, casos de uso y modelos aprobados.

## Decisión

- Se mantiene springdoc para mostrar Swagger UI con el contrato autoritativo del registro.
- El YAML del registro es `specs/api/supplier-registration-openapi.yaml`, habilitado para
  implementación (`x-implementation-ready: true`). Se copia como recurso estático al compilar,
  sin duplicación manual, con el mismo nombre en Maven, Swagger UI y el validador.
- OpenAPI generará interfaces y DTO del adaptador web; no se editará código generado.
- La documentación deducida desde código no sustituirá el YAML autoritativo. Podrá compararse
  con él como comprobación adicional cuando exista la implementación.

## Alternativas consideradas

| Alternativa | A favor | En contra |
| :--- | :--- | :--- |
| Solo cliente externo de pruebas | Evita documentación embebida. | No cumple la decisión de ofrecer Swagger UI. |
| Generar documentación solo desde código | Reduce configuración inicial. | Sustituye la fuente autoritativa de especificación. |

## Consecuencias y verificación

Se conserva la dependencia ya declarada, sin cambio de versión. Al implementar se verificará
que Swagger UI carga el mismo contrato utilizado para generar interfaces y DTO.
Verificado por `ADR0008_swaggerUi_loadsTheAuthoritativeContract` (2026-10-25).
