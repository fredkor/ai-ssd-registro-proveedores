# ADR-0017: PostgreSQL 17 con Flyway para persistencia duradera

- **Estado:** Aceptado
- **Ámbito:** Backend
- **Fecha:** 2026-10-21
- **Actualización:** 2026-10-21 (todos los entornos, ADR-0018)
- **Specs relacionadas:** ADR-0018, ADR-0010, ADR-0015; `architecture/registration-database-design.md`; `architecture/catalog-database-proposal.md`; `architecture/tech-stack.md`.

## Contexto

ADR-0005 acepta H2 en memoria solo para pruebas y deja pendiente la base de datos de otros
entornos. Fuera de pruebas, expedientes, aceptaciones, evidencias, contextos JWT, metadatos
documentales y catálogos deben sobrevivir a reinicios del backend. Una base duradera necesita
además un control versionado de su esquema.

## Decisión

- PostgreSQL 17 es la única base de datos del registro en todos los entornos (pruebas,
  desarrollo y despliegues; ADR-0018): expedientes, catálogos, contextos JWT y metadatos
  documentales comparten la misma base (ADR-0001, ADR-0010, ADR-0015).
- URL, usuario y contraseña se leen de variables de entorno (`REGISTRATION_DB_URL`,
  `REGISTRATION_DB_USERNAME`, `REGISTRATION_DB_PASSWORD`); no se guardan credenciales en el
  repositorio. En las pruebas la conexión la aporta Testcontainers (ADR-0018).
- El esquema lo crea y evoluciona Flyway mediante scripts SQL versionados en
  `backend/src/main/resources/db/migration`, también en las pruebas. Hibernate solo valida el
  esquema (`ddl-auto=validate`).
- Dependencias, con versiones gestionadas por el parent de Spring Boot: `org.postgresql:postgresql`
  (runtime), `spring-boot-starter-flyway` (incluye `flyway-core` y la autoconfiguración, que en
  Spring Boot 4 vive en su propio módulo) y `org.flywaydb:flyway-database-postgresql`.
- Los tipos SQL son los del [diseño de persistencia](../architecture/registration-database-design.md#71-tipos-sql)
  y del [esquema de catálogos](../architecture/catalog-database-proposal.md#tipos-sql).
- El bloqueo pesimista del expediente usa `SELECT ... FOR UPDATE`.
- Las migraciones crean exactamente las tablas, constraints e índices de los diseños aprobados
  y cargan los catálogos de `domain/catalog-values.md` con sus IDs originales. No añaden reglas.
- El volumen documental de ADR-0010 sigue siendo independiente de la base de datos.

## Alternativas consideradas

| Alternativa | A favor | En contra |
| :--- | :--- | :--- |
| PostgreSQL con Flyway | Esquema versionado y reproducible; validación de entidades al arrancar. | Mantener migraciones SQL además de las entidades; opción elegida. |
| PostgreSQL con `ddl-auto` de Hibernate | Sin herramienta adicional. | Sin control de cambios en una base duradera. |
| PostgreSQL con scripts manuales | Control total del DBA. | Proceso manual propenso a desalineación. |
| Mantener H2 | Sin cambios. | Pierde todo al reiniciar y no valida el motor real; retirado por ADR-0018. |

## Consecuencias y verificación

- Cada cambio de entidad JPA requiere su migración Flyway; `validate` detecta la desalineación al arrancar.
- Cada ejecución de pruebas aplica las migraciones sobre una base PostgreSQL vacía y arranca con
  `ddl-auto=validate`, lo que verifica la alineación entre migraciones y entidades.
- La versión del servidor es PostgreSQL 17. Respaldos y retención se definen al desplegar, sin
  cambiar este diseño.
- Reiniciar el backend no pierde datos; las pruebas empiezan con una base vacía por ejecución (ADR-0018).
