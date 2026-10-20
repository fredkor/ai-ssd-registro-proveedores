# HU-REG-008 — Capturar Datos Generales

**Como** solicitante de registro como proveedor, **quiero** capturar mis datos generales,
**para** integrar mi expediente en la Plataforma de Compras de Fredkor Labs.

**Precondiciones:** identidad verificada mediante e.firma, políticas aceptadas y expediente EN_CAPTURA.

## Campos y condiciones definidos

| Campo | Condición |
| :--- | :--- |
| Régimen | Obligatorio y de solo lectura. Se calcula a partir de la longitud del RFC: 12 caracteres corresponden a PERSONA MORAL y 13 a PERSONA FÍSICA. |
| Usuario | Campo de texto de solo lectura, calculado como el RFC obtenido de la Firma Electrónica del solicitante convertido a minúsculas. Se mostrará desde la primera identificación exitosa del proveedor mediante la Firma Electrónica del solicitante. |
| RFC | Obligatorio y de solo lectura. Longitud de 12 caracteres para persona moral y 13 para persona física. |
| Código fiscal extranjero | Campo de texto editable, separado del RFC, visible y obligatorio únicamente para origen Extranjero. Máximo 20 caracteres. |
| Nombre completo / Razón social | Obligatorio, obtenido de la e.firma y de solo lectura. |
| Origen | Obligatorio: Nacional o Extranjero. Seleccionado por el proveedor. |
| Nacionalidad | Obligatoria. Nacional: Mexicana, de solo lectura. Extranjero: captura alfanumérica de hasta 250 caracteres. |
| País | Obligatorio para todos. Origen Nacional: México seleccionado y de solo lectura. Origen Extranjero: seleccionable del catálogo de países. |
| Titularidad jurídica | Obligatoria, seleccionada de catálogo. Para nacionales filtra por país y régimen; para extranjeros incluye las opciones del país o clave NOT_DEF, sin filtro por régimen. Orden ascendente por clave. |
| Sector y Subsector | Obligatorios únicamente para persona moral, seleccionados de catálogo. El subsector corresponde al sector elegido. |
| Giro | Obligatorio, seleccionado de catálogo. |
| Estratificación | Obligatoria: se selecciona exactamente una opción del catálogo, sin asociación ni filtro por régimen. |
| Actividad preponderante | Obligatoria, seleccionada de catálogo. |
| Número de trabajadores | Obligatorio, seleccionado de catálogo. |
| Ventas anuales | Obligatorias, seleccionadas de catálogo. |
| Página web | Opcional, máximo 2048 caracteres. Si se informa, debe ser URL absoluta con esquema http o https y host válido; no se agrega protocolo automáticamente. |

## Dependencias para completar la sección

- [HU-REG-009 — Capturar teléfonos del proveedor](US09-capturar-telefonos-del-proveedor.md): aplicable únicamente a persona moral.
- [HU-REG-010 — Gestionar cédula de identificación fiscal](US10-gestionar-cedula-fiscal.md): obligatoria para nacionales y extranjeros.
- Estas funcionalidades forman parte de Datos Generales; no agregan secciones al asistente.
- El avance hacia CUCoP requiere cumplir las validaciones de esta historia y de las dos historias relacionadas, según apliquen.

La normalización de textos se rige por
[ADR-0009](../adr/0009-shared-null-explicito-en-requests.md); sus resultados verificables
se mantienen en CA-025 a CA-027. Los filtros de selección se definen en
[Catálogos](../domain/catalogs.md).

## Criterios de aceptación

