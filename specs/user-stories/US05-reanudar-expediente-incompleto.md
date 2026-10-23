# HU-REG-005 — Reanudar expediente incompleto

**Como** potencial licitante con expediente incompleto, **quiero** reanudarlo después de identificarme con e.firma, **para** conservar el avance realizado.

## Identidad e historial al reanudar

Al reanudar un expediente IN_PROGRESS tras una nueva identificación aceptada del mismo RFC, se mantiene el UUID del expediente y se actualiza su nombre completo / razón social con el extraído del nuevo certificado verificado conforme a ADR-0016. Se conservan íntegramente la evidencia inicial y las nuevas evidencias, vinculadas históricamente al mismo expediente, sin sobrescribirlas. La actualización no altera los datos capturados, documentos confirmados ni progreso de ninguna sección, incluido CUCoP. El RFC permanece inmutable. La actualización del nombre, el vínculo histórico de firma y la sustitución por la última aceptación del aviso fijo (US03 CA-007) se confirman en la misma transacción; un fallo no deja cambios parciales. Una identificación por sí sola no actualiza el expediente: el cambio se confirma al reanudarlo.

Cada identificación se aplica una sola vez al expediente. Reanudar con un token cuya evidencia ya está vinculada al expediente (la de su creación o una reanudación ya confirmada, por ejemplo al reintentar) no vuelve a aplicarla: devuelve el expediente en su estado actual sin modificar nombre, aceptación, historial, datos ni progreso.

## Criterios de aceptación
- **CA-001:** Dado expediente EN_CAPTURA, cuando valido nuevamente mi e.firma, entonces se habilita la reanudación asociada a mi RFC.
- **CA-002:** Dado que reanudo, cuando se recuperan los datos, entonces veo secciones y valores persistidos.
- **CA-003:** Dado que hay secciones completas y pendientes, cuando abre el wizard, entonces se presenta su progreso.
- **CA-004:** Dado un expediente ajeno, cuando intento acceder, entonces se deniega la operación.
- **CA-005:** Dado error de recuperación, cuando ocurre, entonces no se sobrescriben los datos guardados.
- **CA-006:** Dado expediente REGISTRADO, cuando intento reanudarlo como borrador, entonces el sistema lo impide.
- **CA-007:** Dado un expediente EN_CAPTURA, cuando reanudo, entonces siempre se abre Datos Generales, conservando los datos persistidos y el progreso de las secciones sin exigir su recaptura.
- **CA-008:** Dado un expediente EN_CAPTURA asociado a mi RFC verificado, cuando lo reanudo, entonces se utiliza el mismo expediente sin crear uno nuevo.

- **CA-009:** Dado que el nuevo certificado verificado corresponde al mismo RFC pero tiene otro nombre completo / razón social, cuando se reanuda correctamente, entonces se mantiene el mismo expediente y se actualiza su nombre con el extraído del nuevo certificado, conservando datos capturados, documentos confirmados y progreso, incluido CUCoP.
- **CA-010:** Dado que se reanuda tras una nueva identificación aceptada, cuando se confirma la operación, entonces se vincula la nueva evidencia al historial del expediente y se conservan intactas la inicial y las anteriores, sin sobrescribirlas.
- **CA-011:** Dado un fallo al actualizar el nombre o vincular la evidencia al reanudar, cuando la operación no concluye, entonces se revierte la actualización transaccional y se conservan nombre, vínculos históricos, datos y progreso previos. La evidencia ya conservada en la identificación no se elimina por este fallo.

**Requerimiento relacionado:** RF-REG-06.
