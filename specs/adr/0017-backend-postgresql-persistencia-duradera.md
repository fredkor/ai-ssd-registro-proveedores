# ADR-0017: PostgreSQL 17 con Flyway para persistencia duradera

- **Estado:** Aceptado
- **Ámbito:** Backend
- **Fecha:** 2026-10-21
- **Specs relacionadas:** ADR-0005, ADR-0010, ADR-0015; `architecture/registration-database-design.md`; `architecture/catalog-database-proposal.md`; `architecture/tech-stack.md`.

## Contexto

ADR-0005 acepta H2 en memoria solo para pruebas y deja pendiente la base de datos de otros
entornos. Fuera de pruebas, expedientes, aceptaciones, evidencias, contextos JWT, metadatos
documentales y catálogos deben sobrevivir a reinicios del backend. Una base duradera necesita
además un control versionado de su esquema.

## Decisión

- Los entornos distintos de pruebas usan PostgreSQL 17 como única base de datos del registro:
  expedientes, catálogos, contextos JWT y metadatos documentales comparten la misma base
  (ADR-0001, ADR-0010, ADR-0015).
- H2 en memoria sigue siendo la base de las pruebas automatizadas (ADR-0005), con el esquema
  generado por Hibernate desde las entidades JPA.
- PostgreSQL se activa con el perfil Spring `postgres`. URL, usuario y contraseña se leen de
  variables de entorno; no se guardan credenciales en el repositorio.
- El esquema de PostgreSQL lo crea y evoluciona Flyway mediante scripts SQL versionados en
  `backend/src/main/resources/db/migration`. Con ese perfil Hibernate solo valida el esquema
  (`ddl-auto=validate`) y Flyway queda habilitado; en H2 de pruebas Flyway permanece deshabilitado.
- Dependencias, con versiones gestionadas por el parent de Spring Boot: `org.postgresql:postgresql`
  (runtime), `org.flywaydb:flyway-core` y `org.flywaydb:flyway-database-postgresql`. Se incorporan
  al preparar el primer entorno duradero, junto con la migración inicial.
- Correspondencia de tipos con el diseño aprobado:

| H2 | PostgreSQL |
| :--- | :--- |
| `UUID` | `uuid` |
| `VARCHAR(n)` / `VARCHAR` sin longitud | `varchar(n)` / `text` |
| `CLOB` | `text` |
| `TIMESTAMP(6) WITH TIME ZONE` | `timestamptz(6)` |
| `INTEGER`, `BIGINT`, `BOOLEAN` | `integer`, `bigint`, `boolean` |

- El bloqueo pesimista del expediente usa `SELECT ... FOR UPDATE` también en PostgreSQL.
- Las migraciones crean exactamente las tablas, constraints e índices de los diseños aprobados
  y cargan los catálogos de `domain/catalog-values.md` con sus IDs originales. No añaden reglas.
- El volumen documental de ADR-0010 sigue siendo independiente de la base de datos.

## Alternativas consideradas

| Alternativa | A favor | En contra |
| :--- | :--- | :--- |
| PostgreSQL con Flyway | Esquema versionado y reproducible; validación de entidades al arrancar. | Mantener migraciones SQL además de las entidades; opción elegida. |
| PostgreSQL con `ddl-auto` de Hibernate | Sin herramienta adicional. | Sin control de cambios en una base duradera. |
| PostgreSQL con scripts manuales | Control total del DBA. | Proceso manual propenso a desalineación. |
| Mantener solo H2 | Sin cambios. | Pierde todo al reiniciar; no apto fuera de pruebas. |

## Consecuencias y verificación

- Cada cambio de entidad JPA requiere su migración Flyway; `validate` detecta la desalineación al arrancar.
- Las pruebas automatizadas siguen en H2. Antes de habilitar un entorno duradero se verifica
  que las migraciones se aplican en una instancia PostgreSQL vacía y que la aplicación arranca
  con `ddl-auto=validate`.
- La versión del servidor es PostgreSQL 17. Respaldos y retención se definen al desplegar, sin
  cambiar este diseño.
- La pérdida de datos al reiniciar descrita en ADR-0005 queda limitada a pruebas.
