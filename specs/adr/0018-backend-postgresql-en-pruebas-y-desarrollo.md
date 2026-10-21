# ADR-0018: PostgreSQL en pruebas y desarrollo con base de pruebas desechable

- **Estado:** Aceptado
- **Ámbito:** Backend
- **Fecha:** 2026-10-21
- **Specs relacionadas:** ADR-0005 (reemplazado), ADR-0010, ADR-0015, ADR-0017; `architecture/registration-database-design.md`; `architecture/tech-stack.md`.

## Contexto

ADR-0005 usaba H2 en memoria para pruebas: cada ejecución empezaba con una base vacía, pero
las pruebas no validaban el motor real (tipos, constraints, bloqueo `FOR UPDATE`). ADR-0017 ya
fija PostgreSQL 17 con Flyway para los entornos duraderos. Se decide usar el mismo motor en
todos los entornos sin perder el arranque limpio de cada ejecución de pruebas.

## Decisión

- El registro usa PostgreSQL 17 en todos los entornos: pruebas, desarrollo local y despliegues
  (ADR-0017). Se retiran H2 y su consola.
- **Pruebas automatizadas:** cada ejecución de `./mvnw test` levanta con Testcontainers un
  contenedor nuevo de PostgreSQL 17 en un puerto aleatorio del host, nunca el de una base local.
  Flyway aplica las migraciones sobre la base vacía y el contenedor se destruye al terminar.
  Así cada ejecución empieza desde cero, como con H2.
- Dentro de una misma ejecución las clases de prueba comparten el contenedor. Cada prueba crea
  sus propios datos y no depende del orden ni de tablas vacías.
- Dependencias de prueba, con versiones gestionadas por el parent de Spring Boot:
  `org.springframework.boot:spring-boot-testcontainers` y `org.testcontainers:testcontainers-postgresql`.
  La conexión se configura con `@ServiceConnection`. Ejecutar las pruebas requiere Docker.
- **Desarrollo local:** `./mvnw spring-boot:run` se conecta a una instancia PostgreSQL 17
  administrada por el desarrollador. URL, usuario y contraseña se leen de `REGISTRATION_DB_URL`,
  `REGISTRATION_DB_USERNAME` y `REGISTRATION_DB_PASSWORD`; no se guardan credenciales en el
  repositorio. Flyway migra el esquema al arrancar y los datos persisten entre arranques.
- **Llaves JWT (ADR-0015):** sin ubicaciones configuradas, pruebas y desarrollo usan un par
  efímero con advertencia. El perfil `prod`, de los entornos desplegados, exige las llaves PEM
  y la aplicación no arranca sin ellas.
- Consecuencias de retirar H2:
  - Fuera de las pruebas, reiniciar el backend ya no pierde expedientes, aceptaciones, evidencias,
    contextos ni metadatos documentales.
  - Con llaves efímeras, los tokens emitidos antes de reiniciar dejan de validar porque cambia
    la llave, aunque su contexto siga en la base.
  - Un archivo del volumen sin metadatos (por ejemplo, tras una compensación fallida) sigue fuera
    de la limpieza automática (ADR-0010).

## Alternativas consideradas

| Alternativa | A favor | En contra |
| :--- | :--- | :--- |
| Testcontainers, contenedor por ejecución | Base limpia por ejecución, puerto aleatorio, sin pasos manuales; motor real. | Requiere Docker y añade unos segundos al arranque; opción elegida. |
| PostgreSQL de pruebas fijo en otro puerto (Compose) | Sin dependencias nuevas. | Hay que levantarlo a mano; los datos se acumulan y las ejecuciones en paralelo chocan. |
| Mantener H2 para pruebas (ADR-0005) | Rápido y sin Docker. | No valida tipos, constraints ni bloqueo del motor real. |

## Consecuencias y verificación

- Las pruebas de persistencia verifican tipos y constraints con el catálogo de PostgreSQL.
- La migración inicial de Flyway crea las tablas ya implementadas; Hibernate solo valida
  (`ddl-auto=validate`) en todos los entornos.
- Sin Docker disponible, `./mvnw test` no puede ejecutarse; esto aplica también a CI.
