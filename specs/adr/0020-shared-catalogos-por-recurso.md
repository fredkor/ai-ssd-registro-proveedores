# ADR-0020: Un recurso por catálogo de Datos Generales

- **Estado:** Aceptado
- **Ámbito:** Compartido
- **Fecha:** 2026-10-21
- **Specs relacionadas:** RF-REG-05; US08 CA-003, CA-008, CA-014, CA-022, CA-023; `domain/catalogs.md`; ADR-0001, ADR-0015, ADR-0019.

## Contexto

`getGeneralDataCatalogs` devuelve los ocho catálogos de Datos Generales en una sola respuesta.
Así no se puede cachear cada catálogo por separado, y algunos catálogos pueden recibir datos nuevos
desde otros sistemas en la base compartida, con distinta frecuencia que el resto. Titularidad
Jurídica se entregaba completa y el frontend la filtraba.

## Decisión

- Cada catálogo de Datos Generales es un recurso propio, con su operación, su caso de uso y su
  consulta en `GeneralDataCatalogPort`. Se retira `getGeneralDataCatalogs`.

| Catálogo | Operación | Ruta (`/api/v1/registration-catalogs/...`) | Caso de uso |
| :--- | :--- | :--- | :--- |
| Régimen | `getTaxRegimes` | `tax-regimes` | `GetTaxRegimesUseCase` |
| País | `getCountries` | `countries` | `GetCountriesUseCase` |
| Titularidad jurídica | `getLegalOwnerships` | `legal-ownerships?origin=&countryCode=&taxRegime=` | `GetLegalOwnershipsUseCase` |
| Sector | `getSectors` | `sectors` | `GetSectorsUseCase` |
| Subsector | `getSubsectors` | `sectors/{sectorId}/subsectors` | `GetSubsectorsUseCase` |
| Giro | `getBusinessActivities` | `business-activities` | `GetBusinessActivitiesUseCase` |
| Estratificación | `getStratifications` | `stratifications` | `GetStratificationsUseCase` |
| Actividad preponderante | `getPrimaryActivities` | `primary-activities` | `GetPrimaryActivitiesUseCase` |
| Número de trabajadores | `getWorkerCountRanges` | `worker-count-ranges` | `GetWorkerCountRangesUseCase` |
| Ventas anuales | `getAnnualSalesRanges` | `annual-sales-ranges` | `GetAnnualSalesRangesUseCase` |

- Cada respuesta es un objeto `{ "items": [...] }`, sin paginación, ordenado de forma ascendente
  por `id`, salvo Titularidad Jurídica, que se ordena por `key` (`domain/catalogs.md`). Los catálogos
  con campo `active` (hoy Estratificación) devuelven solo los registros activos.
- **Régimen** (decisión del 2026-10-23): el frontend muestra la descripción del régimen del
  solicitante. El catálogo devuelve `id` y `description`, como los demás, y la identidad del
  solicitante trae `taxRegimeId` con el ID del catálogo (`domain/catalogs.md`).
- **Subsector:** el sector forma parte de la ruta. Un sector inexistente responde 200 con lista
  vacía; un `sectorId` que no es entero responde VALIDATION_ERROR con `parameter` (ADR-0019).
- **Titularidad Jurídica se filtra en el backend** con el algoritmo de `domain/catalogs.md`:
  - `origin` (`NATIONAL` o `FOREIGN`) y `countryCode` son obligatorios.
  - `taxRegime` es obligatorio con `NATIONAL` y no se permite con `FOREIGN`: así cada combinación
    de filtros identifica un único resultado cacheable.
  - Con `NATIONAL` se devuelven los registros del país y del régimen. Con `FOREIGN`, los del país
    más `NOT_DEF`, sin filtro de régimen. Un país sin opciones responde lista vacía (`NATIONAL`) o
    solo `NOT_DEF` (`FOREIGN`).
  - Un parámetro ausente, de más (no definido en la operación o repetido) o inválido responde
    VALIDATION_ERROR con `parameter`. Un `countryCode` que no existe en el catálogo de países
    (comparación exacta) es inválido (decisión del 2026-10-22).
  - Guardar Datos Generales sigue validando las mismas condiciones.
- Todas las operaciones requieren el token de registro (ADR-0015). Su respuesta no depende del
  solicitante: es la misma para todos los que envían los mismos parámetros.
- CUCoP sigue siendo un solo recurso: `getCucopCatalog` devuelve el árbol completo ya jerarquizado
  en una consulta (RF-REG-08); cada nodo trae sus hijos en `items` (decisión del 2026-10-23).
- **Caché:** no se implementa en esta etapa. Cada caso de uso de catálogo podrá envolverse en un
  decorador de caché en `configuration`, como los decoradores transaccionales, sin cambiar el
  contrato. El almacenamiento, la vigencia por catálogo y la invalidación ante datos nuevos se
  decidirán en un ADR propio. No se envían cabeceras `Cache-Control` ni `ETag` por ahora.

## Alternativas consideradas

| Alternativa | A favor | En contra |
| :--- | :--- | :--- |
| Un recurso por catálogo | Caché y vigencia independientes por catálogo; respuestas pequeñas. | Más peticiones al cargar Datos Generales; opción elegida. |
| Respuesta agregada (anterior) | Una sola petición. | No permite cachear ni refrescar un catálogo por separado. |
| Titularidad completa y filtrada en frontend | Un solo recurso cacheable. | Duplica el algoritmo en frontend y backend; no elegida. |

## Consecuencias y verificación

- Al abrir Datos Generales el frontend hace hasta ocho peticiones independientes, que pueden ir en
  paralelo. Subsector se pide al cambiar el Sector y Titularidad al cambiar origen, país o régimen.
- Las pruebas verifican, por catálogo, contenido, orden, filtro de activos y token requerido. Para
  Titularidad verifican el algoritmo de `NOT_DEF` y los parámetros obligatorios, ausentes o de más.
