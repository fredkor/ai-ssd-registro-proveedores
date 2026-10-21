# Requerimientos funcionales — Registro de proveedores de Fredkor Labs

## Alcance

La primera etapa comprende aceptación del aviso, identificación mediante e.firma,
consulta de existencia, inicio o reanudación del expediente, Datos Generales y CUCoP.
Contactos es el punto de continuación; su captura, las secciones posteriores y la
finalización quedan fuera de esta etapa.

Los identificadores RF-REG-01 a RF-REG-08 se conservan para la trazabilidad con las
historias, el modelo de dominio y el contrato.

## RF-REG-01: Identificar al solicitante mediante e.firma

- Mostrar el componente externo de Firma Electrónica del solicitante y solicitar la
  aceptación del aviso conforme a RF-REG-03 antes de firmar.
- Solicitar la firma únicamente cuando el componente esté disponible y listo. Si no lo
  está, informar que deben cargarse los archivos de e.firma e impedir continuar.
- Aceptar únicamente una firma exitosa con RFC, nombre completo / razón social y los
  datos necesarios para verificarla. Error, cancelación o ausencia de resultado impiden
  continuar y crear un expediente.
- Exigir un RFC de 12 caracteres para persona moral o 13 para persona física. El RFC
  recibido debe corresponder al titular del certificado utilizado para verificar la firma;
  el RFC del apoderado no lo sustituye.
- Obtener el nombre completo / razón social de la identidad desde ese mismo certificado.
  El nombre recibido del componente se conserva como evidencia; si difiere, prevalece
  el extraído del certificado para identificar al solicitante.
- Verificar el sello y rechazar la identificación si la comprobación falla o no puede
  realizarse. La vigencia del certificado y su validación OCSP corresponden al componente
  externo; su implementación interna queda fuera del registro.
- Conservar íntegramente cada resultado exitoso aceptado, sin almacenar la llave privada
  ni su contraseña. La firma inicial no finaliza el registro.
- Habilitar las operaciones posteriores solo con una identificación aceptada. Su acceso
  dura 60 minutos. Un mismo RFC puede mantener varios accesos vigentes: una nueva
  identificación exitosa no invalida los anteriores. Un intento fallido no afecta los vigentes.

La integración y sus criterios se detallan en [US01](../user-stories/US01-identificacion-mediante-efirma.md).
La extracción de identidad y verificación se definen en
[ADR-0016](../adr/0016-backend-verificacion-firma-sha256-rsa.md), y el contexto de acceso en
[ADR-0015](../adr/0015-shared-token-de-registro.md).

## RF-REG-02: Verificar la existencia del registro

- Consultar el expediente asociado al RFC de la identificación aceptada.
- Si no existe, permitir iniciar uno nuevo con el aviso previamente aceptado.
- Si está EN_CAPTURA, ofrecer reanudar el mismo expediente conforme a RF-REG-06.
- Si está REGISTRADO, informar que ya existe un registro e impedir una nueva alta.
- Ante un error de consulta, informar el fallo e impedir continuar como si no existiera.
- Conservar el RFC como identidad de negocio, sin permitir su modificación durante la captura.

## RF-REG-03: Presentar y registrar aceptación del aviso

- Mostrar la [Política de Aviso](registration-notice.md), de texto fijo y versión `1.0`,
  antes de cada nueva identificación, incluida la necesaria para reanudar.
- Exigir aceptación expresa para solicitar la firma. Cancelar cierra el diálogo sin
  registrar aceptación, solicitar firma ni crear expediente.
- Registrar versión y fecha/hora de aceptación. Si falla, impedir la solicitud de firma.
- La aceptación por sí sola no crea un expediente: también se requiere identificación
  exitosa y comprobar la inexistencia del registro.
- Asociar al expediente únicamente la última aceptación confirmada, sin historial de
  aceptaciones. Al reanudar correctamente, sustituir la anterior; ante fallo conservarla.
- Conservar las evidencias históricas de firma independientemente de esta sustitución.

## RF-REG-04: Iniciar expediente de registro

- Crear un expediente únicamente con aviso aceptado, identificación exitosa e inexistencia
  comprobada para el RFC.
