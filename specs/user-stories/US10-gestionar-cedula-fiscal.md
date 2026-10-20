# HU-REG-010 — Gestionar cédula de identificación fiscal

**Como** solicitante de registro como proveedor, **quiero** cargar y consultar mi cédula de identificación fiscal y poder eliminarla,
**para** integrar el documento fiscal de mi expediente en la Plataforma de Compras de Fredkor Labs.

**Precondiciones:** identidad verificada mediante e.firma, políticas aceptadas y expediente EN_CAPTURA.

## Alcance y reglas de referencia

La cédula pertenece a Datos Generales y es obligatoria para nacionales y extranjeros.
La carga guarda y asocia inmediatamente un PDF de máximo 5 MiB (5,242,880 bytes).
Eliminar requiere confirmación y desasocia el documento en una transacción de BD, marcando sus metadatos como no asociados y actualizando progreso; no toca el contenido físico. Un fallo revierte esos cambios. El proceso diario elimina los archivos no asociados elegibles.
Estas operaciones no esperan Siguiente ni se revierten con Atrás. La cédula es obligatoria
para completar Datos Generales; una eliminación exitosa deja esa sección PENDING y CUCoP intacto.

Las reglas técnicas de carga, asociación, limpieza y fallos pertenecen a
[ADR-0010](../adr/0010-backend-almacenamiento-documental-en-volumen.md).
El contrato de errores corresponde a [ADR-0004](../adr/0004-shared-codigos-de-error.md).
Los criterios siguientes conservan los casos verificables, incluidos los límites exactos.

## Criterios de aceptación

- **CA-001:** Dado un PDF válido dentro del límite, cuando la carga concluye correctamente, entonces se guarda y asocia al expediente, se devuelve su UUID, se oculta el cargador y se muestra con Descargar y Eliminar sin esperar Siguiente.
- **CA-002:** Dado un archivo con extensión distinta de .pdf (comparada sin distinguir mayúsculas/minúsculas), contenido que no corresponde a PDF o tamaño superior a 5,242,880 bytes, cuando se intenta cargar, entonces se rechaza; el tamaño excedido produce HTTP 413 DOCUMENT_TOO_LARGE y la extensión o contenido no permitido produce HTTP 415 DOCUMENT_FORMAT_NOT_SUPPORTED.
- **CA-003:** Dado un solicitante nacional o extranjero, cuando completa Datos Generales, entonces debe adjuntar su cédula de identificación fiscal en PDF de máximo 5 MiB (5,242,880 bytes) para poder avanzar.
- **CA-004:** Dado que ya existe una cédula de identificación fiscal adjunta, cuando se intenta agregar otro documento, entonces no se permite conservar más de un documento de cédula en el expediente.
- **CA-005:** Dado que la cédula se cargó exitosamente, cuando el proveedor vuelve con Atrás, sale o reanuda sin haber pulsado Siguiente, entonces el archivo asociado se conserva y está disponible para mostrar y descargar.
- **CA-006:** Dado un documento asociado, cuando se solicita Eliminar, entonces se advierte la retirada irrecuperable desde la aplicación y se pide confirmación. Cancelar no cambia nada. Al confirmar, el backend retira la referencia del expediente, marca associated=false en los metadatos y actualiza el progreso en una sola transacción, sin borrar ni mover contenido. Solo tras commit se informa éxito y se retira la fila; un fallo conserva referencia, marca y progreso previos.

**Historia relacionada:** [HU-REG-008 — Capturar Datos Generales](US08-capturar-datos-generales.md).

- **CA-007:** Dado que Datos Generales está COMPLETADA, cuando se elimina exitosamente su cédula, entonces queda PENDING, se conservan sus demás datos y CUCoP permanece intacto. Cargar otra cédula no completa la sección hasta validar y guardar nuevamente con Siguiente. Un fallo de eliminación conserva el progreso anterior.

- **CA-008:** Dado un PDF válido con extensión .pdf o una variante de mayúsculas/minúsculas, cuando su tamaño es exactamente 5,242,880 bytes, entonces no se rechaza por tamaño; si contiene un byte adicional, se rechaza con HTTP 413 DOCUMENT_TOO_LARGE. Las demás condiciones de carga continúan aplicándose.
- **CA-009:** Dado un archivo cuyo contenido no corresponde a un PDF aunque su extensión sea .pdf y declare application/pdf, cuando se intenta cargar, entonces se rechaza con HTTP 415 DOCUMENT_FORMAT_NOT_SUPPORTED y no se conserva como carga exitosa.

- **CA-010:** Dado un archivo asociado por una carga exitosa, cuando se guarda Datos Generales con Siguiente, entonces se valida su presencia desde la asociación del expediente sin recibir un UUID editable para asociarlo. El guardado confirma datos y progreso; si falla, el archivo previamente cargado sigue asociado y disponible.

- **CA-011:** Dado un documento eliminado lógicamente, cuando se vuelve o reanuda, entonces no se muestra ni permite descargar desde el expediente; sus metadatos conservan UUID y ubicación para que el proceso diario elimine el contenido cuando esté no asociado y tenga más de 24 horas desde la carga. Los archivos asociados nunca se eliminan por antigüedad.

**Requerimiento relacionado:** RF-REG-05.
