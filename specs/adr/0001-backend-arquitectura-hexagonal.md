# ADR-0001: Arquitectura hexagonal y modelos por frontera

- **Estado:** Aceptado
- **Ámbito:** Backend
- **Fecha:** 2026-09-26
- **Actualización:** 2026-10-20
- **Specs relacionadas:** RF-REG-01 a RF-REG-08; INV-01 a INV-39; `architecture/architecture.md`; `architecture/domain-model.md`.

## Contexto

Las reglas del registro deben probarse independientemente de HTTP, persistencia, archivos y
Firma Electrónica del solicitante. La verificación criptográfica, los catálogos y el volumen
requieren adaptadores con responsabilidades distintas de las del dominio.

## Decisión

- `domain.model` y `domain.exception` contienen modelo y reglas en Java puro, sin Spring,
  JPA, puertos ni clientes externos.
- `application.port.in` contiene casos de uso y Commands, Queries y Results. Un servicio de
  `application.service` implementa cada caso de uso; una HU puede requerir varios servicios.
- `application.port.out` contiene los puertos de persistencia del expediente, aceptación,
  evidencia y documento, consulta de catálogos, verificación de sello y almacenamiento documental.
- Los adaptadores `in.web`, `out.persistence`, `out.catalog`, `out.signature` y `out.document`
  implementan las fronteras descritas en arquitectura. Se inyectan por constructor.
- El controlador utiliza interfaces de entrada y resultados de aplicación; no recibe agregados
  ni llama directamente a adaptadores de salida. Los servicios no dependen de implementaciones concretas.
- Los puertos y servicios de aplicación no dependen de Spring, JPA, HTTP ni otros frameworks.
  `configuration` construye los servicios Java y ensambla los adaptadores mediante `@Bean`.
- La aplicación coordina validación, acceso al expediente del mismo RFC y unidades de consistencia.
  Los decoradores de `adapter.in.transaction` aplican las transacciones con Spring alrededor
  de los casos de uso que las requieren, abarcando todos los puertos participantes.
  Las reglas puras permanecen en el dominio. Las verificaciones externas se realizan mediante puertos.
- Cada frontera conserva sus propios modelos. Las entidades JPA, DTO HTTP y objetos del
  componente de firma no se convierten en tipos de dominio.
- La hora de negocio proviene de `Clock` inyectado en aplicación; el dominio recibe los instantes.

### Acceso a catálogos en la misma base de datos

Los catálogos de Datos Generales y los cuatro niveles CUCoP residen en la misma base de datos que los expedientes. Se consultan mediante Spring Data JPA desde adapter.out.catalog, que implementa GeneralDataCatalogPort y CucopCatalogPort. No se requiere un servicio HTTP externo ni otra conexión de base de datos. Las entidades y repositorios de catálogo permanecen en el adaptador; el dominio y los puertos no dependen de JPA. El registro consulta catálogos, sin habilitar su administración ni modificar sus claves o relaciones.

El esquema físico y sus mapeos se documentarán antes de implementar el adaptador. La decisión reutiliza las dependencias JPA ya aceptadas y no introduce un servicio nuevo.

## Alternativas consideradas

| Alternativa | A favor | En contra |
| :--- | :--- | :--- |
| Capas con entidades JPA compartidas hasta el controlador | Menos mapeos. | Acopla reglas y contrato a persistencia. |
| Puertos dentro del dominio | Reduce separación de paquetes. | Contradice el límite de dominio puro elegido. |

## Consecuencias y verificación

- Más modelos y mapeos a cambio de aislar cambios de contrato e infraestructura.
- Pruebas puras para reglas; dobles de puertos para aplicación; pruebas específicas para adaptadores.
- Las dependencias se verifican contra la tabla de capas de arquitectura. No se agrega una
  biblioteca de verificación arquitectónica como consecuencia implícita de este ADR.
