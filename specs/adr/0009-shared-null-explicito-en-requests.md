# ADR-0009: Campos requeridos, opcionales y guardado completo de secciones

- **Estado:** Aceptado
- **Ámbito:** Compartido
- **Fecha:** 2026-09-27
- **Actualización:** 2026-10-18
- **Specs relacionadas:** RF-REG-01, RF-REG-05, RF-REG-08; US08 a US11; INV-16 a INV-34.

## Contexto

El registro valida y guarda secciones completas con Siguiente. Incluye campos opcionales y
condicionales. Una regla global heredada de rechazo de null o un PATCH parcial no especificado
no describe estas operaciones ni la conservación íntegra del resultado de firma.

## Decisión

- Las entradas de guardado representan los datos de la sección que deben validarse como
  conjunto. No se interpreta de forma general `null` como “conservar el valor anterior”.
- Los campos obligatorios deben estar presentes y cumplir su regla. Omitirlos, dejarlos sin
  valor o enviarlos inválidos no permite avanzar y produce VALIDATION_ERROR.
- Los campos condicionales se exigen según régimen y origen, conforme a RF-REG-05.
- Los campos de solo lectura y derivados no se actualizan mediante captura editable. El
  contrato separará identidad aceptada de datos de captura.
- La cédula se carga, asocia o elimina mediante sus operaciones propias; Siguiente valida la asociación persistida. No se recibe fiscalDocumentId ni se elimina enviando null en Datos Generales.
- No se añade una configuración global de rechazo de null sobre todos los modelos del
  componente de firma: debe conservarse íntegramente su resultado y solo se rechazan las
  ausencias o formatos que las reglas y la verificación requieran.
- Los campos opcionales admiten null como ausencia. Al guardar la sección completa, null
  deja ese campo sin valor; no conserva automáticamente el valor anterior.
- Un campo obligatorio por régimen u origen no admite null cuando aplica su obligatoriedad.
- Todas las propiedades definidas en los requests deben enviarse, incluidas las de objetos
  anidados y ResultadoFirma. Omitir una propiedad produce VALIDATION_ERROR. Los campos
  opcionales sin valor y los no aplicables se envían como null; por ejemplo, sectorId y
  subsectorId para persona física. Un campo obligatorio por sus condiciones no admite null.
- En los campos editables de texto se quitan los espacios en blanco del inicio y del final antes de validar obligatoriedad, longitud y formato. Los espacios interiores se conservan. Si el resultado queda vacío, un campo obligatorio (incluido el condicional aplicable) se rechaza con VALIDATION_ERROR y un campo opcional se convierte a null, sin conservar automáticamente el valor anterior. La regla también aplica a número y extensión telefónicos. No modifica ResultadoFirma, RFC, valores extraídos del certificado, identificadores, códigos de catálogo, tokens ni nombres originales de archivo.
- El backend garantiza esta normalización antes de aplicar las restricciones de longitud y formato de los DTO y del dominio; el frontend debe reflejarla al validar el formulario. No basta recortar después de una validación web que ya rechazó la entrada.
- Las propiedades del resultado de firma se conservan sin normalizar, pero no se acepta un resultado sin los datos necesarios para verificar.
- Un cuerpo vacío no puede completar una sección cuyos campos obligatorios no satisface.
  No se traslada `minProperties: 1` como regla única de validez para todas las operaciones.
- Todos los requests rechazan propiedades no definidas en su esquema con VALIDATION_ERROR,
  incluidos objetos anidados y ResultadoFirma. Los campos definidos del resultado aceptado
  se conservan íntegramente; ampliar la interfaz del componente requiere actualizar el contrato.

## Alternativas consideradas

| Alternativa | A favor | En contra |
| :--- | :--- | :--- |
| Rechazar todo null globalmente | Una política uniforme. | Confunde campos opcionales y evidencia externa con requerimientos obligatorios. |
| PATCH genérico con null como no enviado | Permite actualización parcial. | No está definido para el guardado conjunto de la sección y oculta intentos de quitar datos requeridos. |

## Consecuencias y verificación

Las pruebas separarán obligatoriedad, condiciones y formato de la representación técnica
de ausencia. Se probará null en opcionales y su rechazo en campos requeridos o condicionales
aplicables. Se probará el rechazo de propiedades desconocidas también en objetos anidados.
Se verificará el rechazo de omisiones y el envío de null en campos no aplicables, incluidos
los objetos anidados. Se probarán recorte previo a formato/longitud, espacios interiores intactos, obligatorio vacío rechazado, opcional vacío convertido a null e identidad/evidencia intactas. No se incorpora una librería adicional por anticipado.
