# HU-REG-006 — Navegar y guardar secciones del wizard

**Como** potencial licitante, **quiero** guardar cada sección al avanzar, **para** completar mi expediente sin perder información previamente validada.

## Criterios de aceptación
- **CA-001:** Dado expediente EN_CAPTURA, cuando accedo al wizard, entonces veo Datos Generales, CUCoP, Contactos, Domicilio fiscal, Cobertura y Firma Electrónica, en ese orden.
- **CA-002:** Dado una sección incompleta, cuando intento Siguiente, entonces se muestran errores y no se avanza.
- **CA-003:** Dado una sección válida, cuando pulso Siguiente, entonces se guarda de forma atómica, se marca COMPLETADA y se abre la siguiente.
- **CA-004:** Dado error de guardado, cuando ocurre, entonces permanezco en la sección y no se marca COMPLETADA.
- **CA-005:** Dado una sección previamente guardada, cuando vuelvo a ella, entonces veo sus datos persistidos.
- **CA-006:** Dado que modifico Datos Generales, cuando guardo sus cambios o elimino su cédula fiscal, entonces solo se afecta esa sección y CUCoP conserva intactos sus tipos de participación, partidas seleccionadas y estado de progreso, sin marcarse para revisión.
- **CA-007:** Dado que se muestran los títulos de las secciones, cuando intento seleccionarlos para navegar, entonces no se permite el acceso directo; la navegación se realiza mediante los botones Atrás y Siguiente.
- **CA-008:** Dado que solicito finalizar el registro en una etapa posterior, cuando se valida el expediente, entonces todas las secciones obligatorias deben estar completas y válidas conforme a sus reglas. Los cambios en Datos Generales no invalidan CUCoP ni exigen una nueva validación de sus selecciones por ese motivo.

- **CA-009:** Dado una sección con una anterior, cuando se pulsa Atrás, entonces se descartan los cambios sin guardar de campos, teléfonos y selecciones, sin persistirlos. Al regresar se muestran los datos guardados o la captura inicial y los archivos actualmente asociados. Atrás no revierte cargas ni eliminaciones de archivos exitosas, que se confirman en sus propias operaciones.

- **CA-010:** Dado que una sección ya está COMPLETADA, cuando vuelvo a ella o la edito, entonces permanece COMPLETADA; un nuevo guardado válido conserva esa condición y un fallo no altera los datos ni el progreso persistidos. La eliminación de la cédula aplica la regla específica de HU-REG-010.

**Requerimiento relacionado:** RF-REG-05.

**Alcance:** en la primera etapa se implementan Datos Generales y CUCoP y el punto de continuación hacia Contactos. La captura de las secciones posteriores se incorporará en etapas posteriores.
