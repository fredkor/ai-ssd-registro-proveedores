# Stack tecnológico — Registro de proveedores de Fredkor Labs

## 1. Fuente y alcance

Este documento describe las tecnologías del módulo de registro. Las versiones exactas
configuradas se consultan en [pom.xml](../../backend/pom.xml). Incorporar o cambiar una tecnología
requiere un ADR; esta especificación no actualiza dependencias ni versiones.

La implementación cubre la primera etapa de [vision.md](../vision.md): inicio y reanudación,
Datos Generales y CUCoP, con continuación hacia Contactos.

### Estructura del repositorio

| Directorio | Contenido |
| :--- | :--- |
| `backend/` | Código Java/Spring Boot, pruebas, recursos y configuración de Maven, incluido su wrapper. |
| `frontend/` | Código Angular, pruebas, recursos estáticos y configuración de Node y herramientas del frontend. |
| `specs/` | Especificaciones, contratos y documentación de arquitectura. |
| `tools/` | Herramientas compartidas de soporte y validación del repositorio. |

Todo el código, las pruebas y la configuración específica del backend deben ubicarse en
`backend/`; los del frontend, en `frontend/`. Esto incluye código generado y artefactos de
compilación de cada aplicación. La raíz contiene únicamente documentación, herramientas y
configuración compartida del repositorio; no se crean aplicaciones ni árboles `src/` en ella.
Los contratos autoritativos permanecen en `specs/api/` y se referencian desde cada aplicación.

## 2. Tecnologías definidas del backend y soporte

| Elemento | Tecnología o configuración | Uso | Decisión |
| :--- | :--- | :--- | :--- |
| Lenguaje | Java 25, propiedad `java.version` | Backend; dominio limitado al JDK. | ADR-0001, ADR-0006. |
| Framework | Spring Boot 4.1.x; Maven declara 4.1.1 | Aplicación, ensamblaje, Web MVC, Validation y Data JPA. | ADR-0001, ADR-0006. |
| Pruebas de persistencia | Testcontainers con PostgreSQL 17: contenedor nuevo por ejecución en puerto aleatorio | Base vacía en cada `./mvnw test`; requiere Docker. | ADR-0018. |
| Persistencia | PostgreSQL 17 con Spring Data JPA y Flyway; driver y Flyway gestionados por el parent de Spring Boot | Todos los entornos. En local, instancia propia vía `REGISTRATION_DB_URL`, `REGISTRATION_DB_USERNAME` y `REGISTRATION_DB_PASSWORD`. | ADR-0017, ADR-0018. |
| Token de registro | Spring Security OAuth2 Resource Server (Nimbus JOSE+JWT), RS256 | Emisión y validación del JWT; incorporado con `identifyApplicant` (2026-10-21). | ADR-0015. |
| Archivos | Volumen de sistema de archivos bajo `/repository` | Archivos de cualquier tipo admitido por sus especificaciones en `{year}/{month}/{day}/{rfc}/{uuid}.{extension}`; nombre original y referencia en base de datos. | ADR-0010. |
| Build | Maven | Generación, compilación, pruebas y ejecución del backend mediante el wrapper `backend/mvnw`. | ADR-0006. |
| Contrato | OpenAPI 3.0.3 | `specs/api/supplier-registration-openapi.yaml`, READY y habilitado para generación ([estado](../api/README.md)). | ADR-0002, ADR-0008, ADR-0009. |
| Generación web | openapi-generator-maven-plugin 7.22.0, generador `spring` | Interfaces y DTO web dentro de `adapter.in.web.generated`. | ADR-0006, ADR-0008. |
| Opciones de generación | `interfaceOnly`, `useSpringBoot4`, `useJackson3`, `useTags`, `useBeanValidation` | Configuración declarada en Maven. Una plantilla propia en `backend/src/main/resources/openapi-templates` omite `@NotNull` en propiedades `nullable` (2026-10-21); la presencia de la propiedad se valida aparte (ADR-0009). | ADR-0006, ADR-0008. |
| Documentación API | springdoc-openapi-starter-webmvc-ui 3.1.1 | Swagger UI cargando el mismo YAML que genera interfaces y DTO. | ADR-0008. |
| Pruebas backend | JUnit y starters de prueba de Spring Boot declarados en Maven | Dominio sin Spring; aplicación con dobles de puertos; adaptadores con infraestructura correspondiente. Las versiones efectivas proceden de Maven. | ADR-0001, ADR-0006. |
| Fuente de tiempo | `java.time.Clock`, UTC | Hora controlable en aplicación y pruebas; instantes recibidos por el dominio. | ADR-0001; arquitectura. |
| Validación documental | Python 3 y PyYAML | `tools/spec_lint.py`, adaptado al registro; alcance y pruebas en tools/README.md. | Convenciones y flujo de verificación del proyecto. |

