# ADR-0011: Frontend con Angular 21

- **Estado:** Aceptado
- **Ámbito:** Frontend
- **Fecha:** 2026-10-18
- **Specs relacionadas:** RF-REG-01 a RF-REG-08; US01 a US11; ADR-0012 a ADR-0014.

## Contexto

La interfaz de registro requiere navegación por secciones, validación condicional, tablas,
carga documental e integración con la Firma Electrónica del solicitante. Su stack debe
quedar separado de las decisiones del backend.

## Decisión

- El frontend de Fredkor Labs se implementará con Angular 21 y TypeScript compatible con esa línea.
- Angular coordinará vistas, rutas, componentes, servicios de aplicación frontend e integración HTTP.
- Las versiones se declaran en `frontend/package.json` y se fijan en `frontend/package-lock.json`
  (npm): Angular 21.0.6, Angular CLI 21.0.4, TypeScript 5.9.3 y RxJS 7.8 a 2026-10-21.
  Node.js, TypeScript y RxJS deben respetar la matriz de compatibilidad de Angular 21.
- Las reglas funcionales provienen de RF y HU. La validación visual no sustituye la del backend.
- El resultado de Firma Electrónica se procesa conforme al contrato; no se implementan
  firma, vigencia u OCSP dentro de los componentes de negocio.
- La estructura del código y formularios se rigen por ADR-0013 y ADR-0014.
- No se selecciona en este ADR una librería adicional de estado ni un mecanismo de autenticación.

## Alternativas consideradas

| Alternativa | A favor | En contra |
| :--- | :--- | :--- |
| Angular 21 | Cumple el stack elegido para la interfaz y la plantilla. | Requiere mantener herramientas y dependencias compatibles. |
| Otro framework o versión mayor | Podría construir las mismas vistas. | No corresponde a la elección tecnológica de Fredkor Labs. |

## Consecuencias y verificación

Se preparará el proyecto Angular sin cambiar el stack Java del backend. Se comprobarán
compilación, dependencias y comportamiento de las secciones cuando exista implementación.
El contrato API y la correlación de identidad permanecen compartidos entre ambos ámbitos.

## Referencia técnica

- [Compatibilidad de versiones de Angular](https://angular.dev/reference/versions).
