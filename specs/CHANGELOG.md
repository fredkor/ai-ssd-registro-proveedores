# Registro de cambios de las especificaciones

Cada entrada registra un cambio aprobado sobre `specs/`: fecha, descripción, motivo, archivos,
compatibilidad del contrato y commits previstos. Lo crea y mantiene `/cambio-spec`.

## 2026-10-25 — Operaciones de la cédula en el modelo de dominio

- **Descripción:** el modelo de dominio lista `associateFiscalDocument` y `dissociateFiscalDocument`
  en las operaciones del agregado (§7) y en el diagrama de clases (§2.1).
- **Motivo:** divergencia E2 de la revisión de diagramas. Las operaciones existen con pruebas
  (US10 CA-001, CA-004, CA-007), y §4 y el ciclo de vida las describen, pero faltaban en §7.
- **Archivos:** `specs/architecture/domain-model.md` §2.1 y §7.
- **Contrato:** sin cambios.
- **Commits:** `design:` (este cambio). Sin commit de código: la implementación ya existe.

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

## 2026-10-25 — Tipos de dominio descritos en el modelo

- **Descripción:** `domain-model.md` describe ocho tipos de `domain.model` que el código ya usaba:
  `CertificateSubjectAttribute` y `CertificateIdentity` (§5.1), `GeneralDataCatalogContext`,
  `GeneralDataInput` y `GeneralDataViolation` (§5.2), `CucopParticipationViolation` (§5.4),
  `LegalOwnershipOption` (§5.5) y `FiscalDocumentFormat` (§4). No agrega reglas: cada una ya estaba
  en ADR-0016, INV-04, INV-20, INV-27, INV-31, INV-32, ADR-0009, ADR-0010 o ADR-0019.
- **Motivo:** avisos SYNC-09 de `tools/spec_sync.py`, abiertos en la revisión SDD del 2026-10-24.
- **Archivos:** `specs/architecture/domain-model.md` §4 y §5.
- **Contrato:** sin cambios.
- **Commits:** `design:` (este cambio).

## 2026-10-25 — ADR-0021: colecciones Postman

- **Descripción:** Postman es una verificación complementaria contra la aplicación en ejecución:
  una colección generada desde el contrato y otra de flujo, hecha a mano, que recorre el ciclo de vida
  con los fixtures de firma de prueba y un PDF ficticio. No reemplaza `./mvnw test`.
- **Motivo:** ejercitar de extremo a extremo la aplicación con su base duradera, su volumen y sus
  tokens reales, algo que las pruebas automatizadas no cubren.
- **Archivos:** `specs/adr/0021-backend-colecciones-postman.md`, `specs/adr/README.md`,
  `specs/traceability.md` §5, `specs/architecture/tech-stack.md`.
- **Contrato:** sin cambios.
- **Commits:** `design:` (este cambio); después `docs:` con las colecciones y el PDF de ejemplo.

## 2026-10-25 — Dónde se garantiza INV-27 e invariante de la selección CUCoP

- **Descripción:** INV-27 se garantiza en el dominio también para formato y tamaño
  (`FiscalDocumentFormat`); la aplicación lo invoca antes de conservar y el adaptador guarda el
  contenido, como ya describía §4. Las violaciones de CUCoP citan solo INV-31: que cada ID sea una
  partida específica es parte de INV-31 y §5.4; INV-32 es la jerarquía del catálogo.
- **Motivo:** sincronización del 2026-10-25, SYNC-20261025-01 y -04 (opción a).
- **Archivos:** `specs/architecture/domain-model.md` §3 (INV-27) y §5.4.
- **Contrato:** sin cambios.
- **Commits:** `design:` (este cambio) y `test:` (nombre de la prueba y comentario de `CucopParticipation`).
