# Guía de diseño de API — Registro de proveedores

## Operaciones y recursos

- Se mantiene el prefijo versionado `/api/v1/`, con recursos en plural y kebab-case.
  Las rutas y operationId están declarados en el borrador OpenAPI; no se cambian desde los controladores.
- Crear un expediente utiliza POST a su colección y responde 201 con Location.
- Consultar utiliza GET; las acciones de negocio se expresan según ADR-0002, sin campos
  editables de estado o progreso. Los contratos de guardado se cierran por sección.
- Cada operación corresponde a un caso de uso de arquitectura. Sus nombres técnicos se
  expresan en inglés y los operationId en camelCase.
- El expediente se identifica con UUID; la consulta por RFC utiliza identidad aceptada
  conforme a ADR-0003. No se agrega administración o listado de proveedores fuera de alcance.
- Atrás es navegación sin guardado; no debe provocar peticiones implícitas de persistencia.
- La cédula tiene carga con asociación inmediata, descarga y DELETE para desasociar y marcar metadatos en BD; no esperan Siguiente. Atrás no revierte sus efectos. El DELETE no toca disco y la limpieza diaria realiza el borrado físico desde metadatos no asociados según ADR-0010.

## Modelos, campos y trazabilidad

- Interfaces y DTO web se generan del YAML autoritativo y no se editan manualmente.
- Los esquemas reutilizables pertenecen a `components/schemas`, con sufijos Request y Response.
- Cada operación declara `x-spec-refs` con RF y US. Las transiciones declaran `x-transition`
  usando los códigos de [supplier-registration-lifecycle.md](../domain/supplier-registration-lifecycle.md).
- Enums y códigos coinciden con `specs/domain/`. Los campos de identidad y valores derivados
  se separan de la captura; no se admiten modificaciones directas de identidad, estado o progreso.
- ADR-0009 exige enviar todas las propiedades de los requests, también en objetos anidados.
  Los campos opcionales sin valor y los no aplicables se envían como null; omitir propiedades
  o enviar propiedades desconocidas produce VALIDATION_ERROR. Null no conserva el valor anterior.
- No se inventan reglas de recorte, normalización o longitud no definidas en RF y modelo.
- El resultado aceptado de Firma Electrónica del solicitante se conserva íntegro; el contrato
  debe permitir su transmisión sin perder propiedades y sin incluir llave privada o contraseña.

## Errores

- Se utiliza Problem Details (RFC 9457, `application/problem+json`, esquema `ProblemResponse`)
  conforme a ADR-0019, con los códigos de ADR-0004 en `code` y el error genérico de ADR-0007.
- Los errores de entrada indican en `errors[]` el JSON Pointer del campo que falló.
- Cada operación declara sus categorías aplicables. HTTP y code son contractuales; los
  mensajes informativos en inglés no requieren coincidencia literal salvo especificación expresa.
- No se exponen trazas, rutas de volumen ni contenido completo de firma o certificado en errores.
- Los errores del componente de Firma Electrónica del solicitante que ocurren antes de llamar
  al backend son parte de la integración de interfaz, no respuestas ficticias del backend.

## Identificación y acceso

- El backend verifica el sello sobre el contenido usando el certificado; vigencia y OCSP
  corresponden al componente externo.
- El acceso al expediente se vincula al RFC de una identificación aceptada. Un UUID o RFC
  enviados libremente no sustituyen esa comprobación.
- Usar JWT de registro firmado en Authorization: Bearer, emitido tras identificar conforme
  a [ADR-0015](../adr/0015-shared-token-de-registro.md), válido 60 minutos; un RFC puede tener
  varias sesiones vigentes. Se valida firma, claims y contexto del jti conforme a ADR-0015.
- Antes de la firma, la aceptación se correlaciona por acceptanceId del backend. Las peticiones
  protegidas usan el token; no autorizan mediante RFC o UUID libres. Los IDs de catálogo son enteros.

## Documentos y documentación navegable

- La carga, descarga y eliminación están en OpenAPI. La carga devuelve UUID; guardar Datos Generales valida la asociación persistida sin recibir fiscalDocumentId. El nombre original se conserva y los formatos y límites pertenecen al tipo documental.
- Las rutas físicas `/repository/...` no se exponen como identificadores públicos.
- Swagger UI mostrará el mismo contrato usado para generación, conforme a ADR-0008.
  El borrador es `supplier-registration-openapi.yaml`; su [estado y decisiones pendientes](README.md)
  deben cerrarse antes de homologar Maven y el validador para generación.
