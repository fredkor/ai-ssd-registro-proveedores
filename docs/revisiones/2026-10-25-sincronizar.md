# Sincronización specs ↔ implementación — 2026-10-25

- **Comando:** `/sincronizar` (`.claude/commands/sincronizar.md`).
- **Revisión:** `main` en `6f500e8` (después de los PR #21 a #30).
- **Revisión anterior:** [2026-10-24-consistencia-sdd.md](2026-10-24-consistencia-sdd.md). Sus 14 hallazgos
  están resueltos; los dos que dejó abiertos se resolvieron en #26 y #27 (ver §5).

## 1. Verificación automática

| Comando | Resultado |
| :--- | :--- |
| `python3 tools/spec_lint.py --require-ready` | `Errores: 0 \| Advertencias: 0` — `RESULTADO: OK — coherencia documental; no valida ejecución`. |
| `python3 tools/spec_sync.py --md docs/sync-report.md` | 22 operaciones; 136 criterios (91 verificables en backend); 409 pruebas `USxx_CAyyy`. `Errores: 0 \| Advertencias: 0` — `RESULTADO: OK - specs e implementación sincronizadas`. |
| `./mvnw clean test` (worktree aislado de `6f500e8`, Docker) | `BUILD SUCCESS`: 82 clases, 642 pruebas, 0 fallos, 0 errores, 0 omitidas. |

## 2. Verificación con criterio

| Revisión | Resultado | Evidencia |
| :--- | :--- | :--- |
| Errores (ADR-0004, ADR-0019) | Conforme. Cada excepción se traduce al `code` correcto; los 18 códigos tienen el HTTP de ADR-0004 y los 17 tipos de ADR-0019, su `title` exacto. | `GlobalExceptionHandler.java:62-304`; `ProblemResponses.java:17,57-91`. |
| Arquitectura (§3) | Conforme. Sin imports prohibidos (SYNC-12). Los controladores no importan `domain` ni `port.out`; solo el handler usa excepciones de dominio, que §3 permite. | `spec_sync`; `adapter/in/web/*Controller.java`. |
| ADR-0007 | Conforme: el 500 responde "Unexpected error" sin detalles y registra la causa. | `GlobalExceptionHandler.java:304-309`; `ProblemErrorController.java:40-41`. |
| ADR-0008 / ADR-0009 | Conforme: Swagger UI carga el contrato; las propiedades ausentes o desconocidas se rechazan. | `ADR0008_swaggerUi_loadsTheAuthoritativeContract`; `ADR0009_*`. |
| ADR-0010 | Conforme: ruta `yyyy/MM/dd/{rfc}/{uuid}.pdf` en UTC y limpieza diaria. | `UploadFiscalDocumentService.java:26-27,55-57`; `DailyFileCleanupScheduler`. |
| ADR-0015 / ADR-0017 / ADR-0018 | Conforme: JWT de 60 min, `ddl-auto: validate`, `Clock` UTC y Testcontainers. | `IdentifyApplicantService.java:28`; `application.yaml:10,13`; `ClockConfiguration.java:13`. |
| ADR-0011 a ADR-0014 | Sin implementación: el frontend tiene solo la plantilla Sakai. No es un desvío, pero la entrega final debe declararlo. | `frontend/src/app`. |
| Invariantes (§3, "Dónde se garantiza") | Desvío en INV-27 (SYNC-20261025-01). Revisé INV-09, 13, 14, 15, 20, 30, 31 y 32 contra el código. | Ver §4. |
| Pruebas `USxx_CAyyy` | Muestreo de US02, US04, US10 y US11. Cinco pruebas técnicas llevan el nombre de un CA que no verifican (SYNC-20261025-03). | Ver §4. |
| Documentación | No existe `docs/tecnica/`. El README y los comandos no contradicen las specs. | — |

## 3. Clasificación

Ningún desvío cambia comportamiento del contrato. SYNC-20261025-02 es un defecto de código que solo
aparece con una configuración regional concreta de la JVM.

## 4. Hallazgos

| ID | Estado | Tipo | Evidencia | Resolución propuesta | Commit |
| :--- | :--- | :--- | :--- | :--- | :--- |
| SYNC-20261025-01 | Nuevo | Spec desactualizada (el código tiene razón) | `domain-model.md:311` dice que el formato y el tamaño de INV-27 se garantizan en "aplicación y adaptador de archivos". El código los valida en el dominio (`domain/model/FiscalDocumentFormat.java:26-37`) y la aplicación solo lo invoca (`UploadFiscalDocumentService.java:52`). `domain-model.md` §4 (#27) ya lo describe así: la spec se contradice. | Columna de INV-27: "Dominio: obligatoriedad, cantidad, formato y tamaño (`FiscalDocumentFormat`); aplicación: validar antes de conservar; adaptador de archivos: guardar el contenido". **Requiere tu aprobación** (ajusta la spec al código; la decisión es coherente con ADR-0001). | `design:` |
| SYNC-20261025-02 | Nuevo | El código no cumple la spec | `ApplicantIdentity.java:16`: `rfc.value().toLowerCase()` usa la configuración regional de la JVM. Con `tr` o `az`, la `I` de un RFC se convierte en `ı`, así que el usuario deja de ser "el RFC en minúsculas" (INV-15). El resto del código ya usa `Locale.ROOT` (`FiscalDocumentFormat.java:30`). | `toLowerCase(Locale.ROOT)` y una prueba de dominio con RFC que contenga `I` bajo `Locale.forLanguageTag("tr")`. | `fix:` |
| SYNC-20261025-03 | Nuevo | Prueba con nombre de un CA que no verifica | Hay comprobaciones técnicas con nombre de CA, que inflan la cobertura del criterio. Cada CA conserva otras pruebas que sí lo verifican. `CheckRegistrationExistenceIntegrationTest.java:125` `US02_CA004_withoutToken_returns401` es un 401 (ADR-0015); CA-004 trata de un fallo de consulta. `NoticeAcceptancePersistenceAdapterTest.java:62` `US03_CA003_schema_usesApprovedSqlTypes`, y en `SupplierRegistrationPersistenceAdapterTest.java`, la `:185` `US04_CA001_schema_usesApprovedSqlTypes`, la `:113` `US02_CA003_database_rejectsUnknownStatus` y la `:176` `US04_CA002_database_rejectsUnknownSection` comprueban tipos SQL y checks de BD (ADR-0017). | Renombrar a `ADR0015_…` y `ADR0017_…`, como el resto de pruebas técnicas. Solo cambia el nombre; `spec_sync` sigue en verde. | `test:` |
| SYNC-20261025-04 | Nuevo | Trazabilidad: invariante citada que no es la de la regla | Se cita INV-32 (jerarquía del catálogo) para la regla "solo se seleccionan partidas específicas", que pertenece a INV-31 y a §5.4 ("referencias a `CucopSpecificItem`"). Ocurre en `CucopParticipation.java:31-32` y en la prueba `INV32_database_rejectsSelectionsThatAreNotSpecificItems`. | Opción (a): citar INV-31 en el comentario y renombrar la prueba (`refactor:`/`test:`). Opción (b): si la intención era que INV-32 cubriera la selección, ampliar su texto (`design:`). **Requiere tu decisión.** | `test:` o `design:` |
| SYNC-20261025-05 | Nuevo | Decisión pendiente (documentación derivada) | `docs/sync-report.md` se generó en esta revisión y no está versionado. `traceability.md` §1 lo nombra como la fuente del estado real. | Opción (a): versionarlo en cada `/sincronizar` como evidencia (`docs:`). Opción (b): ignorarlo en git y regenerarlo cuando haga falta. | `docs:` o `tooling:` |
| SYNC-20261025-06 | Nuevo | Decisión pendiente (documentación derivada) | `.claude/commands/documentacion-final.md` propone la etiqueta `v1.0.0`, mientras el contrato está en `0.2.0` y `API-GUIDELINES.md` reserva `1.0.0` para la primera versión estable con clientes. | Decidir la etiqueta de la entrega (p. ej. `etapa-1` o `v0.2.0`) antes de `/documentacion-final` y ajustar el comando. | `docs:` |

## 5. Seguimiento de la revisión anterior

| ID / nota | Estado | Commit |
| :--- | :--- | :--- |
| SDD-20261024-01 a -14 | Resueltos | Ver la §6 de la revisión anterior. |
| Avisos SYNC-09 (8 tipos de `domain.model`) | Resuelto | `92511de design:` (#27). |
| `assertRejectedWithoutPersisting` cuenta después de la petición | Resuelto | `a6d2632 fix:` (#26). |

## 6. Lo que no se verificó

- **"Dónde se garantiza":** no contrasté esa columna contra el código en las 39 INV, solo en las nueve listadas en §2.
- **Pruebas `USxx_CAyyy`:** no leí las 409 contra su criterio. Muestreé US02, US04, US10 y US11 y busqué
  nombres técnicos (`schema`, `database`, `withoutToken`).
- **Frontend:** no hay pantallas del registro; los 45 CA que no son verificables en backend quedan fuera.

## 7. Seguimiento — 2026-10-25

Resoluciones elegidas por el usuario (recomendación de este informe):

| ID | Estado | Commit |
| :--- | :--- | :--- |
| SYNC-20261025-01 | Resuelto | `5d6cfba design:` — INV-27 se garantiza en el dominio. |
| SYNC-20261025-02 | Resuelto | `24a1b11 fix:` — `toLowerCase(Locale.ROOT)`; la prueba nueva falla con el código anterior. |
| SYNC-20261025-03 | Resuelto | `9f2b5e8 test:` — cinco pruebas renombradas a `ADR0015_` y `ADR0017_`. |
| SYNC-20261025-04 | Resuelto (opción a) | `5d6cfba design:` y `89f2527 test:` — la selección cita INV-31. |
| SYNC-20261025-05 | Resuelto (opción a) | `docs/sync-report.md` se versiona con cada revisión (commit `docs:` de este informe). |
| SYNC-20261025-06 | Resuelto | `451dd2e docs:` — la entrega propone la etiqueta `etapa-1`. |
