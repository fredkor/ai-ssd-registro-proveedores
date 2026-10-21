# Arquitectura — Registro de proveedores de Fredkor Labs

## 1. Estilo y alcance

La solución es una aplicación monolítica con arquitectura hexagonal: dominio, aplicación y
adaptadores. Mantiene la separación de responsabilidades de ADR-0001 y representa el
[modelo de dominio](domain-model.md), sus 39 reglas y el
[ciclo de vida](../domain/supplier-registration-lifecycle.md).

La primera etapa cubre aceptación del aviso, identificación, consulta de existencia, inicio
y reanudación del expediente, Datos Generales, teléfonos, cédula fiscal y CUCoP. Contactos
es el punto de continuación; la captura de secciones posteriores y la finalización están
fuera de esta etapa.

Las tecnologías y versiones se documentan en [tech-stack.md](tech-stack.md) y `pom.xml`.
Este documento no agrega dependencias ni cambia versiones. Las decisiones de infraestructura
pendientes se resolverán mediante ADR antes de implementar el adaptador afectado.

## 2. Componentes y límites

| Componente | Responsabilidad | Relación con el backend |
| :--- | :--- | :--- |
| Interfaz de registro | Presentar aviso y asistente; valores de solo lectura, dependencias, tabla de teléfonos, carga documental y selección CUCoP. | Invoca los casos de uso expuestos por el adaptador web. No accede directamente a persistencia. |
| Componente externo de Firma Electrónica del solicitante | Gestionar archivos y contraseña, comprobar vigencia y OCSP y firmar la cadena `Temporal`. | Su resultado se envía al backend para verificación y procesamiento. Su implementación queda fuera del alcance. |
| Aplicación de registro | Coordinar identificación aceptada, expediente, acceso por RFC, catálogos, secciones y documentos. | Orquesta dominio y puertos de salida. |
| Dominio | Identidad, reglas condicionales, participación, expediente y condiciones de avance. | Java puro, sin HTTP, JPA ni llamadas al componente de firma. |
| Adaptadores de salida | Persistencia, consulta de catálogos, verificación del sello y almacenamiento de documentos. | Implementan las capacidades requeridas por aplicación. |

El componente de firma se utiliza desde la interfaz. El backend no solicita al usuario su
llave privada ni contraseña, no vuelve a firmar y no repite las verificaciones OCSP del
componente. Verifica el sello sobre el contenido firmado con el certificado del solicitante.

El frontend utiliza Angular 21, PrimeNG y la plantilla Sakai conforme a
[ADR-0011](../adr/0011-frontend-angular-21.md) y [ADR-0012](../adr/0012-frontend-primeng-sakai.md).
El árbol CUCoP utiliza PrimeNG Tree sin trasladar sus nodos o eventos al modelo de dominio.
La estructura por dominios DDD / Nx Style se define en
[ADR-0013](../adr/0013-frontend-arquitectura-por-dominios.md), sin obligar a instalar Nx.
Se prefieren formularios reactivos según
[ADR-0014](../adr/0014-frontend-formularios-reactivos.md). Estas decisiones no cambian las
reglas de navegación, guardado, firma o selección ya especificadas.

## 3. Capas y regla de dependencia

```mermaid
flowchart TB
    CLIENT["Cliente HTTP"]
    subgraph IN["adapter.in.web"]
        WEB["Controlador y mapeador"]
    end
    subgraph APP["application"]
        INPUT["port.in: UseCase y modelos de entrada/salida"]
        SERVICE["service: casos de uso"]
        OUTPUT["port.out: capacidades externas"]
    end
    subgraph DOMAIN["domain: Java puro"]
        MODEL["SupplierRegistration, valores y reglas"]
    end
    subgraph OUT["adapter.out"]
        ADAPTER["Persistencia, catalogos, firma y archivos"]
    end
    INFRA["BD compartida, volumen y criptografia JDK"]

    CLIENT -. HTTP .-> WEB
    WEB -->|usa| INPUT
    INPUT -. implementada por .-> SERVICE
    SERVICE -->|usa| MODEL
    SERVICE -->|usa| OUTPUT
    OUTPUT -. implementados por .-> ADAPTER
    ADAPTER --> INFRA
```

