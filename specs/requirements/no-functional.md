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
- La unicidad del expediente por RFC y el máximo de una cédula asociada deben sostenerse
  también ante solicitudes concurrentes, incluidas las de varias sesiones del mismo RFC.
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
- Ejecutar la limpieza diaria a las **02:00**, hora de Ciudad de México
  (`America/Mexico_City`), únicamente para archivos no asociados con más de 24 horas
  desde la carga, conforme a ADR-0010.
- La limpieza debe excluir archivos asociados, comprobar nuevamente la ausencia de referencias
  y permitir reintentar fallos sin perder los metadatos necesarios para localizar el archivo.
- No deducir ausencia de asociación desde nombres o rutas ni borrar automáticamente archivos
  sin metadatos.
- Los límites y formatos pertenecen a cada tipo documental. Para la cédula rigen RF-REG-05
  y US10: un PDF de hasta 5 MiB (5,242,880 bytes), con validación de extensión y contenido.
  No trasladar esa restricción a los archivos de etapas futuras sin especificación.

**Verificación prevista:** pruebas de carga y descarga, desasociación sin acceso al disco,
limpieza con reloj fijo en el límite de 24 horas, exclusión de asociados y recuperación tras fallos.

## RNF-05: Persistencia y recuperación

- Los datos guardados y archivos asociados deben poder recuperarse al salir y reanudar
  mientras la instancia y su almacenamiento sigan disponibles.
- La persistencia es PostgreSQL 17 con Flyway en todos los entornos (ADR-0017). Las pruebas
  automatizadas usan una base desechable que empieza vacía en cada ejecución (ADR-0018).
  Los archivos del volumen no reconstruyen por sí mismos el expediente.
- La infraestructura de respaldo debe definirse antes del despliegue.

### Respaldo y recuperación de producción

- Realizar respaldos diarios de la base de datos y del repositorio de archivos, manteniendo
  un punto de recuperación consistente entre contenido, metadatos y referencias del expediente.
  Copiar ambos por separado sin comprobar su consistencia no acredita este requisito.
- **RPO máximo: 24 horas.** Ante un incidente, la pérdida de información no debe superar
  las 24 horas anteriores al fallo. La frecuencia y el seguimiento de respaldos deben sostener
  ese objetivo; un respaldo diario programado que falle no constituye por sí solo cumplimiento.
- **RTO máximo: 4 horas.** Desde la interrupción por el incidente hasta restablecer el servicio
  operativo, incluyendo restauración y comprobación de integridad, no deben transcurrir más de 4 horas.
- Restaurar conjuntamente BD y archivos y comprobar que los documentos referenciados están
  disponibles y que datos y progreso permiten continuar los expedientes recuperados.
- Verificar estos objetivos mediante ejercicios de restauración antes de comprometer el
  servicio productivo. Los objetivos están aprobados; no se declara que la infraestructura
  ya permita cumplirlos ni que se hayan ejecutado esos ejercicios.
- Conservar los respaldos diarios durante **30 días desde su creación**. La depuración de
  respaldos vencidos no debe borrar ni modificar datos o archivos del sistema operativo.
- La tecnología, ubicación, protección, horario de respaldos y el procedimiento
  operativo de recuperación quedan por concretar. No se presume un proveedor de almacenamiento
  ni se extiende la ventana de mantenimiento para justificar un incidente.

**Verificación prevista:** reanudación y reinicio en pruebas; para producción, restauración
coordinada desde un respaldo con medición del punto recuperado y del tiempo hasta servicio operativo,
verificando RPO de hasta 24 horas, RTO de hasta 4 horas y retención de respaldos de 30 días.

## RNF-06: Eficiencia de consultas y uso de recursos

- La carga habitual esperada es de 1 usuario por hora, con picos de hasta 10 usuarios
  por minuto. Estos valores describen llegadas de usuarios, no peticiones HTTP por segundo.
- El sistema debe soportar hasta **10 usuarios activos simultáneos** realizando el flujo
  de registro. Este máximo de diseño es independiente de la tasa de llegadas por minuto;
  no representa un límite de sesiones JWT existentes ni habilita rechazos por capacidad.
- Los escenarios de verificación deben contemplar el ritmo habitual, el pico de llegadas
  y hasta 10 usuarios simultáneos, recorriendo identificación, captura, consultas y carga
  de la cédula, sin exceder los tiempos de respuesta definidos en este requerimiento.

- Cargar CUCoP completo en una sola petición de API; expandir y buscar en el árbol no deben
  generar peticiones adicionales. Esto no exige resolver toda la lectura con una sola consulta SQL.
- Consultar cada catálogo de Datos Generales en su propio recurso (ADR-0020); las peticiones son
  independientes y pueden ir en paralelo. Evitar consultas repetidas por cada fila al resolver relaciones.
- Aplicar los límites de carga en servidor. El límite de la petición multipart debe contemplar
  su envoltura sin alterar el tamaño máximo permitido para el archivo.
- No exigir llamadas de red para comprobar la disponibilidad de la Página web capturada.
### Objetivos de tiempo de respuesta

