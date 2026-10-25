---
description: Prepara la documentación final de la solución con evidencia de consistencia (ítem 5)
---

Prepara la documentación final del Registro de proveedores para entregar, siguiendo CLAUDE.md.

## Precondiciones (si alguna falla, DETENTE y reporta)
1. `python tools/spec_lint.py` → OK.
2. `python tools/spec_sync.py --md docs/sync-report.md` → OK (0 errores). Las advertencias se listan en la entrega.
3. `./mvnw clean test` (desde `backend/`, con Docker) → todas las pruebas en verde.
4. `docs/tecnica/` generada con `/documentar` y posterior al último cambio de `specs/`
   (compara fechas de commit). Si está desactualizada, regenérala primero.

## Entregable: `docs/entrega/`
1. `README.md` — documento principal, en castellano:
   - Resumen de la solución y alcance (de `vision.md`), incluido lo que queda **fuera de alcance**.
   - Cómo ejecutar: requisitos (Java de `backend/pom.xml`, Maven wrapper, Docker, Python + PyYAML),
     PostgreSQL y variables de entorno (del `README.md` raíz), Swagger UI y colecciones Postman si existen.
   - Arquitectura en una página con un diagrama y enlace a `docs/tecnica/arquitectura.md`.
   - Tabla de funcionalidades: RF → historia → `operationId` → estado (implementado y probado).
   - Decisiones de arquitectura: tabla de ADR con su estado.
   - Calidad: número de pruebas por capa, criterios de aceptación verificables en backend cubiertos según `traceability.md` (valor real),
     resultado de `spec_lint` y `spec_sync`.
   - Proceso SDD seguido: fases, tipos de commit y comandos usados (`/implementar`, `/refactorizar`,
     `/cambio-spec`, `/sincronizar`, `/documentar`). Si hay historial de git, resume los commits por prefijo.
   - Limitaciones conocidas y trabajo futuro: requerimientos no funcionales pendientes y advertencias de `spec_sync`.
2. `matriz-trazabilidad.md` — matriz completa RF → US → CA → INV → operationId → caso de uso → servicio →
   clases de prueba → métodos de prueba (desde `traceability.md` y el informe de `spec_sync`).
3. `evidencia/` — salida de `spec_lint`, `spec_sync` y el resumen de `backend/target/surefire-reports`.

## Reglas
- No inventes métricas ni estados: cada número sale de un comando ejecutado en este momento.
- Enlaza las specs; no las dupliques.
- Verifica que todos los enlaces relativos funcionan.
- Propón el commit `docs: documentación final de la solución` y la etiqueta `v1.0.0`. No hagas commit ni tag.