Las flechas `implementada por` e `implementados por` indican realización, no dependencias
hacia los adaptadores. El grupo de salida resume varios adaptadores independientes, detallados
en la tabla. El controlador usa `port.in`; los servicios usan dominio y `port.out`.
El dominio solo conoce el JDK y sus propios tipos. Dominio y aplicación no dependen de
frameworks: no importan Spring, JPA, Jackson ni tipos HTTP. Las dependencias tecnológicas
permanecen en los adaptadores y en la configuración de arranque.

| Paquete | Responsabilidad | Dependencias permitidas | Dependencias excluidas |
| :--- | :--- | :--- | :--- |
| `domain.model` | Agregados, entidades, objetos de valor y enums del modelo. | JDK y otros tipos de dominio. | Spring, JPA, HTTP, aplicación, adaptadores, librerías del componente de firma. |
| `domain.exception` | Incumplimientos de reglas de dominio. | JDK. | Spring, JPA, HTTP, aplicación, adaptadores y clases de infraestructura. |
| `application.port.in` | Interfaces de casos de uso; Commands, Queries y Results. | JDK y tipos de aplicación; dominio solo si es necesario sin exponerlo al adaptador web. | Spring, JPA, HTTP, servicios concretos y adaptadores. |
| `application.port.out` | Capacidades externas y sus modelos de intercambio. | JDK y dominio. | Spring, JPA, HTTP, adaptadores y resultados propios de `port.in`. |
| `application.service` | Implementar casos de uso, resolver contexto, autorizar acceso, coordinar transacciones y puertos. | JDK, dominio, `port.in` y `port.out`. | Spring, JPA, HTTP e implementaciones concretas de adaptadores. |
| `adapter.in.web` | Traducir HTTP y archivos a entradas de aplicación; mapear resultados y errores. | JDK, Spring MVC, validación y serialización del contrato; `application.port.in`; excepciones de dominio solo para traducir errores. | Agregados de dominio, servicios concretos, JPA y adaptadores de salida. |
| `adapter.out.persistence` | Guardar y reconstruir expedientes, aceptaciones, evidencias y asociaciones documentales. | `application.port.out`, dominio y JPA. | Controladores y modelos web. |
| `adapter.out.catalog` | Resolver registros y relaciones de catálogos existentes. | `application.port.out`, dominio y tecnología de consulta elegida. | Interfaz y casos de uso de entrada. |
| `adapter.out.signature` | Verificar criptográficamente sello, contenido y certificado; traducir el resultado externo. | `application.port.out`, dominio y mecanismo criptográfico elegido. | Interfaz, servicios concretos y acceso directo a sus transacciones. |
| `adapter.out.document` | Gestionar contenido de archivos de cualquier tipo admitido mediante referencias opacas. | `application.port.out` y tecnología de almacenamiento elegida. | Agregados completos, interfaz y controladores. |
| `adapter.in.transaction` | Aplicar la transacción de un caso de uso mediante un decorador del puerto de entrada. | JDK, Spring y `application.port.in`. | HTTP, reglas de negocio y adaptadores de salida concretos. |
| `adapter.in.scheduling` | Activar periódicamente el caso de uso de limpieza. | JDK, Spring y `application.port.in`. | Reglas de negocio y adaptadores de salida concretos. |
| `configuration` | Ensamblar casos de uso, adaptadores y decoradores transaccionales. | JDK, Spring, puertos, servicios y adaptadores necesarios para el ensamblaje. | Reglas de negocio. |

Las dependencias se inyectan por constructor. El controlador conoce los puertos de entrada;
el servicio conoce los puertos de salida. El ensamblaje de implementaciones ocurre en el
arranque de la aplicación, en `configuration`, sin trasladar reglas de negocio a la configuración.
Los servicios son clases Java sin `@Service`, `@Component`, `@Autowired` ni `@Transactional`;
se registran mediante métodos `@Bean` que exponen sus puertos de entrada.

## 4. Modelos por frontera

