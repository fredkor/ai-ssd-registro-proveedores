<!-- Generado con /documentar desde specs/ y backend/src/. No editar a mano: cambia la spec y regenera. -->

# Pruebas

Estrategia de [architecture.md §10](../../specs/architecture/architecture.md#10-verificación-y-preparación),
[ADR-0018](../../specs/adr/0018-backend-postgresql-en-pruebas-y-desarrollo.md) y
[traceability.md §5](../../specs/traceability.md#5-comprobaciones-transversales-previstas). El estado real lo
genera `python3 tools/spec_sync.py --md docs/sync-report.md`: [docs/sync-report.md](../sync-report.md).

## Cómo se ejecutan

`./mvnw test` desde `backend/`, con Docker: cada ejecución levanta un PostgreSQL 17 nuevo con Testcontainers en un
puerto aleatorio y aplica las migraciones Flyway. No usa la base local ni las variables `REGISTRATION_DB_*`.

## Estrategia por capa

| Capa | Ubicación en `backend/src/test/java/…/registration` | Clases de prueba | Enfoque |
| :--- | :--- | :--- | :--- |
| Dominio | `domain/` | 12 | JUnit puro, sin Spring (CLAUDE.md, regla 7). |
| Aplicación | `application/service/` | 23 | Servicios con dobles de los puertos (`testsupport/Unused*`), sin contexto Spring (architecture.md §10). |
| Adaptadores de entrada | `adapter/in/` | 8 | Controladores, Problem Details, recorte de textos y el disparador de la limpieza. |
| Adaptadores de salida | `adapter/out/` | 8 | `@DataJpaTest` con PostgreSQL de Testcontainers, firma y volumen documental. |
| Integración | raíz del paquete | 31 | `@SpringBootTest` por operación (`*IntegrationTest`), rollback de transacciones (`*RollbackTest`), seguridad del token y documentación de la API. |
| Configuración | `configuration/` | 1 | Serialización de respuestas. |

## Nombres de las pruebas

- `USxx_CAyyy_descripcion`: verifica un criterio de aceptación (CLAUDE.md, regla 7). `spec_sync` las cruza con los
  criterios verificables en backend.
- `ADRnnnn_descripcion`: verificación técnica de un ADR (p. ej. tipos SQL de ADR-0017 o el token de ADR-0015).
- `INVnn_descripcion`: regla de dominio sin criterio propio.

## Cobertura de criterios

Último resultado de `spec_sync`: Operaciones: 22 | Criterios: 136 (91 verificables en backend) | Pruebas USxx_CAyyy: 403.

| Historia | Criterios | Verificables en backend | Con al menos una prueba |
| :--- | :--- | :--- | :--- |
| [US01](../../specs/user-stories/US01-identificacion-mediante-efirma.md) | 27 | 19 | 19 |
| [US02](../../specs/user-stories/US02-verificar-existencia-del-registro.md) | 5 | 5 | 5 |
| [US03](../../specs/user-stories/US03-aceptar-politicas.md) | 8 | 6 | 6 |
| [US04](../../specs/user-stories/US04-crear-expediente-de-registro.md) | 6 | 6 | 6 |
| [US05](../../specs/user-stories/US05-reanudar-expediente-incompleto.md) | 11 | 10 | 10 |
| [US06](../../specs/user-stories/US06-navegar-y-guardar-secciones.md) | 10 | 5 | 5 |
| [US07](../../specs/user-stories/US07-finalizar-registro.md) | 7 | 0 | 0 |
| [US08](../../specs/user-stories/US08-capturar-datos-generales.md) | 27 | 17 | 17 |
| [US09](../../specs/user-stories/US09-capturar-telefonos-del-proveedor.md) | 9 | 7 | 7 |
| [US10](../../specs/user-stories/US10-gestionar-cedula-fiscal.md) | 11 | 11 | 11 |
| [US11](../../specs/user-stories/US11-capturar-participacion-y-partidas-cucop.md) | 15 | 5 | 5 |

Los criterios no verificables en backend son de interfaz, de integración externa o de etapas posteriores
([traceability.md §3](../../specs/traceability.md#3-cobertura-de-criterios-de-aceptación)); el frontend del
registro aún no tiene pantallas. El detalle por criterio, con sus clases de prueba, está en
[docs/sync-report.md](../sync-report.md) y en cada ficha de [operaciones/](operaciones/).
