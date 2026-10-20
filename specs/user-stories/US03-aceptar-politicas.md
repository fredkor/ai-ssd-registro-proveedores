# HU-REG-003 — Aceptar políticas

**Como** solicitante de registro como proveedor, **quiero** conocer y aceptar las políticas aplicables, **para** iniciar la captura bajo condiciones informadas.

**Contenido:** [Política de Aviso](../requirements/registration-notice.md), versión `1.0`.

La Política de Aviso es fija, con texto y versión 1.0. Cada nuevo inicio del flujo, incluida la reanudación, requiere aceptación expresa antes de firmar. El expediente conserva únicamente la última aceptación confirmada, con versión y fecha/hora del backend, sin historial de aceptaciones. Al reanudar correctamente se sustituye la anterior por la aceptación vinculada a la nueva identificación; esta sustitución se confirma en la misma transacción que actualiza el nombre y vincula la evidencia. Si falla la reanudación, la aceptación previamente asociada permanece intacta. Esto no elimina ni sobrescribe el historial de evidencias de firma.

## Criterios de aceptación
- **CA-001:** Dado que inicio el proceso de registro, cuando intento solicitar la firma inicial, entonces se muestran las políticas vigentes antes de ejecutar la firma.
- **CA-002:** Dado que cancelo, cuando cierro el diálogo, entonces no se registra aceptación, no se solicita la firma y no se crea expediente.
- **CA-003:** Dado que acepto, cuando se procesa la operación, entonces se conserva versión y fecha/hora de aceptación y se habilita la solicitud de firma; la creación del expediente requiere además una firma exitosa y la comprobación de inexistencia.
- **CA-004:** Dado que falla el registro de aceptación, cuando ocurre, entonces no se permite solicitar la firma ni iniciar el expediente hasta resolverlo.
- **CA-005:** Dado que acepté las políticas y se crea mi expediente, cuando se consulta la aceptación conservada, entonces su versión y fecha/hora están asociadas a ese expediente.
- **CA-006:** Dado que se muestra el aviso, cuando el solicitante consulta su contenido, entonces ve el texto definido en la Política de Aviso y las opciones Aceptar y Cancelar.

- **CA-007:** Dado el aviso fijo versión 1.0, cuando el solicitante inicia una nueva identificación para reanudar, entonces debe aceptarlo expresamente antes de firmar. Al reanudar correctamente, el expediente conserva solo esa última aceptación y su fecha/hora, reemplazando la anterior sin historial de aceptaciones; las evidencias de firma históricas permanecen intactas.
- **CA-008:** Dado que existe una aceptación asociada al expediente, cuando falla la reanudación, entonces se conserva la aceptación anterior sin sustitución parcial.

**Requerimiento relacionado:** RF-REG-03.