| Frontera | Modelos | Regla |
| :--- | :--- | :--- |
| HTTP | Interfaces y DTO generados desde OpenAPI. | El contrato define rutas y operationId; Maven genera interfaces y DTO. No duplicar DTO manualmente. |
| Entrada de aplicación | Commands, Queries y Results, preferentemente `record`. | No contienen `MultipartFile`, entidades JPA, `ResponseEntity` ni objetos del componente visual. |
| Dominio | `SupplierRegistration`, `ApplicantIdentity`, `GeneralData`, `PhoneNumber`, `CucopParticipation`, `SectionProgress` y tipos documentales y de evidencia. | No se devuelven directamente al controlador. |
| Persistencia | Entidades y mapeadores propios del adaptador. | Reconstituyen el dominio sin ejecutar el alta ni nuevas transiciones. |
| Firma del solicitante | Resultado completo del componente y proyección de sus campos conocidos. | Se conserva íntegro el resultado aceptado. El mapeo de nombres externos a nombres internos no debe perder propiedades. |
| Documento | Contenido y metadatos en modelos de aplicación; referencia opaca `StoredDocumentReference` en dominio. | El adaptador web traduce el archivo y el adaptador documental gestiona su almacenamiento; el dominio no contiene bytes, rutas ni streams. |
| Catálogos | Referencias y registros con datos y relaciones necesarios para validar. | La aplicación no usa DTO web como modelo de catálogo. |

`SignatureResultInput` representa el resultado completo recibido para identificación,
con sus proyecciones de contenido, RFC, razón social, sello y certificado. Sus campos están definidos en el borrador OpenAPI. El dominio y los puertos no dependen de Jackson ni de una clase
externa del componente de firma.

Los resultados de aplicación proyectan estado, identidad, datos guardados y progreso sin
exponer las entidades. Conservar toda la evidencia no implica devolver su contenido completo
en cada consulta del expediente.

## 5. Puertos de entrada y servicios

Los nombres siguientes definen responsabilidades de aplicación. Las firmas concretas y los
campos de Commands, Queries y Results se cerrarán con el contrato API. Las referencias US
corresponden a los archivos de historias, cuyos encabezados conservan HU-REG.