- **CA-001:** Dado que inicio o reanudo la captura del expediente, cuando se abre el asistente, entonces Datos Generales aparece como primera sección seleccionada y se muestran sus campos conforme al régimen y origen aplicables.
- **CA-002:** Dado que se muestran datos de identidad de solo lectura, cuando capturo mis datos generales, entonces no puedo modificar esos datos mediante la captura.
- **CA-003:** Dado un solicitante nacional, cuando selecciona su titularidad jurídica, entonces se incluyen únicamente los registros cuyo iso_country corresponde al país y cuyo régimen es PERSONA MORAL para régimen ID 1 o PERSONA FÍSICA para persona física, ordenados por clave.
- **CA-004:** Dado un solicitante persona física, cuando se muestran sus datos generales, entonces no se solicitan Sector ni Subsector.
- **CA-005:** Dado que se muestran los campos, cuando el usuario consulta el formulario, entonces ve la leyenda “Los campos marcados con asterisco son obligatorios” y el botón Siguiente permanece inactivo mientras falten campos obligatorios.
- **CA-006:** Dado que la sección cumple sus validaciones, incluidas las de teléfonos y cédula fiscal, cuando se pulsa Siguiente y el guardado concluye correctamente, entonces se conserva la información, se muestra “Exitoso. La información se ha capturado correctamente.” y se establece la continuación hacia CUCoP.
- **CA-007:** Dado un fallo de validación o persistencia, cuando se intenta guardar la sección, entonces se informa el problema, no se avanza y la sección no se declara completa.
- **CA-008:** Dado un campo de catálogo dinámico, cuando se valida la sección, entonces se verifica que la selección corresponda a un registro vigente cuando aplique.
- **CA-009:** Dado un solicitante de origen Nacional, cuando se muestra País, entonces México aparece seleccionado y no puede modificarse.
- **CA-010:** Dado un solicitante de origen Extranjero, cuando completa Datos Generales, entonces debe seleccionar un país del catálogo.
- **CA-011:** Dado un RFC de 12 caracteres, cuando se calcula el régimen, entonces se muestra PERSONA MORAL de solo lectura.
- **CA-012:** Dado un RFC de 13 caracteres, cuando se calcula el régimen, entonces se muestra PERSONA FÍSICA de solo lectura.
- **CA-013:** Dado que se informa Página web, cuando se valida la sección después del recorte de ADR-0009, entonces debe ser una URL absoluta de máximo 2048 caracteres, con esquema http o https y host válido. Se acepta https://empresa.com; se rechazan empresa.com sin protocolo, https:// sin host y esquemas como ftp o javascript. No se agrega protocolo automáticamente. El texto vacío se convierte a null y null es válido como ausencia; la propiedad website debe estar presente conforme a CA-024. No se requiere consultar DNS ni realizar una petición a la URL para validarla.
- **CA-014:** Dado un solicitante extranjero, cuando selecciona su titularidad jurídica, entonces se incluyen los registros cuyo iso_country corresponde al país seleccionado o cuya clave es NOT_DEF, sin filtrar por régimen y ordenados por clave. NOT_DEF se incluye también si existen opciones del país; si el resultado está vacío, se consultan los registros con clave NOT_DEF. Si tampoco existen, se mantiene la lista vacía, sin crear opciones. El backend valida la selección con las mismas reglas al guardar.
- **CA-015:** Dado un solicitante de origen Extranjero, cuando se presenta Datos Generales, entonces se muestra el campo editable Código fiscal extranjero y se exige su captura; para origen Nacional no se muestra.
- **CA-016:** Dado un RFC de longitud distinta de 12 o 13 caracteres, cuando se procesa, entonces se informa un error y se impide continuar.
- **CA-017:** Dado que se solicita el Origen, cuando el proveedor completa Datos Generales, entonces debe seleccionar Nacional o Extranjero.
- **CA-018:** Dado un código fiscal extranjero de más de 20 caracteres, cuando se valida Datos Generales, entonces se rechaza y no se permite avanzar.
- **CA-019:** Dado que el proveedor se identifica exitosamente mediante la Firma Electrónica del solicitante por primera vez, cuando se muestran sus datos, entonces Usuario contiene su RFC en minúsculas y es de solo lectura.
- **CA-020:** Dado un campo con selecciones dependientes, cuando cambia su valor, entonces se descartan las selecciones dependientes y se exige seleccionarlas nuevamente según el nuevo valor; los valores predeterminados de solo lectura conservan sus reglas específicas.

- **CA-021:** Dado un campo opcional enviado como null, cuando se guarda correctamente la sección completa, entonces se conserva sin valor, sin mantener automáticamente el valor anterior; si el campo es obligatorio por las condiciones aplicables, null se rechaza.

- **CA-022:** Dado que se carga Datos Generales, cuando se consultan sus catálogos, entonces se reciben completos salvo Subsector, que depende del Sector seleccionado. Estratificación se recibe completa y exige seleccionar exactamente una opción, sin asociación ni filtro por régimen.
- **CA-023:** Dado que cambia el Sector seleccionado, cuando se actualizan sus dependencias, entonces se descarta el Subsector anterior y se consultan las opciones del nuevo Sector antes de seleccionar nuevamente.

- **CA-024:** Dado un request de Datos Generales, cuando se envía, entonces incluye todas las propiedades definidas, también las de los teléfonos; los opcionales sin valor y los campos no aplicables se envían como null. Para persona física, sectorId, subsectorId y phones se envían como null. Omitir una propiedad o incluir una no definida produce VALIDATION_ERROR.

- **CA-025:** Dado un texto editable con espacios en blanco al inicio o al final, cuando se guarda Datos Generales, entonces se recortan antes de validar obligatoriedad, longitud y formato, conservando los espacios interiores.
- **CA-026:** Dado un texto editable vacío o compuesto únicamente por espacios en blanco, cuando se valida el guardado, entonces se rechaza con VALIDATION_ERROR si es obligatorio por las condiciones aplicables, o se guarda como null si es opcional, sin conservar el valor previo.
- **CA-027:** Dado el resultado PKI, el RFC o los valores extraídos del certificado, cuando se normalizan los textos editables, entonces esos valores de identidad y evidencia permanecen sin cambios.

**Requerimiento relacionado:** RF-REG-05.
