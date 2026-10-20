# Requerimientos no funcionales — Registro de proveedores de Fredkor Labs

## Alcance

Estos requerimientos aplican a la primera etapa del registro: aviso, identificación mediante
Firma Electrónica del solicitante, inicio y reanudación, Datos Generales y CUCoP.
Complementan los [requerimientos funcionales](functional.md); los ADR documentan las decisiones
técnicas que los soportan. No declaran implementación ni verificaciones ya ejecutadas.

Los valores del documento de ejemplo no constituyen compromisos del registro. Los objetivos
cuantitativos que todavía no se han acordado se identifican al final, sin asumir tiempos de
respuesta, disponibilidad o capacidades no verificados.

## RNF-01: Seguridad de acceso y comunicación

- Las comunicaciones entre navegador y backend deben protegerse mediante HTTPS en los
  entornos desplegados; el HTTP local de desarrollo no define la configuración productiva.
- Las operaciones protegidas deben validar la firma y los claims del JWT y comprobar su
  contexto vigente en la misma base de datos, conforme a
  [ADR-0015](../adr/0015-shared-token-de-registro.md).
- La validez criptográfica no sustituye la autorización: solo se permite acceder a expedientes
  y archivos asociados al RFC de la identificación aceptada.
- Las claves de firma del backend y las credenciales de infraestructura deben mantenerse
  fuera del código fuente y no entregarse al frontend.
- El alcance protegido y las excepciones públicas deben coincidir con el
  [contrato OpenAPI](../api/supplier-registration-openapi.yaml).

**Verificación prevista:** pruebas de acceso sin JWT, alterado, vencido o de sesión sustituida;
acceso a expediente ajeno; revisión de configuración HTTPS y gestión de secretos.

## RNF-02: Protección e integridad de la información

- No almacenar ni registrar en logs la llave privada del solicitante ni su contraseña.
- Conservar íntegramente los resultados de firma aceptados y las evidencias históricas
  conforme a RF-REG-01 y RF-REG-06. No normalizar ni sobrescribir su contenido original.
- No incluir JWT completos, claves, certificados completos ni evidencia íntegra en logs,
  mensajes de error o respuestas que no requieran esos datos.
- No exponer rutas físicas del volumen en identificadores públicos o respuestas de error.
- La verificación de firma y extracción de identidad deben seguir
  [ADR-0016](../adr/0016-backend-verificacion-firma-sha256-rsa.md).

**Verificación prevista:** pruebas del contenido persistido y revisión de logs, respuestas y
manejo de errores, incluida la separación entre evidencia original e identidad verificada.

## RNF-03: Consistencia transaccional y concurrencia

- El guardado de una sección debe confirmar sus datos y progreso en una única transacción
  de base de datos; un fallo debe conservar los valores previamente persistidos.
- La unicidad del expediente por RFC, el máximo de una cédula asociada y el único contexto
  JWT vigente por RFC deben sostenerse también ante solicitudes concurrentes.
- La eliminación lógica de un archivo debe confirmar conjuntamente la retirada de su
  referencia de negocio, la marca de no asociado y el progreso aplicable. Un fallo debe
  revertir esos cambios sin tocar el contenido físico.
- La sustitución de nombre, aceptación y vínculo histórico al reanudar debe evitar estados
  parciales, conforme a RF-REG-06.

**Verificación prevista:** pruebas de rollback, fallos de persistencia y operaciones concurrentes
sobre el mismo RFC, expediente o archivo; comprobar que CUCoP permanece intacto cuando corresponde.

## RNF-04: Almacenamiento y ciclo de vida de archivos

- Usar el servicio general de archivos definido en
  [ADR-0010](../adr/0010-backend-almacenamiento-documental-en-volumen.md): contenido en volumen
  persistente y metadatos en la misma base de datos, referenciados mediante UUID.
- Una carga exitosa debe dejar contenido y asociación disponibles al volver o reanudar,
  independientemente de Siguiente. No informar éxito si la operación queda incoherente.
- Eliminar desde la aplicación solo debe modificar la base de datos transaccionalmente;
  el proceso diario realiza el borrado físico a partir de metadatos no asociados elegibles.
- La limpieza debe excluir archivos asociados, comprobar nuevamente la ausencia de referencias
  y permitir reintentar fallos sin perder los metadatos necesarios para localizar el archivo.
- No deducir ausencia de asociación desde nombres o rutas ni borrar automáticamente archivos
  cuyos metadatos se perdieron al reiniciar H2.
- Los límites y formatos pertenecen a cada tipo documental. Para la cédula rigen RF-REG-05
  y US10: un PDF de hasta 5 MiB (5,242,880 bytes), con validación de extensión y contenido.
  No trasladar esa restricción a los archivos de etapas futuras sin especificación.

**Verificación prevista:** pruebas de carga y descarga, desasociación sin acceso al disco,
limpieza con reloj fijo en el límite de 24 horas, exclusión de asociados y recuperación tras fallos.

## RNF-05: Persistencia y recuperación

- Los datos guardados y archivos asociados deben poder recuperarse al salir y reanudar
  mientras la instancia y su almacenamiento sigan disponibles.
- H2 en memoria se acepta únicamente como persistencia de pruebas conforme a
  [ADR-0005](../adr/0005-backend-base-de-datos-h2.md). Su reinicio pierde datos y referencias;
  los archivos del volumen no reconstruyen por sí mismos el expediente.
- No presentar las capacidades de H2 de pruebas como garantía de durabilidad de un entorno
  productivo. Antes de ese despliegue deben definirse persistencia duradera, copias de seguridad
  y restauración, con sus objetivos de recuperación.