| Puerto de entrada | Entrada / salida conceptual | Servicio | Fuente |
| :--- | :--- | :--- | :--- |
| `GetRegistrationNoticeUseCase` | Consulta → contenido y versión vigente del aviso. | `GetRegistrationNoticeService` | RF-REG-03; US03. |
| `AcceptRegistrationNoticeUseCase` | Versión aceptada → referencia de aceptación y fecha/hora. | `AcceptRegistrationNoticeService` | RF-REG-03; US03. |
| `IdentifyApplicantUseCase` | Resultado de firma y contexto de aceptación → identificación aceptada o rechazo. | `IdentifyApplicantService` | RF-REG-01; US01. |
| `CheckRegistrationExistenceUseCase` | Identificación aceptada → inexistencia, expediente en captura o registrado. | `CheckRegistrationExistenceService` | RF-REG-02; US02. |
| `StartSupplierRegistrationUseCase` | Identificación y aceptación previamente procesadas → expediente nuevo. | `StartSupplierRegistrationService` | RF-REG-04; US04. |
| `ResumeSupplierRegistrationUseCase` | Nueva identificación aceptada → mismo expediente, datos y progreso. | `ResumeSupplierRegistrationService` | RF-REG-06; US05. |
| `SaveGeneralDataUseCase` | Datos Generales y teléfonos de la sección → datos y progreso guardados. | `SaveGeneralDataService` | RF-REG-05; US08, US09, US06. |
| `UploadFiscalDocumentUseCase` | Archivo y expediente identificado → UUID y documento asociado inmediatamente. | `UploadFiscalDocumentService` | RF-REG-05; US10. |
| `DownloadFiscalDocumentUseCase` | Expediente identificado → contenido y metadatos del documento asociado. | `DownloadFiscalDocumentService` | RF-REG-05; US10. |
| `DeleteFiscalDocumentUseCase` | Expediente autorizado → desasociación y marca de metadatos en transacción de BD, sin borrar contenido. | `DeleteFiscalDocumentService` | RF-REG-05; US10. |
| `GetCountriesUseCase` | Consulta → países. | `GetCountriesService` | RF-REG-05; US08; ADR-0020. |
| `GetLegalOwnershipsUseCase` | Origen, país y régimen → titularidades aplicables con NOT_DEF según catalogs.md. | `GetLegalOwnershipsService` | RF-REG-05; US08; ADR-0020. |
| `GetSectorsUseCase` | Consulta → sectores. | `GetSectorsService` | RF-REG-05; US08; ADR-0020. |
| `GetSubsectorsUseCase` | Sector → sus subsectores. | `GetSubsectorsService` | RF-REG-05; US08; ADR-0020. |
| `GetBusinessActivitiesUseCase` | Consulta → giros. | `GetBusinessActivitiesService` | RF-REG-05; US08; ADR-0020. |
| `GetStratificationsUseCase` | Consulta → estratificaciones activas, sin filtro por régimen. | `GetStratificationsService` | RF-REG-05; US08; ADR-0020. |
| `GetPrimaryActivitiesUseCase` | Consulta → actividades preponderantes. | `GetPrimaryActivitiesService` | RF-REG-05; US08; ADR-0020. |
| `GetWorkerCountRangesUseCase` | Consulta → rangos de trabajadores. | `GetWorkerCountRangesService` | RF-REG-05; US08; ADR-0020. |
| `GetAnnualSalesRangesUseCase` | Consulta → rangos de ventas anuales. | `GetAnnualSalesRangesService` | RF-REG-05; US08; ADR-0020. |
| `GetCucopCatalogUseCase` | Consulta única → todos los registros CUCoP y sus relaciones. | `GetCucopCatalogService` | RF-REG-08; US11. |
| `SaveCucopParticipationUseCase` | Tipos de participación y específicas seleccionadas → selección y progreso guardados. | `SaveCucopParticipationService` | RF-REG-08; US11, US06. |

Las acciones locales del formulario (Atrás, Agregar/Eliminar teléfono, selección del árbol) no generan guardados ni endpoints individuales. Los casos de uso de
sección reciben la captura completa y confirman únicamente conjuntos válidos.
La finalización permanece fuera de la primera etapa.

## 6. Puertos de salida

Los puertos usan tipos de dominio o modelos propios de `application.port.out`. Sus resultados
no dependen de `application.port.in` ni de clases de infraestructura.

| Puerto | Operaciones conceptuales | Responsabilidad y límites |
| :--- | :--- | :--- |
| `SupplierRegistrationPersistencePort` | Buscar por RFC o ID; insertar expediente; guardar datos y progreso. | Reconstruir `SupplierRegistration`, impedir duplicidad de RFC y persistir cambios coherentes. No habilita borrado de expedientes. |
| `NoticeAcceptancePersistencePort` | Conservar aceptación y recuperar su referencia. | Registrar versión y fecha/hora antes de firmar y permitir su asociación posterior al expediente. No crea expediente por sí mismo. |
| `ApplicantSignatureVerificationPort` | Verificar sello sobre contenido usando certificado. | Devolver éxito o fallo de verificación sin repetir OCSP ni solicitar la llave privada. Errores técnicos y verificación negativa impiden aceptar el resultado. |
| `ApplicantSignatureEvidencePersistencePort` | Conservar y recuperar evidencia aceptada y sus referencias. | Preservar íntegramente el resultado, aun cuando todavía no exista expediente. No reemplazarlo por una selección de campos. |
| `GeneralDataCatalogPort` | Una consulta por catálogo (país, titularidad filtrada, sector, subsector por sector, giro, estratificación activa, actividad preponderante, trabajadores, ventas); resolver referencias para validar. | Titularidad jurídica se filtra en backend con el algoritmo de domain/catalogs.md (ADR-0020) y el guardado valida las mismas condiciones. Cada consulta es independiente para permitir un caché por catálogo en el futuro. Aportar vigencia cuando aplique. |
| `CucopCatalogPort` | Obtener los cuatro niveles y sus relaciones; resolver específicas para el guardado. | Devuelve el catálogo completo en una consulta de API. Búsqueda y expansión son locales al frontend. No modifica catálogos al seleccionar partidas. |
| `RegistrationContextPort` | Emitir, resolver y validar token; un RFC puede tener varias sesiones vigentes. | Vincular identidad, evidencia y aceptación; comprobar vencimiento de 60 minutos. Mantener en la misma BD un contexto por jti con RFC, emisión, vencimiento y referencias a evidencia/aceptación; inserción transaccional y consulta en cada petición conforme a ADR-0015. |
| `FileStoragePort` | Preparar, leer y borrar contenido por referencia opaca; exclusión con confirmación y limpieza. | Gestionar bytes y metadatos en el almacenamiento elegido. No decide si Datos Generales está completo. |
| `FileMetadataPort` | Consultar metadatos; confirmar o retirar asociaciones; seleccionar temporales elegibles para limpieza. | Asociar durante la carga; marcar como no asociado al eliminar lógicamente y conservar metadatos hasta limpieza; coordinar referencias según las restricciones del tipo documental. El caso de uso de cédula impone máximo una. |

