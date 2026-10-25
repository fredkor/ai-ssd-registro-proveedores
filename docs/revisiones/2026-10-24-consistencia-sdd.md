# Revisión de consistencia SDD — 2026-10-24

- **Prompt:** `prompts/04-revisar-consistencia-sdd.md`
- **Alcance:** todo el proyecto.
- **Revisión:** commit `f836db1` (rama `feat/save-cucop-participation`). También se revisaron los
  archivos no confirmados `README.md`, `prompts/` y `.claude/commands/` del árbol de trabajo.
- **Modo:** solo lectura. Este informe es el único archivo creado.
- No hay revisiones anteriores en `docs/revisiones/`; todos los hallazgos son nuevos.

## 1. Resumen ejecutivo

**Consistencia global: alta en specs, contrato y código; baja en la documentación derivada.**

La cadena visión → RF → US → dominio → INV → contrato → trazabilidad está completa:
- 39 de 39 invariantes definidas y referenciadas.
- 136 CA con fila en la trazabilidad.
- 22 de 22 operaciones del contrato en la matriz.
- El lint y las 621 pruebas pasan.

Las capas hexagonales se respetan, salvo una dependencia entre adaptadores.

Riesgos principales:
1. **Comandos y prompts heredados de otro proyecto** (`catalogo-productos`). Al ejecutarlos, piden
   archivos y herramientas que no existen (`tools/spec_sync.py`, `catalog-openapi.yaml`,
   `product-lifecycle.md`). (SDD-20261024-01, -02)
2. **Nueve CA sin prueba `USxx_CAyyy`**, aunque la trazabilidad prevé para ellos verificación en
   backend, contra la regla 7 de CLAUDE.md. (SDD-20261024-03)
3. **La limpieza diaria de ADR-0010 no está implementada**, y la arquitectura no le da nombre de
   caso de uso. (SDD-20261024-04)

## 2. Verificación automática

| Comprobación | Resultado |
| :--- | :--- |
| `python3 tools/spec_lint.py --require-ready` | `Errores: 0 \| Advertencias: 0` — `RESULTADO: OK — coherencia documental; no valida ejecución`. |
| `./mvnw clean test` (worktree aislado de `f836db1`) | Salida 0: 621 pruebas, 0 fallos, 0 errores y 0 omitidas. |

**Comprobaciones propias** (scripts de solo lectura sobre specs y pruebas):
- INV-01 a INV-39 definidas en `domain-model.md`; ninguna falta en `traceability.md`.
- Cada CA de US01 a US11 tiene fila en la trazabilidad, y no hay filas sin CA.
- 402 métodos de prueba siguen la convención `USxx_CAyyy`; ninguno apunta a un CA inexistente.
- Los casos de uso y puertos de `architecture.md` coinciden 1:1 con `application.port.in/out`.
- Las dependencias de `pom.xml` aparecen todas en `tech-stack.md`; no queda H2 (ADR-0005 reemplazado).
- El índice de `specs/adr/README.md` coincide con los archivos 0001 a 0020.

## 3. Mapa de fuentes

| Nivel | Documento que manda | Observación |
| :--- | :--- | :--- |
| Visión | `specs/vision.md` | Sin vacíos. US07 (finalizar) existe y está marcada como etapa posterior, coherente con §4. |
| Requerimientos | `specs/requirements/functional.md` (RF-REG-01 a 08) | Sin vacíos. |
| Historias | `specs/user-stories/US01–US11` | Usan la denominación funcional (EN_CAPTURA, COMPLETADA), que el ciclo de vida §1 mapea a códigos técnicos. Es coherente. |
| Dominio | `specs/domain/` | Coincide con los enums del código, p. ej. `SectionStatus`. |
| Invariantes | `specs/architecture/domain-model.md` | INV-30 dice dónde se garantiza de dos formas distintas (SDD-20261024-09). |
| Arquitectura | `specs/architecture/architecture.md` | No nombra el caso de uso de limpieza (SDD-20261024-04). |
| ADR | `specs/adr/` | El texto de estado está desactualizado en el índice, ADR-0008 y arquitectura §10 (SDD-20261024-06). |
| Contrato | `specs/api/supplier-registration-openapi.yaml` | Correcto. `specs/api/README.md` tiene conteos viejos (SDD-20261024-07); no hay regla de versionado (SDD-20261024-08). |
| Trazabilidad | `specs/traceability.md` | Completa. Por decisión, solo declara verificación prevista; ninguna spec declara qué está implementado (SDD-20261024-13). |
| Convenciones | `specs/conventions.md`, `CLAUDE.md` | Ejemplos de otro proyecto y prefijos de commit incompletos (SDD-20261024-05, -10). |
| Documentación derivada | `README.md`, `prompts/`, `.claude/commands/` | Restos de otro proyecto (SDD-20261024-01, -02, -11). |

