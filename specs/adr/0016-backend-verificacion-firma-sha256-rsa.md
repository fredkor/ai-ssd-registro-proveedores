# ADR-0016: Verificación del sello RSA PKCS#1 v1.5 con SHA-256

- **Estado:** Aceptado
- **Ámbito:** Backend
- **Fecha:** 2026-10-18
- **Actualización:** 2026-10-21
- **Specs relacionadas:** RF-REG-01; US01; INV-03 a INV-06; contrato OpenAPI; ADR-0015.

## Contexto

El componente externo utiliza node-forge y una clave privada para crear un digest SHA-256,
incorporar la cadena original en UTF-8, ejecutar `pkey.sign(mdObj)` sin segundo argumento
y codificar los bytes de la firma en Base64 como `cadenaFirma`.

La API RSA de node-forge utiliza RSASSA-PKCS1-v1_5 por defecto en esa llamada. El backend
necesita verificar el mismo esquema con la clave pública del certificado del solicitante.

## Decisión

- El mecanismo de sello es RSA PKCS#1 v1.5 con SHA-256. En Java se utiliza
  `java.security.Signature` con el nombre estándar `SHA256withRSA`.
- La cadena original es texto. Sus bytes se obtienen con UTF-8 sin recorte, cambios de
  mayúsculas o incorporación de saltos de línea. En la identificación inicial es `Temporal`.
- `cadenaFirma` se decodifica desde Base64 para obtener los bytes de firma.
- La clave pública se obtiene del certificado entregado por el componente: se decodifica
  `certificado` desde Base64. `certificado` y `certificadoPem` son obligatorios (2026-10-21);
  `certificadoPem` se conserva como evidencia y no sustituye a `certificado`.
  La lectura del certificado y la verificación se encapsulan en `adapter.out.signature`.
- El verificador SHA256withRSA recibe los bytes UTF-8 de la cadena original y los bytes
  decodificados de firma. No se calcula manualmente otro hash antes de alimentar a Signature,
  porque ese algoritmo ya incorpora SHA-256.
- El backend obtiene el RFC del titular del atributo del sujeto del certificado con OID `2.5.4.45`: divide su valor por `/` y toma el segmento de índice 0. En `rfc/rfc_apoderado`, solo el primer RFC identifica al titular; no se utiliza el RFC del apoderado como alternativa. El RFC extraído debe tener 12 o 13 caracteres y coincidir exactamente con `ResultadoFirma.rfc`, sin recorte ni conversión de mayúsculas/minúsculas. Se usa el mismo certificado que verifica el sello. La ausencia del atributo o un primer segmento vacío o de longitud inválida produce VALIDATION_ERROR; la discrepancia produce SIGNATURE_VERIFICATION_FAILED. Solo tras superar ambas comprobaciones puede aceptarse la identificación y emitirse token.
- Para persona física y moral, el backend obtiene el nombre completo / razón social del sujeto del mismo certificado utilizado para verificar el sello. Recorre los atributos en su orden original y toma el valor del primer atributo cuyo OID sea `2.5.4.3`, `2.5.4.41` o `2.5.4.10`. La lista de OID es un filtro, no un orden de prioridad. No concatena valores ni sustituye el primer atributo coincidente por uno posterior. Ese valor se usa como `ApplicantIdentity.legalName`; `ResultadoFirma.razonSocial` se conserva sin cambios en la evidencia. Si ambos difieren, la identidad utiliza el valor extraído del certificado. Si no existe un atributo coincidente o su valor está vacío, se rechaza con VALIDATION_ERROR, sin emitir token ni permitir continuar o crear expediente. No se agrega recorte ni normalización al valor extraído.
- No se sustituye el esquema por RSA-PSS, firma de un texto hexadecimal del hash ni
  comparación de hashes sin verificación con la clave pública.
- La implementación utiliza las capacidades del JDK. Este ADR no agrega node-forge al
  backend ni expone la llave privada fuera del componente de Firma Electrónica del solicitante.
- Vigencia y OCSP siguen siendo responsabilidad del componente externo. El backend
  conserva el resultado íntegro aceptado y emite token solo después de verificar el sello.
- Entrada inválida, verificación negativa y fallo técnico se traducen según ADR-0004.

## Alternativas consideradas

| Alternativa | A favor | En contra |
| :--- | :--- | :--- |
| SHA256withRSA del JDK | Corresponde al esquema de firma y no requiere otra biblioteca backend. | Requiere una prueba de interoperabilidad con el componente. |
| RSA-PSS | Otro esquema de firma RSA. | No corresponde al valor predeterminado de sign(mdObj) mostrado. |
| Calcular únicamente SHA-256 y comparar textos | Implementación reducida. | No verifica que la firma corresponda a la clave del certificado. |

## Consecuencias y verificación

Al implementar, se usará una firma de prueba creada con node-forge y un certificado de
prueba para comprobar interoperabilidad, sin solicitar certificados privados de proveedores.
Se comprobará éxito con la cadena correcta y rechazo al cambiar la cadena, firma o certificado.
Se probarán el atributo con un único RFC, `rfc/rfc_apoderado`, atributo ausente, primer segmento vacío, longitud inválida y discrepancia con el RFC recibido, incluida la coincidencia exclusiva con el RFC del apoderado.
Se probará la extracción del nombre con distintos órdenes de los tres OID, múltiples coincidencias, discrepancia con el nombre de PKI, ausencia y primer valor vacío.
También se probarán Base64 y certificados mal formados conforme a los errores definidos.

Este ADR especifica el mecanismo externo observado; no autoriza modificar internamente el
componente de firma ni afirma que las pruebas de interoperabilidad ya estén ejecutadas.

## Referencias técnicas

- [Ejemplo RSA de node-forge y esquema predeterminado](https://github.com/digitalbazaar/forge/blob/main/README.md#rsa).
- [Nombres estándar de algoritmos Java: Signature](https://docs.oracle.com/en/java/javase/25/docs/specs/security/standard-names.html#signature-algorithms).