La consulta de inexistencia previa al alta no reemplaza la garantía de unicidad en persistencia.
La violación concurrente se traduce en un resultado o error de aplicación que conduzca al
flujo correspondiente al expediente existente. La representación HTTP y el código están definidos en ADR-0004 y OpenAPI; las clases concretas se fijarán al implementar.

`java.time.Clock` se inyecta en los servicios que requieren la hora; se utiliza UTC y las
pruebas pueden proporcionar un reloj fijo. El dominio recibe los instantes de negocio, sin
obtener la hora del sistema internamente.

Las entidades y relaciones de persistencia se derivarán del modelo de dominio y las reglas
especificadas al implementar el adaptador. Las tablas y columnas no se toman como fuente
de reglas adicionales de negocio.

Los catálogos de Datos Generales y los cuatro niveles CUCoP residen en la misma base de datos que los expedientes. Se consultan mediante Spring Data JPA desde adapter.out.catalog, que implementa GeneralDataCatalogPort y CucopCatalogPort. No se requiere un servicio HTTP externo ni otra conexión de base de datos. Las entidades y repositorios de catálogo permanecen en el adaptador; el dominio y los puertos no dependen de JPA. El registro consulta catálogos, sin habilitar su administración ni modificar sus claves o relaciones.

## 7. Coordinación y transacciones

La aplicación define la unidad de consistencia de cada caso de uso. Cuando se requiera una
transacción de BD, un decorador en `adapter.in.transaction` implementará el puerto de entrada,
invocará al servicio y aplicará la transacción con Spring. `configuration` expondrá el decorador
como implementación del puerto para los consumidores y construirá el servicio Java que envuelve.
El servicio no importa APIs transaccionales ni conoce el decorador. La transacción debe abarcar
los puertos participantes; transacciones independientes por repositorio no sustituyen esa unidad.
Los casos de uso que no requieren transacción, como consultar el aviso fijo, se registran directamente.

| Caso de uso | Coordinación y unidad de consistencia |
| :--- | :--- |
| Aceptar aviso | Conservar aceptación y referencia antes de habilitar firma; no crear expediente. |
| Identificar | Verificar sello y extraer identidad mediante ADR-0016; conservar evidencia íntegra antes de emitir contexto JWT de ADR-0015, verificando firma, claims y contexto del jti. Un fallo no invalida una sesión vigente. |
| Crear expediente | Identidad, aviso, evidencia inicial, UUID y estado en una transacción; unicidad de RFC respaldada por persistencia. |
| Reanudar | Actualizar nombre del certificado, vínculo histórico de evidencia y última aceptación del aviso conjuntamente; un fallo conserva los valores previos. La evidencia aceptada al identificar no se elimina. |
| Guardar Datos Generales | Validar catálogos, teléfonos y cédula ya asociada; confirmar datos y progreso. No modifica asociaciones documentales; CUCoP no cambia. |
| Guardar CUCoP | Resolver específicas, validar dominio y confirmar selección y progreso conjuntamente. |
| Cargar archivo | Conservar contenido, metadatos y asociación antes de devolver éxito y UUID; disponible sin Siguiente. |
| Descargar | Autorizar y resolver el archivo asociado sin exponer rutas. |
| Eliminar archivo | Desasociar, marcar metadatos no asociados y dejar Datos Generales PENDING en una transacción de BD sin tocar disco. Fallo hace rollback; CUCoP intacto. |
| Limpieza diaria | Coordinarse con asociación; eliminar solo no asociados con más de 24 horas según ADR-0010. No es un endpoint del usuario. |

