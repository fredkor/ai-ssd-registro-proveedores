# HU-REG-001 — Identificación mediante e.firma

**Como** potencial licitante, **quiero** identificarme mediante mi e.firma, **para** que la Plataforma de Compras de Fredkor Labs determine si puedo iniciar o continuar mi registro.

**Precondiciones:** disponer de e.firma compatible, componente COMP-EFIRMA-001 disponible y políticas aceptadas antes de solicitar la firma.

## Alcance y referencias

El componente externo comprueba vigencia y OCSP; su implementación está fuera del registro.
Fredkor Labs integra la firma, verifica el sello y la identidad y conserva el resultado aceptado.
La firma inicial no finaliza el expediente ni permite almacenar la llave privada o contraseña.

- Campos y mapeo: [integración de firma](../api/signature-integration.md).
- Verificación y extracción de identidad: [ADR-0016](../adr/0016-backend-verificacion-firma-sha256-rsa.md).
- Acceso y sesión: [ADR-0015](../adr/0015-shared-token-de-registro.md).

## Criterios de aceptación
- **CA-001:** Dado que accedo a Registro, cuando carga la pantalla, entonces veo el componente externo de e.firma.
- **CA-002:** Dado que el componente está disponible y declara estar listo para firmar, cuando la firma concluye exitosamente y entrega un resultado con RFC de 12 o 13 caracteres y razón social, entonces Fredkor Labs procesa los datos devueltos, incluidos RFC y razón social, para continuar con la consulta de existencia del registro.
- **CA-003:** Dado que el componente no está disponible o no declara estar listo para firmar, cuando intento continuar, entonces se informa que debo cargar los archivos de e.firma y no se solicita la firma ni se avanza en el registro.
- **CA-004:** Dado que la operación de firma produce un error, cuando la aplicación lo recibe, entonces se informa un error de firma y no se continúa ni se crea un expediente.
- **CA-005:** Dado que cancelo la identificación mediante e.firma, cuando se recibe la cancelación, entonces se informa el resultado y no se habilita la captura.
- **CA-006:** Dado que utilizo el componente de e.firma, cuando la aplicación procesa la identificación, entonces no almacena el archivo de llave privada `.key` ni su contraseña.
- **CA-007:** Dado que se solicitó la firma, cuando el componente concluye sin entregar un resultado, entonces se informa que no se obtuvo la firma electrónica y no se continúa con el registro.
- **CA-008:** Dado un certificado vencido, cuando se intenta realizar la firma, entonces el componente de Firma Electrónica del solicitante devuelve un error y Fredkor Labs informa el fallo sin permitir continuar ni crear un expediente.
- **CA-009:** Dado que el certificado no supera la validación contra OCSP realizada por el componente de Firma Electrónica del solicitante, cuando se intenta realizar la firma, entonces el componente de Firma Electrónica del solicitante devuelve un error y Fredkor Labs informa el fallo sin permitir continuar ni crear un expediente.

- **CA-010:** Dado un resultado de la Firma Electrónica del solicitante sin RFC o sin razón social, cuando Fredkor Labs lo procesa, entonces informa el error, rechaza el resultado y no permite continuar ni crear un expediente.
- **CA-011:** Dado un resultado de la Firma Electrónica del solicitante con RFC de longitud distinta de 12 o 13 caracteres, cuando se procesa, entonces se informa un error y se impide continuar.
- **CA-012:** Dado que se aceptaron las políticas y el componente de Firma Electrónica del solicitante está listo para firmar, cuando se solicita la firma inicial, entonces se envía la cadena literal `Temporal`.
- **CA-013:** Dado un resultado exitoso de la Firma Electrónica del solicitante que supera las validaciones de aceptación, cuando se conserva, entonces se mantienen todos los datos entregados por el componente, incluidos contenido firmado, RFC, CURP, razón social, firma, número de serie, indicador y fecha de vigencia y certificado.

