# ADR-0015: JWT de registro con vigencia de 60 minutos y sesiones concurrentes por solicitante

- **Estado:** Aceptado
- **Ámbito:** Compartido
- **Fecha:** 2026-10-18
- **Actualización:** 2026-10-21
- **Specs relacionadas:** RF-REG-01, RF-REG-02, RF-REG-04, RF-REG-06; US01, US02, US04, US05; contrato API.

## Contexto

Las operaciones posteriores a identificar al solicitante deben utilizar la identidad y
aceptación que el backend procesó. Un RFC o UUID enviados libremente no autorizan acceso.
Fredkor Labs establece token de registro con vigencia de 60 minutos.

El 2026-10-21 se retiró la sesión única por solicitante para no complicar la implementación:
una nueva identificación del mismo RFC ya no invalida los tokens anteriores. Cada token
vale hasta su vencimiento o hasta perder su contexto en BD. La concurrencia entre sesiones
del mismo expediente se resuelve con el bloqueo pesimista del
[diseño de persistencia](../architecture/registration-database-design.md).

## Decisión

- El backend emite un JWT de registro firmado después de verificar correctamente la Firma
  Electrónica del solicitante y conservar íntegramente el resultado aceptado.
- Se transporta por `Authorization: Bearer <jwt>` entre frontend y backend. La emisión y
  validación pertenecen al backend; el frontend no firma tokens ni recibe la clave de firma.
- El token se vincula en backend al RFC verificado, la evidencia aceptada y la aceptación
  del aviso. No contiene una identidad que el cliente pueda editar para cambiar de expediente.
- La aceptación previa se correlaciona mediante una referencia opaca `acceptanceId` emitida
  por el backend al conservar versión y fecha/hora. El request de identificación envía
  `noticeAcceptanceId` y el objeto íntegro `signatureResult` conforme a ResultadoFirma.
  La referencia de aceptación no es un token de acceso ni sustituye verificar la firma.
- La aceptación referida debe existir. Un `noticeAcceptanceId` inexistente o que no corresponda
  al formato de las referencias emitidas responde 400 VALIDATION_ERROR, sin token.
- Cada aceptación respalda una sola identificación exitosa: queda consumida al confirmarse el
  contexto de su token. Reutilizarla responde 400 VALIDATION_ERROR. Un intento rechazado no la
  consume, por lo que la firma puede reintentarse con la misma aceptación (decisión del 2026-10-21).
- La respuesta de identificación entrega `registrationToken`, `tokenExpiresAt` e identidad.
- El token vence 60 minutos después de emitirse. No se establece renovación automática
  por actividad. La hora y el vencimiento se obtienen del reloj del backend en UTC.
- Un mismo RFC puede tener varias sesiones vigentes a la vez. Emitir un token nuevo no
  invalida los anteriores del mismo RFC ni los de otros solicitantes. Un intento de
  identificación rechazado no emite token ni afecta los vigentes.
- Las operaciones de existencia, inicio, reanudación, guardado, documentos y catálogos del
  registro requieren el token. Consultar aviso, aceptar aviso y procesar la firma no requieren
  un token de registro anterior; sí deben cumplir sus precondiciones funcionales.
- El backend comprueba token, vencimiento y contexto en cada operación protegida.
  Para operaciones sobre un expediente comprueba además que su RFC corresponda al del token.
- Token ausente, desconocido, vencido o sin contexto produce 401 REGISTRATION_TOKEN_INVALID.
  Token válido para otro RFC produce 403 REGISTRATION_ACCESS_DENIED al solicitar un expediente ajeno.
- El UUID del expediente no funciona como token. No se envía el token en la URL ni se devuelve
  como parte de los datos de negocio del expediente.
- El estado de tokens se gestiona en aplicación e infraestructura, no en el agregado de dominio.
  No se adopta Redis ni un servicio nuevo; JWT es el formato de intercambio elegido.
- El contexto de cada sesión se conserva en la misma BD del registro mediante JPA (PostgreSQL,
  ADR-0017 y ADR-0018).

### Contenido y validación del JWT

- JWT firmado como JWS compacto, con algoritmo permitido `RS256` fijado por configuración
  del servidor. Rechazar `none`, firma inválida y algoritmos distintos; no aceptar el algoritmo
  únicamente porque lo indique la cabecera recibida.
