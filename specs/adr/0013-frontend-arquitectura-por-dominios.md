# ADR-0013: Arquitectura frontend por dominios: DDD / Nx Style

- **Estado:** Aceptado
- **Ámbito:** Frontend
- **Fecha:** 2026-10-18
- **Specs relacionadas:** RF-REG-01 a RF-REG-08; modelo de dominio; ADR-0011, ADR-0012.

## Contexto

La organización del frontend debe seguir las capacidades del negocio y evitar una carpeta
global de componentes o servicios que mezcle firma, captura, documentos y catálogos.
La estructura visual de una plantilla no define esos límites.

## Decisión

- Se organizará Angular por dominios funcionales, con separación de responsabilidades
  inspirada en DDD y el estilo de organización de Nx.
- El dominio funcional inicial es `supplier-registration`. Datos Generales, teléfonos,
  cédula y CUCoP son funcionalidades dentro del registro, no aplicaciones independientes
  ni agregados nuevos por el solo hecho de tener carpetas propias.
- Dentro de cada dominio se distinguen estas responsabilidades:

| Área | Contenido | Límite |
| :--- | :--- | :--- |
| `domain` | Modelos frontend y reglas puras de validación y transformación. | Sin HTTP ni dependencias de PrimeNG o de la plantilla. |
| `data-access` | Clientes API, DTO, mapeadores y acceso a datos del dominio funcional. | No contiene componentes de pantalla. |
| `application` | Fachadas y coordinación de flujo, resultados de casos de uso y estado de captura. | No depende de componentes concretos ni del layout de Sakai. |
| `feature` | Páginas, rutas y contenedores de identificación, Datos Generales y CUCoP. | Coordina la interacción visual mediante application. |
| `ui` | Componentes de presentación del dominio. | Reciben datos y emiten eventos; no llaman directamente al backend. |

- `core` concentra ensamblaje e integraciones transversales de la aplicación y `shared`
  solo elementos reutilizables sin reglas particulares del registro. No se trasladan allí
  funcionalidades de negocio por comodidad.
- Las dependencias fluyen de feature hacia application, de application hacia domain y
  data-access, y de data-access hacia modelos de domain. Domain no importa las otras áreas.
  Los componentes ui no importan feature ni data-access.
- El layout y tema de Sakai se mantienen fuera de las reglas del dominio.
- La integración del componente de Firma Electrónica se encapsula para que sus tipos y
  eventos no se propaguen por todos los componentes del registro.
- “Nx Style” define la organización y los límites; no obliga a instalar Nx, crear un monorepo
  ni generar librerías por cada carpeta. Incorporar esas herramientas requiere decisión adicional.
- No se traduce el modelo Java literalmente a clases TypeScript ni se comparten entidades
  JPA. El contrato define el intercambio entre ambos.

## Alternativas consideradas

| Alternativa | A favor | En contra |
| :--- | :--- | :--- |
| Carpetas globales por tipo técnico | Inicio sencillo. | Dispersa una funcionalidad entre áreas sin límite de negocio. |
| Conservar toda la estructura de demostración de Sakai | Menos reorganización inicial. | El layout y las demos no representan el dominio del registro. |
| Dominios con responsabilidades internas | Agrupa negocio y mantiene fronteras de dependencia. | Exige disciplina en imports y APIs internas. |

## Consecuencias y verificación

La ubicación física exacta de carpetas se fijará al crear el frontend. Se revisarán imports,
separación de HTTP y UI, y pruebas de validadores, mapeadores y coordinación. No se incorpora
una herramienta de comprobación de límites como dependencia implícita de este ADR.

## Referencia técnica

- [Organización y tamaño de proyectos en Nx](https://nx.dev/docs/kb/project-size).