- Asignar un identificador interno único, estado EN_CAPTURA y la identidad verificada.
- Abrir Datos Generales como primera sección.
- Impedir duplicados por RFC, incluso ante solicitudes concurrentes. Si otra solicitud ya
  creó el expediente, conducir al flujo correspondiente a su estado.
- Ante fallo, informar el error sin presentar el expediente como creado.

La identidad técnica se define en [ADR-0003](../adr/0003-shared-identificador-uuid.md).

## RF-REG-05: Capturar, validar y guardar el registro por secciones

### Asistente y guardado

- Presentar Datos Generales, CUCoP, Contactos, Domicilio fiscal, Cobertura y Firma Electrónica,
  en ese orden. En la primera etapa solo se captura Datos Generales y CUCoP.
- Navegar mediante Atrás y Siguiente, sin acceso directo desde los títulos de las secciones.
- Siguiente valida y guarda conjuntamente datos y progreso antes de avanzar. Solo un
  guardado exitoso marca la sección COMPLETADA.
- Ante error de validación o guardado, informar el problema, permanecer en la sección y
  conservar los datos y el progreso previamente guardados.
- Una sección guardada permanece COMPLETADA al volver o editarla. Un formulario local
  inválido impide avanzar, pero no cambia el progreso persistido por sí solo.
- Atrás descarta cambios de campos sin guardar. Al regresar se muestran los datos persistidos
  y los archivos asociados actuales; las cargas y eliminaciones exitosas son independientes de Siguiente.
- Los cambios en Datos Generales afectan únicamente a esa sección; conservan intactos
  los tipos de participación, partidas y progreso de CUCoP, sin marcarlo para revisión.

### Datos Generales

| Campo | Regla |
| :--- | :--- |
| RFC | Obligatorio, de solo lectura y validado conforme a RF-REG-01. |
| Nombre completo / Razón social | Obligatorio y de solo lectura, extraído del certificado verificado. |
| Régimen | De solo lectura: RFC de 12 caracteres corresponde a persona moral; de 13, a persona física. |
| Usuario | RFC en minúsculas, de solo lectura, visible desde la primera identificación exitosa. |
| Origen | Obligatorio: Nacional o Extranjero, seleccionado por el proveedor. |
| Código fiscal extranjero | Separado del RFC; visible y obligatorio solo para Extranjero, máximo 20 caracteres. |
| Nacionalidad | Obligatoria. Nacional: Mexicana y de solo lectura. Extranjero: texto alfanumérico de máximo 250 caracteres. |
| País | Obligatorio. Nacional: México y de solo lectura. Extranjero: selección del catálogo. |
| Titularidad jurídica | Obligatoria. Nacional: opciones del país y régimen. Extranjero: opciones del país o de clave NOT_DEF, sin filtro por régimen. |
| Sector y Subsector | Obligatorios solo para persona moral; el subsector debe pertenecer al sector seleccionado. |
| Giro | Una selección obligatoria del catálogo. |
| Estratificación | Una selección obligatoria del catálogo, sin asociación ni filtro por régimen. |
| Actividad preponderante | Una selección obligatoria del catálogo. |
| Número de trabajadores | Una selección obligatoria del catálogo. |
| Ventas anuales | Una selección obligatoria del catálogo. |
| Página web | Opcional; URL absoluta con esquema HTTP o HTTPS y host válido, máximo 2048 caracteres después del recorte. No se agrega protocolo automáticamente. |

- Al cambiar un campo del que dependen otras selecciones, descartarlas y exigir una nueva
  selección compatible. Los valores predeterminados de solo lectura siguen sus reglas propias.
- Recortar espacios en blanco al inicio y final de textos editables antes de validar,
  conservando los interiores. Un obligatorio vacío se rechaza; un opcional vacío queda sin
  valor y no conserva el dato anterior. La identidad y la evidencia de firma no se modifican.
- Validar obligatoriedad, formatos, límites y pertenencia a los catálogos, incluida su
  vigencia cuando aplique: los registros inactivos no se muestran y se rechazan al guardar. No comprobar disponibilidad en red de la página web.
