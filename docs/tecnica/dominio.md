<!-- Generado con /documentar desde specs/ y backend/src/. No editar a mano: cambia la spec y regenera. -->

# Dominio

Resume [domain-model.md](../../specs/architecture/domain-model.md) y
[el ciclo de vida](../../specs/domain/supplier-registration-lifecycle.md) con las clases de
`domain.model`. Las definiciones completas están en las specs; aquí se enlazan con el código.

## Términos y su clase

Definiciones en [domain-model.md §1](../../specs/architecture/domain-model.md#1-glosario-de-negocio).

| Término | Clase o código técnico |
| :--- | :--- |
| Expediente | `SupplierRegistration` (raíz), `SupplierRegistrationId` |
| RFC | `Rfc` |
| Régimen | `TaxRegime` (`LEGAL_ENTITY`, `INDIVIDUAL`), derivado por `ApplicantIdentity.taxRegime()` |
| Usuario | `Username`, derivado por `ApplicantIdentity.username()` |
| Origen | `Origin` (`NATIONAL`, `FOREIGN`) |
| Código fiscal extranjero | `ForeignFiscalCode` |
| Aceptación de políticas | `NoticeAcceptance` |
| Resultado de la Firma Electrónica del solicitante | `ApplicantSignatureEvidence` (dominio), `SignatureResultInput` (aplicación) |
| Sección | `RegistrationSection`, `SectionStatus`, `SectionProgress` |
| Cédula fiscal | `FiscalIdentificationDocument`, `StoredDocumentReference`, `FiscalDocumentFormat` |
| Tipo de participación | `ParticipationType` (`GOODS_AND_SERVICES_SUPPLIER`, `PUBLIC_WORKS_CONTRACTOR`) |
| Partida específica | `CucopSpecificItem` (padres: `CucopGenericItem`, `CucopConcept`, `CucopChapter`) |
| Clave del proveedor | Sin clase: etapa posterior |

## Estados y condiciones de sección

Según [lifecycle.md](../../specs/domain/supplier-registration-lifecycle.md) §1, §2 y §6:

| Tipo | Código técnico | Denominación funcional | Enum |
| :--- | :--- | :--- | :--- |
| Estado del expediente | `IN_PROGRESS` | EN_CAPTURA | `RegistrationStatus` |
| Estado del expediente | `REGISTERED` | REGISTRADO | `RegistrationStatus` |
| Condición de sección | `PENDING` | Pendiente | `SectionStatus` |
| Condición de sección | `COMPLETED` | COMPLETADA | `SectionStatus` |
| Condición de sección | `REQUIRES_REVIEW` | Requiere revisión (reservado) | `SectionStatus` |

Secciones en orden (`RegistrationSection`): `GENERAL_DATA`, `CUCOP`, `CONTACTS`, `FISCAL_ADDRESS`, `COVERAGE`,
`ELECTRONIC_SIGNATURE`. Solo las dos primeras se capturan en esta etapa; `CONTACTS` es el punto de continuación.

## Transiciones y operaciones

Según [lifecycle.md §4](../../specs/domain/supplier-registration-lifecycle.md#4-transiciones-y-operaciones-sobre-el-expediente),
[§7](../../specs/domain/supplier-registration-lifecycle.md#7-documento-y-progreso-de-datos-generales) y
[domain-model.md §7](../../specs/architecture/domain-model.md#7-operaciones-del-agregado):

| Estado previo | Acción | Método de `SupplierRegistration` | Operación | Resultado |
| :--- | :--- | :--- | :--- | :--- |
| Sin expediente | Crear expediente | `SupplierRegistration.start` | `startSupplierRegistration` | IN_PROGRESS, seis secciones PENDING. Duplicado: `DUPLICATE_RFC` (409). |
| IN_PROGRESS | Reanudar | `ensureResumable`, `resume` | `resumeSupplierRegistration` | Mismo expediente y progreso; nombre, aceptación y evidencia actualizados. |
| IN_PROGRESS | Guardar sección con Siguiente | `saveGeneralData`, `saveCucopParticipation` | `saveGeneralData`, `saveCucopParticipation` | Sección COMPLETED. CUCoP exige GENERAL_DATA COMPLETED (INV-30). |
| IN_PROGRESS | Cargar cédula | `associateFiscalDocument` | `uploadFiscalDocument` | Asociada; no completa Datos Generales (INV-28). |
| IN_PROGRESS | Eliminar cédula | `dissociateFiscalDocument` | `deleteFiscalDocument` | GENERAL_DATA PENDING; CUCoP intacto (INV-29, INV-37). |
| IN_PROGRESS | Volver con Atrás | — | Sin petición | Solo interfaz; no guarda. |
| IN_PROGRESS | Finalizar registro | — | — | Etapa posterior; no implementado. |
| REGISTERED | Nueva alta o reanudación | `ensureResumable` y unicidad por RFC | `startSupplierRegistration`, `resumeSupplierRegistration` | Rechazo: `DUPLICATE_RFC` o `INVALID_STATE_TRANSITION` (409). |

## Invariantes

Texto completo y fuentes en [domain-model.md §3](../../specs/architecture/domain-model.md#3-invariantes-y-responsabilidades).
"Clases" lista las clases de `backend/src/main/java` que nombran la invariante en su código o comentarios, más las
clases de dominio que la hacen cumplir sin nombrarla (`Rfc`, `Nationality`, `ForeignFiscalCode`, `Website`,
`PhoneNumber`, `TaxRegime`, …). Una celda vacía corresponde a reglas de interfaz, del componente externo o de etapas
posteriores, o a reglas que solo coordinan aplicación y persistencia sin citarlas.

| INV | Grupo | Regla (primera frase) | Dónde se garantiza (spec) | Clases | Criterios |
| :--- | :--- | :--- | :--- | :--- | :--- |
| INV-01 | Identificación y políticas | El RFC obtenido de la Firma Electrónica del solicitante es obligatorio y solo admite 12 o 13 caracteres. | Dominio: valor RFC; aplicación: aceptar resultado de la Firma Electrónica del solicitante. | `Rfc` | US01 CA-010, CA-011; US08 CA-016 |
| INV-02 | Identificación y políticas | La identidad requiere nombre extraído del certificado verificado; la evidencia conserva el nombre recibido. | Dominio: extraer el nombre del certificado (`CertificateIdentity`); aplicación: aceptar resultado de la Firma Electrónica del solicitante. | `CertificateIdentity`, `IdentifyApplicantService` | US01 CA-002, CA-010, CA-023 a CA-025 |
| INV-03 | Identificación y políticas | El componente de Firma Electrónica del solicitante solo procesa certificados vigentes y validados contra OCSP. | Componente externo de Firma Electrónica del solicitante: vigencia y OCSP; integración y aplicación: procesar resultado o error. | — | US01 CA-003, CA-004, CA-005, CA-007, CA-008, CA-009 |
| INV-04 | Identificación y políticas | La identificación exige sello verificado y correspondencia del RFC con el titular del mismo certificado según ADR-0016; un fallo impide aceptarla. | Integración criptográfica del backend: verificar el sello; dominio: RFC del titular y correspondencia con el recibido (`CertificateIdentity`); aplicación: coordinar. | `CertificateIdentity`, `IdentifyApplicantService`, `JdkApplicantSignatureVerificationAdapter` | US01 CA-014, CA-015, CA-020 a CA-022 |
| INV-05 | Identificación y políticas | La firma inicial utiliza la cadena literal `Temporal` y no finaliza el registro. | Integración de Firma Electrónica del solicitante y flujo de aplicación. | — | US01 CA-012 |
| INV-06 | Identificación y políticas | Se conserva íntegramente el resultado exitoso aceptado de la Firma Electrónica del solicitante. | Aplicación y adaptadores de persistencia e integración. | `ApplicantSignatureEvidencePersistenceAdapter`, `IdentifyApplicantService` | US01 CA-006, CA-013 |
| INV-07 | Identificación y políticas | La Política de Aviso debe aceptarse expresamente antes de solicitar la firma. | Interfaz: presentación; aplicación: condición previa de firma y creación. | — | US03 CA-001, CA-002, CA-003, CA-004 |
| INV-08 | Identificación y políticas | El aviso es fijo, versión 1.0. | Dominio: versión fija 1.0 (`NoticeAcceptance`); aplicación y persistencia de la aceptación. | `AcceptRegistrationNoticeService`, `NoticeAcceptance`, `ResumeSupplierRegistrationService` | US03 CA-003, CA-005, CA-007, CA-008 |
| INV-09 | Expediente y acceso | Existe como máximo un expediente para el mismo RFC, incluso ante concurrencia. | Aplicación: consulta y decisión; persistencia: garantía de unicidad. | `CheckRegistrationExistenceService`, `StartSupplierRegistrationService`, `SupplierRegistrationPersistencePort` | US02 CA-001, CA-002, CA-003, CA-004; US04 CA-003 |
| INV-10 | Expediente y acceso | Crear expediente requiere políticas aceptadas, identificación exitosa e inexistencia comprobada. | Dominio: estado e identidad; aplicación: precondiciones y creación; persistencia: guardado. | `StartSupplierRegistrationService`, `SupplierRegistration` | US04 CA-001, CA-004, CA-005, CA-006 |
| INV-11 | Expediente y acceso | El RFC del expediente es inmutable; el RFC y la razón social no se modifican mediante la captura. | Dominio: identidad; interfaz: solo lectura; aplicación: respetar identidad verificada. | `ApplicantIdentity` | US02 CA-005; US08 CA-002 |
| INV-12 | Expediente y acceso | Reanudar exige una nueva identificación y solo permite el expediente del mismo RFC. | Aplicación: identidad y recuperación; persistencia. | `OwnedRegistration`, `ResumeSupplierRegistrationService` | US05 CA-001, CA-004, CA-005, CA-008 |
| INV-13 | Expediente y acceso | Un expediente REGISTRADO no permite nueva alta ni reanudación como borrador en este flujo. | Dominio: condición de estado; aplicación: decidir flujo. | `ResumeSupplierRegistrationService`, `SupplierRegistration` | US02 CA-003; US05 CA-006 |
| INV-14 | Datos Generales | El régimen se calcula con la longitud del RFC: 12 para PERSONA MORAL y 13 para PERSONA FÍSICA. | Dominio: cálculo; interfaz: solo lectura; persistencia: copia para reportes. | `ApplicantIdentity`, `TaxRegime` | US08 CA-011, CA-012 |
| INV-15 | Datos Generales | Usuario es el RFC convertido a minúsculas, de solo lectura, y se muestra desde la primera identificación exitosa. | Dominio: valor derivado; aplicación e interfaz: presentación. | `ApplicantIdentity` | US08 CA-019 |
| INV-16 | Datos Generales | Origen es obligatorio y lo selecciona el proveedor entre Nacional y Extranjero. | Dominio: catálogo fijo y obligatoriedad; interfaz: selección. | `GeneralData`, `InvalidGeneralDataException` | US08 CA-017 |
| INV-17 | Datos Generales | País es obligatorio. | Dominio: coherencia de país y origen; aplicación: catálogo; interfaz. | `GeneralData` | US08 CA-009, CA-010 |
| INV-18 | Datos Generales | Nacionalidad es obligatoria: Mexicana y de solo lectura para Nacional (el guardado exige exactamente `Mexicana`); captura alfanumérica de hasta 250 caracteres para Extranjero. | Dominio: validación condicional; interfaz: captura y solo lectura. | `GeneralData`, `Nationality` | US08: tabla de campos; CA-005, CA-006 |
| INV-19 | Datos Generales | Código fiscal extranjero es un texto separado del RFC, editable, visible y obligatorio solo para Extranjero, con máximo 20 caracteres. | Dominio: obligatoriedad y longitud; interfaz: visibilidad. | `ForeignFiscalCode`, `GeneralData` | US08 CA-015, CA-018 |
| INV-20 | Datos Generales | Titularidad jurídica es obligatoria: para Nacional sus opciones coinciden con país y régimen; para Extranjero coinciden con país o clave NOT_DEF, sin filtro por régimen, incluso cuando existen opciones del país. | Aplicación: consultar catálogo y relaciones; dominio: selección coherente. | `GeneralData`, `LegalOwnershipOption` | US08 CA-003, CA-014 |
| INV-21 | Datos Generales | Sector y Subsector son obligatorios solo para persona moral; el subsector corresponde al sector elegido. | Dominio: condición y coherencia; aplicación: relaciones de catálogo; interfaz. | `GeneralData` | US08: tabla de campos; CA-004, CA-006 |
| INV-22 | Datos Generales | Giro, Estratificación, Actividad preponderante, Número de trabajadores y Ventas anuales son obligatorios y seleccionados de catálogo; Estratificación requiere exactamente una opción, sin asociación ni filtro por régimen. | Dominio: obligatoriedad; aplicación: registros de catálogo. | `GeneralData` | US08: tabla de campos; CA-005, CA-006, CA-022 |
| INV-23 | Datos Generales | Las selecciones de catálogos dinámicos se validan contra registros vigentes cuando aplique. | Aplicación mediante acceso a catálogos; dominio consume los datos necesarios para validar. | `GeneralData` | US08 CA-008 |
| INV-24 | Datos Generales | Página web es opcional; si se informa debe ser URL absoluta con esquema http o https y host válido, de hasta 2048 caracteres tras recortar extremos; vacío se convierte a null. | Dominio: formato y longitud. | `GeneralData`, `Website` | US08 CA-013 |
| INV-25 | Datos Generales | Cambiar el campo de dependencia descarta sus selecciones dependientes y exige elegirlas nuevamente. | Interfaz: limpiar selecciones; dominio y aplicación: validar coherencia al guardar. | — | US08 CA-020 |
| INV-26 | Teléfonos y cédula fiscal | Solo persona moral captura teléfonos y requiere al menos uno para avanzar. | Dominio: colección y valores telefónicos; aplicación: validar guardado. | `GeneralData`, `InvalidGeneralDataException`, `PhoneNumber` | US09 CA-001, CA-003, CA-004, CA-005 |
| INV-27 | Teléfonos y cédula fiscal | La cédula fiscal es obligatoria para Nacional y Extranjero al completar Datos Generales; se admite un solo PDF de máximo 5 MiB (5,242,880 bytes). | Dominio: obligatoriedad, cantidad, formato y tamaño (`FiscalDocumentFormat`); aplicación: validar antes de conservar; adaptador de archivos: guardar el contenido. | `DocumentFormatNotSupportedException`, `DocumentTooLargeException`, `FiscalDocumentAlreadyExistsException`, `FiscalDocumentFormat`, `FiscalDocumentRequiredException`, `SupplierRegistration` | US10 CA-002, CA-003, CA-004 |
| INV-28 | Teléfonos y cédula fiscal | La carga exitosa conserva y asocia inmediatamente el archivo, incluso sin Siguiente; volver o reanudar lo recupera. | Dominio: asociación sin completar Datos Generales (`SupplierRegistration.associateFiscalDocument`); aplicación, almacenamiento y persistencia de la carga. | `SupplierRegistration` | US10 CA-005 a CA-007, CA-010 |
| INV-29 | Teléfonos y cédula fiscal | Eliminar exige confirmación; desasociación, marca de metadatos y progreso se confirman en una transacción de BD, sin borrar contenido físico. | Dominio: desasociación y Datos Generales PENDING (`SupplierRegistration.dissociateFiscalDocument`); interfaz, aplicación y persistencia; limpieza física por ADR-0010. | `SupplierRegistration`, `SupplierRegistrationPersistencePort` | US10 CA-006, CA-007, CA-011 |
| INV-30 | CUCoP | Acceder a CUCoP requiere Datos Generales guardados correctamente: la sección GENERAL_DATA en COMPLETED. | Dominio: precondición de la sección (`SupplierRegistration.saveCucopParticipation`). | `SupplierRegistration` | US11 CA-001 |
| INV-31 | CUCoP | Completar CUCoP exige al menos un tipo de participación y una partida específica. | Dominio: validación de la sección. | `CucopParticipation`, `InvalidCucopParticipationException`, `RawRequestBodyAdvice`, `RepeatedRequestItemException`, `RequestUniqueItems`, `SupplierRegistration` | US11 CA-003, CA-004, CA-005, CA-011 |
| INV-32 | CUCoP | La jerarquía es Capítulo → Concepto → Partida genérica → Partida específica; cada hijo referencia el ID de su padre inmediato y todo capítulo, concepto y genérica tiene al menos una específica. | Catálogos y su persistencia; aplicación: consulta jerárquica. | `CucopCatalogAdapter`, `GetCucopCatalogService` | US11: Campos y condiciones; CA-005 |
| INV-33 | CUCoP | Solo genéricas y específicas tienen selección editable. | Interfaz: operaciones de selección; aplicación: recibir la selección para validación y guardado. | — | US11 CA-007, CA-008, CA-013, CA-014 |
| INV-34 | Guardado y avance | Siguiente valida y guarda transaccionalmente antes de avanzar. | Dominio: reglas de sección; aplicación: transacción y avance; persistencia. | `SupplierRegistration`, `SupplierRegistrationPersistencePort` | US06 CA-002, CA-003, CA-004; US08 CA-006, CA-007; US11 CA-011, CA-012 |
| INV-35 | Guardado y avance | Atrás descarta los cambios sin guardar de la sección abandonada; al regresar se muestran solo valores persistidos o la captura inicial si no existen. | Interfaz y aplicación: separación de navegación y guardado. | — | US06 CA-009; US11 CA-010 |
| INV-36 | Guardado y avance | Iniciar o reanudar abre Datos Generales. | Aplicación e interfaz: recuperación y navegación. | — | US04 CA-002; US05 CA-002, CA-003, CA-007; US06 CA-007; US08 CA-001 |
| INV-37 | Guardado y avance | Los cambios en Datos Generales afectan únicamente a esa sección. | Dominio y aplicación: consistencia entre secciones. | `SupplierRegistration` | US06 CA-006, CA-008 |
| INV-38 | Finalización — etapa posterior | Finalizar exige secciones obligatorias completas, válidas y consistentes; cualquier pendiente impide el cierre. | Dominio y aplicación; pendiente de reglas de las secciones futuras. | — | US07 CA-001, CA-002 |
| INV-39 | Finalización — etapa posterior | La finalización asigna SupplierCode, cambia a REGISTRADO y registra fecha/hora de forma atómica, sin resultados parciales ni duplicados ante reintentos. | Dominio: transición; aplicación: finalización; persistencia: atomicidad y unicidad. | — | US07 CA-003, CA-004, CA-005, CA-006 |
