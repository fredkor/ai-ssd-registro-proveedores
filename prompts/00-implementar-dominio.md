# Prompt: Implementar la capa de dominio del registro

Usar antes de implementar operaciones con `01-implementar-operacion.md`.
Las rutas indicadas se resuelven desde la raíz del repositorio.

```text
Implementa la capa domain del registro de proveedores siguiendo CLAUDE.md.

1. ANALIZA sin escribir código:
   - Lee specs/vision.md, specs/requirements/functional.md, specs/architecture/domain-model.md,
     specs/domain/supplier-registration-lifecycle.md y specs/domain/catalogs.md.
   - Consulta specs/user-stories/US01 a US11 y specs/traceability.md para identificar los CA
     que corresponden al dominio de la primera etapa.
   - Lee specs/architecture/architecture.md y los ADR aplicables en specs/adr/;
     usa specs/adr/README.md para localizar los nombres completos.
   - Limita el alcance a inicio/reanudación, Datos Generales, teléfonos, cédula y CUCoP.
     Contactos es continuación; no implementes su captura, finalización ni SupplierCode.
   - Revisa src/ y pom.xml. El paquete previsto del registro es mx.com.fredkorlabs.registration;
     el arranque actual está en mx.com.fredkorlabs.sdd.supplier. Incluye en el plan cualquier
     alineación necesaria, sin mover archivos ajenos por inferencia.
   - Reporta contradicciones o vacíos y consulta los que bloqueen reglas afectadas.
     Continúa con el análisis independiente; no inventes decisiones de negocio.

2. PLANIFICA: lista archivos en domain.model y domain.exception, responsabilidades,
   invariantes INV y fuentes RF/US/CA. Reutiliza código existente. Espera mi aprobación.

3. IMPLEMENTA Java puro: sin Spring, JPA, Jackson, HTTP ni puertos en dominio.
   - Los puertos pertenecen a aplicación; las consultas a catálogos no ocurren en dominio.
   - No conviertas catálogos dinámicos en enums. Usa los códigos técnicos de specs/domain/.
   - Recibe instantes como datos; Clock se inyecta en aplicación.
   - Separa reglas puras de autorización JWT, criptografía, transacciones y archivos.
   - Respeta ADR-0009 y las reglas de progreso: modificar Datos Generales no invalida CUCoP.

4. PRUEBA reglas puras con JUnit sin contexto Spring. Nombra las pruebas
   USxx_CAyyy_descriptionInEnglish, por ejemplo US01_CA010_missingRfc_isRejected.
   Cubre los CA de dominio aplicables y asigna los demás a sus capas en el reporte.
   No declares cubierto un CA de integración o interfaz mediante una prueba de dominio.

5. VERIFICA desde la raíz:
   - python3 tools/spec_lint.py
   - ./mvnw test (incluye generación OpenAPI configurada en pom.xml).
   Reporta comandos, resultados y bloqueos reales. No agregues dependencias sin ADR.
   No edites target/generated-sources ni crees commits sin instrucción.
```