Carga y eliminación lógica son operaciones independientes de Siguiente. Eliminar solo modifica
BD: referencia de negocio, marca en metadatos y progreso comparten transacción. No se invoca
FileStoragePort para borrar ni mover contenido durante la operación del usuario.
El proceso diario consulta metadatos no asociados y verifica ausencia de referencias y antigüedad
antes de borrar físicamente; conserva metadatos ante fallos para reintentar según
[ADR-0010](../adr/0010-backend-almacenamiento-documental-en-volumen.md).
La carga sigue necesitando coordinar volumen y BD; una compensación fallida se registra para
reconciliación. Catálogos, expediente y metadatos comparten la BD PostgreSQL (ADR-0017, ADR-0018).

## 8. Diagrama de secuencia: guardar Datos Generales

Flujo de `saveGeneralData` (RF-REG-05; US06, US08, US09 y US10), equivalente al ejemplo
de activación de producto. `SaveGeneralDataService` implementa `SaveGeneralDataUseCase`;
los nombres de mensajes expresan responsabilidades, no firmas Java definitivas.

```mermaid
sequenceDiagram
    autonumber
    participant Frontend
    participant Web
    participant Service as SaveGeneralDataService
    participant Context as RegistrationContextPort
    participant Persistence as SupplierRegistrationPersistencePort
    participant Catalogs as GeneralDataCatalogPort
    participant Files as FileMetadataPort
    participant Registration as SupplierRegistration

    Frontend->>Web: PUT /api/v1/supplier-registrations/{registrationId}/general-data con JWT
    Web->>Web: Normalizar textos y validar contrato
    Web->>Service: Ejecutar SaveGeneralDataUseCase con comando y contexto
    Service->>Context: Verificar JWT y resolver sesion vigente
    Context-->>Service: Identidad aceptada o rechazo
    alt JWT invalido, vencido o sin contexto
        Service-->>Web: REGISTRATION_TOKEN_INVALID
        Web-->>Frontend: 401 ProblemResponse
    else Sesion vigente
        Service->>Persistence: Recuperar expediente por registrationId
        Persistence-->>Service: Expediente o ausencia
        alt Expediente inexistente
            Service-->>Web: REGISTRATION_NOT_FOUND
            Web-->>Frontend: 404 ProblemResponse
        else Expediente de otro RFC
            Service-->>Web: REGISTRATION_ACCESS_DENIED
            Web-->>Frontend: 403 ProblemResponse
        else Expediente propio
            Service->>Service: Iniciar unidad transaccional de guardado
            Service->>Catalogs: Resolver selecciones y relaciones
            Catalogs-->>Service: Contexto de catalogos
            Service->>Files: Consultar cedula asociada al expediente
            Files-->>Service: Referencia vigente o ausencia
            Service->>Registration: saveGeneralData con datos y contexto validable
            alt Estado incompatible
                Registration-->>Service: Transicion rechazada
                Service-->>Web: INVALID_STATE_TRANSITION
                Web-->>Frontend: 409 ProblemResponse
            else Campos, telefonos o cedula invalidos
                Registration-->>Service: Reglas incumplidas
                Service-->>Web: VALIDATION_ERROR
                Web-->>Frontend: 400 ProblemResponse
            else Seccion valida
                Registration->>Registration: Sustituir GeneralData y marcar COMPLETED
                Service->>Persistence: Guardar datos y progreso conjuntamente
                alt Fallo de persistencia o commit
                    Persistence-->>Service: Fallo y rollback
                    Service-->>Web: INTERNAL_ERROR
                    Web-->>Frontend: 500 ProblemResponse
                else Commit exitoso
                    Persistence-->>Service: Guardado confirmado
                    Service-->>Web: Resultado de aplicacion
                    Web->>Web: Mapear a SectionSaveResponse
                    Web-->>Frontend: 200 con GENERAL_DATA COMPLETED y nextSection CUCOP
                end
            end
        end
    end
```