Los siguientes objetivos aplican a la carga habitual, al pico de llegadas y a escenarios
de hasta 10 usuarios activos simultáneos:

| Grupo de operaciones | Objetivo |
| :--- | :--- |
| Consultas y guardados de la API | Al menos el 95 % de las operaciones debe concluir en menos de 2 segundos. |
| Carga de la cédula PDF de hasta 5 MiB | Al menos el 95 % de las cargas debe concluir en menos de 10 segundos. |
| Verificación de firma en backend, dentro de identifyApplicant | Al menos el 95 % de las verificaciones debe concluir en menos de 3 segundos. |

- La carga de PDF y la verificación de firma tienen sus propios objetivos; no se les aplica
  adicionalmente el límite de 2 segundos de consultas y guardados.
- Consultas y guardados se miden desde que el backend recibe la petición completa hasta que
  termina de producir la respuesta. La carga se mide desde la recepción del primer byte
  del cuerpo hasta completar almacenamiento, asociación y respuesta de éxito.
- La verificación se mide desde su inicio en backend hasta su resultado, incluyendo el
  procesamiento del certificado y comprobación de identidad. Excluye interacción del usuario,
  ejecución del componente externo y validación OCSP realizada por ese componente; no representa
  el tiempo total del flujo de identificación.
- Verificar cada operación y perfil de carga por separado, con muestras suficientes para
  evaluar el percentil 95. Registrar cantidad de muestras, duración, datos, entorno y condiciones
  de red de las pruebas de carga de archivos. No afirmar cumplimiento con una petición aislada.
- Las respuestas fallidas y su tasa se reportan por separado; un rechazo rápido no acredita
  el cumplimiento del tiempo de una operación exitosa. Estos umbrales no son timeouts de la API.
- Los objetivos están aprobados como requisitos; su cumplimiento queda pendiente de medición
  cuando exista implementación y un entorno de prueba representativo.

**Verificación prevista:** inspección de peticiones y consultas de catálogo, búsqueda local y
pruebas de rendimiento con los umbrales anteriores, tanto en carga habitual como en pico.
La concurrencia de diseño es de hasta 10 usuarios activos simultáneos; el volumen de datos
y la mezcla concreta de operaciones se concretarán para el plan de pruebas.

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
- Conservar los logs operativos durante **30 días desde cada evento registrado**; después
  de ese plazo pueden depurarse según el procedimiento operativo. No registrar JWT completos,
  contraseñas, llaves privadas ni evidencia de firma íntegra, conforme a RNF-02.
- Esta retención aplica a logs de operación, no a expedientes, documentos asociados ni
  evidencias de firma persistidas como información de negocio.
- Una consulta fallida no debe interpretarse como inexistencia de expediente o autorización válida.
- Las operaciones programadas deben registrar fallos y permitir reintentar el trabajo pendiente;
  no marcar un borrado físico como concluido si no terminó su tratamiento.

**Verificación prevista:** inyección de fallos en puertos y revisión de respuestas, logs y
estado persistido después de errores y reintentos; comprobar la retención de logs y su depuración sin afectar información de negocio.

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

## RNF-11: Disponibilidad y mantenimiento

- El servicio debe estar disponible para operar las 24 horas del día, los 7 días de la semana,
  salvo los periodos de mantenimiento planificado.
- La ventana permitida de mantenimiento es los sábados y domingos, de 00:00 a 03:00,
  en horario local de Ciudad de México (`America/Mexico_City`). No autoriza indisponibilidad
  planificada fuera de esos días y horas.
- La ventana es una posibilidad de mantenimiento, no una obligación de interrumpir el servicio
  todos los fines de semana. Fuera de una intervención, el servicio mantiene su horario 24/7.
- El objetivo de disponibilidad es **al menos 99.5 % por mes calendario**, delimitado según
  `America/Mexico_City`.
- Excluir únicamente el tiempo efectivo de mantenimiento planificado y registrado que coincida
  con las ventanas permitidas. No descontar automáticamente las ventanas completas ni excluir
  incidentes por el solo hecho de ocurrir durante un fin de semana.
- El tiempo de mantenimiento que se extienda fuera de la ventana y las interrupciones no
  planificadas cuentan como indisponibilidad.
- Calcular disponibilidad como `100 × (T - D) / T`, donde `T` es la duración del mes menos
  el mantenimiento excluido y `D` la indisponibilidad durante ese tiempo evaluable, sin duplicar
  intervalos de interrupción superpuestos.
- Este porcentaje es un objetivo por verificar, no una declaración de disponibilidad ya medida.
  El RTO de 4 horas de RNF-05 es un límite de recuperación por incidente y no amplía el presupuesto
  mensual de indisponibilidad; ambos objetivos deben evaluarse de forma independiente.

**Verificación prevista:** revisar calendario y duración efectiva de mantenimientos y registrar
inicio y fin de las interrupciones para calcular disponibilidad mensual. Antes del despliegue se
deben concretar los puntos de monitoreo, frecuencia de comprobación y criterios de servicio operativo.

