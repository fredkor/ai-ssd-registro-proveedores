---
description: Genera la documentación técnica del proyecto desde specs/ y el código (ítem 1)
argument-hint: [sección a regenerar, opcional: arquitectura | api | dominio | pruebas | operacion]
---

Genera la documentación técnica del Registro de proveedores en `docs/tecnica/`, siguiendo CLAUDE.md.
Si se indicó `$ARGUMENTS`, regenera solo esa sección.

## Principios (SDD)
- `specs/` es la fuente de verdad. La documentación **explica y enlaza** las specs; no las copia ni
  las contradice, y **nunca introduce reglas, campos ni comportamientos** que no estén en ellas.
- Todo dato debe venir de una fuente verificable: cita el archivo de spec (y sección) o la clase Java.
  Si el código hace algo que ninguna spec describe, no lo documentes como regla: anótalo en
  "Divergencias detectadas" y continúa.
- La documentación es **regenerable**: al inicio de cada archivo pon
  `<!-- Generado con /documentar desde specs/ y backend/src/. No editar a mano: cambia la spec y regenera. -->`.

## Pasos
1. **Inventario (sin escribir):** lee las fuentes de verdad de CLAUDE.md, `backend/pom.xml`, `backend/src/main/resources/application.yaml`
   y los paquetes de `backend/src/main/java`. Ejecuta `python tools/spec_lint.py` y `python tools/spec_sync.py`.
   Si `spec_sync` da errores, muéstralos y pregunta si documento igualmente (marcando las divergencias)
   o si primero se corrigen.
2. **Plan:** lista los archivos que vas a crear o actualizar y la fuente de cada sección. Espera mi aprobación.
3. **Genera** en `docs/tecnica/`:
   - `README.md` — índice, propósito (de `vision.md`), cómo compilar, probar y ejecutar (comandos de CLAUDE.md
     y del `README.md` raíz: Docker, PostgreSQL y variables de entorno; Swagger UI; colecciones de
     `postman/` si existen). Si existe `diagramas.md`, inclúyelo en el índice.
   - `arquitectura.md` — capas y regla de dependencia (ADR-0001), tabla **paquete → clases reales**
     (recorriendo `backend/src/main/java`), flujo Request → Command → `SupplierRegistration` → entidad JPA →
     Result → Response.
     Los diagramas de capas y de modelos por frontera reflejan los de `architecture.md` §2 y §3
     (mismas dependencias y sentidos de conversión).
   - `diagramas.md` — **no lo generes ni lo edites**: lo mantiene `/diagramas`. Si existe, enlázalo desde el
     índice, desde `arquitectura.md` y desde cada `operaciones/<operationId>.md` (sección de su operación).
   - `api.md` — una sección por `operationId`: método, ruta, request, respuestas y códigos de error
     (de `supplier-registration-openapi.yaml`, ADR-0004 y ADR-0019), con un ejemplo de petición y respuesta tomado del contrato.
   - `dominio.md` — glosario, estados y transiciones, invariantes INV-01..INV-39 con la clase que las garantiza.
   - `operaciones/<operationId>.md` — por operación: historia, criterios, caso de uso, servicio, invariantes,
     archivos implicados en cada capa y pruebas (de `traceability.md` y del código).
   - `pruebas.md` — estrategia por capa y cobertura de criterios (usa `python tools/spec_sync.py --md`).
   - `decisiones.md` — resumen de una línea por ADR con enlace.
4. **Verifica:** todos los enlaces relativos apuntan a archivos existentes; ningún `operationId`, código de
   error, estado o categoría aparece escrito distinto a la spec; `python tools/spec_sync.py` sigue igual.
5. **Informe:**
   - archivos generados;
   - divergencias detectadas, cada una con ID `DOC-AAAAMMDD-NN` (fecha + correlativo), listadas también
     en la sección "Divergencias detectadas" de `docs/tecnica/README.md`;
   - mensaje de commit propuesto (`docs: documentación técnica generada desde specs`).
   No hagas commit.