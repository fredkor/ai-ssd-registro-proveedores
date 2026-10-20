# ADR-0003: UUID del expediente y RFC como identidad de negocio

- **Estado:** Aceptado
- **Ámbito:** Compartido
- **Fecha:** 2026-09-26
- **Actualización:** 2026-10-18
- **Specs relacionadas:** RF-REG-01, RF-REG-02, RF-REG-04, RF-REG-06; INV-01, INV-09 a INV-15; ADR-0010.

## Contexto

El expediente requiere identidad técnica independiente de la identidad fiscal del solicitante
y de la clave de proveedor que se generará al finalizar.

## Decisión

- `SupplierRegistrationId` envuelve un UUID generado por el sistema al crear el expediente.
- El contrato identificará el expediente mediante ese UUID en las operaciones que lo requieran.
  La ruta concreta se definirá en OpenAPI.
- El RFC obtenido de la Firma Electrónica del solicitante es la clave de negocio única e
  inmutable del expediente. Se comprueba su longitud de 12 o 13 caracteres.
- La consulta de existencia y reanudación utilizan el RFC de una identificación aceptada;
  un RFC libre o un UUID suministrado por la interfaz no autorizan acceso por sí mismos.
- La unicidad de RFC se verifica en aplicación y se respalda en persistencia ante concurrencia.
- Usuario es el RFC en minúsculas, no un segundo identificador técnico del expediente.
- SupplierCode es una clave de negocio futura distinta del UUID y del RFC; su algoritmo sigue pendiente.
- Cada archivo tiene su propio UUID conforme a ADR-0010, distinto del UUID del expediente.
- No se establece una normalización adicional del RFC ni se limita su consulta a un listado
  administrativo que no forma parte del alcance.

## Alternativas consideradas

| Alternativa | A favor | En contra |
| :--- | :--- | :--- |
| RFC como identificador técnico en todas las rutas | Es conocido por el solicitante. | Acopla localización de recursos e identidad fiscal. |
| Identificador secuencial dependiente de la base de datos | Sencillo en persistencia. | Hace depender la asignación técnica de la base. |

## Consecuencias y verificación

Se distinguen expediente, RFC, usuario, clave final y archivo. Se verifican creación de UUID,
reanudación del mismo expediente, rechazo de duplicados y acceso por RFC aceptado.
