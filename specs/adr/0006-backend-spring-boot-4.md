# ADR-0006: Stack backend con Spring Boot 4.1 y Java 25

- **Estado:** Aceptado
- **Ámbito:** Backend
- **Fecha:** 2026-09-26
- **Actualización:** 2026-10-18
- **Specs relacionadas:** `architecture/tech-stack.md`; `pom.xml`; ADR-0001.

## Contexto

El módulo ya declara Spring Boot 4.1.1 y Java 25 en Maven. La generación del contrato declara
openapi-generator 7.22.0 con opciones para Spring Boot 4 y Jackson 3.

## Decisión

- Se mantiene Spring Boot 4.1.x y Java 25 conforme a la configuración del repositorio.
- Las versiones exactas pertenecen a `pom.xml`; no se cambian durante la homologación documental.
- Spring se utiliza para adaptadores, ensamblaje, validación y transacciones de aplicación.
  El dominio continúa usando únicamente el JDK.
- La generación de interfaces y DTO deberá comprobar su compatibilidad con la configuración
  de Maven al incorporar el contrato de registro.
- La verificación de firma utilizará un adaptador; el algoritmo, codificación y posibles
  bibliotecas se concretarán con el contrato técnico, sin introducir dependencias implícitas.

## Alternativas consideradas

| Alternativa | A favor | En contra |
| :--- | :--- | :--- |
| Mantener el stack configurado | Evita una migración ajena al registro. | Requiere validar generación y adaptadores al implementar. |
| Cambiar de versión o framework | Podría responder a otra necesidad tecnológica. | No existe una necesidad definida para este cambio. |

## Consecuencias y verificación

Se verificará generación, compilación y pruebas cuando se prepare la implementación. Este
ADR describe la elección del repositorio y no afirma disponibilidad o soporte actual de
versiones externas. El frontend se define en ADR-0011 a ADR-0014; Angular, PrimeNG y Sakai
no son dependencias del backend.
