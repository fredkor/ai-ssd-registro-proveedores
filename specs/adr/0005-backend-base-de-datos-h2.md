# ADR-0005: H2 en memoria para pruebas y pérdida de referencias al reiniciar

- **Estado:** Aceptado
- **Ámbito:** Backend
- **Fecha:** 2026-09-26
- **Actualización:** 2026-10-18
- **Specs relacionadas:** RF-REG-04 a RF-REG-06; ADR-0010; `architecture/architecture.md`.

## Contexto

Para las pruebas se acepta perder la información almacenada en memoria cuando reinicia el
backend. Los archivos físicos se guardan en un volumen según ADR-0010, de manera independiente.

## Decisión

- Se mantiene H2 en memoria con Spring Data JPA dentro de `adapter.out.persistence` para pruebas.
- Expedientes, aceptaciones, evidencias de firma, progreso y metadatos documentales conservados
  en esta base se pierden al reiniciar el proceso.
- También se pierden el nombre original, las asociaciones y las referencias a archivos que
  solo estuvieran conservadas en esa base. El volumen no recupera esos datos automáticamente.
- Los archivos del volumen permanecen mientras el volumen se conserve; reiniciar H2 no los
  elimina. Sin sus referencias, no podrán recuperarse mediante las operaciones normales del registro.
- La conservación al salir de la interfaz o reanudar dentro de la misma ejecución permanece
  vigente. No se promete recuperar el expediente anterior tras reiniciar el backend en pruebas.
- La limpieza diaria de ADR-0010 solo elimina temporales con metadatos que acrediten más de 24 horas y ausencia de asociación. Los archivos sin metadatos tras reiniciar no se borran automáticamente ni se reconstruyen desde rutas.
- La base de datos duradera de otros entornos se definirá en una decisión posterior.

## Alternativas consideradas

| Alternativa | A favor | En contra |
| :--- | :--- | :--- |
| H2 en memoria para pruebas | Configuración existente, aceptada para pruebas. | Pierde datos y referencias al reiniciar. |
| Base duradera | Conserva expedientes y referencias entre reinicios. | Motor y configuración de otros entornos aún no definidos. |

## Consecuencias y verificación

- Las pruebas que reinicien el backend no deben esperar recuperar referencias anteriores.
- Un archivo persistente en el volumen no demuestra que exista una asociación recuperable en H2.
- Se prueban las restricciones de unicidad y transacciones mientras la instancia está activa.
- No se cambia la tecnología de persistencia ni se agrega una dependencia en esta homologación.
