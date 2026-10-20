# HU-REG-002 — Verificar existencia del licitante

**Como** potencial licitante identificado, **quiero** saber si ya existe un registro para mi RFC en la Plataforma de Compras de Fredkor Labs, **para** iniciar o continuar el proceso correcto.

## Criterios de aceptación
- **CA-001:** Dado un RFC validado sin expediente, cuando se consulta la Plataforma de Compras de Fredkor Labs, entonces se habilita la creación de un expediente nuevo con las políticas previamente aceptadas.
- **CA-002:** Dado un RFC con expediente EN_CAPTURA, cuando se consulta la Plataforma de Compras de Fredkor Labs, entonces se ofrece reanudar sin crear duplicado.
- **CA-003:** Dado un RFC con expediente REGISTRADO, cuando se consulta la Plataforma de Compras de Fredkor Labs, entonces se informa registro existente y no se crea otro.
- **CA-004:** Dado un error de consulta, cuando ocurre, entonces se informa el fallo y se impide decidir incorrectamente que no existe.
- **CA-005:** Dado un RFC obtenido de la identidad verificada, cuando se consulta la existencia del registro y se continúa la captura, entonces se conserva ese RFC y el usuario no puede modificarlo.

**Requerimiento relacionado:** RF-REG-02.
