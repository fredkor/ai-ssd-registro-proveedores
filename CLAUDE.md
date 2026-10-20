# CLAUDE.md — Registro de proveedores de Fredkor Labs

El proyecto sigue Spec-Driven Development (SDD). Las especificaciones de `specs/` definen
el comportamiento; no se implementan reglas que no estén especificadas.

## Fuentes de verdad

Leer los documentos aplicables antes de modificar o implementar una funcionalidad:

1. [Visión](specs/vision.md): alcance de la primera etapa y etapas posteriores.
2. [Requerimientos funcionales](specs/requirements/functional.md): RF-REG-01 a RF-REG-08.
3. [Historias de usuario](specs/user-stories/): archivos US01 a US11, con criterios CA-001, etc.
   Sus encabezados conservan los identificadores HU-REG-001 a HU-REG-011; la numeración corresponde.
4. [Ciclo de vida](specs/domain/supplier-registration-lifecycle.md) y
   [catálogos](specs/domain/catalogs.md): estados, condiciones de avance, códigos y relaciones.
5. [Modelo de dominio](specs/architecture/domain-model.md): entidades, objetos de valor,
   relaciones, operaciones e invariantes INV-01 a INV-39.
6. [Arquitectura](specs/architecture/architecture.md): capas, casos de uso, puertos y transacciones.
7. [ADR aceptados](specs/adr/README.md): decisiones técnicas y precisiones pendientes.
8. [Política de Aviso](specs/requirements/registration-notice.md): texto y versión 1.0.
9. [Guía de API](specs/api/API-GUIDELINES.md), [estado del contrato](specs/api/README.md) y
   [borrador OpenAPI](specs/api/supplier-registration-openapi.yaml).
10. [Convenciones](specs/conventions.md) y [stack](specs/architecture/tech-stack.md).
11. [Trazabilidad](specs/traceability.md): operaciones, RF/HU, invariantes y verificación prevista del registro.


## Reglas obligatorias
1. **Si la spec no alcanza, DETENTE.** No inventes campos, reglas, estados, rutas ni códigos de error.
   Explica el vacío y propone el cambio a la spec; solo se continúa cuando la spec esté actualizada.
2. **Contrato 1:1:** el controlador implementa la interfaz generada `ProductsApi`. Nunca escribas
   a mano DTO web ni cambies rutas, nombres u operationId.
3. **Arquitectura hexagonal (ADR-0001):**
    - `domain`: Java puro. Sin Spring, sin JPA, sin puertos. Las reglas INV-xx viven aquí.
    - `application.port.in`: interfaces `*UseCase` + Commands, Queries y Results (`record`).
    - `application.port.out`: `ProductPersistencePort`.
    - `application.service`: un servicio por caso de uso; orquesta, no contiene reglas de negocio.
    - `adapter.in.web`: solo traduce HTTP <-> Command/Result y excepciones -> ErrorResponse.
    - `adapter.out.persistence`: implementa el puerto con JPA.
4. **El dominio nunca sale de `application`:** el controlador recibe `ProductResult`, nunca `Product`.
5. **Enums** idénticos a `specs/domain/` (mismos valores, mismo orden).
6. **Errores** exactamente como ADR-0004 (HTTP + `code`).
7. **Pruebas:** al menos una por criterio de aceptación, con el nombre
   `USxx_CAyy_descripcionEnIngles` (ej. `US03_CA02_activateWithoutPrice_isRejected`).
   Las reglas de dominio se prueban con JUnit puro, sin Spring.
8. **Idioma (`specs/conventions.md`):** código en inglés; documentación en castellano.
9. **Antes de cada commit:** `python tools/spec_lint.py` debe dar OK y `mvn test` debe pasar.
10. **Commits:** prefijos `spec:`, `design:`, `contract:`, `tooling:`, `feat:`, `test:`, `fix:`.

## Stack y comandos
Stack: `specs/architecture/tech-stack.md`. No agregues dependencias sin un ADR.

| Acción | Comando |
| :--- | :--- |
| Validar specs y contrato | `python tools/spec_lint.py` |
| Regenerar la API desde el contrato | `./mvnw clean generate-sources` |
| Compilar y probar | `./mvnw test` |
| Ejecutar la aplicación | `./mvnw spring-boot:run` |

Nunca edites archivos dentro de `target/generated-sources`: se regeneran desde el contrato.