No hay documentos duplicados en un mismo nivel.

## 4. Hallazgos

| ID | Dimensión | Tipo | Severidad | Evidencia | Resolución propuesta | Commit |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| SDD-20261024-01 | 9. Documentación derivada | Desactualizado | Media | `.claude/commands/diagramas.md:15,17` (`ProductController`, `product-lifecycle.md`); `documentar.md:6,34` y `documentacion-final.md:5` (`catalogo-productos`, `catalog-openapi.yaml`); `cambio-spec.md:26`; `prompts/03-documentacion-ia.md:25-37` (`catalogo-productos-v2`, "22/22 criterios"); `prompts/02-refactorizar-desde-diseno.md:21` (`CatalogoProductosApplication`). | Adaptar los comandos y prompts a este proyecto: `supplier-registration-openapi.yaml`, `supplier-registration-lifecycle.md`, los controladores y servicios reales y los 136 CA. | `docs:` |
| SDD-20261024-02 | 9. Documentación derivada | Huérfano | Media | `tools/spec_sync.py` no existe y `docs/` no existía, pero se usan en `.claude/commands/sincronizar.md:9-10`, `diagramas.md:35`, `documentar.md:20,38,41`, `documentacion-final.md:9,29` y `prompts/03-documentacion-ia.md:12,16,27,36`. Además, en `sincronizar.md` el Paso 1 numera dos veces "2." y omite `spec_lint`. | Decidir: (a) crear `spec_sync.py` adaptado, que es tooling nuevo y necesita su especificación en `tools/README.md`, o (b) quitar sus referencias y usar solo `spec_lint` y las pruebas. | `tooling:` o `docs:` |
| SDD-20261024-03 | 1 y 7. Trazabilidad y pruebas | Huérfano (CA sin prueba) | Media | La trazabilidad prevé verificación en backend, pero no hay prueba `USxx_CAyyy`: US01 CA-006 (`traceability.md:56`), US03 CA-001 (`:93`) y CA-008 (`:100`), US05 CA-002 (`:118`) y CA-007 (`:123`), US06 CA-010 (`:142`), US09 CA-008 (`:199`) y CA-009 (`:200`), US10 CA-010 (`:215`). Puede haber cobertura indirecta en pruebas con otro nombre, p. ej. `US05_retryWithTheSameToken_isAppliedOnce`, pero no se verificó. | Agregar una prueba con nombre por CA, o reclasificar en la matriz los que en realidad son de interfaz, como US03 CA-001. | `test:` (y `spec:` si se reclasifica) |
| SDD-20261024-04 | 5 y 6. ADR y arquitectura | Incumplimiento / vacío de spec | Media | ADR-0010 y `architecture.md:208` exigen la limpieza diaria; `architecture.md:92,315,318` prevé `adapter.in.scheduling` y la lógica en `application.service`. No hay caso de uso, paquete `scheduling` ni `@EnableScheduling` en `backend/src/main`. `architecture.md` §5 no nombra el caso de uso. | Primero nombrar el caso de uso de limpieza y su puerto de entrada en `architecture.md` §5 y en la trazabilidad; después implementarlo. | `design:` y luego `feat:` |
| SDD-20261024-05 | 10. Proceso SDD | Contradicción | Media | `CLAUDE.md:57` (regla 10) no incluye `refactor:` ni `docs:`, pero los usan `prompts/02-refactorizar-desde-diseno.md:3`, `.claude/commands/cambio-spec.md:32`, `sincronizar.md:29,31` y `documentar.md:46`. | Agregar `refactor:` y `docs:` a la regla 10, ya decidido para `docs:` en la propuesta de ADR-0021, o cambiar los comandos. | `tooling:` |
| SDD-20261024-06 | 8. Estado declarado | Desactualizado | Media | `specs/adr/README.md:46` lista como pendiente "incorporar driver, Flyway, Testcontainers y la migración inicial" (ya existen V1 a V17) y `:51` dice "No se declara infraestructura instalada ni pruebas de implementación ejecutadas". `0008-backend-swagger-ui-con-contrato.md:17,24` llama "borrador" al contrato, que es READY y que Maven ya usa (`pom.xml:108,128`). `architecture.md:338-339` dice "Antes de implementar catálogos / el contexto JWT", y ambos ya están implementados. | Actualizar "Preparación pendiente" y arquitectura §10. ADR-0008 se actualiza con una línea de "Actualización", como ya hizo el 2026-10-18, sin cambiar la decisión. | `design:` |
| SDD-20261024-07 | 8. Estado declarado | Desactualizado | Baja | `specs/api/README.md:5-6` dice "21 operaciones y 38 esquemas"; el contrato tiene 22 y 39 desde `ef2f344` (`getTaxRegimes`). | Corregir los conteos. | `contract:` |
| SDD-20261024-08 | 4. Contrato | Vacío de spec | Baja | `info.version` sigue en `0.1.0` tras varios cambios (`getTaxRegimes`, `taxRegimeId` obligatorio y `uniqueItems`). `API-GUIDELINES.md:5` solo fija `/api/v1`; la regla de subir versión menor o mayor existe solo en `.claude/commands/cambio-spec.md`. | Definir la regla de versionado del contrato en `API-GUIDELINES.md` y aplicarla. | `contract:` |
| SDD-20261024-09 | 2. Fuente única | Contradicción | Baja | `domain-model.md:319` dice que INV-30 se garantiza en "Aplicación: precondición del flujo"; `domain-model.md:490` la asigna a `SupplierRegistration.saveCucopParticipation`. El código la aplica en el dominio (`SupplierRegistration.java:124,134`). | Cambiar la columna "Dónde se garantiza" de INV-30 a "Dominio: precondición de la sección". | `design:` |
| SDD-20261024-10 | 3. Vocabulario | Desactualizado | Baja | `specs/conventions.md:8` da como ejemplos `DRAFT` y `ELECTRONICS`, códigos de otro proyecto que no existen en `specs/domain/`. | Usar ejemplos propios, p. ej. `IN_PROGRESS` y `LEGAL_ENTITY`. | `spec:` |
| SDD-20261024-11 | 5. ADR en el código | Desactualizado | Baja | `backend/src/main/resources/application.yaml:15` cita ADR-0011 (Angular 21) para los mensajes de validación en inglés; la regla está en ADR-0004 `:18` y ADR-0019 `:26-33`. | Corregir el comentario para que cite ADR-0004 y ADR-0019. | `fix:` |
| SDD-20261024-12 | 6. Arquitectura | Incumplimiento de código | Baja | `SupplierRegistrationPersistenceAdapter.java:3,59,68` importa `adapter.out.catalog.CucopSpecificItemJpaRepository`. `architecture.md:87` permite a `adapter.out.persistence` solo `port.out`, dominio y JPA. Se introdujo en `f836db1`. | Mover el repositorio o su consulta a un tipo compartido de persistencia, o resolver las partidas con `CucopCatalogPort` en el servicio. Es un refactor puro. | `refactor:` |
| SDD-20261024-13 | 8. Estado declarado | Vacío de spec | Baja | `traceability.md:6-7` "No declara implementación, pruebas existentes ni resultados"; `architecture.md:337` dice lo mismo. Con 22 de 22 operaciones implementadas, ninguna spec registra qué está implementado y verificado. | Decidir si la matriz gana una columna de estado, o si ese estado vive solo en la documentación derivada (`docs/`). | `spec:` o `docs:` |
| SDD-20261024-14 | 10. Proceso SDD | Incumplimiento de proceso | Baja | El contrato cambió en commits `spec:` (p. ej. `23b6903` y `808fe6a`), `design:` (`a3fa89d`) y `feat:` (`ef2f344`), no en `contract:`. Además, 10 commits iniciales no tienen prefijo, p. ej. "Creacion del dominio" y "java: Se agrega el pom". Separar los cambios evita que el lint falle a mitad de serie, pero CLAUDE.md no lo permite de forma explícita. | Para el futuro: separar `contract:`, o documentar en CLAUDE.md cuándo el contrato acompaña a otro commit. El historial no se reescribe. | `tooling:` |