- Claims obligatorios: `sub` (RFC verificado), `jti` (identificador único del token), `iat`
  (emisión), `exp` (emisión más 3600 segundos), `iss` (emisor configurado) y `aud`
  (audiencia del backend de registro configurada). Se validan emisor, audiencia, sujeto,
  fechas y firma antes de autorizar; a partir de `exp` el token ya no es válido.
- No incluir evidencia completa, certificados, archivos ni secretos en el payload.
  La firma no cifra el contenido. La evidencia, aceptación e identidad ampliada se resuelven
  desde el contexto de backend asociado a `jti`.
- El backend comprueba en cada petición protegida que exista el contexto del `jti`, que su
  RFC coincida con `sub` y que no haya vencido. Validar únicamente la firma y `exp` no basta.
- No se añade refresh token ni renovación por actividad. El cliente conserva la respuesta
  `registrationToken` y `tokenExpiresAt`; nunca puede modificar claims para extender acceso.

### Biblioteca y claves

- Se utiliza Spring Security OAuth2 Resource Server (`spring-boot-starter-oauth2-resource-server`,
  versión gestionada por el parent de Spring Boot), que incluye Nimbus JOSE+JWT. La emisión usa
  su `JwtEncoder` y la validación su `JwtDecoder`, restringidos a `RS256`, emisor y audiencia.
  La dependencia se incorpora al implementar la primera operación que emite o valida tokens.
- La configuración de seguridad vive fuera del núcleo (`configuration` y adaptadores de
  entrada). Permite sin token únicamente `getRegistrationNotice`, `acceptRegistrationNotice` e
  `identifyApplicant`, además de la documentación de ADR-0008; exige Bearer en las demás.
  Sin sesión HTTP ni CSRF: la API es stateless y no usa cookies.
- La validación del contexto en BD se añade como validador del token o filtro posterior a la
  autenticación; un token sin contexto vigente se trata como no autenticado.
- Los rechazos de autenticación responden 401 con `ErrorResponse` REGISTRATION_TOKEN_INVALID y
  cabecera `WWW-Authenticate: Bearer`, no con el formato por defecto de Spring Security.
  La pertenencia del expediente sigue siendo comprobación de aplicación (403).
- Las claves son un par RSA propio del backend, separado de la e.firma del solicitante. Las
  llaves pública y privada en PEM se leen de ubicaciones configuradas por variables de entorno;
  no se distribuyen al frontend ni se guardan en el repositorio. La rotación de claves queda
  fuera de la primera etapa.
- Si las ubicaciones no están configuradas, el backend genera un par RSA efímero al arrancar y
  registra una advertencia; los tokens dejan de validar al reiniciar porque cambia la llave.
  Así funcionan las pruebas y el desarrollo local. Con el perfil `prod` (entornos desplegados,
  ADR-0018) las llaves son obligatorias y el backend no arranca sin ellas.
- Emisor y audiencia se configuran en `backend/src/main/resources/application.yaml` con las
  propiedades `registration.jwt.issuer` y `registration.jwt.audience`; se pueden sobrescribir
  por entorno. Las rutas de las llaves usan `registration.jwt.public-key-location` y
  `registration.jwt.private-key-location`, cuyo valor proviene de variables de entorno.

### Contexto en la misma base de datos

El adaptador de persistencia implementa la gestión de contexto de `RegistrationContextPort`
mediante JPA sobre la misma base de datos que los expedientes, evidencias y aceptaciones.
No se introduce Redis, otra base de datos ni un servicio externo. El JWT completo no se
almacena: se conserva un registro por token emitido con sus datos de correlación.

Tabla `registration_context`, con un registro por token emitido:

| Campo | Restricción y propósito |
| :--- | :--- |
| `jti` | Clave primaria; identifica el JWT. |
| `rfc` | Obligatorio, no único; RFC de la identidad verificada, sin normalización adicional. |
| `issued_at` | Obligatorio; emisión UTC coincidente con `iat`. |
| `expires_at` | Obligatorio; emisión más 60 minutos, coincidente con `exp`. |
| `signature_evidence_id` | Referencia obligatoria y única a la evidencia aceptada y conservada. |
| `notice_acceptance_id` | Referencia obligatoria y única a la aceptación procesada antes de identificar; una aceptación respalda un solo token. |