**Verificación prevista:** pruebas de reanudación con datos y documentos guardados y de reinicio
del entorno de pruebas; para producción, verificar el procedimiento de restauración cuando se defina.

## RNF-06: Eficiencia de consultas y uso de recursos

- Cargar CUCoP completo en una sola petición de API; expandir y buscar en el árbol no deben
  generar peticiones adicionales. Esto no exige resolver toda la lectura con una sola consulta SQL.
- Cargar completos los catálogos de Datos Generales, salvo Subsector, que depende del Sector;
  evitar consultas repetidas por cada nodo o fila al reconstruir sus relaciones.
- Aplicar los límites de carga en servidor. El límite de la petición multipart debe contemplar
  su envoltura sin alterar el tamaño máximo permitido para el archivo.
- No exigir llamadas de red para comprobar la disponibilidad de la Página web capturada.
- Medir rendimiento con escenarios y volúmenes representativos antes de fijar umbrales
  de latencia y concurrencia. No se adopta el límite de 300 ms del ejemplo como requisito aprobado.

**Verificación prevista:** inspección de peticiones de catálogo, consultas de persistencia,
comportamiento de búsqueda local y pruebas de rendimiento una vez acordados sus objetivos.

## RNF-07: Mantenibilidad y separación de responsabilidades

- Respetar la [arquitectura hexagonal](../architecture/architecture.md): dominio en Java puro,
  puertos en aplicación y adaptadores separados para HTTP, persistencia, catálogos, firma y archivos.
- Mantener independientes los modelos de dominio, DTO HTTP, entidades JPA y tipos visuales.
- Reutilizar el servicio general de archivos y las capacidades de catálogo, sin duplicar
  infraestructura por sección o introducir servicios externos no acordados.
- Usar el [stack aprobado](../architecture/tech-stack.md): Java 25 y Spring Boot 4.1.x para backend;
  Angular 21, PrimeNG y Sakai para frontend. Las versiones exactas se fijan en los archivos de build.
- Incorporar o cambiar tecnologías mediante ADR, manteniendo documentación en castellano
  y nombres técnicos en inglés según [convenciones](../conventions.md).

**Verificación prevista:** revisión de dependencias entre capas, configuración de build,
modelos por frontera y correspondencia con ADR aceptados.

## RNF-08: Verificabilidad y trazabilidad

- Mantener sincronizados RF, HU/CA, reglas de dominio, ADR, contrato e implementación conforme a SDD.
- Generar interfaces y DTO web desde el contrato una vez validado y habilitado; no editar
  fuentes generadas ni mantener modelos web manuales equivalentes.
- Probar reglas puras sin infraestructura y coordinación con dobles de puertos; verificar
  persistencia, archivos y firma en pruebas de sus adaptadores.
- Obtener la hora mediante Clock en aplicación y usar UTC y relojes fijos para comprobar
  vencimiento JWT y antigüedad de archivos sin depender de esperas reales.
- Distinguir comprobaciones documentales, ensayo de generación, compilación y pruebas de
  comportamiento. Un resultado correcto del lint no acredita que el sistema esté implementado.

**Verificación prevista:** revisión de [trazabilidad](../traceability.md), ejecución del validador
y pruebas pertinentes, con resultados y limitaciones explícitos.

## RNF-09: Diagnóstico de fallos

- Presentar errores de API con el formato común de ADR-0004 y ADR-0007, distinguiendo
  rechazos de validación, acceso, conflictos y fallos técnicos.
- Registrar los fallos técnicos de persistencia, firma, carga y limpieza con información
  suficiente para localizar la operación afectada, respetando RNF-02.
- Una consulta fallida no debe interpretarse como inexistencia de expediente o autorización válida.
- Las operaciones programadas deben registrar fallos y permitir reintentar el trabajo pendiente;
  no marcar un borrado físico como concluido si no terminó su tratamiento.

**Verificación prevista:** inyección de fallos en puertos y revisión de respuestas, logs y
estado persistido después de errores y reintentos.

## RNF-10: Claridad de interacción

- Diferenciar carga completada, guardado de sección, eliminación lógica y error; los mensajes
  no deben atribuir a una operación resultados que todavía no ocurrieron.
- Mantener los datos de identidad de solo lectura y comunicar los errores que impiden avanzar.
- No retirar un archivo de la tabla como eliminado antes de confirmar su transacción;
  ante fallo, conservar la representación del documento asociado.
- Al volver, mostrar datos guardados y archivos actualmente asociados según las reglas de Atrás,
  sin revertir operaciones documentales exitosas.

**Verificación prevista:** pruebas de interfaz de navegación, validación, carga, confirmación
de eliminación y tratamiento de errores conforme a los CA aplicables.

## Objetivos cuantitativos pendientes

Antes de comprometer un despliegue operativo se deben acordar y documentar:

| Tema | Definición pendiente |
| :--- | :--- |
| Rendimiento | Latencia objetivo y percentil por operación, volumen de catálogos y expedientes, concurrencia y condiciones de medición. |
| Disponibilidad | Porcentaje objetivo, ventana de servicio, exclusiones y forma de medición. |
| Recuperación | Pérdida de datos máxima admisible, tiempo de recuperación y política de respaldo/restauración. |
| Capacidad y operación | Volumen de archivos esperado, crecimiento, capacidad del repositorio, retención de logs y horario del proceso diario. |
| Entorno cliente | Matriz de navegadores y dispositivos soportados y objetivo de accesibilidad verificable. |

Estas precisiones no habilitan funciones de etapas posteriores ni modifican las reglas
funcionales o los límites documentales ya acordados.
