<!-- Generado con /documentar desde specs/ y backend/src/. No editar a mano: cambia la spec y regenera. -->

# Decisiones de arquitectura

Un renglón por ADR, tomado del [índice de ADR](../../specs/adr/README.md). Los ADR describen cómo se
implementan las reglas; RF y HU siguen siendo la fuente de las reglas funcionales.

| ADR | Ámbito | Decisión | Estado |
| :--- | :--- | :--- | :--- |
| [ADR-0001](../../specs/adr/0001-backend-arquitectura-hexagonal.md) | Backend | Arquitectura hexagonal y modelos por frontera | Aceptado |
| [ADR-0002](../../specs/adr/0002-shared-transiciones-como-acciones.md) | Compartido | Acciones de registro y guardado explícito por sección | Aceptado |
| [ADR-0003](../../specs/adr/0003-shared-identificador-uuid.md) | Compartido | UUID del expediente y RFC como identidad de negocio | Aceptado |
| [ADR-0004](../../specs/adr/0004-shared-codigos-de-error.md) | Compartido | Errores del registro y representación HTTP | Aceptado |
| [ADR-0005](../../specs/adr/0005-backend-base-de-datos-h2.md) | Backend | H2 en memoria para pruebas y pérdida de referencias al reiniciar | Reemplazado por ADR-0018 |
| [ADR-0006](../../specs/adr/0006-backend-spring-boot-4.md) | Backend | Stack backend con Spring Boot 4.1 y Java 25 | Aceptado |
| [ADR-0007](../../specs/adr/0007-backend-error-interno-generico.md) | Backend | Error interno genérico sin detalles de infraestructura | Aceptado |
| [ADR-0008](../../specs/adr/0008-backend-swagger-ui-con-contrato.md) | Backend | Swagger UI con contrato autoritativo del registro | Aceptado |
| [ADR-0009](../../specs/adr/0009-shared-null-explicito-en-requests.md) | Compartido | Campos requeridos, opcionales y guardado completo de secciones | Aceptado |
| [ADR-0010](../../specs/adr/0010-backend-almacenamiento-documental-en-volumen.md) | Backend | Servicio general de archivos, asociación inmediata, eliminación lógica y limpieza diaria | Aceptado |
| [ADR-0011](../../specs/adr/0011-frontend-angular-21.md) | Frontend | Frontend con Angular 21 | Aceptado |
| [ADR-0012](../../specs/adr/0012-frontend-primeng-sakai.md) | Frontend | PrimeNG y plantilla Sakai para Angular 21 | Aceptado |
| [ADR-0013](../../specs/adr/0013-frontend-arquitectura-por-dominios.md) | Frontend | Arquitectura frontend por dominios: DDD / Nx Style | Aceptado |
| [ADR-0014](../../specs/adr/0014-frontend-formularios-reactivos.md) | Frontend | Preferencia por formularios reactivos de Angular | Aceptado |
| [ADR-0015](../../specs/adr/0015-shared-token-de-registro.md) | Compartido | JWT de registro de 60 minutos y sesiones concurrentes por solicitante | Aceptado |
| [ADR-0016](../../specs/adr/0016-backend-verificacion-firma-sha256-rsa.md) | Backend | Verificación RSA PKCS#1 v1.5 con SHA-256 y Java SHA256withRSA | Aceptado |
| [ADR-0017](../../specs/adr/0017-backend-postgresql-persistencia-duradera.md) | Backend | PostgreSQL 17 con Flyway para persistencia duradera | Aceptado |
| [ADR-0018](../../specs/adr/0018-backend-postgresql-en-pruebas-y-desarrollo.md) | Backend | PostgreSQL en pruebas y desarrollo con base de pruebas desechable | Aceptado |
| [ADR-0019](../../specs/adr/0019-shared-problem-details-rfc-9457.md) | Compartido | Errores como Problem Details (RFC 9457) | Aceptado |
| [ADR-0020](../../specs/adr/0020-shared-catalogos-por-recurso.md) | Compartido | Un recurso por catálogo de Datos Generales | Aceptado |
| [ADR-0021](../../specs/adr/0021-backend-colecciones-postman.md) | Backend | Colecciones Postman como verificación complementaria | Aceptado |

Los ADR de frontend (0011 a 0014) están aceptados, pero el frontend del registro aún no tiene pantallas.
