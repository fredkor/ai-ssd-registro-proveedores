# HU-REG-007 — Finalizar registro

**Como** potencial licitante con expediente completo, **quiero** finalizar mi registro, **para** quedar registrado en la Plataforma de Compras de Fredkor Labs.

**Alcance:** etapa posterior; fuera de la primera etapa de implementación.

## Criterios de aceptación
- **CA-001:** Dado que todas las secciones obligatorias están completas y válidas, cuando solicito finalizar, entonces el sistema comprueba también la consistencia entre sus datos antes de finalizar.
- **CA-002:** Dado que falta una sección obligatoria, cuando intento finalizar, entonces el sistema impide el cierre e identifica pendientes.
- **CA-003:** Dado cierre exitoso, cuando se confirma la operación, entonces el expediente pasa a REGISTRADO.

**Pendientes de definición:** reglas de la sección final de Firma Electrónica y algoritmo de generación de la clave del proveedor. Los criterios que dependen de estas reglas no están listos para implementación.
- **CA-004:** Dado que el registro finaliza correctamente, cuando se confirma el cierre, entonces se asigna la clave del proveedor y se registra la fecha y hora de finalización.
- **CA-005:** Dado que ocurre un fallo durante la finalización, cuando la operación no concluye, entonces no se conserva una finalización parcial.
- **CA-006:** Dado que una solicitud de finalización se reintenta, cuando se procesa nuevamente, entonces no genera registros ni claves de proveedor duplicados.
- **CA-007:** Dado que la finalización concluye correctamente, cuando se presenta el resultado, entonces el solicitante recibe una confirmación de registro exitoso.

**Requerimiento relacionado:** RF-REG-07.
