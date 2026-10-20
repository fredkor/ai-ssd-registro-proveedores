# Ciclo de vida del registro de proveedores

## 1. Propósito y alcance

Definir el ciclo de vida del expediente de registro de proveedores de Fredkor Labs y su
relación con el avance de las secciones del asistente.

La primera etapa comprende identificación, inicio o reanudación, Datos Generales y CUCoP,
con punto de continuación hacia Contactos. La finalización se documenta como etapa posterior.

Los nombres funcionales de los RF y HU se corresponden con los siguientes códigos técnicos
del modelo, conforme a [conventions.md](../conventions.md). No representan estados adicionales.

| Tipo | Código técnico | Denominación funcional |
| :--- | :--- | :--- |
| Estado del expediente | `IN_PROGRESS` | EN_CAPTURA |
| Estado del expediente | `REGISTERED` | REGISTRADO |
| Condición de sección | `PENDING` | Pendiente |
| Condición de sección | `COMPLETED` | COMPLETADA |
| Condición de sección | `REQUIRES_REVIEW` | Requiere revisión |

Las secciones del modelo siguen este orden:

| Orden | Código técnico | Sección |
| :--- | :--- | :--- |
| 1 | `GENERAL_DATA` | Datos Generales |
| 2 | `CUCOP` | CUCoP |
| 3 | `CONTACTS` | Contactos |
| 4 | `FISCAL_ADDRESS` | Domicilio fiscal |
| 5 | `COVERAGE` | Cobertura |
| 6 | `ELECTRONIC_SIGNATURE` | Firma Electrónica |

## 2. Estados del expediente

| Denominación funcional | Significado | Alcance |
| :--- | :--- | :--- |
| EN_CAPTURA | Expediente creado, cuyo proceso de registro no ha finalizado. Permite guardar las secciones aplicables y reanudar la captura. | Primera etapa. |
| REGISTRADO | Expediente cuyo registro finalizó correctamente. No permite una nueva alta ni reanudarse como borrador mediante este flujo. | La detección de un registro existente aplica en la primera etapa; la transición de finalización es posterior. |

SIN_EXPEDIENTE describe la ausencia de un expediente para el RFC; no es un estado persistido.
La aceptación de políticas y el resultado de la Firma Electrónica del solicitante no constituyen por sí mismos un expediente.

El estado del expediente no representa el origen, régimen, tipo de participación ni la
sección visible del asistente.

## 3. Precondiciones del expediente

Aceptar el aviso e identificarse no crea un expediente. Tras verificar la firma e identidad,
la consulta por RFC determina si corresponde crear, reanudar o impedir una nueva alta.
Las condiciones de identificación están en RF-REG-01 a RF-REG-03 y US01 a US03.

## 4. Transiciones y operaciones sobre el expediente

| Situación o estado previo | Acción | Condiciones | Resultado | Fuente |
| :--- | :--- | :--- | :--- | :--- |
| Sin expediente | Crear expediente | Políticas aceptadas, identificación exitosa e inexistencia comprobada. | Se crea un UUID único en EN_CAPTURA, asociado al RFC, identidad y aceptación; se abre Datos Generales. | RF-REG-03, RF-REG-04; HU-REG-003, HU-REG-004. |
| EN_CAPTURA | Reanudar | Nueva identificación mediante e.firma; el RFC corresponde al expediente. | Mismo expediente y mismo estado, con datos y progreso recuperados; siempre se abre Datos Generales. | RF-REG-06; HU-REG-005. |
| EN_CAPTURA | Guardar una sección con Siguiente | Validación y persistencia transaccional exitosas. | Se conserva EN_CAPTURA, se marca la sección COMPLETADA y se avanza. | RF-REG-05, RF-REG-08; HU-REG-006, HU-REG-008 a HU-REG-011. |
| EN_CAPTURA | Volver con Atrás | Existe una sección anterior. | Se conserva EN_CAPTURA y se muestra la sección anterior sin guardar los datos de la sección abandonada. | RF-REG-05; HU-REG-006 CA-009, HU-REG-011 CA-010. |
| EN_CAPTURA | Finalizar registro | Todas las secciones obligatorias completas, válidas y consistentes; reglas finales definidas. | De forma atómica pasa a REGISTRADO, se asigna SupplierCode y se registra fecha/hora de finalización. | RF-REG-07; HU-REG-007. Etapa posterior. |
| REGISTRADO | Nueva alta o reanudación como borrador | Se detecta expediente para el mismo RFC. | Se rechaza la operación; el estado no cambia. | RF-REG-02, RF-REG-06; HU-REG-002 CA-003, HU-REG-005 CA-006. |

- Un error de consulta no se interpreta como inexistencia del expediente.
- Se impide crear expedientes duplicados para un RFC incluso ante concurrencia. Si otra
  solicitud creó el expediente, se utiliza el flujo correspondiente al estado encontrado.
- Un error de creación no debe presentarse como expediente creado.
- Reanudar no crea un expediente nuevo ni permite acceder a uno ajeno.
- Un error de recuperación no sobrescribe los datos persistidos.
- Un fallo de guardado no completa la sección ni permite avanzar.
- La finalización futura no debe dejar resultados parciales ni duplicar registros o claves ante reintentos.

## 5. Avance y navegación por secciones