- Tras validar firma e identidad, preparar y firmar el nuevo JWT e insertar su contexto en una
  transacción de BD. Devolver el JWT solo después del commit; si falla la preparación, firma o
  persistencia, no devolver un token utilizable. Los contextos existentes no se modifican.
- En cada petición protegida, después de verificar firma y claims, consultar el contexto por
  `jti` y exigir RFC coincidente y vencimiento vigente. Sin contexto o con contexto vencido,
  responder REGISTRATION_TOKEN_INVALID. Un fallo de consulta es un fallo técnico, no una
  autorización ni una falsa sesión inexistente; se aplica ADR-0004/ADR-0007.
- Resolver identidad, evidencia y aceptación desde ese contexto; no aceptar referencias editables
  del formulario para autorizar. El contexto puede existir antes del expediente, por lo que no
  requiere un ID de expediente obligatorio.
- Emitir contextos no borra las evidencias históricas de firma ni actualiza por sí mismo
  los datos del expediente. La última aceptación del expediente se sustituye al reanudar.
- Los contextos vencidos se conservan sin purga en la primera etapa, como las aceptaciones
  de correlación; ya no autorizan. Una purga futura requiere su propia decisión.
- Un JWT aún no vencido no autoriza si su contexto no existe en la base de datos; se requiere
  una nueva identificación.

No se agrega un historial de sesiones consultable ni un endpoint de administración.

### Última aceptación del aviso fijo

La Política de Aviso es fija, con texto y versión 1.0. Cada nuevo inicio del flujo, incluida la reanudación, requiere aceptación expresa antes de firmar. El expediente conserva únicamente la última aceptación confirmada, con versión y fecha/hora del backend, sin historial de aceptaciones. Al reanudar correctamente se sustituye la anterior por la aceptación vinculada a la nueva identificación; esta sustitución se confirma en la misma transacción que actualiza el nombre y vincula la evidencia. Si falla la reanudación, la aceptación previamente asociada permanece intacta. Esto no elimina ni sobrescribe el historial de evidencias de firma.

Las referencias de aceptación usadas antes de identificar correlacionan el flujo; no constituyen un historial de aceptaciones del expediente. Su gestión no debe afectar sesiones vigentes ante un intento fallido ni borrar evidencias históricas de firma.

## Alternativas consideradas

| Alternativa | A favor | En contra |
| :--- | :--- | :--- |
| JWT firmado con contexto por token y sesiones concurrentes | Formato estándar; implementación simple; un token sin contexto no autoriza. | Un RFC puede operar desde varias sesiones; opción elegida. |
| Sesión única por RFC (decisión anterior) | Invalida inmediatamente tokens previos. | Exige sustitución atómica y serialización por RFC; retirada el 2026-10-21. |
| Token opaco | Permite resolver toda la identidad en backend. | No es el formato de intercambio elegido. |
| Sesión HTTP con cookie | Puede correlacionar el estado en servidor. | No es el transporte elegido. |
| Nimbus JOSE+JWT sin Spring Security | Una sola dependencia. | Validación y respuestas 401 manuales; no elegida. |
| JWT solo con JDK | Sin dependencias. | Parseo y validación escritos a mano; no elegida. |

## Consecuencias y verificación

- El contrato declara Bearer con `bearerFormat: JWT` y errores 401/403. La validación real
  requiere el adaptador y el contexto; declarar el formato no verifica un JWT.
- Se prueban emisión tras firma exitosa, vencimiento a los 60 minutos con reloj fijo,
  coexistencia de dos tokens vigentes del mismo RFC y aislamiento entre RFC.
- Probar firma alterada, algoritmo no permitido, claims ausentes, emisor/audiencia incorrectos,
  expiración, `jti` sin contexto y RFC del contexto distinto de `sub`; la decodificación del
  frontend no autoriza acceso.
- Probar que los endpoints públicos responden sin token y que los rechazos usan `ErrorResponse`.
- Una sesión nueva no crea por sí misma un expediente ni cambia su estado o progreso.
- Probar rollback si falla la emisión/persistencia y rechazo de un JWT no vencido sin
  contexto en la base de datos.

## Referencias

- [RFC 7519: JSON Web Token](https://www.rfc-editor.org/rfc/rfc7519.html).
- [RFC 8725: JWT Best Current Practices](https://www.rfc-editor.org/rfc/rfc8725.html).