- Cargar completos los catálogos de Datos Generales, salvo Subsector, que se consulta según
  el Sector seleccionado. Titularidad Jurídica se filtra según sus relaciones y reglas.

El algoritmo de Titularidad Jurídica, su orden por clave y el tratamiento de `NOT_DEF`
se definen en [Catálogos](../domain/catalogs.md). Los criterios de Datos Generales se
encuentran en [US08](../user-stories/US08-capturar-datos-generales.md).

### Teléfonos

- Solicitar teléfonos únicamente a persona moral y exigir al menos uno válido para avanzar.
- Cada número debe contener 10 dígitos; la extensión es opcional y admite hasta 5 caracteres.
- Agregar inserta una fila con Número y Extensión vacíos, editables en la tabla. No se
  requiere captura previa ni botón Cancelar.
- Cada fila dispone de una acción Eliminar, incluso si está vacía o ya estaba guardada.
  La retirada es local y se confirma únicamente al guardar Datos Generales con Siguiente.
- Se permite quitar la última fila, pero no completar la sección sin al menos un teléfono
  válido. Todas las filas restantes deben cumplir sus validaciones.
- Salir o volver con Atrás sin guardar descarta los cambios locales; reanudar recupera
  los teléfonos guardados. Un fallo de guardado conserva la colección anterior.

Los criterios se detallan en [US09](../user-stories/US09-capturar-telefonos-del-proveedor.md).

### Cédula de identificación fiscal

- Exigir una cédula fiscal para nacionales y extranjeros al completar Datos Generales.
- Admitir un único PDF de máximo 5 MiB (5,242,880 bytes), inclusive. La extensión `.pdf`
  no distingue mayúsculas/minúsculas y el contenido debe corresponder a PDF.
- Al completar correctamente la carga, conservar el archivo y su asociación inmediatamente,
  mostrar el documento y habilitar Descargar y Eliminar, sin esperar Siguiente.
- Al volver o reanudar, mostrar el archivo asociado aunque los demás datos de la sección
  no se hayan guardado. Atrás no revierte la carga ni una eliminación exitosa.
- Antes de eliminar, advertir que el archivo se eliminará y no podrá recuperarse y pedir confirmación.
- Eliminar desasocia el documento y marca sus metadatos como no asociados en una transacción
  junto con el progreso de la sección, sin tocar el archivo físico. Tras confirmar, retirar
  el documento de la tabla; ante fallo, conservar referencia, marca y progreso anteriores.
- Los metadatos se conservan para la eliminación física posterior mediante el proceso diario.
  El documento desasociado deja de estar disponible desde el expediente y no se restaura al volver.
- Eliminar exitosamente deja Datos Generales PENDING sin borrar sus demás datos ni alterar CUCoP.
  Cargar otra cédula no completa la sección: se requiere validar y guardar con Siguiente.
- Un fallo de eliminación conserva el progreso anterior. Sin cédula no se permite completar la sección.
- Descargar conserva el nombre original.

Los criterios se detallan en [US10](../user-stories/US10-gestionar-cedula-fiscal.md).
La gestión general de archivos, asociación y limpieza se define en
[ADR-0010](../adr/0010-backend-almacenamiento-documental-en-volumen.md).

### Validación y avance

- Mostrar la leyenda “Los campos marcados con asterisco son obligatorios”.
- Mantener Siguiente inactivo mientras no se cumplan las condiciones para completar la sección.
- Validar conjuntamente Datos Generales, teléfonos y cédula fiscal antes de guardar y avanzar.
- Al guardar correctamente, mostrar “Exitoso. La información se ha capturado correctamente.”
  y continuar hacia CUCoP.

La representación de campos ausentes, opcionales y no aplicables, y las reglas de entrada
se definen en [ADR-0009](../adr/0009-shared-null-explicito-en-requests.md) y en el
[contrato API](../api/supplier-registration-openapi.yaml).

## RF-REG-06: Reanudar un expediente incompleto

- Exigir una nueva identificación aceptada conforme a RF-REG-01 y RF-REG-03.
- Permitir reanudar únicamente el expediente EN_CAPTURA del mismo RFC, sin crear otro.
  Denegar acceso a expedientes ajenos y reanudación de un expediente REGISTRADO.
