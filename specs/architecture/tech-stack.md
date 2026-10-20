# Stack tecnológico — Registro de proveedores de Fredkor Labs

## 1. Fuente y alcance

Este documento describe las tecnologías del módulo de registro. Las versiones exactas
configuradas se consultan en [pom.xml](../../pom.xml). Incorporar o cambiar una tecnología
requiere un ADR; esta especificación no actualiza dependencias ni versiones.

La implementación cubre la primera etapa de [vision.md](../vision.md): inicio y reanudación,
Datos Generales y CUCoP, con continuación hacia Contactos.

## 2. Tecnologías definidas del backend y soporte

| Elemento | Tecnología o configuración | Uso | Decisión |
| :--- | :--- | :--- | :--- |
| Lenguaje | Java 25, propiedad `java.version` | Backend; dominio limitado al JDK. | ADR-0001, ADR-0006. |
| Framework | Spring Boot 4.1.x; Maven declara 4.1.1 | Aplicación, ensamblaje, Web MVC, Validation y Data JPA. | ADR-0001, ADR-0006. |
| Persistencia para pruebas | H2 en memoria con Spring Data JPA | Expedientes, aceptaciones, evidencias y metadatos documentales. | ADR-0005. |
| Archivos | Volumen de sistema de archivos bajo `/repository` | Archivos de cualquier tipo admitido por sus especificaciones en `{year}/{month}/{day}/{rfc}/{uuid}.{extension}`; nombre original y referencia en base de datos. | ADR-0010. |
| Build | Maven | Generación, compilación, pruebas y ejecución del backend. El wrapper aún no está incorporado al módulo. | ADR-0006. |
| Contrato | OpenAPI 3.0.3 | Borrador `specs/api/supplier-registration-openapi.yaml`, pendiente de revisión final y alineación de herramientas para generación. | ADR-0002, ADR-0008, ADR-0009. |
| Generación web | openapi-generator-maven-plugin 7.22.0, generador `spring` | Interfaces y DTO web dentro de `adapter.in.web.generated`. | ADR-0006, ADR-0008. |
| Opciones de generación | `interfaceOnly`, `useSpringBoot4`, `useJackson3`, `useTags`, `useBeanValidation` | Configuración declarada en Maven; deberá verificarse contra el contrato de registro. | ADR-0006, ADR-0008. |
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
| Framework | Angular 21 y TypeScript compatible; versiones exactas por fijar en package.json y lockfile. | [ADR-0011](../adr/0011-frontend-angular-21.md). |
| Componentes y plantilla | PrimeNG 21 y Sakai, compatibles con Angular 21; revisión exacta pendiente de fijar. | [ADR-0012](../adr/0012-frontend-primeng-sakai.md). |
| Árbol CUCoP | PrimeNG Tree con selección editable desde partida genérica. | ADR-0012; RF-REG-08. |
| Arquitectura | Organización por dominios, DDD / Nx Style, sin obligación de instalar Nx. | [ADR-0013](../adr/0013-frontend-arquitectura-por-dominios.md). |
| Formularios | Preferencia por Reactive Forms tipados de @angular/forms. | [ADR-0014](../adr/0014-frontend-formularios-reactivos.md). |

El frontend aún no está creado en este módulo; no se declaran instalación ni pruebas de
compilación frontend realizadas. Su gestor de paquetes y versiones auxiliares se concretarán
al preparar el proyecto, respetando la compatibilidad de Angular 21 y la distribución de Sakai.

## 3. Integraciones

| Capacidad | Frontera y fuente |
| :--- | :--- |
| Firma y extracción de identidad | Adaptador de firma con JDK; [ADR-0016](../adr/0016-backend-verificacion-firma-sha256-rsa.md) e [integración](../api/signature-integration.md). |
| Catálogos compartidos | Adaptador JPA en la misma BD; ADR-0001 y [esquema de catálogos aprobado](catalog-database-proposal.md). |
| Contexto de identificación | JWT firmado y contexto JPA en la misma BD, un jti vigente por RFC; ADR-0015. |
| Archivos | Servicio interno general, volumen y metadatos en la misma BD; ADR-0010. |

## 4. Conservación en pruebas

H2 en memoria pierde expedientes, aceptaciones, evidencias, metadatos y referencias de archivos
al reiniciar el backend. Los archivos permanecen mientras se conserve el volumen, pero la
aplicación no puede recuperarlos mediante sus operaciones normales sin las referencias.
Esta limitación está aceptada para pruebas en ADR-0005 y ADR-0010.

Salir de la interfaz y reanudar mientras la instancia sigue activa conserva los datos ya
guardados y el archivo asociado inmediatamente al cargar, incluso sin Siguiente. La limpieza diaria de ADR-0010 elimina únicamente temporales registrados no asociados con más de 24 horas. No recupera referencias ni borra archivos sin metadatos tras reiniciar.

## 5. Preparación y comandos

Los siguientes comandos se ejecutan desde `SSD_REAL/` cuando se cumplan sus precondiciones:

| Acción | Comando | Precondición |
| :--- | :--- | :--- |
| Validar especificaciones y contrato | `python3 tools/spec_lint.py` | Python 3 y PyYAML disponibles; valida coherencia aunque el contrato sea borrador. |
| Generar interfaces y DTO | `mvn generate-sources` | YAML autoritativo existente y Maven alineado con su ruta y paquetes. |
| Compilar y ejecutar pruebas | `mvn test` | Código y pruebas implementados, contrato y generación configurados. |
| Ejecutar el backend | `mvn spring-boot:run` | Aplicación implementada, configuración de H2 y volumen preparada. |

Actualmente existe un borrador YAML, pero no está listo para generación. No hay wrapper Maven
ni código Java en `SSD_REAL/`. El archivo
Maven usa mx.com.fredkorlabs:supplier-registration, paquete mx.com.fredkorlabs.registration
y el contrato del registro. El validador está adaptado; la generación integral sigue pendiente.
La matriz de trazabilidad ya documenta operaciones y criterios del registro, con verificación
prevista. No declara pruebas implementadas ni comandos de generación ejecutados.

## 6. Restricciones

Respetar fronteras de [arquitectura](architecture.md), versiones de Maven y ADR aceptados.
No añadir dependencias ni editar fuentes generadas por inferencia. Las versiones exactas de
frontend, persistencia duradera y compensación de cargas fallidas continúan por concretar.

## 7. Referencias

- [Arquitectura](architecture.md) y [modelo de dominio](domain-model.md).
- [Índice de ADR](../adr/README.md).
- [ADR-0005: persistencia de pruebas](../adr/0005-backend-base-de-datos-h2.md).
- [ADR-0006: stack backend](../adr/0006-backend-spring-boot-4.md).
- [ADR-0008: contrato y Swagger UI](../adr/0008-backend-swagger-ui-con-contrato.md).
- [ADR-0010: volumen documental](../adr/0010-backend-almacenamiento-documental-en-volumen.md).