| Orden | Sección | Condición para avanzar definida | Alcance |
| :--- | :--- | :--- | :--- |
| 1 | Datos Generales | Cumplir los campos y dependencias de HU-REG-008, teléfonos de HU-REG-009 cuando aplica y cédula fiscal de HU-REG-010; guardar mediante Siguiente. | Primera etapa. |
| 2 | CUCoP | Datos Generales guardados; al menos un tipo de participación y una partida específica seleccionados; validación y guardado exitosos mediante Siguiente. | Primera etapa. |
| 3 | Contactos | Pendiente de su especificación funcional. | Punto de continuación; captura posterior. |
| 4 | Domicilio fiscal | Pendiente de su especificación funcional. | Etapa posterior. |
| 5 | Cobertura | Pendiente de su especificación funcional. | Etapa posterior. |
| 6 | Firma Electrónica | Pendiente de sus reglas finales. | Etapa posterior. |

- Los títulos de las secciones no permiten navegación directa; se utilizan Atrás y Siguiente.
- Iniciar o reanudar abre Datos Generales, aunque existan secciones ya completas.
- Se recuperan los datos guardados sin exigir recaptura de información completa.
- Atrás descarta cambios locales; al regresar se restaura lo persistido o la captura inicial.
- Siguiente valida y confirma datos y progreso; no hay guardados por pulsación.
- La sección visible no es un estado del expediente y no demuestra que la sección esté completa.
- Completar Datos Generales o CUCoP no finaliza el registro ni genera SupplierCode.

## 6. Condición de las secciones

Las siguientes son condiciones funcionales de avance, representadas por los códigos de
sección definidos en el apartado 1; no son estados adicionales del expediente:

| Condición | Significado | Fuente |
| :--- | :--- | :--- |
| Pendiente | Aún no se ha completado satisfactoriamente la validación y el guardado de la sección. | RF-REG-05, RF-REG-06. |
| COMPLETADA | La sección superó sus validaciones y se guardó correctamente mediante Siguiente. | RF-REG-05; HU-REG-006 CA-003. |
| Requiere revisión | Reservado para dependencias de etapas posteriores que deberán especificarse; no se asigna a CUCoP por cambios en Datos Generales. | Modelo de dominio; alcance de la primera etapa. |

Si cambia un campo del que dependen selecciones, estas se descartan y deben seleccionarse
nuevamente; los valores predeterminados de solo lectura mantienen sus reglas específicas.
Los cambios en Datos Generales afectan únicamente a esa sección. CUCoP conserva sus tipos de participación, partidas seleccionadas y estado de progreso, incluso al eliminar la cédula fiscal; no se borra, invalida ni marca para revisión por esos cambios.

Una sección previamente guardada permanece COMPLETADA al volver a ella o editarla; un nuevo
guardado válido conserva COMPLETADA. Un error no altera los datos ni el progreso persistidos.
La eliminación exitosa de la cédula devuelve Datos Generales a PENDING; cargar otra exige Siguiente para completar nuevamente. Un fallo no cambia el progreso. CUCoP permanece intacto.

## 7. Documento y progreso de Datos Generales

- Cargar conserva y asocia el documento inmediatamente; no completa la sección.
- Volver o reanudar recupera el archivo asociado aunque no se haya pulsado Siguiente.
- Eliminar requiere confirmación; éxito significa desasociar y marcar metadatos en BD, junto con el progreso, sin tocar contenido físico. La limpieza diaria realiza después el borrado.
- La eliminación exitosa deja Datos Generales PENDING sin modificar sus demás datos ni CUCoP.
  Si falla, conserva archivo, referencia y progreso.
- Cargar otra cédula no completa la sección hasta validar y guardar con Siguiente.
- Atrás descarta cambios de campos, pero no revierte cargas ni eliminaciones exitosas.

Gestión de archivos: [ADR-0010](../adr/0010-backend-almacenamiento-documental-en-volumen.md).
Criterios: [US10](../user-stories/US10-gestionar-cedula-fiscal.md).

## 8. Límites y definiciones pendientes

- No se definen transiciones de cancelación, caducidad, abandono, eliminación o reapertura
  del expediente, al no contar con reglas aprobadas para esas operaciones.
- REGISTRADO no admite reanudación como borrador en el flujo de alta; esto no define su
  tratamiento en futuros módulos de administración.
- CAPTURA_COMPLETA no forma parte del modelo vigente. Agregar estados exige una regla nueva.
- Las dependencias concretas entre secciones posteriores, la firma final y la generación
  de SupplierCode se definirán antes de implementar esas funcionalidades.

## 9. Referencias

- [Visión](../vision.md).
- [Requerimientos funcionales](../requirements/functional.md).
- [Identificación](../user-stories/US01-identificacion-mediante-efirma.md), [existencia](../user-stories/US02-verificar-existencia-del-registro.md),
  [políticas](../user-stories/US03-aceptar-politicas.md) y [creación](../user-stories/US04-crear-expediente-de-registro.md).
- [Reanudación](../user-stories/US05-reanudar-expediente-incompleto.md), [navegación](../user-stories/US06-navegar-y-guardar-secciones.md)
  y [finalización futura](../user-stories/US07-finalizar-registro.md).
- [Datos Generales](../user-stories/US08-capturar-datos-generales.md), [teléfonos](../user-stories/US09-capturar-telefonos-del-proveedor.md),
  [cédula fiscal](../user-stories/US10-gestionar-cedula-fiscal.md) y [CUCoP](../user-stories/US11-capturar-participacion-y-partidas-cucop.md).