- Recuperar datos, documentos confirmados y progreso; abrir siempre Datos Generales sin
  exigir recaptura de información guardada.
- Actualizar el nombre completo / razón social desde el nuevo certificado verificado,
  manteniendo el RFC y el identificador del expediente.
- Conservar íntegramente las evidencias inicial y posteriores en el historial del expediente,
  sin sobrescribirlas. Sustituir la aceptación del aviso conforme a RF-REG-03.
- Confirmar conjuntamente el cambio de nombre, el vínculo de evidencia y la última aceptación,
  sin alterar datos capturados, documentos ni progreso, incluido CUCoP.
- Ante fallo, informar el problema y conservar el expediente sin cambios parciales.
  La evidencia aceptada durante la identificación no se elimina por ese fallo.
- La identificación por sí sola no actualiza el expediente; los cambios se confirman al reanudar.

## RF-REG-07: Finalizar el registro

**Alcance:** etapa posterior; fuera de la primera etapa de implementación.

- Exigir que las secciones obligatorias estén completas, válidas y consistentes.
  Si no lo están, impedir finalizar e indicar qué debe corregirse.
- Al finalizar, asignar la clave del proveedor, cambiar a REGISTRADO y registrar fecha/hora.
- No conservar resultados parciales ni generar registros o claves duplicados ante reintentos.
- Mostrar una confirmación de registro exitoso.
- Definir las reglas de Firma Electrónica final y generación de la clave antes de implementar.

## RF-REG-08: Capturar tipo de participación y partidas CUCoP

- Exigir Datos Generales guardados correctamente para acceder a CUCoP.
- Seleccionar al menos un tipo de participación, permitiendo ambos: proveedor de adquisiciones,
  arrendamientos y servicios; contratista de obra pública o servicios relacionados con las mismas.
- Mostrar “Con relación al catálogo CUCoP, seleccione su objeto social”.
- Presentar la jerarquía Capítulo → Concepto → Partida genérica → Partida específica.
  Solo genéricas y específicas tienen casillas editables; capítulos y conceptos permiten navegar.
- Toda genérica tiene específicas. Seleccionar una genérica selecciona todas sus específicas;
  deseleccionarla las desmarca. Permitir seleccionar múltiples específicas y exigir al menos una.
- Mostrar las específicas seleccionadas en una tabla con columnas CUCoP y Descripción,
  buscador general y 10 registros por página.
- Cargar el árbol completo con sus cuatro niveles y relaciones, sin paginación ni nuevas
  consultas al expandir o buscar. Buscar por clave o palabra a partir del tercer carácter.
- Deshacer selección limpia el árbol y la tabla.
- Mantener Siguiente inactivo si falta participación o específicas. Al guardar válidamente,
  marcar la sección COMPLETADA y continuar hacia Contactos; ante fallo no avanzar.
- Atrás vuelve a Datos Generales y descarta los cambios sin guardar conforme a RF-REG-05.

La estructura y relaciones se definen en [Catálogos](../domain/catalogs.md), y los criterios
en [US11](../user-stories/US11-capturar-participacion-y-partidas-cucop.md).

## Referencias transversales y límites

- El RFC es único por expediente e inmutable. El estado del expediente se distingue del
  progreso de cada sección; sus transiciones se definen en el
  [ciclo de vida](../domain/supplier-registration-lifecycle.md).
- Los errores y su representación se definen en
  [ADR-0004](../adr/0004-shared-codigos-de-error.md), la
  [guía de API](../api/API-GUIDELINES.md) y el [contrato](../api/supplier-registration-openapi.yaml).
- El esquema físico de catálogos está en [esquema de catálogos aprobado](../architecture/catalog-database-proposal.md).
  Su consulta en la misma base de datos ya está definida; no es una decisión pendiente.
- Las secciones posteriores y la generación de la clave del proveedor requieren especificaciones
  propias. No se incorporan caducidad, abandono, reapertura ni nuevos estados del expediente
  sin reglas expresamente definidas.
