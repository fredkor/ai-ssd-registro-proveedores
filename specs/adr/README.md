# Decisiones de arquitectura — Registro de proveedores

## Organización y numeración

Todos los ADR se almacenan directamente en `specs/adr/`, con el formato
`NNNN-ambito-descripcion.md`:

- `shared`: contrato y decisiones que deben respetar backend y frontend.
- `backend`: implementación del servidor, persistencia, firma y almacenamiento.
- `frontend`: Angular, componentes, plantilla, organización y formularios.
- La numeración es global y única. Renombrar un ADR no cambia su identificador.
- La plantilla común permanece en [0000-template.md](0000-template.md).
- RF y HU siguen siendo la fuente de reglas funcionales; los ADR describen cómo implementarlas.

## Índice general

| ADR | Ámbito | Decisión | Estado |
| :--- | :--- | :--- | :--- |
| [0001](0001-backend-arquitectura-hexagonal.md) | Backend | Arquitectura hexagonal y modelos por frontera | Aceptado |
| [0002](0002-shared-transiciones-como-acciones.md) | Compartido | Acciones de registro y guardado explícito por sección | Aceptado |
| [0003](0003-shared-identificador-uuid.md) | Compartido | UUID del expediente y RFC como identidad de negocio | Aceptado |
| [0004](0004-shared-codigos-de-error.md) | Compartido | Errores del registro y representación HTTP | Aceptado |
| [0005](0005-backend-base-de-datos-h2.md) | Backend | H2 en memoria para pruebas y pérdida de referencias al reiniciar | Aceptado |
| [0006](0006-backend-spring-boot-4.md) | Backend | Stack backend con Spring Boot 4.1 y Java 25 | Aceptado |
| [0007](0007-backend-error-interno-generico.md) | Backend | Error interno genérico sin detalles de infraestructura | Aceptado |
| [0008](0008-backend-swagger-ui-con-contrato.md) | Backend | Swagger UI con contrato autoritativo del registro | Aceptado |
| [0009](0009-shared-null-explicito-en-requests.md) | Compartido | Campos requeridos, opcionales y guardado completo de secciones | Aceptado |
| [0010](0010-backend-almacenamiento-documental-en-volumen.md) | Backend | Servicio general de archivos, asociación inmediata, eliminación lógica y limpieza diaria | Aceptado |
| [0011](0011-frontend-angular-21.md) | Frontend | Frontend con Angular 21 | Aceptado |
| [0012](0012-frontend-primeng-sakai.md) | Frontend | PrimeNG y plantilla Sakai para Angular 21 | Aceptado |
| [0013](0013-frontend-arquitectura-por-dominios.md) | Frontend | Arquitectura frontend por dominios: DDD / Nx Style | Aceptado |
| [0014](0014-frontend-formularios-reactivos.md) | Frontend | Preferencia por formularios reactivos de Angular | Aceptado |
| [0015](0015-shared-token-de-registro.md) | Compartido | JWT de registro de 60 minutos y sesiones concurrentes por solicitante | Aceptado |
| [0016](0016-backend-verificacion-firma-sha256-rsa.md) | Backend | Verificación RSA PKCS#1 v1.5 con SHA-256 y Java SHA256withRSA | Aceptado |
| [0017](0017-backend-postgresql-persistencia-duradera.md) | Backend | PostgreSQL 17 con Flyway para persistencia duradera | Aceptado |

## Preparación pendiente

- Implementar el contexto JWT en la misma BD según ADR-0015 (Spring Security Resource Server, llaves PEM por entorno, varias sesiones por RFC).
- Implementar y verificar interoperabilidad de firma (ADR-0016); el mecanismo ya está definido.
- Tipos SQL y vigencia de catálogos fijados el 2026-10-21; claves únicas por tabla. Queda el mapeo país/régimen de Titularidad Jurídica. El esquema de archivos y la compensación de fallos de carga (ADR-0010) quedaron definidos el 2026-10-20.
- Contrato, Maven y validador alineados; la generación se comprobó el 2026-10-20.
- Persistencia duradera definida en ADR-0017; incorporar dependencias y migración inicial al preparar el primer entorno duradero.
- Versiones del frontend fijadas en `frontend/package.json` y `frontend/package-lock.json` (Sakai 21.0.0, Angular 21.0.6, PrimeNG 21.0.2).

Las políticas de null, propiedades desconocidas, carga/asociación inmediata y limpieza están
definidas. API-07 está resuelta mediante eliminación lógica transaccional en BD y borrado
físico reintentable por el proceso diario a partir de metadatos no asociados. No se declara
infraestructura instalada ni pruebas de implementación ejecutadas.