## RNF-12: Compatibilidad de navegadores y dispositivos

- Soportar Chrome, Edge y Firefox en sus dos últimas versiones estables.
- Soportar Safari en su versión estable actual y la anterior.
- Proporcionar una interfaz adaptable a escritorio, tablet y móvil, conservando los campos,
  validaciones y acciones del flujo de registro.
- Verificar la matriz de navegadores en cada entrega y registrar las versiones concretas
  utilizadas en las pruebas; no fijar números de versión que queden desactualizados en esta especificación.
- Incluir en la comprobación la integración del componente externo de Firma Electrónica,
  selección y carga de archivos, descarga, formularios y árbol CUCoP. Una limitación del componente
  externo debe registrarse como brecha de compatibilidad, no como soporte ya verificado.
- Los tamaños concretos de prueba y sistemas operativos se fijarán al preparar el plan de
  verificación; esta matriz no afirma compatibilidad ya probada ni habilita una aplicación nativa.

**Verificación prevista:** ejecutar los escenarios del registro en los navegadores y tamaños
representativos de escritorio, tablet y móvil, documentando versiones, resultados y limitaciones.

## RNF-13: Accesibilidad de la interfaz

- Permitir completar las interacciones del registro mediante teclado, con orden de foco
  coherente y foco visible en los elementos interactivos.
- Proporcionar etiquetas claras y asociadas a campos y controles. Los botones de acción,
  incluidos Eliminar teléfono y Eliminar documento, deben tener un nombre accesible que
  permita identificar su propósito y el elemento afectado.
- Asociar los errores de validación al campo correspondiente, describiendo el problema
  mediante texto. No depender únicamente del color para comunicar obligatoriedad, errores,
  selección o estado de una sección.
- Exponer los mensajes de carga, éxito y error a lectores de pantalla sin exigir que el
  usuario encuentre visualmente el cambio. Comunicar también los estados de espera y las
  condiciones que impiden avanzar.
- Permitir navegar, expandir, contraer, seleccionar y deseleccionar el árbol CUCoP mediante
  teclado, exponiendo nombre, jerarquía y estado de selección, incluido el estado parcial
  de una genérica cuando corresponda.
- Gestionar el foco de los diálogos: moverlo al abrir, mantener la navegación dentro del
  diálogo modal y devolverlo al control que lo abrió al cerrar. Permitir aceptar y cancelar
  con teclado según las acciones del diálogo, sin confirmar una eliminación accidentalmente.
- Incluir la integración del componente externo de Firma Electrónica en la revisión de
  accesibilidad. Registrar las limitaciones detectadas; no asumir cumplimiento por utilizar
  un componente o una biblioteca de interfaz.

**Verificación prevista:** recorrer con teclado los escenarios de aviso, identificación,
formulario, teléfonos, archivos, CUCoP y diálogos; revisar etiquetas, foco, mensajes y errores
con lector de pantalla. Estos requisitos no declaran una certificación ni un nivel de conformidad
con un estándar de accesibilidad; cualquier objetivo formal adicional debe acordarse expresamente.

## Objetivos cuantitativos pendientes

Antes de comprometer un despliegue operativo se deben acordar y documentar:

| Tema | Definición pendiente |
| :--- | :--- |
| Rendimiento | Volumen de catálogos y expedientes, mezcla de operaciones y configuración del plan de pruebas. Concurrencia aprobada: hasta 10 usuarios activos simultáneos. Umbrales aprobados en RNF-06: 95 % bajo 2 s en consultas/guardados, 10 s en carga PDF y 3 s en verificación de firma backend. Llegadas: 1 usuario/hora habitual y hasta 10 usuarios/minuto en pico. |
| Disponibilidad | Puntos de monitoreo, frecuencia de comprobación y criterios de servicio operativo. Objetivo y cálculo definidos en RNF-11: al menos 99.5 % por mes, excluyendo solo mantenimiento efectivo dentro de las ventanas de sábados y domingos 00:00–03:00, America/Mexico_City. |
| Recuperación | Tecnología, ubicación, protección, horario de respaldos y procedimiento operativo. Retención aprobada: 30 días desde la creación de cada respaldo. Objetivos aprobados en RNF-05: respaldos diarios coordinados de BD/archivos, RPO máximo de 24 h y RTO máximo de 4 h; cumplimiento por verificar. |
| Capacidad y operación | Volumen de archivos esperado, crecimiento, capacidad del repositorio y mecanismo de depuración. Retención de logs definida: 30 días desde el evento. Horario de limpieza definido: diariamente a las 02:00, America/Mexico_City. |
| Entorno cliente | Sistemas operativos, tamaños concretos y combinación de navegador/lector de pantalla del plan de pruebas. Compatibilidad aprobada en RNF-12 y requisitos de accesibilidad definidos en RNF-13; cumplimiento por verificar. |

Estas precisiones no habilitan funciones de etapas posteriores ni modifican las reglas
funcionales o los límites documentales ya acordados.
