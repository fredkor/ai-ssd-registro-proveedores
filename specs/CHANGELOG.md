# Registro de cambios de las especificaciones

Cada entrada registra un cambio aprobado sobre `specs/`: fecha, descripción, motivo, archivos,
compatibilidad del contrato y commits previstos. Lo crea y mantiene `/cambio-spec`.

## 2026-10-25 — Verificación de US03 CA-001 y US05 CA-007

- **Descripción:** la verificación prevista de US03 CA-001 y US05 CA-007 pasa a ser de interfaz.
  Su parte de backend ya la verifican US03 CA-006 (aviso servido sin token) y US05 CA-003
  (reanudar conserva datos y progreso).
- **Motivo:** revisión de consistencia SDD-20261024-03. Mostrar el aviso antes de firmar y abrir
  siempre Datos Generales los decide el frontend; la respuesta de `resumeSupplierRegistration`
  no indica sección.
- **Archivos:** `specs/traceability.md` §3 (US03 CA-001, US05 CA-007).
- **Contrato:** sin cambios.
- **Commits:** `spec:` (este cambio) y `test:` (pruebas de los siete CA restantes de SDD-20261024-03).

## 2026-10-25 — Caso de uso de la limpieza diaria de archivos

- **Descripción:** la arquitectura nombra el caso de uso de la limpieza diaria de ADR-0010
  (`CleanUpUnassociatedFilesUseCase`, `CleanUpUnassociatedFilesService`, su decorador
  transaccional y `DailyFileCleanupScheduler`), las operaciones concretas de `FileMetadataPort` y
  `FileStoragePort` y la coordinación: una transacción por ejecución con bloqueo de candidatos.
- **Motivo:** revisión de consistencia SDD-20261024-04. ADR-0010 y RNF-04 definían las reglas, pero
  la arquitectura no nombraba el caso de uso y no estaba implementado.
- **Archivos:** `specs/architecture/architecture.md` §5, §6, §7 y §9; `specs/traceability.md` §5.
- **Contrato:** sin cambios. ADR-0010 sin cambios: la decisión es la misma.
- **Commits:** `design:` (este cambio) y `feat:` (implementación y pruebas).

## 2026-10-25 — Estado declarado de ADR y arquitectura

- **Descripción:** el índice de ADR registra PostgreSQL, las migraciones y la limpieza diaria como
  incorporados y remite a `tools/spec_sync.py` para el estado de implementación. ADR-0008 deja de
  llamar borrador al contrato (decisión sin cambios). Arquitectura §10 ya no presenta como
  pendientes los tipos SQL de catálogos ni el contexto JWT.
- **Motivo:** revisión de consistencia SDD-20261024-06.
- **Archivos:** `specs/adr/README.md`, `specs/adr/0008-backend-swagger-ui-con-contrato.md`,
  `specs/architecture/architecture.md` §10.
- **Contrato:** sin cambios.
- **Commits:** `design:` (este cambio).

## 2026-10-25 — Regla de versionado del contrato y versión 0.2.0

- **Descripción:** `API-GUIDELINES.md` define el versionado SemVer de `info.version`, con fase `0.x`.
  El contrato pasa de 0.1.0 a 0.2.0 para reflejar los cambios hechos sin regla:
  - `getTaxRegimes` (compatible).
  - `taxRegimeId` obligatorio en `ApplicantIdentityResponse` (compatible: propiedad nueva en respuesta).
  - `uniqueItems` en `CucopParticipationRequest.specificItemIds` (**incompatible**: validación más estricta).
- **Motivo:** revisión de consistencia SDD-20261024-08.
- **Archivos:** `specs/api/API-GUIDELINES.md`, `specs/api/supplier-registration-openapi.yaml`
  (`info.version`), `specs/api/README.md`.
- **Contrato:** versión 0.2.0; sin cambios de operaciones ni esquemas en este commit.
- **Commits:** `contract:` (este cambio).

## 2026-10-25 — Dónde se garantiza INV-30

- **Descripción:** INV-30 se garantiza en el dominio (`SupplierRegistration.saveCucopParticipation`),
  como ya indicaba la tabla de operaciones del agregado (§7) y como lo implementa el código.
- **Motivo:** revisión de consistencia SDD-20261024-09: §3 decía "Aplicación: precondición del flujo".
- **Archivos:** `specs/architecture/domain-model.md` §3.
- **Contrato:** sin cambios.
- **Commits:** `design:` (este cambio).

## 2026-10-25 — Ejemplos de códigos en las convenciones

- **Descripción:** `conventions.md` usa como ejemplos `IN_PROGRESS` y `GOODS_AND_SERVICES_SUPPLIER`,
  definidos en `specs/domain/`, en lugar de `DRAFT` y `ELECTRONICS`, que eran de otro proyecto.
- **Motivo:** revisión de consistencia SDD-20261024-10.
- **Archivos:** `specs/conventions.md`.
- **Contrato:** sin cambios.
- **Commits:** `spec:` (este cambio).

## 2026-10-25 — Dónde vive el estado de implementación

- **Descripción:** las specs declaran la verificación prevista; el estado real de implementación y
  cobertura lo genera `python3 tools/spec_sync.py --md docs/sync-report.md`, que no se edita a mano.
  `traceability.md` §1 y arquitectura §10 remiten a ese informe.
- **Motivo:** revisión de consistencia SDD-20261024-13.
- **Archivos:** `specs/traceability.md` §1, `specs/architecture/architecture.md` §10.
- **Contrato:** sin cambios.
- **Commits:** `design:` (este cambio).
