# ADR-0010: Servicio general de archivos con asociación inmediata y eliminación lógica

- **Estado:** Aceptado
- **Ámbito:** Backend
- **Fecha:** 2026-10-18
- **Actualización:** 2026-10-19
- **Specs relacionadas:** `architecture/architecture.md`; `architecture/domain-model.md`; ADR-0005. Aplicación actual: RF-REG-05, US10, INV-27 a INV-29.

## Contexto

Las secciones del registro requieren cargar archivos y asociarlos a sus datos de negocio.
Las secciones futuras podrán incorporar otros documentos. Se necesita un servicio reutilizable
que gestione contenido, metadatos, referencias y limpieza sin depender de un tipo documental.
La carga exitosa conserva y asocia el archivo inmediatamente; Eliminar desasocia y marca sus metadatos en una transacción independiente del guardado de sección. El contenido se elimina después mediante el proceso diario.

## Decisión

### Servicio general y límites

- El servicio de archivos es interno al backend y reutilizable por las secciones. Sus metadatos
  comparten base de datos y transacción con los registros que los referencian; no se introduce
  un microservicio ni otra base de datos.
- Cada carga devuelve un UUID propio del archivo, distinto del identificador del expediente.
  El registro de negocio conserva ese UUID, sin duplicar bytes, nombres físicos ni rutas.
- El servicio gestiona carga y asociación inmediatas, metadatos, descarga y eliminación lógica.
  Cada sección define formatos, cantidad, obligatoriedad y consecuencias sobre su progreso.
- Este ADR no habilita capturas de etapas futuras ni crea endpoints genéricos no especificados.
  No sustituye la conservación de evidencia de Firma Electrónica definida en RF-REG-01.

### Volumen, ruta y nombre físico

El contenido se almacena en un volumen persistente bajo:

```text
/repository/{year}/{month}/{day}/{rfc}/{uuid}.{extension}
```

- La fecha es la de carga, obtenida de `java.time.Clock` en UTC, con formato `yyyy/MM/dd`.
  El RFC corresponde al expediente identificado; esta organización no depende de la sección.
- La extensión corresponde al formato admitido y validado para el tipo de archivo.
  El nombre original se conserva como metadato y no construye directorios ni nombres físicos.
- El adaptador resuelve la ubicación desde su referencia registrada, sin aceptar rutas del cliente.
- El contenido no se almacena en disco efímero. Varias instancias deben compartir volumen.

### Metadatos

| Dato | Propósito |
| :--- | :--- |
| UUID del archivo | Referencia técnica opaca del contenido. |
| Propietario y contexto de expediente | Autorizar carga, consulta y eliminación del archivo asociado. |
| Nombre original | Mostrar y descargar con el nombre de carga. |
| Ruta relativa | Localizar el contenido sin exponer `/repository` al dominio ni al cliente. |
| Extensión y formato | Registrar el tipo validado según las reglas aplicables. |
| Tamaño en bytes | Conservar el tamaño efectivo del archivo. |
| Fecha y hora de carga | Calcular antigüedad desde la carga en UTC. |
| Marca de asociado y referencia al registro propietario | Acreditar la asociación persistida y distinguir residuos registrados no asociados. |

La estructura física de tablas y columnas se concretará al implementar persistencia.
La marca y la referencia deben permanecer coherentes: un archivo referenciado por un registro
no puede considerarse temporal eliminable. El dominio utiliza referencias opacas, sin rutas.

### Carga y asociación inmediatas

1. La operación de carga autoriza el expediente y valida el tipo, tamaño y cantidad de archivos.
2. Conserva el contenido y confirma metadatos y asociación por UUID en BD antes de informar éxito.
3. El archivo está disponible para mostrar y descargar al volver o reanudar, aunque no se pulse Siguiente.
4. Siguiente valida la disponibilidad del documento asociado; no recibe UUID para confirmar una
   carga ni vuelve a asociar el archivo. Cargar no completa por sí solo la sección.
5. Atrás descarta cambios de los campos del formulario, pero no revierte cargas o eliminaciones exitosas.

La transacción de BD no hace atómica la escritura en volumen. Si falla la asociación tras
crear contenido, compensar la carga y no informar éxito. Una compensación fallida se registra
para reconciliación; no se presenta un residuo como archivo asociado.
La autorización y la garantía de cantidad se sostienen también ante operaciones concurrentes.

### Descarga y eliminación lógica transaccional

- Descargar comprueba propiedad y asociación vigente y conserva el nombre original sin exponer rutas.
- Eliminar requiere advertencia de retirada irrecuperable desde la aplicación y confirmación explícita.
- El backend no borra ni mueve contenido físico durante esta operación. En una única transacción
  de BD retira la referencia del registro de negocio, marca el archivo `associated = false` en
  sus metadatos y actualiza el progreso aplicable. La fila de metadatos, UUID y ubicación física
  se conservan para que el proceso diario pueda localizar y eliminar el contenido.
- La marca y las referencias deben ser coherentes; un archivo con referencias vigentes nunca
  puede marcarse como no asociado. La aplicación valida propiedad y estado antes de desasociar.