- **CA-014:** Dado un resultado de la Firma Electrónica del solicitante, cuando el backend lo procesa, entonces verifica que el sello digital corresponde al contenido firmado usando el certificado del usuario antes de permitir continuar.
- **CA-015:** Dado un sello digital que no corresponde al contenido firmado y al certificado del usuario, o cuya verificación no puede realizarse, cuando el backend lo procesa, entonces se rechaza el resultado, se informa el error y no se permite continuar ni crear un expediente.

- **CA-016:** Dado un ResultadoFirma con resultado=false, cuando se procesa, entonces se informa el fallo y no se emite un token ni se permite continuar.
- **CA-017:** Dado que el backend verificó la firma y conservó el resultado íntegro, cuando acepta la identificación, entonces emite un JWT de registro firmado vinculado al RFC y a la aceptación; las peticiones posteriores lo utilizan sin enviar un RFC editable como autorización.

- **CA-018:** Dado un token emitido, cuando transcurren 60 minutos desde su emisión, entonces se rechaza su uso y se requiere una nueva identificación.
- **CA-019:** Dado un token vigente para un RFC, cuando se verifica correctamente una nueva identificación y se emite su token, entonces ambos tokens permanecen vigentes hasta su propio vencimiento, sin afectar a otros solicitantes.

- **CA-020:** Dado un certificado cuyo atributo de sujeto 2.5.4.45 contiene un RFC o varios separados por `/`, cuando el backend valida la identidad, entonces toma exclusivamente el segmento de índice 0 como RFC del titular y exige que coincida exactamente con el RFC recibido; el RFC del apoderado no permite aceptar una coincidencia alternativa.
- **CA-021:** Dado que falta el atributo 2.5.4.45, su primer segmento está vacío o no tiene 12 o 13 caracteres, cuando se procesa la identificación, entonces se responde VALIDATION_ERROR sin emitir token ni permitir continuar o crear expediente.
- **CA-022:** Dado un RFC recibido que no coincide con el primer segmento del atributo 2.5.4.45 del certificado utilizado para verificar el sello, cuando se procesa la identificación, entonces se responde SIGNATURE_VERIFICATION_FAILED sin emitir token ni permitir continuar o crear expediente.

- **CA-023:** Dado un certificado con atributos de sujeto cuyos OID incluyen 2.5.4.3, 2.5.4.41 o 2.5.4.10, cuando se obtiene el nombre completo / razón social para persona física o moral, entonces se utiliza el valor del primer atributo coincidente en el orden original del certificado, sin priorizar los OID ni concatenar valores.
- **CA-024:** Dado que el nombre recibido de PKI difiere del extraído del certificado, cuando se acepta la identificación, entonces legalName utiliza el extraído y la evidencia conserva sin cambios razonSocial recibido; esa diferencia por sí sola no rechaza la identificación.
- **CA-025:** Dado que no existe ningún atributo de nombre coincidente o el primero tiene valor vacío, cuando se procesa la identificación, entonces se responde VALIDATION_ERROR sin emitir token ni permitir continuar o crear expediente; no se busca otro valor posterior.

- **CA-026:** Dado un JWT recibido en una operación protegida, cuando el backend lo valida, entonces comprueba firma, algoritmo permitido, claims obligatorios, emisor, audiencia, vencimiento y la existencia del contexto de su jti para ese RFC conforme a ADR-0015. Un JWT alterado, incompleto o sin contexto produce REGISTRATION_TOKEN_INVALID sin autorizar la operación.

- **CA-027:** Dado una nueva identificación exitosa, cuando se emite su JWT, entonces el backend confirma en la misma BD del registro un contexto para ese jti con RFC, emisión, vencimiento y referencias de evidencia/aceptación antes de devolverlo. Un fallo no devuelve token ni modifica los contextos existentes. Un JWT no vencido se rechaza si no existe su contexto, incluido después de reiniciar H2.

**Requerimiento relacionado:** RF-REG-01; COMP-EFIRMA-001.
