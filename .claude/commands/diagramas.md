---
description: Verifica y actualiza los diagramas Mermaid y modelos arquitectónicos contra el código (ítem 2)
argument-hint: [archivo de spec, opcional]
---

Revisa y actualiza los diagramas y modelos arquitectónicos del proyecto, siguiendo CLAUDE.md.
Si se indicó `$ARGUMENTS`, limítate a ese archivo.

## Alcance
| Diagrama | Archivo | Se contrasta con |
| :--- | :--- | :--- |
| Capas y dependencias (flowchart) | `specs/architecture/architecture.md` §3 | Paquetes e `import` reales de `backend/src/main/java` |
| Modelos por frontera (tabla) | `specs/architecture/architecture.md` §4 | Commands, Queries y Results de `application.port.in` y DTO de `adapter.in.web.generated` |
| Árbol de paquetes | `specs/architecture/architecture.md` §9 | Estructura real de `backend/src/main/java/mx/com/fredkorlabs/registration` |
| Guardar Datos Generales (secuencia) | `specs/architecture/architecture.md` §8 | `SupplierRegistrationsController`, `SaveGeneralDataService`, sus puertos y `SupplierRegistration.saveGeneralData()` |
| Modelo de dominio (classDiagram) | `specs/architecture/domain-model.md` §2 | Campos y métodos públicos de `domain.model` |
| Modelo de datos (erDiagram) | `specs/architecture/registration-database-design.md` §1–§5 y `catalog-database-proposal.md` | Migraciones de `backend/src/main/resources/db/migration` y entidades JPA |
| Ciclo de vida (tabla de transiciones) | `specs/domain/supplier-registration-lifecycle.md` §4 | Métodos de `SupplierRegistration` y sus pruebas |

## Pasos
1. **Compara (sin editar):** para cada diagrama, tabla de diferencias: elemento, qué dice el diagrama,
   qué hay en el código, y clasificación:
   - **El diagrama está desactualizado** (el código cumple la spec escrita y el dibujo quedó atrás):
     se corrige el diagrama con un commit `design:`.
   - **El código se desvió de la spec:** NO toques el diagrama. Es un `fix:` o `refactor:` aparte;
     repórtalo y DETENTE en ese elemento.
   - **Error de forma** (indentación del árbol, nodos o flechas mal escritos, sintaxis Mermaid inválida).
   Revisa también que cada diagrama sea coherente con las tablas de su mismo archivo.
2. **Espera mi aprobación** de la clasificación.
3. **Actualiza** solo los diagramas aprobados. Mantén el estilo existente (nombres, idioma, nivel de detalle).
   No agregues elementos que no existan en el código **y** en la spec.
4. **Diagramas nuevos (opcional, solo en `docs/tecnica/diagramas.md`, nunca en specs/):** un diagrama de
   secuencia por cada `operationId` siguiendo el formato de `architecture.md` §8, y un diagrama de componentes
   adaptador → puerto → servicio. Son documentación derivada: deben poder regenerarse.
5. **Valida** la sintaxis de cada bloque Mermaid modificado (si tienes `npx @mermaid-js/mermaid-cli`, úsalo;
   si no, revísalo con cuidado y dilo). Ejecuta `python tools/spec_lint.py` y `python tools/spec_sync.py`.
6. **Informe:** diagramas modificados, divergencias de código reportadas y commits propuestos
   (`design: diagramas sincronizados con la implementación`, `docs: diagramas de secuencia por operación`).