- Solo tras confirmar la transacción se informa éxito y la interfaz retira el documento.
  Si falla, el rollback conserva referencia, marca y progreso previos; el contenido no se tocó.
- Después del éxito no se muestra ni descarga como archivo del expediente, aunque sus bytes
  permanezcan en volumen. No se ofrece restauración al usuario ni reasociación de ese UUID;
  si necesita el documento de nuevo debe cargarlo mediante la operación normal.
- La eliminación lógica no espera Siguiente y Atrás no la revierte.
- Para la cédula, el éxito deja Datos Generales PENDING sin modificar los demás datos ni CUCoP.
  Cargar otra no completa la sección hasta validar y guardar con Siguiente.

### Eliminación física mediante proceso diario

- Programar la ejecución todos los días a las **02:00**, en la zona horaria
  `America/Mexico_City`. La hora de programación es local; la antigüedad del archivo
  continúa calculándose con instantes UTC para exigir más de 24 horas transcurridas.

- El proceso diario consulta la tabla de metadatos y elimina archivos marcados como no asociados
  con más de 24 horas desde su carga. Con exactamente 24 horas aún no son candidatos. La fecha
  de carga no se reinicia al desasociar; un archivo antiguo puede eliminarse en la siguiente ejecución.
- Las cargas exitosas quedan asociadas inmediatamente y se excluyen. También se excluyen todos
  los archivos con referencias vigentes, aunque su marca fuera incoherente; registrar la anomalía.
- Comprobar de nuevo marca, referencias y antigüedad bajo coordinación de persistencia antes
  de eliminar. No deducir ausencia de asociación por nombre o ruta.
- El borrado físico ocurre solo en este proceso: primero elimina el contenido y después los
  metadatos del archivo no asociado. Si falla el borrado, conservar metadatos para reintentar.
  Si falta contenido por una ejecución previa interrumpida, completar la retirada de metadatos.
- El proceso es reintentable y registra fallos. La interrupción entre disco y BD no deja una
  referencia de negocio a contenido borrado, porque el archivo ya estaba desasociado.
- No borrar archivos sin metadatos tras reiniciar H2 ni reconstruir referencias desde rutas.
  Los residuos de carga con metadatos que acrediten ausencia de asociación siguen la misma regla.

### Validación por tipo de documento

El servicio no impone PDF, un tamaño único ni una cantidad de documentos para todas las secciones.
Cada tipo documental debe definir formatos, tamaño máximo, cantidad, obligatoriedad y condiciones
de asociación antes de habilitar su carga. La validación se realiza en servidor sobre extensión
y contenido; el nombre o MIME declarado por el cliente no bastan.

Los límites se aplican a bytes del archivo; el límite del multipart debe permitir encabezados y
separadores adicionales. Las respuestas de error se rigen por el contrato aplicable.
Las restricciones actuales de la cédula fiscal permanecen en RF-REG-05 y US10: PDF de máximo
5 MiB (5,242,880 bytes), extensión sin distinguir mayúsculas/minúsculas y una cédula asociada.
Estas restricciones no se trasladan automáticamente a documentos futuros.

## Alternativas consideradas

| Alternativa | A favor | En contra |
| :--- | :--- | :--- |
| Servicio interno general, volumen y metadatos compartidos | Reutiliza gestión y asocia archivos al completar cada carga. | Requiere coordinar contenido físico y metadatos ante fallos. |
| Servicio específico por documento | Aísla cada caso de uso. | Duplica almacenamiento, asociación y limpieza. |
| Servicio externo u object storage | Ofrece almacenamiento especializado. | Requiere infraestructura y coordinación diferentes; no es la opción elegida. |
| Contenido binario en base de datos | Contenido y asociación pueden compartir transacción. | No cumple el almacenamiento en volumen decidido. |

## Consecuencias y verificación

- `FileStoragePort` y `FileMetadataPort` pertenecen a `application.port.out` y ofrecen
  capacidades generales. Los casos de uso de sección imponen sus restricciones; los adaptadores
  gestionan contenido físico y metadatos conforme a arquitectura. No hay puertos por tipo documental.
- Verificar UUID, ruta, nombre original y autorización; carga asociada sin Siguiente y
  recuperación al regresar/reanudar; carga sin completar sección; eliminación exitosa y fallida,
  progreso local y persistido, máximo de archivos y concurrencia. Verificar límites de 24 horas,
  exclusión de asociados y reintentos de limpieza con reloj fijo.
- H2 en memoria pierde metadatos al reiniciar (ADR-0005). Los archivos sin metadatos recuperables
  quedan fuera de la limpieza diaria: no puede probarse que no estaban asociados. No se deduce
  su condición desde rutas, no se reconstruyen referencias ni se borran automáticamente por esa pérdida.
- Persistencia duradera para otros entornos, esquema físico y compensación de fallos de carga permanecen pendientes. La eliminación lógica se resuelve con rollback de BD y el borrado físico es reintentable. No se incorpora una papelera de usuario.
