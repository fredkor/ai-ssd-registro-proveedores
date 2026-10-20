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

El contrato YAML continúa con `x-implementation-ready: false`; requiere revisión final y
alineación y verificación de herramientas antes de generar o implementar. La matriz ya representa el registro y
distingue verificación prevista de ejecución real. El validador está adaptado al registro y sus controles se describen en
[tools/README.md](tools/README.md); un resultado correcto no habilita el borrador ni acredita implementación.

## Alcance y cambios de especificación

- La primera etapa comprende aceptación del aviso, identificación y existencia, inicio y
  reanudación del expediente, Datos Generales con teléfonos y cédula fiscal, y CUCoP.
  Contactos es el punto de continuación, sin implementar su captura ni la finalización.
- Si una regla está incompleta o hay contradicción, identificar el vacío y consultarlo antes
  de implementar la parte afectada. Puede continuarse con trabajo independiente ya definido.
- No inventar campos, formatos, transiciones, algoritmos de clave, plazos o rutas para resolver un vacío.
- Los artefactos se redactan como especificaciones de Fredkor Labs o la empresa, sin referencias
  a ejercicios, historias de origen u otras plataformas.
- Usar “Firma Electrónica del solicitante” como término de negocio.

## Reglas de implementación

1. Leer las fuentes aplicables y respetar las fronteras de arquitectura: dominio JDK puro,
   puertos en aplicación, un servicio por caso de uso y adaptadores separados.
2. El controlador usa puertos de entrada y Results; no expone agregados ni entidades JPA.
   Una vez validado el contrato, implementar interfaces y DTO generados; no duplicarlos
   manualmente ni editar `target/generated-sources`.
3. Mantener los identificadores RF/US/CA/INV y los códigos definidos en dominio. Los
   catálogos dinámicos se consultan mediante puertos; no se convierten en enums.
4. Aplicar identificación de ADR-0016, contexto de ADR-0015 y campos/null/normalización
   de ADR-0009. Los errores HTTP se traducen conforme a ADR-0004 y ADR-0007,
   sin introducir infraestructura en las excepciones puras de dominio.
5. Usar el servicio general de archivos de ADR-0010: carga con asociación inmediata y
   eliminación lógica transaccional de referencia, marca de metadatos y progreso, sin tocar disco
   ni esperar Siguiente. Atrás no revierte operaciones documentales exitosas; el proceso diario
   consulta metadatos no asociados para el borrado físico posterior.
   No reconstruir referencias ni borrar archivos sin metadatos por el reinicio de H2.
6. Inyectar `java.time.Clock` en aplicación con UTC; el dominio recibe instantes.
7. No agregar ni actualizar dependencias sin ADR. Consultar versiones en Maven;
   no asumir compatibilidad con otra versión del framework.
8. Frontend conforme a ADR-0011 a ADR-0014: Angular 21, PrimeNG/Sakai, dominios y
   formularios reactivos tipados. Mantener tipos visuales separados del negocio y DTO.
   No instalar Nx ni crear guardados por cambios de control por inferencia.
9. Los ADR están directamente en `specs/adr/`, con nombres `NNNN-ambito-descripcion.md`;
   los ámbitos son `backend`, `frontend` y `shared`. Conservar IDs globales.

## Idioma, pruebas y verificación

- Documentación en castellano; clases, campos, métodos, rutas, operationId y códigos técnicos en inglés.
- Las pruebas de comportamiento se vinculan a los criterios de aceptación. Convención:
  `USxx_CAyyy_descriptionInEnglish`, por ejemplo `US01_CA010_missingRfc_isRejected`.
- Probar reglas puras con JUnit sin Spring. Probar aplicación con dobles de puertos y usar
  pruebas de adaptadores para unicidad concurrente, transacciones, archivos, firma y contrato.
- No exigir infraestructura para probar una regla pura ni confundir revisión documental con pruebas ejecutadas.
- En cambios de especificación, verificar formato, enlaces y referencias RF/US/INV aplicables.
- Ejecutar `python3 tools/spec_lint.py` para cambios relacionados con las especificaciones.
  Usar `--require-ready` solo al exigir habilitación del contrato. Una vez preparada y autorizada
  la implementación, ejecutar generación y pruebas pertinentes.
  Reportar las limitaciones reales mientras estos artefactos sigan pendientes; no afirmar que pasaron.
- Antes de un commit, completar los controles aplicables al cambio. Los prefijos son
  `spec:`, `design:`, `contract:`, `tooling:`, `feat:`, `test:` y `fix:`. No crear commits sin instrucción.

## Comandos y preparación

Ejecutar desde `SSD_REAL/`. El detalle y las precondiciones están en
[tech-stack.md](specs/architecture/tech-stack.md).

| Acción | Comando | Disponibilidad |
| :--- | :--- | :--- |
| Validar especificaciones y contrato | `python3 tools/spec_lint.py` | Disponible; requiere Python 3 y PyYAML. No habilita implementación. |
| Generar interfaces y DTO | `mvn generate-sources` | Requiere contrato y Maven alineados. |
| Compilar y probar | `mvn test` | Requiere implementación y generación preparadas. |
| Ejecutar backend | `mvn spring-boot:run` | Requiere aplicación y configuración implementadas. |

El módulo todavía no incluye wrapper Maven ni código Java. No usar `./mvnw` como comando
disponible hasta incorporarlo. Maven ya apunta al contrato del registro y usa mx.com.fredkorlabs; el validador está adaptado.
La generación y validación integral de OpenAPI siguen pendientes; no se declaran realizadas.
