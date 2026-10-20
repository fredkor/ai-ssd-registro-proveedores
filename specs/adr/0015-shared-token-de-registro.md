# ADR-0015: JWT de registro con vigencia de 60 minutos y sesión única por solicitante

- **Estado:** Aceptado
- **Ámbito:** Compartido
- **Fecha:** 2026-10-18
- **Actualización:** 2026-10-19
- **Specs relacionadas:** RF-REG-01, RF-REG-02, RF-REG-04, RF-REG-06; US01, US02, US04, US05; contrato API.

## Contexto

Las operaciones posteriores a identificar al solicitante deben utilizar la identidad y
aceptación que el backend procesó. Un RFC o UUID enviados libremente no autorizan acceso.
Fredkor Labs establece token de registro, vigencia de 60 minutos e invalidación de tokens
anteriores al abrir una nueva sesión del mismo solicitante.

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
- La respuesta de identificación entrega `registrationToken`, `tokenExpiresAt` e identidad.
- El token vence 60 minutos después de emitirse. No se establece renovación automática
  por actividad. La hora y el vencimiento se obtienen del reloj del backend en UTC.
- Al emitir un token para una nueva sesión de un RFC, se invalidan todos los tokens anteriores
  de ese mismo RFC. No se invalidan tokens de otros solicitantes.
- La sustitución se coordina de forma atómica para que no queden dos sesiones actuales
  válidas por concurrencia. Un intento de identificación rechazado no emite un token nuevo
  ni invalida por sí mismo el token válido existente.
- Las operaciones de existencia, inicio, reanudación, guardado, documentos y catálogos del
  registro requieren el token. Consultar aviso, aceptar aviso y procesar la firma no requieren
  un token de registro anterior; sí deben cumplir sus precondiciones funcionales.
- El backend comprueba token, vencimiento y vigencia de sesión en cada operación protegida.
  Para operaciones sobre un expediente comprueba además que su RFC corresponda al del token.
- Token ausente, desconocido, vencido o invalidado produce 401 REGISTRATION_TOKEN_INVALID.
  Token válido para otro RFC produce 403 REGISTRATION_ACCESS_DENIED al solicitar un expediente ajeno.
- El UUID del expediente no funciona como token. No se envía el token en la URL ni se devuelve
  como parte de los datos de negocio del expediente.
- El estado de tokens se gestiona en aplicación e infraestructura, no en el agregado de dominio.
  La estrategia de almacenamiento no obliga a adoptar Redis ni un servicio nuevo; JWT es el formato de intercambio elegido.
- El contexto de sesión se conserva en la misma BD del registro mediante JPA. En H2 de pruebas se pierde al reiniciar conforme
  a ADR-0005 y requieren identificarse nuevamente.

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
  desde el contexto de backend asociado a `jti` y RFC.
- El backend comprueba en cada petición protegida que `jti` sigue siendo el token vigente
  para `sub`. Al emitir otro para el mismo RFC, sustituye atómicamente esa referencia.
  Validar únicamente la firma y `exp` no cumple la invalidación de sesión anterior.
- No se añade refresh token ni renovación por actividad. El cliente conserva la respuesta
  `registrationToken` y `tokenExpiresAt`; nunca puede modificar claims para extender acceso.
- Las claves JWT son propias del backend, separadas de la e.firma del solicitante; no se
  distribuyen al frontend ni se guardan en el repositorio. La biblioteca y versión, provisión
  y rotación de claves y valores de emisor/audiencia se concretan
  al preparar implementación, antes de habilitar emisión y validación.

### Contexto vigente en la misma base de datos

El adaptador de persistencia implementa la gestión de contexto de `RegistrationContextPort`
mediante JPA sobre la misma base de datos que los expedientes, evidencias y aceptaciones.
No se introduce Redis, otra base de datos ni un servicio externo. El JWT completo no necesita
almacenarse: se conserva la referencia vigente y sus datos de correlación.

Tabla propuesta `registration_context`, con un registro actual por RFC:

| Campo | Restricción y propósito |
| :--- | :--- |
| `rfc` | Clave primaria; RFC de la identidad verificada, sin normalización adicional. |
| `jti` | Obligatorio y único; identifica el JWT vigente del RFC. |
| `issued_at` | Obligatorio; emisión UTC coincidente con `iat`. |
| `expires_at` | Obligatorio; emisión más 60 minutos, coincidente con `exp`. |
| `signature_evidence_id` | Referencia obligatoria a la evidencia aceptada y conservada. |
| `notice_acceptance_id` | Referencia obligatoria a la aceptación procesada antes de identificar. |

