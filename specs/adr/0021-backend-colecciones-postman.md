# ADR-0021: Colecciones Postman como verificación complementaria

- **Estado:** Aceptado
- **Ámbito:** Backend
- **Fecha:** 2026-10-25
- **Specs relacionadas:** `api/supplier-registration-openapi.yaml`; `domain/supplier-registration-lifecycle.md`;
  `architecture/tech-stack.md`; `traceability.md`; ADR-0008, ADR-0010, ADR-0015, ADR-0016, ADR-0018.

## Contexto

`./mvnw test` verifica cada capa: el dominio con JUnit puro, la aplicación con dobles de puertos, y
la web y la persistencia con MockMvc y un PostgreSQL desechable (ADR-0018). No ejercita la aplicación
en ejecución de extremo a extremo con su base duradera, su volumen documental (ADR-0010) y sus
tokens reales (ADR-0015). Swagger UI (ADR-0008) explora operaciones sueltas, pero no encadena el
token, el expediente y la cédula entre peticiones.

## Decisión

1. Postman es una verificación **complementaria** contra la aplicación en ejecución
   (`./mvnw spring-boot:run` con `REGISTRATION_DB_URL`, `_USERNAME` y `_PASSWORD`, ADR-0018).
   No reemplaza `./mvnw test` ni es requisito para hacer commit.
2. `postman/registro-proveedores-contrato.postman_collection.json` se **genera** en Postman desde
   `specs/api/supplier-registration-openapi.yaml`. Es un artefacto derivado: no se edita a mano.
   Cuando cambia el contrato se regenera y se vuelve a exportar en un commit `docs:`.
3. `postman/registro-proveedores-flujo.postman_collection.json` recorre a mano el ciclo de vida de
   `domain/supplier-registration-lifecycle.md`:
   - aviso y aceptación;
   - identificación;
   - existencia, y luego inicio o reanudación del expediente;
   - carga de la cédula;
   - Datos Generales;
   - CUCoP;
   - reanudación con las secciones COMPLETED;
   - descarga y eliminación de la cédula, que deja Datos Generales PENDING sin tocar CUCoP (INV-37).

   Lleva aserciones `pm.test` y las variables `baseUrl`, `registrationToken` y `registrationId`.
4. **Identificación:** el cuerpo de `identifyApplicant` usa el certificado y el sello de prueba
   `legal-entity` de `backend/src/test/resources/signature/fixtures.json` (ADR-0016). Son material
   de prueba generado con node-forge, no una e.firma real. Si se regeneran los fixtures, se
   actualiza la colección.
5. **Cédula:** el flujo carga `postman/fixtures/cedula-ejemplo.pdf`, un PDF ficticio de pocos KiB.
6. La base de desarrollo es duradera: si el RFC del fixture ya tiene expediente, el flujo lo
   reanuda en lugar de iniciarlo.

## Alternativas consideradas

| Alternativa | A favor | En contra |
| :--- | :--- | :--- |
| Sin Postman | Una sola fuente de verificación. | No hay prueba manual ni E2E contra la aplicación en ejecución. |
| Una sola colección mantenida a mano | Simple. | Se desincroniza del contrato sin que nadie lo note. |
| Postman como verificación obligatoria | Más cobertura E2E. | Requiere la aplicación, su base y Docker levantados; duplica lo que ya cubre `./mvnw test`. |
| Token pegado a mano | No copia material de firma a la colección. | El flujo no corre completo y el token vence a los 60 minutos (ADR-0015). |

## Consecuencias y verificación

- La colección de contrato sigue al contrato y la de flujo sigue al ciclo de vida. Si cambia una
  transición, una precondición de sección o una invariante del flujo, se revisa la colección de flujo.
- `traceability.md` §5 indica qué cubre cada colección.
- Swagger UI (ADR-0008) sigue siendo la exploración interactiva; Postman añade escenarios encadenados.
- Ninguna colección guarda credenciales de base de datos ni llaves JWT.
