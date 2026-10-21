# Anexo: Primera operación del registro

Agregar al final de `01-implementar-operacion.md`, sustituyendo `{{OPERATION_ID}}`
por `getRegistrationNotice`: GET `/api/v1/registration-notices/current`.
No requiere completar todo el dominio antes de implementar esta consulta independiente.

```text
CONSIDERACIONES PARA getRegistrationNotice:
- Lee specs/requirements/registration-notice.md, RF-REG-03, US03 en specs/user-stories/,
  specs/traceability.md y el esquema RegistrationNoticeResponse del contrato.
- Implementa GetRegistrationNoticeUseCase y GetRegistrationNoticeService definidos en
  specs/architecture/architecture.md. El resultado entrega texto fijo y versión 1.0,
  respetando literalmente la política vigente. No inventes almacenamiento JPA para esta consulta.
- Es una operación pública: responde 200 sin JWT previo y no crea expediente, aceptación,
  evidencia ni sesión. No implementes acceptRegistrationNotice o identifyApplicant como efecto lateral.
- Establece solo la infraestructura común necesaria para esta operación y reutilizable después.
  No implementes por anticipado todos los puertos de persistencia, JWT, firma o archivos.
- Genera con ./mvnw generate-sources y usa la interfaz real de la operación. No asumas nombres
  de interfaces: el pom usa useTags y skipDefaultInterface. Si la interfaz agrupa operaciones,
  resuelve su compilación en el plan sin publicar éxitos ficticios ni UnsupportedOperationException
  como comportamiento definitivo. No modifiques el contrato para acomodar el controlador.
- La respuesta es 200 con RegistrationNoticeResponse; no corresponde 201 ni Location.
- El error técnico declarado es 500 INTERNAL_ERROR. Si agregas manejo común, respeta
  ErrorResponse y ADR-0007; no expongas excepciones, trazas ni rutas internas al cliente.
- Verifica que el arranque Spring detecte los componentes bajo mx.com.fredkorlabs.registration:
  la clase actual está en mx.com.fredkorlabs.registration. Incluye la solución en el plan.
- Prueba el servicio con el texto y versión oficiales y el adaptador web con 200, JSON y acceso
  sin token. Comprueba 500 con su code si existe una ruta de fallo técnico reproducible.
  Aceptar/cancelar y mostrar el aviso son CA de interfaz o de operaciones posteriores.
- Usa las dependencias y Java 25 del pom.xml. Una dependencia nueva requiere ADR;
  no asumas APIs de Spring Boot 3 para este proyecto con Spring Boot 4.
- Ejecuta python3 tools/spec_lint.py --require-ready y ./mvnw test.
  Reporta resultados reales; la existencia del esqueleto Java no acredita comportamiento.
```