## 5. Lo que no se pudo verificar

- **Cobertura indirecta de los CA de SDD-20261024-03:** no revisé si otras pruebas, con otro nombre,
  cubren su comportamiento.
- **Columna "Dónde se garantiza" de las 39 INV:** solo la contrasté con el código en INV-14, INV-30
  e INV-31. El resto se infirió por los nombres de las pruebas.
- **Pruebas `USxx_CAyyy` frente a su criterio (dimensión 7):** muestreé US11. No revisé las 402.
- **Frontend:** contiene la plantilla Sakai y no implementa pantallas del registro. Los CA de interfaz
  quedan sin verificar por diseño.
- **ADR-0016:** no validé la interoperabilidad de firma con un componente real. Solo existen los
  fixtures de node-forge.
- **Ejecución E2E:** no levanté la aplicación con `./mvnw spring-boot:run`.

## 6. Seguimiento — 2026-10-25

Todos los hallazgos quedaron **resueltos** y están en `main` (PR #21 a #24):

| ID | Estado | Commit |
| :--- | :--- | :--- |
| SDD-20261024-01 | Resuelto | Comandos y prompts adaptados (commit `docs:` de este informe). |
| SDD-20261024-02 | Resuelto | `c08ad81 tooling:` — `tools/spec_sync.py` adaptado, con pruebas. |
| SDD-20261024-03 | Resuelto | `47f8379 spec:` y `b0f6796 test:`. |
| SDD-20261024-04 | Resuelto | `83653ec design:` y `0a58e42 feat:`. |
| SDD-20261024-05 | Resuelto | `749a32c tooling:`. |
| SDD-20261024-06 | Resuelto | `b63727d design:`. |
| SDD-20261024-07 | Resuelto | `3b721de contract:`. |
| SDD-20261024-08 | Resuelto | `8c0943f contract:` — regla SemVer; contrato 0.2.0. |
| SDD-20261024-09 | Resuelto | `4360a2e design:`. |
| SDD-20261024-10 | Resuelto | `222c6db spec:`. |
| SDD-20261024-11 | Resuelto | `5d43dbf fix:`. |
| SDD-20261024-12 | Resuelto | `75b2b12 refactor:`. |
| SDD-20261024-13 | Resuelto | `407b11c design:`. |
| SDD-20261024-14 | Resuelto | `77167ab tooling:`. |

Detectados durante la resolución, abiertos para la próxima revisión:

- `spec_sync` SYNC-09: 8 tipos de `domain.model` que `domain-model.md` no describe
  (`CertificateIdentity`, `CertificateSubjectAttribute`, `CucopParticipationViolation`,
  `FiscalDocumentFormat`, `GeneralDataCatalogContext`, `GeneralDataInput`, `GeneralDataViolation`,
  `LegalOwnershipOption`).
- `IdentifyApplicantIntegrationTest.assertRejectedWithoutPersisting` cuenta evidencias y contextos
  después de ejecutar la petición, por lo que la comprobación "sin persistir" nunca falla.
