<!-- Generado con /documentar desde specs/ y backend/src/. No editar a mano: cambia la spec y regenera. -->


# `getRegistrationNotice`

**GET `/api/v1/registration-notices/current`** — Consultar Política de Aviso vigente.

- Contrato: [api.md](../api.md#getregistrationnotice).
- Secuencia: [diagramas.md](../diagramas.md#21-getregistrationnotice).
- Trazabilidad: [traceability.md §2](../../../specs/traceability.md#2-operaciones-de-la-primera-etapa).

## Historias y requerimientos

[RF-REG-03](../../../specs/requirements/functional.md), [US03](../../../specs/user-stories/US03-aceptar-politicas.md).

## Criterios de aceptación

| Criterio | Reglas o fuente | Verificación prevista | Tipo (spec_sync) | Pruebas | Clases de prueba |
| :--- | :--- | :--- | :--- | :--- | :--- |
| US03 CA-001 | INV-07 | Interfaz: mostrar el aviso antes de solicitar la firma; el backend lo sirve sin token (US03 CA-006). | interfaz / externa / futura | 0 | — |
| US03 CA-006 | US03 CA-006 | Interfaz y web. | backend | 3 | GetRegistrationNoticeServiceTest, RegistrationNoticesControllerTest, RegistrationTokenSecurityTest |

Criterios cuya operación principal es esta, según [traceability.md §3](../../../specs/traceability.md#3-cobertura-de-criterios-de-aceptación). Conteo de [`docs/sync-report.md`](../../sync-report.md).

## Caso de uso

| Elemento | Clase |
| :--- | :--- |
| Puerto de entrada | [`GetRegistrationNoticeUseCase.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/in/GetRegistrationNoticeUseCase.java) |
| Entrada | Sin parámetros |
| Resultado | [`GetRegistrationNoticeResult.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/in/GetRegistrationNoticeResult.java) |
| Decorador transaccional | Ninguno: se registra el servicio directo ([arquitectura §7](../../../specs/architecture/architecture.md#7-coordinación-y-transacciones)) |
| Servicio | [`GetRegistrationNoticeService.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/service/GetRegistrationNoticeService.java) |

## Invariantes

INV-07, INV-08. Detalle en [dominio.md](../dominio.md#invariantes).

## Archivos por capa

| Capa | Archivos |
| :--- | :--- |
| Web | [`RegistrationNoticesController.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/web/RegistrationNoticesController.java) implementa `RegistrationNoticesApi` (generada desde el contrato) |
| Errores | [`GlobalExceptionHandler.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/web/GlobalExceptionHandler.java) |
| Aplicación | [`GetRegistrationNoticeService.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/service/GetRegistrationNoticeService.java) |
| Dominio | — |
| Puertos de salida | — |
| Adaptadores de salida | — |

## Pruebas

| Clase de prueba | Ubicación |
| :--- | :--- |
| `GetRegistrationNoticeServiceTest` | [mx/com/fredkorlabs/registration/application/service/GetRegistrationNoticeServiceTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/application/service/GetRegistrationNoticeServiceTest.java) |
| `RegistrationNoticesControllerTest` | [mx/com/fredkorlabs/registration/adapter/in/web/RegistrationNoticesControllerTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/adapter/in/web/RegistrationNoticesControllerTest.java) |
| `RegistrationTokenSecurityTest` | [mx/com/fredkorlabs/registration/RegistrationTokenSecurityTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/RegistrationTokenSecurityTest.java) |

Incluye las clases nombradas como el caso de uso o el controlador y las que verifican sus criterios.