- Web traduce HTTP y normaliza los textos editables antes de validar el contrato, según ADR-0009.
  Una entrada inválida se rechaza antes de invocar el caso de uso.
- El contexto verifica firma, claims y `jti` vigente en la misma BD conforme a ADR-0015.
  La comprobación del RFC propietario corresponde a aplicación.
- El dominio recibe datos y relaciones resueltos; no llama a puertos ni conoce HTTP o JPA.
- Datos, progreso y comprobación de la asociación documental deben coordinarse con la
  eliminación lógica concurrente para impedir completar la sección con una cédula ya desasociada.
  Se usa bloqueo pesimista de la fila del expediente conforme al [diseño de persistencia](registration-database-design.md).
- El éxito HTTP se devuelve después del commit. Todo fallo de la unidad de guardado revierte
  sus cambios; CUCoP y la cédula cargada en una operación previa permanecen intactos.
- Cargar o eliminar lógicamente un archivo son operaciones propias. Este guardado solo consulta
  la asociación; el proceso diario es el único que realiza el borrado físico elegible.
- Los fallos técnicos de contexto, consulta de catálogos o archivos también se traducen a
  INTERNAL_ERROR según ADR-0007, sin avanzar. La tabla de errores completa está en OpenAPI.

El [diseño de persistencia del registro](registration-database-design.md), aprobado el
2026-10-20, define tablas, tipos SQL de PostgreSQL, concurrencia y compensación, complementando el
esquema de catálogos aprobado. No representa migraciones ejecutadas.

## 9. Estructura prevista

Relativa al paquete base `mx.com.fredkorlabs.registration`, alineado en Maven:

```text
domain/model/               Agregado, valores, entidades de referencia y enums
domain/exception/           Incumplimientos de reglas puras
application/port/in/        UseCase, Commands, Queries y Results
application/port/out/       Capacidades externas y modelos de intercambio
application/service/        Coordinación por caso de uso y lógica de limpieza sin frameworks
configuration/              Ensamblaje Spring mediante @Bean y selección de decoradores
adapter/in/transaction/    Decoradores transaccionales de puertos de entrada
adapter/in/scheduling/     Activación programada de la limpieza
adapter/in/web/             Controladores, mapeadores y errores
adapter/in/web/generated/   Interfaces y DTO de OpenAPI
adapter/out/persistence/    Expediente, aviso, evidencia y metadatos de archivos
adapter/out/catalog/        Consultas JPA a catálogos compartidos
adapter/out/signature/      Verificación y extracción desde certificado
adapter/out/document/       Contenido físico en volumen
```

El frontend se organiza por dominios conforme a ADR-0013. Sus tipos visuales, DTO y modelos
se mantienen separados; no se instala Nx por inferencia.

## 10. Verificación y preparación

- Dominio: JUnit puro para reglas y transiciones.
- Aplicación: pruebas sin contexto Spring, con dobles de puertos para coordinación, autorización y fallos.
- Dependencias: dominio y aplicación sin imports de frameworks; ensamblaje Spring fuera del núcleo.
- Adaptadores: persistencia, unicidad concurrente, firma interoperable, archivos y limpieza.
- Web e interfaz: contrato, errores, captura y navegación conforme a los CA.
- La cobertura prevista está en [trazabilidad](../traceability.md); no representa pruebas ejecutadas.
- Antes de implementar catálogos: concretar tipos SQL del [esquema de catálogos aprobado](catalog-database-proposal.md).
  Antes de implementar el contexto JWT: concretar los pendientes de ADR-0015. Consultar el [estado de API](../api/README.md).
- Versiones y comandos: [stack](tech-stack.md). Reglas funcionales: [requerimientos](../requirements/functional.md).
