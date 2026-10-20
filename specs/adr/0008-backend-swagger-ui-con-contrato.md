# ADR-0008: Swagger UI con contrato autoritativo del registro

- **Estado:** Aceptado
- **Ámbito:** Backend
- **Fecha:** 2026-09-27
- **Actualización:** 2026-10-18
- **Specs relacionadas:** `architecture/architecture.md`; `architecture/tech-stack.md`; borrador del contrato OpenAPI.

## Contexto

La API debe documentarse desde las especificaciones y no deducirse del código de manera
que sustituya las reglas, casos de uso y modelos aprobados.

## Decisión

- Se mantiene springdoc para mostrar Swagger UI con el contrato autoritativo del registro.
- El YAML del registro es `specs/api/supplier-registration-openapi.yaml`, actualmente borrador.
  Una vez cerrado, se copiará como recurso estático al compilar, sin duplicación manual,
  usando el mismo nombre en Maven, Swagger UI y el validador.
- OpenAPI generará interfaces y DTO del adaptador web; no se editará código generado.
- La documentación deducida desde código no sustituirá el YAML autoritativo. Podrá compararse
  con él como comprobación adicional cuando exista la implementación.
- La configuración actual de Maven y stack que referencia un contrato anterior debe
  homologarse al cerrar el borrador del registro; su existencia no significa que esté listo
  para generación ni que Swagger UI ya lo esté mostrando.

## Alternativas consideradas

| Alternativa | A favor | En contra |
| :--- | :--- | :--- |
| Solo cliente externo de pruebas | Evita documentación embebida. | No cumple la decisión de ofrecer Swagger UI. |
| Generar documentación solo desde código | Reduce configuración inicial. | Sustituye la fuente autoritativa de especificación. |

## Consecuencias y verificación

Se conserva la dependencia ya declarada, sin cambio de versión. Al implementar se verificará
que Swagger UI carga el mismo contrato utilizado para generar interfaces y DTO.
