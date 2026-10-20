# Catálogos cerrados

Un valor fuera de estos catálogos debe ser rechazado.

## Acceso a los catálogos

Los catálogos comparten BD con el expediente y se consultan mediante adaptador JPA conforme a
[ADR-0001](../adr/0001-backend-arquitectura-hexagonal.md). La administración de catálogos no
forma parte del registro. Los valores están separados en [catalog-values.md](catalog-values.md);
los nombres, tipos y FK físicos están en [esquema de catálogos aprobado](../architecture/catalog-database-proposal.md).

## Códigos técnicos de los catálogos fijos del registro

Los códigos del modelo se corresponden con las denominaciones funcionales existentes.
Los IDs de los registros de catálogo no se sustituyen por estos códigos.

| Tipo del modelo | Código técnico | Denominación funcional |
| :--- | :--- | :--- |
| `TaxRegime` | `LEGAL_ENTITY` | PERSONA MORAL |
| `TaxRegime` | `INDIVIDUAL` | PERSONA FÍSICA |
| `Origin` | `NATIONAL` | NACIONAL |
| `Origin` | `FOREIGN` | EXTRANJERO |
| `ParticipationType` | `GOODS_AND_SERVICES_SUPPLIER` | Proveedor de adquisiciones, arrendamientos y servicios |
| `ParticipationType` | `PUBLIC_WORKS_CONTRACTOR` | Contratista de obra pública o servicios relacionados con las mismas |

El tipo de participación es una selección que admite uno o ambos valores. No equivale al
estado del expediente ni al catálogo de tipo de proveedor.

## 1. Catálogo Régimen

[Valores del catálogo](catalog-values.md#catalog-1).

## 2. Catálogo Origen

[Valores del catálogo](catalog-values.md#catalog-2).

## 3. Catálogo Estatus del proveedor

[Valores del catálogo](catalog-values.md#catalog-3).

## 4. Catálogo de titularidad jurídica.

La selección es obligatoria y el backend valida las mismas condiciones que el filtrado del frontend:

1. Nacional (origen ID 1): `iso_country = pais.clave` y régimen `PERSONA MORAL` para
   régimen ID 1 o `PERSONA FÍSICA` para persona física.
2. Extranjero: `iso_country = pais.clave` **o** `clave = NOT_DEF`, sin filtro de régimen.
   Incluir `NOT_DEF` incluso si hay opciones del país. Si el resultado es vacío, consultar
   `NOT_DEF`; si tampoco existe, mantener la lista vacía sin crear opciones.
3. Orden final ascendente por `clave`.

La API representa `clave` con `key` e `iso_country` con `countryCode`, además del régimen
cuando exista. No se asignan nuevos IDs ni descripciones a `NOT_DEF`.

[Valores del catálogo](catalog-values.md#catalog-4).

## 5. Catálogo de Sector.

[Valores del catálogo](catalog-values.md#catalog-5).

## 6. Catálogo de SubSector clasificados por sector.

Se consulta según el Sector seleccionado; no se carga completo al iniciar Datos Generales.

[Valores del catálogo](catalog-values.md#catalog-6).

## 7. Catálogo de Giro.

[Valores del catálogo](catalog-values.md#catalog-7).

## 8. Catálogo de estratificación.

Se debe seleccionar exactamente una opción. El catálogo se consulta completo y no tiene
reglas de asociación ni filtros por régimen. Las restricciones de Titularidad Jurídica
por régimen u origen/país se mantienen según sus reglas propias.

[Valores del catálogo](catalog-values.md#catalog-8).

## 9. Catálogo de Actividad preponderante.

[Valores del catálogo](catalog-values.md#catalog-9).

## 10. Catálogo de Número de trabajadores.

[Valores del catálogo](catalog-values.md#catalog-10).

## 11. Catálogo de Ventas anuales.

[Valores del catálogo](catalog-values.md#catalog-11).

## 12. Catálogo de paises:

[Valores del catálogo](catalog-values.md#catalog-12).

## 13. Catálogo de tipo de proveedor.

[Valores del catálogo](catalog-values.md#catalog-13).

## Catálogos CUCoP

Los cuatro catálogos existentes proporcionan la estructura del árbol CUCoP.
Todos los IDs de catálogos son enteros, incluidos los IDs de padre. No se presupone
un rango ni un tamaño físico adicional.

| Catálogo | Campos | Valor de `nivel` |
| :--- | :--- | :--- |
| `capitulo` | `id`, `clave`, `descripcion`, `nivel` | 1 |
| `concepto` | `id`, `clave`, `descripcion`, `nivel` | 2 |
| `partida_generica` | `id`, `clave`, `descripcion`, `nivel` | 3 |
| `partida_especifica` | `id`, `clave`, `descripcion`, `nivel` | 4 |

- `id` identifica el registro; `clave` contiene su clave de catálogo y `descripcion` su descripción.
- La jerarquía de presentación es Capítulo → Concepto → Partida genérica → Partida específica.
- Cada registro hijo contiene el ID de su padre inmediato: Concepto referencia un Capítulo,
  Partida genérica referencia un Concepto y Partida específica referencia una Partida genérica.
  Capítulo es el nivel raíz. El nombre físico del campo de referencia se precisará al
  documentar el esquema de los catálogos existentes.
- Todas las partidas genéricas tienen partidas específicas.
- Las reglas de selección se definen en RF-REG-08 y HU-REG-011.

### Definiciones pendientes de CUCoP

- Tipos físicos de almacenamiento, formatos y longitudes de clave y descripción. Los IDs son enteros.
- Nombre físico del campo que contiene el ID del padre en cada catálogo hijo.
- Mapeo físico del esquema existente al adaptador JPA de la misma base de datos. La API devuelve el árbol completo en una consulta; búsqueda y expansión se realizan en frontend.
