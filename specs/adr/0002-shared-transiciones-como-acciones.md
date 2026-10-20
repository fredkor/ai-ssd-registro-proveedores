# ADR-0002: Acciones de registro y guardado explícito por sección

- **Estado:** Aceptado
- **Ámbito:** Compartido
- **Fecha:** 2026-09-26
- **Actualización:** 2026-10-18
- **Specs relacionadas:** RF-REG-04 a RF-REG-08; US04 a US11; `domain/supplier-registration-lifecycle.md`.

## Contexto

Crear el expediente, guardar una sección y finalizar son operaciones con precondiciones
diferentes. Moverse entre pantallas no debe guardar datos ni cambiar el estado del expediente.

## Decisión

- La creación inicia el expediente en IN_PROGRESS. El estado no es un campo editable del solicitante.
- Siguiente ejecuta el caso de uso de guardado de la sección, valida y persiste datos y progreso
  transaccionalmente. Solo tras éxito se permite avanzar.
- Atrás navega sin guardar. Carga y eliminación de cédula tienen efecto inmediato según ADR-0010; Atrás no revierte esas operaciones exitosas.
- Reanudar recupera el mismo expediente y siempre abre Datos Generales; no crea otro ni
  modifica el estado global por cambiar la sección visible.
- La finalización futura es una acción explícita que asigna la clave, registra hora y cambia
  a REGISTERED atómicamente. No se implementa hasta completar sus reglas.
- Las acciones de negocio se representan como operaciones explícitas del contrato. La creación
  utiliza POST a una colección y las acciones utilizan POST donde corresponda. El verbo y
  payload de guardado de cada sección se cerrarán al definir OpenAPI, respetando el guardado completo validado.
- No se permite modificar directamente estado o progreso mediante un request genérico.
- No se definen rutas de activación o descontinuación. Las rutas y operationId del registro
  se fijarán en el contrato, sin convertir ejemplos de este ADR en endpoints ya existentes.

## Alternativas consideradas

| Alternativa | A favor | En contra |
| :--- | :--- | :--- |
| Cambiar estado y progreso con un PATCH genérico | Un único mecanismo de actualización. | Permite eludir precondiciones y mezcla navegación con negocio. |
| Guardar al navegar en ambas direcciones | Automatiza guardado. | Contradice Atrás sin persistencia. |

## Consecuencias y verificación

Los casos de uso verifican precondiciones y no dependen de la secuencia visual para proteger
las reglas. Se prueba que fallar no completa secciones, Atrás no guarda y reanudar no duplica.
La finalización queda identificada como posterior.
