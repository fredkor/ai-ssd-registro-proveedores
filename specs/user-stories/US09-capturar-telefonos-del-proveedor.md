# HU-REG-009 — Capturar teléfonos del proveedor

**Como** solicitante persona moral, **quiero** capturar mis teléfonos,
**para** integrar los datos telefónicos de mi expediente en la Plataforma de Compras de Fredkor Labs.

**Precondiciones:** identidad verificada mediante e.firma, políticas aceptadas y expediente EN_CAPTURA.

## Alcance y validaciones

- La captura se realiza dentro de Datos Generales y aplica únicamente a persona moral.
- Se requiere al menos un teléfono para avanzar.
- Número y extensión se recortan antes de validar; el número vacío se rechaza y una extensión vacía se convierte a null, conservando los espacios interiores según ADR-0009.
- Cada número es obligatorio y debe contener 10 dígitos; la extensión es opcional y admite hasta 5 caracteres.
- Agregar inserta una fila vacía editable directamente en la tabla; cada fila dispone además de una acción Eliminar.
- Cada fila de teléfono tiene un botón de acción Eliminar, disponible también para filas vacías, incompletas o previamente guardadas. Al pulsarlo se retira la fila de la captura local; la eliminación se persiste únicamente al guardar correctamente Datos Generales con Siguiente. Salir o reanudar sin guardar recupera los teléfonos persistidos. Se permite retirar la última fila, pero persona moral no puede completar la sección sin al menos un teléfono válido. No se crea una petición de borrado por teléfono.
- Las filas incompletas o con formato inválido impiden avanzar en Datos Generales.

## Criterios de aceptación

- **CA-001:** Dado un solicitante persona física, cuando se muestran sus datos generales, entonces no se solicita la sección de teléfonos de persona moral.
- **CA-002:** Dado un solicitante persona moral, cuando pulsa el botón Agregar de teléfonos, entonces se inserta directamente una fila con Número y Extensión vacíos y editables en la tabla, sin requerir captura previa ni botón Cancelar.
- **CA-003:** Dado que hay filas de teléfono agregadas, cuando se valida el avance, entonces todos los números deben estar completos y contener 10 dígitos, y las extensiones informadas deben tener hasta 5 caracteres; cualquier fila incompleta o inválida impide avanzar.
- **CA-004:** Dado un solicitante persona moral sin teléfonos agregados, cuando intenta completar Datos Generales, entonces se impide avanzar hasta agregar al menos un teléfono.
- **CA-005:** Dado que se informa una extensión telefónica, cuando supera 5 caracteres, entonces se rechaza.

- **CA-006:** Dado un teléfono con número válido y extensión null, cuando se valida el guardado, entonces la extensión se acepta como no informada. El número obligatorio no puede ser null.

- **CA-007:** Dado una fila de teléfono vacía, incompleta o previamente guardada, cuando el proveedor pulsa su botón de acción Eliminar, entonces se retira de la tabla local sin llamar a un endpoint de borrado; la colección restante se persiste únicamente al guardar Datos Generales con Siguiente.
- **CA-008:** Dado que el proveedor elimina la última fila, cuando se valida el avance de persona moral, entonces Siguiente permanece inactivo hasta que exista al menos un teléfono válido; el backend también rechaza un guardado sin teléfonos.
- **CA-009:** Dado que se elimina una fila previamente guardada, cuando el proveedor sale sin un guardado exitoso con Siguiente y reanuda el expediente, entonces se muestran los teléfonos previamente persistidos. Si falla el guardado, la colección persistida tampoco cambia.

**Historia relacionada:** [HU-REG-008 — Capturar Datos Generales](US08-capturar-datos-generales.md).

**Requerimiento relacionado:** RF-REG-05.
