# HU-REG-011 — Capturar participación y partidas CUCoP

**Como** solicitante de registro como proveedor, **quiero** indicar mi tipo de participación y las partidas específicas que ofrezco,
**para** registrar mi objeto social en la Plataforma de Compras de Fredkor Labs.

**Precondiciones:** expediente EN_CAPTURA y Datos Generales guardados correctamente.

## Campos y condiciones

- Tipo de participación obligatorio: Proveedor de adquisiciones, arrendamientos y servicios;
  Contratista de obra pública o servicios relacionados con las mismas. Se permite elegir uno o ambos.
- Selección obligatoria de al menos una partida específica.
- Árbol integrado por los catálogos Capítulos, Conceptos, Partida genérica y Partida específica.
  Todas las partidas genéricas tienen partidas específicas.
- Cada registro hijo referencia el ID de su padre inmediato: Concepto a Capítulo,
  Partida genérica a Concepto y Partida específica a Partida genérica.
- Solo las partidas genéricas y específicas tendrán casillas de selección editables.
  Capítulos y conceptos permiten navegar por la jerarquía sin selección editable.
- Tabla de específicas seleccionadas con columnas CUCoP y Descripción, buscador general
  y paginación de 10 registros por página.

## Criterios de aceptación

- **CA-001:** Dado que Datos Generales se guardó correctamente, cuando se accede a CUCoP, entonces aparece como sección seleccionada y se muestran los tipos de participación, el árbol, la tabla, Deshacer selección, Atrás y Siguiente.
- **CA-002:** Dado que se presenta CUCoP, cuando el proveedor consulta el formulario, entonces ve “Con relación al catálogo CUCoP, seleccione su objeto social” y “Los campos marcados con asterisco son obligatorios”.
- **CA-003:** Dado que se captura el tipo de participación, cuando se selecciona una o ambas opciones, entonces se conservan las opciones marcadas en la captura.
- **CA-004:** Dado que falta un tipo de participación o no hay partidas específicas seleccionadas, cuando se evalúa el avance, entonces Siguiente permanece inactivo.
- **CA-005:** Dado el árbol CUCoP, cuando se despliega una partida genérica, entonces se muestran sus partidas específicas y se permite seleccionar varias mediante casillas.
- **CA-006:** Dado que se seleccionan partidas específicas, cuando se actualiza la tabla, entonces se muestran sus claves CUCoP y descripciones, con 10 registros por página y buscador general.
- **CA-007:** Dado que una partida genérica tiene específicas seleccionadas, cuando se deselecciona esa genérica, entonces se deseleccionan todas sus específicas y dejan de aparecer en la tabla de selección.
- **CA-008:** Dado que hay selecciones en el árbol CUCoP, cuando se pulsa Deshacer selección, entonces se desmarcan todas las partidas genéricas y específicas del árbol y la tabla de partidas seleccionadas queda vacía.
- **CA-009:** Dado que se busca por clave o palabra, cuando se ingresa al menos el tercer carácter, entonces se filtran en frontend las partidas específicas del árbol completo ya cargado, sin realizar otra consulta al backend.
- **CA-010:** Dado que se está capturando CUCoP, cuando se pulsa Atrás, entonces se vuelve a Datos Generales y se descartan los cambios sin guardar de CUCoP. Al regresar se muestran los tipos de participación y partidas previamente persistidos; si nunca se guardó CUCoP, se presenta la captura inicial.
- **CA-011:** Dado que existe al menos un tipo de participación y una partida específica seleccionados y los datos cumplen sus validaciones, cuando se pulsa Siguiente y el guardado concluye correctamente, entonces se guarda de forma atómica, se marca la sección COMPLETADA y se establece la continuación hacia Contactos.
- **CA-012:** Dado un error de validación o guardado, cuando se intenta avanzar, entonces se informa el problema, se permanece en CUCoP y no se declara la sección completa.
- **CA-013:** Dado una partida genérica, cuando el proveedor marca su casilla, entonces se seleccionan automáticamente todas sus partidas específicas y se muestran en la tabla de selección.
- **CA-014:** Dado el árbol CUCoP, cuando el proveedor interactúa con sus niveles, entonces solo puede editar las casillas de selección de partidas genéricas y específicas; capítulos y conceptos permiten navegar sin selección editable.

**Estructura de los catálogos:** [Catálogos de dominio](../domain/catalogs.md), apartado Catálogos CUCoP.

- **CA-015:** Dado que se ingresa a CUCoP, cuando se consulta el catálogo, entonces se reciben todos los nodos de sus cuatro niveles con sus relaciones en una sola respuesta, sin paginación; expandir nodos y buscar no generan nuevas peticiones de catálogo.

## Definiciones pendientes

- Tipos físicos y formatos y nombres físicos de referencias al padre en los catálogos existentes. El acceso está definido mediante adaptador JPA sobre la misma base de datos; la API devuelve el árbol completo y los IDs son enteros.

**Requerimientos relacionados:** RF-REG-08 y RF-REG-05.

**Historia relacionada:** [HU-REG-006 — Navegar y guardar secciones](US06-navegar-y-guardar-secciones.md).
