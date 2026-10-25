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
