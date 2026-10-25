# Prompt: Revisión de consistencia SDD (especialista agnóstico)

Se usa para auditar el proyecto completo con la mirada de un especialista en Spec-Driven Development
que no depende de un stack, una herramienta ni un proveedor de IA concreto. A diferencia de
`/sincronizar`, no parte de los scripts del repositorio: evalúa si la cadena de especificaciones es
coherente por sí misma y si el código la respeta.
Variables: {{ALCANCE}} = todo el proyecto, o una parte (p. ej. "US10 y la cédula fiscal");
{{FECHA}} = fecha de la revisión (AAAA-MM-DD).

```
Actúa como especialista en Spec-Driven Development (SDD), agnóstico de tecnología, framework y
herramientas. Tu criterio es metodológico: las especificaciones son la fuente de verdad, cada regla
tiene un único origen y todo lo implementado se puede rastrear hasta una spec, y al revés.

Revisa la consistencia del proyecto "Registro de proveedores de Fredkor Labs".
Alcance: {{ALCANCE}}.

## Reglas de la revisión
- Es una revisión de SOLO LECTURA: no edites specs/, código, pruebas ni documentación.
- Toda afirmación lleva evidencia `archivo:línea` o `archivo §sección`. Sin evidencia, no es hallazgo.
- No inventes reglas: si algo no está especificado, repórtalo como VACÍO de spec, no como error del código.
- Distingue lo verificado (lo leíste o ejecutaste) de lo inferido. Si no pudiste verificar algo, dilo.
- No propongas tecnologías ni dependencias nuevas; evalúa coherencia, no preferencias.

## Paso 1 — Mapa de fuentes
Lee CLAUDE.md y el orden de fuentes de verdad que define. Construye la cadena del proyecto:
visión → requerimientos (RF-REG-xx) → historias (USxx, CA-xxx) → dominio (ciclo de vida, catálogos)
→ modelo de dominio (INV-xx) → arquitectura → ADR → contrato OpenAPI → trazabilidad → código → pruebas
→ documentación derivada (README.md, prompts/, .claude/commands/).
Indica qué documento manda en cada nivel y si alguno falta o está duplicado.

## Paso 2 — Verificación automática disponible
Ejecuta `python3 tools/spec_lint.py --require-ready` desde la raíz y reporta su salida. Recuerda que
solo valida coherencia documental: no sustituye las revisiones siguientes. Si puedes, ejecuta
`./mvnw test` desde `backend/` (requiere Docker) y reporta pruebas, fallos y errores; si no, dilo.

## Paso 3 — Revisión con criterio
Evalúa cada dimensión y reporta hallazgos con evidencia:

1. **Trazabilidad vertical:** cada RF tiene historias; cada CA verificable en backend tiene operación,
   regla y prueba `USxx_CAyyy_...`; cada operación del contrato aparece en `traceability.md` y tiene
   controlador. Busca huérfanos en ambos sentidos (spec sin código, código sin spec).
2. **Fuente única de cada regla:** una INV-xx, un código de error o un valor de catálogo se define en
   un solo lugar y los demás lo referencian. Señala reglas repetidas con redacción distinta o
   contradictoria (p. ej. historia vs. `domain-model.md` vs. descripción del contrato).
3. **Vocabulario y enums:** mismos nombres, valores y orden entre `specs/domain/`, el modelo de dominio,
   el contrato y el código (regla 5 de CLAUDE.md). Mismos términos de negocio en todos los documentos.
4. **Contrato:** rutas, operationId, esquemas, obligatoriedad, null explícito (ADR-0009) y errores
   (HTTP y `code` de ADR-0004 como Problem Details de ADR-0019) coinciden con las historias y el dominio.
5. **Decisiones (ADR):** cada ADR aceptado se refleja en specs y código; ningún ADR reemplazado sigue
   usándose; ninguna decisión técnica vigente carece de ADR (dependencias, persistencia, seguridad).
   Revisa que el índice `specs/adr/README.md` coincida con los archivos.
6. **Arquitectura:** las capas y dependencias de `architecture.md` y ADR-0001 se cumplen (dominio sin
   framework, servicios sin anotaciones, controladores sin tipos de dominio). Revisa los `import`.
7. **Pruebas como especificación ejecutable:** las pruebas verifican lo que dice su CA, no solo llevan
   su nombre; las reglas de dominio se prueban sin framework.
8. **Estado declarado vs. real:** lo que los documentos dicen que está pendiente, implementado o
   verificado (p. ej. "Preparación pendiente" de los ADR, estado del contrato) corresponde al repositorio.
9. **Documentación derivada:** README.md, prompts y comandos no contradicen las specs ni prometen
   comandos, rutas o archivos que no existen.
10. **Proceso SDD:** el historial de commits respeta el orden spec → design → contract → código y los
    prefijos de CLAUDE.md; los cambios de comportamiento entraron primero en la spec.

## Paso 4 — Clasificación
Para cada hallazgo indica:
- **Tipo:** contradicción · vacío de spec · huérfano · duplicado · desactualizado · incumplimiento de código.
- **Severidad:** alta (comportamiento o contrato distinto de la spec) · media (trazabilidad rota o
  regla duplicada que puede divergir) · baja (redacción, enlaces, índice).
- **Quién manda:** la spec, salvo que el código refleje una decisión legítima no documentada; en ese
  caso la resolución es actualizar la spec o un ADR, y requiere aprobación explícita.
- **Resolución propuesta y commit:** `spec:`, `design:`, `contract:`, `feat:`/`fix:`, `test:`, `docs:`.

## Paso 5 — Informe
Escribe el informe en `docs/revisiones/{{FECHA}}-consistencia-sdd.md` con:
1. Resumen ejecutivo: nivel de consistencia global (alto / medio / bajo) y los 3 riesgos principales.
2. Resultado de la verificación automática (Paso 2).
3. Mapa de fuentes (Paso 1) con los vacíos o duplicados detectados.
4. Tabla de hallazgos: ID `SDD-AAAAMMDD-NN`, dimensión, tipo, severidad, evidencia, resolución, commit.
   Si existe una revisión anterior en `docs/revisiones/`, conserva el ID de los hallazgos que sigan
   abiertos y marca su estado (nuevo / sigue abierto / resuelto).
5. Lo que no se pudo verificar y por qué.

No corrijas nada: termina con la lista de hallazgos y espera a que yo elija cuáles resolver.
```

## Ejemplo de uso
- {{ALCANCE}} = todo el proyecto
- {{FECHA}} = 2026-10-24
