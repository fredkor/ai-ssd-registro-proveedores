# ADR-0014: Preferencia por formularios reactivos de Angular

- **Estado:** Aceptado
- **Ámbito:** Frontend
- **Fecha:** 2026-10-18
- **Specs relacionadas:** RF-REG-05, RF-REG-08; US08 a US11; ADR-0011 a ADR-0013.

## Contexto

Datos Generales incluye campos condicionales, dependencias, valores de solo lectura y
filas dinámicas de teléfonos. La selección CUCoP también condiciona el avance y exige
separar captura local de información persistida.

## Decisión

- Se prefieren los formularios reactivos de `@angular/forms` para la captura de negocio,
  utilizando controles y grupos tipados, validadores y colecciones dinámicas cuando aplique.
- Los teléfonos se representan mediante un FormArray con un grupo por fila: Número
  obligatorio de 10 dígitos y Extensión opcional de hasta 5 caracteres. Agregar crea una
  fila vacía editable; no persiste el teléfono ni exige capturarlo previamente.
- Los validadores condicionales y la limpieza de selecciones dependientes siguen RF-REG-05.
  País, régimen, usuario y demás campos de solo lectura respetan sus reglas funcionales.
- El mapeo al request es explícito: que un control esté deshabilitado o visible no decide
  por sí mismo si un campo forma parte del contrato ni permite enviar una identidad editable.
- Los controles pueden alimentar la validación inmediata y el estado de Siguiente. Un
  formulario válido solo permite intentar el guardado; la sección se declara completa tras
  respuesta exitosa del backend.
- Cambios de control, selección de nodos y Atrás no ejecutan guardados automáticos. La
  carga de cédula guarda y asocia inmediatamente y devuelve UUID; Eliminar llama a su operación y retira la fila solo tras éxito. Siguiente valida la cédula asociada.
- Se mantiene separado el estado de captura del progreso persistido y del estado global
  del expediente. Reanudar abre Datos Generales con los datos ya guardados.
- Los componentes PrimeNG se integran con los controles reactivos. Los validadores puros
  se ubican en el área de dominio frontend y la coordinación de formularios en su feature.
- No se mezclan ngModel y controles reactivos para gestionar el mismo campo. Una excepción
  a la preferencia debe justificarse sin cambiar las reglas ni añadir otra biblioteca de formularios.

## Alternativas consideradas

| Alternativa | A favor | En contra |
| :--- | :--- | :--- |
| Formularios reactivos | Estado explícito, validación condicional y filas dinámicas comprobables. | Requieren construir y mapear los controles. |
| Formularios dirigidos por plantilla | Menos código inicial en capturas simples. | No son la opción preferida para estas dependencias y colecciones dinámicas. |

## Consecuencias y verificación

Se probarán obligatoriedad, campos dependientes, formato de teléfonos, filas vacías,
valores derivados y peticiones solo al ejecutar las acciones definidas. Las pruebas
comprobarán que una validación frontend no sustituye la respuesta del backend.

## Referencia técnica

- [Formularios reactivos de Angular](https://angular.dev/guide/forms/reactive-forms).
