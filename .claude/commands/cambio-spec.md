---
description: Gestiona un cambio sobre las especificaciones con análisis de impacto, orden de commits y registro (ítem 4)
argument-hint: <descripción del cambio>
---

Gestiona este cambio sobre las especificaciones siguiendo SDD y CLAUDE.md:

> $ARGUMENTS

Si no se indicó un cambio, pide que lo describa y DETENTE.

## Paso 1 — Clasificar el cambio
- **Tipo:** negocio (visión, RF, historia, criterio, catálogo, ciclo de vida) · diseño (arquitectura,
  modelo, ADR) · contrato (OpenAPI) · tooling.
- **Compatibilidad del contrato:** ¿rompe a los clientes (campo obligatorio nuevo, validación más estricta,
  ruta o código cambiado, valor de enum eliminado)? Clasifícalo y fija la nueva `info.version` según
  "Versionado del contrato" de `specs/api/API-GUIDELINES.md`. Desde `1.0.0`, un cambio incompatible
  exige primero un ADR sobre la convivencia con `/api/v1/`: si no existe, DETENTE y propónlo.
- **¿Contradice un ADR aceptado?** Entonces el cambio exige un ADR nuevo que lo reemplace (estado
  "Reemplazado por ADR-NNNN" en el anterior), nunca editar el ADR aceptado en silencio.

## Paso 2 — Análisis de impacto (sin editar)
Recorre la cadena de trazabilidad y lista **cada** archivo afectado con la línea o sección:
visión → RF → historia y criterios → `domain/` → `domain-model.md` (INV) → `architecture.md` → ADR →
`supplier-registration-openapi.yaml` → `traceability.md` → código (por capa) → pruebas → documentación derivada → Postman (si existe).
Indica también qué **no** cambia y por qué. Señala ambigüedades y pregúntame (máximo 5 preguntas).

## Paso 3 — Propuesta y orden de commits
Redacta el texto exacto de cada cambio de spec y el plan de commits, uno por tipo, en este orden:
1. `spec:` (negocio) → 2. `design:` (arquitectura / ADR) → 3. `contract:` (OpenAPI) →
4. `feat:` / `fix:` / `refactor:` (código + pruebas) → 5. `docs:` (documentación derivada).
Espera mi aprobación antes de editar.

## Paso 4 — Aplicar las specs
Aplica solo los commits de specs aprobados (1 a 3). Tras cada uno: `python tools/spec_lint.py`.
Registra el cambio en `specs/CHANGELOG.md` (créalo si no existe) con: fecha, descripción, motivo,
archivos de spec modificados, compatibilidad del contrato y commits previstos.

## Paso 5 — Siguiente paso
No implementes código en este comando. Indica qué ejecutar después:
`/implementar <operationId>` (operación nueva), `/refactorizar ...` (cambio de diseño) o una corrección
`fix:`; y al final `/sincronizar` y `/documentar` para cerrar el ciclo.
