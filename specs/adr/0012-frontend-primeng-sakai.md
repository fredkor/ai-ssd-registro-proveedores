# ADR-0012: PrimeNG y plantilla Sakai para Angular 21

- **Estado:** Aceptado
- **Ámbito:** Frontend
- **Fecha:** 2026-10-18
- **Specs relacionadas:** RF-REG-05, RF-REG-08; US06, US08 a US11; ADR-0011.

## Contexto

La interfaz necesita componentes consistentes de formularios, tablas, mensajes y árbol
CUCoP. Fredkor Labs elige Sakai como plantilla gratuita para evitar adquirir una plantilla comercial.

## Decisión

- Se utilizará PrimeNG con la plantilla Sakai de PrimeTek, en una distribución compatible
  con Angular 21. Se utilizará la línea PrimeNG 21 compatible con la plantilla y Angular 21.
- Las versiones exactas de paquetes y revisión de Sakai se fijarán y verificarán al preparar
  el frontend; no se usará automáticamente la última versión de otra línea mayor.
- Se conservarán los avisos de la licencia MIT de Sakai al incorporar la plantilla.
- Se reutilizarán sus componentes y estilos para las vistas del registro, sin incorporar
  páginas de demostración como funcionalidades del negocio.
- El árbol CUCoP utilizará PrimeNG Tree. Capítulos y conceptos son navegación sin casilla
  editable; genéricas y específicas admiten selección conforme a RF-REG-08.
- CUCoP carga sus cuatro niveles completos en una consulta; la búsqueda y expansión se
  ejecutan sobre ese árbol en frontend, sin carga diferida por nodo ni búsqueda remota. El
  volumen aproximado de 300 elementos no se convierte en un límite de registros.
- Marcar una genérica selecciona todas sus específicas, desmarcarla las deselecciona y
  Deshacer selección limpia todo el árbol. La propagación automática del componente se
  configurará o adaptará para mantener estas reglas.
- Los modelos de PrimeNG, nodos del árbol y eventos visuales no se enviarán como modelo
  de negocio al backend. Se mapearán a tipos de participación y referencias de específicas.
- La estructura inicial de Sakai se adaptará a los límites por dominio de ADR-0013.

## Alternativas consideradas

| Alternativa | A favor | En contra |
| :--- | :--- | :--- |
| PrimeNG con Sakai | Plantilla gratuita elegida y componentes coherentes. | Requiere adaptar su organización y comportamiento al registro. |
| Plantilla comercial | Ofrece otras composiciones visuales. | No corresponde a la elección de plantilla gratuita. |
| UI construida sin plantilla | Control total de cada vista. | Requiere construir la base visual ya disponible en Sakai. |

## Consecuencias y verificación

Se verificará integración de la versión elegida, formularios y selección del árbol,
particularmente las diferencias entre expandir y seleccionar. La plantilla no cambia RF,
campos requeridos, persistencia con Siguiente ni navegación con Atrás.

## Referencias técnicas

- [Documentación de Sakai](https://sakai.primeng.org/documentation).
- [Licencia MIT de Sakai](https://github.com/primefaces/sakai-ng/blob/master/LICENSE.md).
- [PrimeNG 21](https://v21.primeng.org/installation) y [Tree](https://v21.primeng.org/tree).