La configuración `openApiNullable=false` existente no define por sí misma la semántica de
campos omitidos o null: se aplicará ADR-0009 y el contrato de cada esquema. No se configura
un rechazo global de null sobre el resultado de Firma Electrónica del solicitante.

### Frontend

| Elemento | Decisión | ADR |
| :--- | :--- | :--- |
| Framework | Angular 21.0.6 y TypeScript 5.9.3; versiones en `frontend/package.json` y `package-lock.json` (npm). | [ADR-0011](../adr/0011-frontend-angular-21.md). |
| Componentes y plantilla | PrimeNG 21.0.2 y Sakai (`sakai-ng`) 21.0.0, compatibles con Angular 21. | [ADR-0012](../adr/0012-frontend-primeng-sakai.md). |
| Árbol CUCoP | PrimeNG Tree con selección editable desde partida genérica. | ADR-0012; RF-REG-08. |
| Arquitectura | Organización por dominios, DDD / Nx Style, sin obligación de instalar Nx. | [ADR-0013](../adr/0013-frontend-arquitectura-por-dominios.md). |
| Formularios | Preferencia por Reactive Forms tipados de @angular/forms. | [ADR-0014](../adr/0014-frontend-formularios-reactivos.md). |

El proyecto frontend existe en `frontend/` a partir de Sakai; no se declaran instalación ni
pruebas de compilación frontend realizadas. Usa npm; las versiones están en `frontend/package.json`
y quedan fijadas en `frontend/package-lock.json`, que es la fuente para consultarlas.

## 3. Integraciones

| Capacidad | Frontera y fuente |
| :--- | :--- |
| Firma y extracción de identidad | Adaptador de firma con JDK; [ADR-0016](../adr/0016-backend-verificacion-firma-sha256-rsa.md) e [integración](../api/signature-integration.md). |
| Catálogos compartidos | Adaptador JPA en la misma BD; ADR-0001 y [esquema de catálogos aprobado](catalog-database-proposal.md). |
| Contexto de identificación | JWT firmado con Spring Security OAuth2 Resource Server y contexto JPA por jti en la misma BD; varias sesiones por RFC; ADR-0015. |
| Archivos | Servicio interno general, volumen y metadatos en la misma BD; ADR-0010. |

## 4. Conservación de datos

Expedientes, aceptaciones, evidencias, contextos y metadatos de archivos se conservan en
PostgreSQL y sobreviven a los reinicios del backend (ADR-0017). Cada ejecución de pruebas usa
una base nueva y vacía que se destruye al terminar (ADR-0018). Los archivos permanecen mientras
se conserve el volumen.

Salir de la interfaz y reanudar conserva los datos ya guardados y el archivo asociado
inmediatamente al cargar, incluso sin Siguiente. La limpieza diaria de ADR-0010 elimina
únicamente temporales registrados no asociados con más de 24 horas y no borra archivos sin metadatos.

## 5. Preparación y comandos

Los comandos de Python se ejecutan desde la raíz del repositorio y los de Maven desde `backend/`:

| Acción | Comando | Precondición |
| :--- | :--- | :--- |
| Validar especificaciones y contrato | `python3 tools/spec_lint.py` | Python 3 y PyYAML disponibles; `--require-ready` exige además el contrato habilitado. |
| Generar interfaces y DTO | `./mvnw clean generate-sources` | YAML autoritativo existente y Maven alineado con su ruta y paquetes. |
| Compilar y ejecutar pruebas | `./mvnw test` | Docker disponible para Testcontainers (ADR-0018). |
| Ejecutar el backend | `./mvnw spring-boot:run` | PostgreSQL 17 accesible con las variables `REGISTRATION_DB_*` y volumen preparado. |

Maven usa mx.com.fredkorlabs:supplier-registration, paquete mx.com.fredkorlabs.registration
y el contrato del registro; la generación y la compilación se comprobaron el 2026-10-20.
La matriz de trazabilidad documenta operaciones y criterios del registro con su verificación
prevista; las pruebas existentes se consultan en `backend/src/test`.

## 6. Restricciones

Respetar fronteras de [arquitectura](architecture.md), versiones de Maven y ADR aceptados.
No añadir dependencias ni editar fuentes generadas por inferencia. Las versiones del frontend
están en su `package-lock.json`; la persistencia duradera está definida en ADR-0017 y la
compensación de cargas fallidas en ADR-0010.

## 7. Referencias

- [Arquitectura](architecture.md) y [modelo de dominio](domain-model.md).
- [Índice de ADR](../adr/README.md).
- [ADR-0005: persistencia de pruebas](../adr/0005-backend-base-de-datos-h2.md).
- [ADR-0006: stack backend](../adr/0006-backend-spring-boot-4.md).
- [ADR-0008: contrato y Swagger UI](../adr/0008-backend-swagger-ui-con-contrato.md).
- [ADR-0010: volumen documental](../adr/0010-backend-almacenamiento-documental-en-volumen.md).
