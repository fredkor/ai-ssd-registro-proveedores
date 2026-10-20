# HU-REG-004 — Crear expediente de registro

**Como** potencial licitante identificado que aceptó las políticas, **quiero** iniciar un expediente, **para** completar progresivamente las secciones de registro.

## Criterios de aceptación
- **CA-001:** Dado RFC validado, inexistencia en la Plataforma de Compras de Fredkor Labs y aceptación registrada, cuando inicio captura, entonces se crea expediente EN_CAPTURA.
- **CA-002:** Dado que se crea correctamente, cuando se abre el wizard, entonces Datos Generales es la primera sección.
- **CA-003:** Dado que otro proceso creó un expediente para el mismo RFC, cuando intento crearlo, entonces se impide duplicidad y se redirige al flujo adecuado.
- **CA-004:** Dado que falla la creación, cuando ocurre, entonces se informa el error y no se simula un expediente creado.
- **CA-005:** Dado que se crea un expediente, cuando concluye la operación, entonces tiene un identificador interno UUID único generado por el sistema.
- **CA-006:** Dado que mi identidad fue verificada, cuando se crea el expediente, entonces quedan asociados el RFC y los datos de identidad verificados correspondientes.

**Requerimiento relacionado:** RF-REG-04.
