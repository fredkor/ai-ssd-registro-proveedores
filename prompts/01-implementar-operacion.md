# Prompt: Implementar una operación del registro desde el contrato

Sustituir `{{OPERATION_ID}}` por una operación del contrato del registro.
Las rutas indicadas se resuelven desde la raíz del repositorio.

```text
Implementa la operación {{OPERATION_ID}} siguiendo CLAUDE.md.

1. ANALIZA sin escribir código:
   - Localiza la operación en specs/api/supplier-registration-openapi.yaml y lee
     x-spec-refs, x-use-case, seguridad, requests, responses y x-error-codes resueltos.
   - Lee los RF en specs/requirements/functional.md, las historias aplicables de
     specs/user-stories/ y sus criterios CA, y specs/traceability.md.
   - Consulta specs/architecture/architecture.md, specs/architecture/domain-model.md,
     specs/domain/supplier-registration-lifecycle.md y specs/domain/catalogs.md.
   - Lee specs/api/API-GUIDELINES.md y los ADR aplicables localizados desde specs/adr/README.md:
     0004/0007 para errores, 0009 para presencia/null/normalización, 0010 para archivos,
     0015 para JWT y 0016 para Firma Electrónica del solicitante.
   - Para persistencia usa specs/architecture/registration-database-design.md y
     specs/architecture/catalog-database-proposal.md, distinguiendo lo aprobado de lo pendiente.
   - Revisa src/, pom.xml y código generado existente para reutilizar capacidades.
   - Verifica el estado actual: YAML declara READY y x-implementation-ready: true;
     esto no prueba validación integral, generación, compilación ni comportamiento.
     CLAUDE.md y otras descripciones aún pueden reflejar el borrador o ausencia de wrapper:
     reporta esas discrepancias y usa el contrato y archivos actuales como evidencia.
   - No inventes reglas ante vacíos; consulta los bloqueos de la parte afectada.

2. PLANIFICA: lista archivos, capa, paquete, fuente y pruebas aplicables. Usa
   mx.com.fredkorlabs.registration y los paquetes generated del pom.xml. Considera el arranque
   existente en mx.com.fredkorlabs.sdd.supplier y su escaneo de componentes/entidades.
   Espera mi aprobación antes de implementar.

3. IMPLEMENTA dominio faltante -> puertos de aplicación -> servicio -> adaptadores.
   - Mantén Commands, Queries y Results preferentemente como record en application.port.in.
     Un servicio por caso de uso; controladores usan puertos de entrada, no agregados ni JPA.
   - Genera interfaces/DTO con ./mvnw generate-sources; inspecciona nombres y firmas reales.
     No dupliques DTO manualmente ni edites target/generated-sources.
   - Garantiza ADR-0009: propiedades presentes, rechazo de desconocidas, null condicional y
     recorte previo a validación. No supongas que anotaciones generadas bastan.
   - Usa Clock inyectado, UTC y transacciones según el caso. Devuelve éxito después del commit.
   - Aplica JWT y propiedad del expediente solo donde el contrato lo requiere.
   - Carga documental asocia inmediatamente; DELETE desasocia y marca metadatos en BD;
     el proceso diario realiza el borrado físico. Siguiente no confirma cargas diferidas.
   - Mantén fuera de alcance captura de Contactos y finalización. No agregues endpoints.

4. PRUEBA los CA aplicables por capa, con nombres USxx_CAyyy_descriptionInEnglish:
   dominio con JUnit puro, aplicación con dobles y adaptadores con pruebas pertinentes.
   Cubre HTTP/code declarados por operación, entradas inválidas y condiciones relevantes.
   Distingue los CA de interfaz de los verificables por backend; reporta cobertura pendiente.

5. VERIFICA desde la raíz:
   - python3 tools/spec_lint.py --require-ready
   - ./mvnw test
   Reporta generación, compilación y pruebas realmente ejecutadas, y limitaciones.
   Nuevas dependencias requieren ADR; no cambies versiones ni crees commits sin instrucción.
```