- Tras validar firma e identidad, preparar y firmar el nuevo JWT y confirmar la sustitución
  del contexto en una transacción de BD. Devolver el JWT solo después del commit; si falla la
  preparación, firma o persistencia, no sustituir la sesión anterior ni devolver un token utilizable.
- La unicidad de RFC y `jti` se respalda con constraints. Las actualizaciones del mismo RFC se
  serializan con control de concurrencia en BD, incluyendo el caso de creación simultánea de
  la primera sesión. La comprobación previa por sí sola no es garantía de unicidad.
- Si hay emisiones concurrentes exitosas, solo el último contexto confirmado queda vigente;
  los JWT anteriores, aunque no hayan vencido, se rechazan al no coincidir su `jti`.
- En cada petición protegida, después de verificar firma y claims, consultar el contexto por
  RFC y exigir `jti` coincidente y vencimiento vigente. Sin contexto, con contexto vencido o
  con `jti` sustituido, responder REGISTRATION_TOKEN_INVALID. Un fallo de consulta es un fallo
  técnico, no una autorización ni una falsa sesión inexistente; se aplica ADR-0004/ADR-0007.
- Resolver identidad, evidencia y aceptación desde ese contexto; no aceptar referencias editables
  del formulario para autorizar. El contexto puede existir antes del expediente, por lo que no
  requiere un ID de expediente obligatorio.
- Reemplazar el contexto no borra las evidencias históricas de firma ni actualiza por sí mismo
  los datos del expediente. La última aceptación del expediente se sustituye al reanudar.
- Al reiniciar H2 se pierden los contextos. Un JWT aún no vencido tampoco autoriza sin su registro
  vigente y se requiere una nueva identificación.

El ancho SQL y las FK concretas se alinearán con el esquema de evidencia y aceptación.
No se agrega un historial de sesiones ni un endpoint de administración por esta decisión.

### Última aceptación del aviso fijo

La Política de Aviso es fija, con texto y versión 1.0. Cada nuevo inicio del flujo, incluida la reanudación, requiere aceptación expresa antes de firmar. El expediente conserva únicamente la última aceptación confirmada, con versión y fecha/hora del backend, sin historial de aceptaciones. Al reanudar correctamente se sustituye la anterior por la aceptación vinculada a la nueva identificación; esta sustitución se confirma en la misma transacción que actualiza el nombre y vincula la evidencia. Si falla la reanudación, la aceptación previamente asociada permanece intacta. Esto no elimina ni sobrescribe el historial de evidencias de firma.

Las referencias de aceptación usadas antes de identificar correlacionan el flujo; no constituyen un historial de aceptaciones del expediente. Su gestión no debe romper la sesión vigente ante un intento fallido ni borrar evidencias históricas de firma.

## Alternativas consideradas

| Alternativa | A favor | En contra |
| :--- | :--- | :--- |
| JWT firmado con comprobación de sesión vigente | Formato estándar entre frontend y backend; conserva invalidación por RFC. | Requiere verificar firma, claims y contexto vigente; opción elegida. |
| Token opaco | Permite resolver toda la identidad en backend. | No es el formato de intercambio elegido. |
| Sesión HTTP con cookie | Puede correlacionar el estado en servidor. | No es el transporte elegido. |
| JWT sin comprobar invalidación | Reduce consultas para validar firma del token. | Por sí solo no satisface invalidación inmediata de tokens anteriores. |

## Consecuencias y verificación

- El contrato declara Bearer con `bearerFormat: JWT` y errores 401/403. La validación real
  requiere el adaptador y el contexto de sesión; declarar el formato no verifica un JWT.
- Se prueban emisión tras firma exitosa, vencimiento a los 60 minutos con reloj fijo,
  invalidación del token anterior, aislamiento entre RFC y concurrencia de nuevas sesiones.
- Probar firma alterada, algoritmo no permitido, claims ausentes, emisor/audiencia incorrectos,
  expiración y jti sustituido; la decodificación del frontend no autoriza acceso.
- Una sesión nueva no crea por sí misma un expediente ni cambia su estado o progreso.
- Probar sustitución transaccional en la misma BD, unicidad concurrente desde el primer registro,
  rollback si falla la emisión/persistencia y rechazo de un JWT no vencido cuyo contexto se perdió
  al reiniciar H2.
- El tratamiento de solicitudes ya autorizadas y en ejecución al sustituir una sesión se
  concretará al preparar la implementación; las nuevas autorizaciones consultan el contexto vigente.

## Referencias

- [RFC 7519: JSON Web Token](https://www.rfc-editor.org/rfc/rfc7519.html).
- [RFC 8725: JWT Best Current Practices](https://www.rfc-editor.org/rfc/rfc8725.html).
