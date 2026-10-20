# Matriz de trazabilidad — Registro de proveedores de Fredkor Labs

## 1. Estado y alcance

Esta matriz vincula la primera etapa de registro con el borrador OpenAPI, los casos de uso,
las invariantes y la verificación prevista. No declara implementación, pruebas existentes
ni resultados de ejecución. El contrato continúa en borrador hasta revisar su coherencia y alinear y verificar las herramientas de generación.

Los prefijos US01 a US11 de los archivos corresponden a HU-REG-001 a HU-REG-011 en los
encabezados. Cada CA se referencia con su número actual de tres dígitos.

## 2. Operaciones de la primera etapa

| operationId | HTTP y recurso | Caso de uso / servicio | RF y HU | Reglas | Verificación prevista |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `getRegistrationNotice` | GET `/api/v1/registration-notices/current` | `GetRegistrationNoticeUseCase` / `GetRegistrationNoticeService` | RF-REG-03, US03 | INV-07, INV-08 | Interfaz y web. |
| `acceptRegistrationNotice` | POST `/api/v1/registration-notice-acceptances` | `AcceptRegistrationNoticeUseCase` / `AcceptRegistrationNoticeService` | RF-REG-03, US03 | INV-07, INV-08 | Aplicación, persistencia y web. |
| `identifyApplicant` | POST `/api/v1/applicant-identifications` | `IdentifyApplicantUseCase` / `IdentifyApplicantService` | RF-REG-01, RF-REG-03, US01, US03 | INV-01 a INV-08, INV-14, INV-15; ADR-0015, ADR-0016 | Aplicación, firma, contexto y persistencia. |
| `checkRegistrationExistence` | GET `/api/v1/supplier-registrations/existence` | `CheckRegistrationExistenceUseCase` / `CheckRegistrationExistenceService` | RF-REG-02, US02 | INV-09, INV-11, INV-13 | Aplicación y persistencia. |
| `startSupplierRegistration` | POST `/api/v1/supplier-registrations` | `StartSupplierRegistrationUseCase` / `StartSupplierRegistrationService` | RF-REG-04, US04 | INV-07 a INV-11, INV-36 | Dominio, aplicación y persistencia. |
| `resumeSupplierRegistration` | POST `/api/v1/supplier-registrations/{registrationId}/resume` | `ResumeSupplierRegistrationUseCase` / `ResumeSupplierRegistrationService` | RF-REG-06, US05 | INV-12, INV-13, INV-28, INV-36 | Aplicación y persistencia. |
| `saveGeneralData` | PUT `/api/v1/supplier-registrations/{registrationId}/general-data` | `SaveGeneralDataUseCase` / `SaveGeneralDataService` | RF-REG-05, US06, US08, US09, US10 | INV-14 a INV-28, INV-34, INV-37 | Dominio, aplicación, catálogos y persistencia. |
| `saveCucopParticipation` | PUT `/api/v1/supplier-registrations/{registrationId}/cucop` | `SaveCucopParticipationUseCase` / `SaveCucopParticipationService` | RF-REG-08, RF-REG-05, US11, US06 | INV-30 a INV-34, INV-37 | Dominio, aplicación y persistencia. |
| `uploadFiscalDocument` | POST `/api/v1/supplier-registrations/{registrationId}/fiscal-document` | `UploadFiscalDocumentUseCase` / `UploadFiscalDocumentService` | RF-REG-05, US10 | INV-27, INV-28; ADR-0010 | Documento, aplicación, web y persistencia. |
| `downloadFiscalDocument` | GET `/api/v1/supplier-registrations/{registrationId}/fiscal-document` | `DownloadFiscalDocumentUseCase` / `DownloadFiscalDocumentService` | RF-REG-05, US10 | INV-12, INV-27, INV-28; ADR-0010 | Aplicación, web y documento. |
| `deleteFiscalDocument` | DELETE `/api/v1/supplier-registrations/{registrationId}/fiscal-document` | `DeleteFiscalDocumentUseCase` / `DeleteFiscalDocumentService` | RF-REG-05, US10 | INV-27 a INV-29 | Web, aplicación y persistencia: desasociación y marca transaccionales; disco solo en limpieza diaria. |
| `getGeneralDataCatalogs` | GET `/api/v1/registration-catalogs/general-data` | `GetGeneralDataCatalogsUseCase` / `GetGeneralDataCatalogsService` | RF-REG-05, US08 | INV-17, INV-20 a INV-23 | Catálogos y aplicación. |
| `getCucopCatalog` | GET `/api/v1/registration-catalogs/cucop` | `GetCucopCatalogUseCase` / `GetCucopCatalogService` | RF-REG-08, US11 | INV-32; RF-REG-08 | Catálogos, web e interfaz. |

## 3. Cobertura de criterios de aceptación

Una operación indicada es el punto principal de verificación, no una afirmación de que
un CA pertenezca exclusivamente a esa capa. “Sin petición propia” evita inventar endpoints
para interacción local. La navegación, carga externa de firma y búsqueda local requieren
pruebas frontend; no se sustituyen con pruebas de controladores.

### US01 — [US01-identificacion-mediante-efirma](user-stories/US01-identificacion-mediante-efirma.md)

| CA | Operación principal | Reglas o fuente | Verificación prevista |
| :--- | :--- | :--- | :--- |
| CA-001 | Sin petición propia | US01 CA-001 | Interfaz. |
| CA-002 | `identifyApplicant` | INV-02 | Aplicación, web y firma. |
| CA-003 | Sin petición propia | INV-03 | Interfaz e integración externa. |
| CA-004 | Sin petición propia | INV-03 | Interfaz e integración externa. |
| CA-005 | Sin petición propia | INV-03 | Interfaz e integración externa. |
| CA-006 | `identifyApplicant` | INV-06 | Integración y persistencia. |
| CA-007 | Sin petición propia | INV-03 | Interfaz e integración externa. |
| CA-008 | Sin petición propia | INV-03 | Integración externa. |
| CA-009 | Sin petición propia | INV-03 | Integración externa. |
| CA-010 | `identifyApplicant` | INV-01, INV-02 | Dominio, aplicación y web. |
| CA-011 | `identifyApplicant` | INV-01 | Dominio y web. |
| CA-012 | Sin petición propia | INV-05 | Integración externa. |
| CA-013 | `identifyApplicant` | INV-06 | Aplicación y persistencia. |
| CA-014 | `identifyApplicant` | INV-04; ADR-0016 | Adaptador de firma. |
| CA-015 | `identifyApplicant` | INV-04; ADR-0016 | Aplicación, firma y web. |
| CA-016 | `identifyApplicant` | US01 CA-016 | Aplicación y web. |
| CA-017 | `identifyApplicant` | ADR-0015 | Aplicación y web. |
| CA-018 | Sin petición propia | ADR-0015 | Contexto de token y web. |
| CA-019 | `identifyApplicant` | ADR-0015 | Contexto de token, aplicación y concurrencia. |
| CA-020 | `identifyApplicant` | INV-04; ADR-0016 | Firma y aplicación: extracción del primer RFC, sin alternativa del apoderado. |
| CA-021 | `identifyApplicant` | INV-01, INV-04; ADR-0004, ADR-0016 | Firma, aplicación y web: VALIDATION_ERROR. |
| CA-022 | `identifyApplicant` | INV-04; ADR-0004, ADR-0016 | Firma, aplicación y web: SIGNATURE_VERIFICATION_FAILED. |
| CA-023 | `identifyApplicant` | INV-02; ADR-0016 | Firma y aplicación: primer atributo de nombre según orden del certificado. |
| CA-024 | `identifyApplicant` | INV-02, INV-06; ADR-0016 | Aplicación y persistencia: nombre de identidad y evidencia original separados. |
| CA-025 | `identifyApplicant` | INV-02; ADR-0004, ADR-0016 | Firma, aplicación y web: nombre ausente o vacío produce VALIDATION_ERROR. |
| CA-026 | Operaciones protegidas | ADR-0015; US01 CA-026 | Web y contexto: JWT firmado, claims, expiración, algoritmo y jti vigente; rechazo 401 ante token inválido. |
| CA-027 | `identifyApplicant` y operaciones protegidas | ADR-0015; US01 CA-027 | Persistencia y aplicación: contexto JPA en la misma BD, jti único vigente por RFC, emisión tras commit, rollback, concurrencia y pérdida de contexto al reiniciar H2. |

### US02 — [US02-verificar-existencia-del-registro](user-stories/US02-verificar-existencia-del-registro.md)

| CA | Operación principal | Reglas o fuente | Verificación prevista |
| :--- | :--- | :--- | :--- |
| CA-001 | `checkRegistrationExistence` | INV-09 | Aplicación y web. |
| CA-002 | `checkRegistrationExistence` | INV-09 | Aplicación y web. |
| CA-003 | `checkRegistrationExistence` | INV-09, INV-13 | Aplicación y web. |
| CA-004 | `checkRegistrationExistence` | INV-09 | Aplicación y web. |
| CA-005 | `checkRegistrationExistence` | INV-11 | Aplicación y web. |

### US03 — [US03-aceptar-politicas](user-stories/US03-aceptar-politicas.md)

| CA | Operación principal | Reglas o fuente | Verificación prevista |
| :--- | :--- | :--- | :--- |
| CA-001 | `getRegistrationNotice` | INV-07 | Interfaz y web. |
| CA-002 | Sin petición propia | INV-07 | Interfaz. |
| CA-003 | `acceptRegistrationNotice` | INV-07, INV-08 | Aplicación y persistencia. |
| CA-004 | `acceptRegistrationNotice` | INV-07 | Aplicación y web. |
| CA-005 | `startSupplierRegistration` | INV-08 | Aplicación y persistencia. |
| CA-006 | `getRegistrationNotice` | US03 CA-006 | Interfaz y web. |
| CA-007 | `acceptRegistrationNotice`, `resumeSupplierRegistration` | INV-07, INV-08; ADR-0015 | Aplicación y persistencia: aviso fijo, aceptación expresa y sustitución por la última al reanudar sin historial. |
| CA-008 | `resumeSupplierRegistration` | INV-08; ADR-0015 | Aplicación y persistencia: fallo conserva aceptación asociada anterior e historial de evidencias de firma intacto. |

### US04 — [US04-crear-expediente-de-registro](user-stories/US04-crear-expediente-de-registro.md)

| CA | Operación principal | Reglas o fuente | Verificación prevista |
| :--- | :--- | :--- | :--- |
| CA-001 | `startSupplierRegistration` | INV-10 | Dominio, aplicación y persistencia. |
| CA-002 | `startSupplierRegistration` | INV-36 | Dominio, aplicación y persistencia. |
| CA-003 | `startSupplierRegistration` | INV-09 | Dominio, aplicación y persistencia. |
| CA-004 | `startSupplierRegistration` | INV-10 | Dominio, aplicación y persistencia. |
| CA-005 | `startSupplierRegistration` | INV-10 | Dominio, aplicación y persistencia. |
| CA-006 | `startSupplierRegistration` | INV-10 | Dominio, aplicación y persistencia. |

### US05 — [US05-reanudar-expediente-incompleto](user-stories/US05-reanudar-expediente-incompleto.md)

| CA | Operación principal | Reglas o fuente | Verificación prevista |
| :--- | :--- | :--- | :--- |
| CA-001 | `resumeSupplierRegistration` | INV-12 | Aplicación, persistencia y web. |
| CA-002 | `resumeSupplierRegistration` | INV-36 | Aplicación, persistencia y web. |
| CA-003 | `resumeSupplierRegistration` | INV-36 | Aplicación, persistencia y web. |
| CA-004 | `resumeSupplierRegistration` | INV-12 | Aplicación, persistencia y web. |
| CA-005 | `resumeSupplierRegistration` | INV-12 | Aplicación, persistencia y web. |
| CA-006 | `resumeSupplierRegistration` | INV-13 | Aplicación, persistencia y web. |
| CA-007 | `resumeSupplierRegistration` | INV-36 | Aplicación, persistencia y web. |
| CA-008 | `resumeSupplierRegistration` | INV-12 | Aplicación, persistencia y web. |
| CA-009 | `resumeSupplierRegistration` | INV-11, INV-12; US05 CA-009 | Aplicación, persistencia y web: actualizar nombre del certificado sin cambiar UUID, datos ni progreso. |
| CA-010 | `resumeSupplierRegistration` | INV-06; US05 CA-010 | Aplicación y persistencia: historial de evidencias sin sobrescritura. |
| CA-011 | `resumeSupplierRegistration` | INV-12; US05 CA-011 | Aplicación y persistencia: rollback del nombre y vínculo histórico; evidencia aceptada conservada. |

### US06 — [US06-navegar-y-guardar-secciones](user-stories/US06-navegar-y-guardar-secciones.md)

| CA | Operación principal | Reglas o fuente | Verificación prevista |
| :--- | :--- | :--- | :--- |
| CA-001 | Sin petición propia | US06 CA-001 | Interfaz. |
| CA-002 | Sin petición propia | INV-34 | Interfaz y validación de secciones. |
| CA-003 | Sin petición propia | INV-34 | Aplicación y persistencia de secciones. |
| CA-004 | Sin petición propia | INV-34 | Aplicación y web de secciones. |
| CA-005 | `resumeSupplierRegistration` | US06 CA-005 | Interfaz y aplicación. |
| CA-006 | `saveGeneralData` | INV-37 | Dominio, aplicación y persistencia: CUCoP conserva selecciones y progreso intactos. |
| CA-007 | Sin petición propia | INV-36 | Interfaz. |
| CA-008 | Finalización futura | INV-37, INV-38 | Dominio; validar secciones obligatorias sin invalidar CUCoP por cambios en Datos Generales. |
| CA-009 | Sin petición propia | INV-28, INV-35 | Interfaz: descartar cambios con Atrás y restaurar valores persistidos al regresar, incluso en la misma sesión; captura inicial si no existen; archivos asociados recuperados y operaciones documentales exitosas sin revertir. |
| CA-010 | Sin petición propia | US06 CA-010 | Aplicación y persistencia del progreso. |

### US07 — [US07-finalizar-registro](user-stories/US07-finalizar-registro.md)

| CA | Operación principal | Reglas o fuente | Verificación prevista |
| :--- | :--- | :--- | :--- |
| CA-001 | Finalización futura | INV-38 | Etapa posterior; sin endpoint. |
| CA-002 | Finalización futura | INV-38 | Etapa posterior; sin endpoint. |
| CA-003 | Finalización futura | INV-39 | Etapa posterior; sin endpoint. |
| CA-004 | Finalización futura | INV-39 | Etapa posterior; sin endpoint. |
| CA-005 | Finalización futura | INV-39 | Etapa posterior; sin endpoint. |
| CA-006 | Finalización futura | INV-39 | Etapa posterior; sin endpoint. |
| CA-007 | Finalización futura | US07 CA-007 | Etapa posterior; sin endpoint. |

### US08 — [US08-capturar-datos-generales](user-stories/US08-capturar-datos-generales.md)

| CA | Operación principal | Reglas o fuente | Verificación prevista |
| :--- | :--- | :--- | :--- |
| CA-001 | Sin petición propia | INV-36 | Interfaz y reglas de Datos Generales. |
| CA-002 | Sin petición propia | INV-11 | Interfaz y reglas de Datos Generales. |
| CA-003 | `getGeneralDataCatalogs`, `saveGeneralData` | INV-20 | Catálogos, aplicación e interfaz: país y régimen nacionales; orden por clave. |
| CA-004 | `saveGeneralData` | US08 CA-004 | Dominio, aplicación y web. |
| CA-005 | Sin petición propia | US08 CA-005 | Interfaz y reglas de Datos Generales. |
| CA-006 | `saveGeneralData` | INV-34 | Dominio, aplicación y web. |
| CA-007 | `saveGeneralData` | INV-34 | Dominio, aplicación y web. |
| CA-008 | `getGeneralDataCatalogs` | INV-23 | Catálogos, dominio e interfaz. |
| CA-009 | Sin petición propia | INV-17 | Interfaz y reglas de Datos Generales. |
| CA-010 | Sin petición propia | INV-17 | Interfaz y reglas de Datos Generales. |
| CA-011 | Sin petición propia | INV-14 | Interfaz y reglas de Datos Generales. |
| CA-012 | Sin petición propia | INV-14 | Interfaz y reglas de Datos Generales. |
| CA-013 | `saveGeneralData` | INV-24; ADR-0009 | Dominio, aplicación y web: URL absoluta http/https con host; rechazar falta de protocolo/host, otros esquemas y longitud mayor a 2048 tras recorte; vacío a null, sin acceso a red. |
| CA-014 | `getGeneralDataCatalogs`, `saveGeneralData` | INV-20 | Catálogos, aplicación e interfaz: país o NOT_DEF extranjero sin filtro por régimen; NOT_DEF con y sin opciones del país; respaldo vacío y orden por clave. |
| CA-015 | Sin petición propia | INV-19 | Interfaz y reglas de Datos Generales. |
| CA-016 | Sin petición propia | INV-01 | Interfaz y reglas de Datos Generales. |
| CA-017 | Sin petición propia | INV-16 | Interfaz y reglas de Datos Generales. |
| CA-018 | `saveGeneralData` | INV-19 | Dominio, aplicación y web. |
| CA-019 | Sin petición propia | INV-15 | Interfaz y reglas de Datos Generales. |
| CA-020 | Sin petición propia | INV-25 | Interfaz y reglas de Datos Generales. |
| CA-021 | `saveGeneralData` | ADR-0009; RF-REG-05 | Dominio, aplicación y web. |
| CA-022 | `getGeneralDataCatalogs` | US08 CA-022 | Catálogos, dominio e interfaz. |
| CA-023 | `getGeneralDataCatalogs` | US08 CA-023 | Catálogos, dominio e interfaz. |
| CA-024 | `saveGeneralData` | US08 CA-024 | Dominio, aplicación y web. |
| CA-025 | `saveGeneralData` | ADR-0009; RF-REG-05 | Web, aplicación y dominio: recorte previo a validación y espacios interiores conservados. |
| CA-026 | `saveGeneralData` | ADR-0009; RF-REG-05 | Web, aplicación y persistencia: obligatorio vacío rechazado, opcional vacío guardado como null. |
| CA-027 | `saveGeneralData` | ADR-0009; INV-06, INV-11 | Aplicación: identidad y evidencia PKI sin normalización. |

### US09 — [US09-capturar-telefonos-del-proveedor](user-stories/US09-capturar-telefonos-del-proveedor.md)

| CA | Operación principal | Reglas o fuente | Verificación prevista |
| :--- | :--- | :--- | :--- |
| CA-001 | Sin petición propia | INV-26 | Interfaz. |
| CA-002 | Sin petición propia | US09 CA-002 | Interfaz: fila local. |
| CA-003 | `saveGeneralData` | INV-26 | Dominio, aplicación y web. |
| CA-004 | `saveGeneralData` | INV-26 | Dominio, aplicación y web. |
| CA-005 | `saveGeneralData` | INV-26 | Dominio y web. |
| CA-006 | `saveGeneralData` | ADR-0009; RF-REG-05 | Dominio y web. |
| CA-007 | Sin petición propia; `saveGeneralData` al confirmar | US09 CA-007; INV-26, INV-34 | Interfaz y aplicación: botón Eliminar por fila y persistencia de colección con Siguiente. |
| CA-008 | `saveGeneralData` | INV-26; US09 CA-008 | Interfaz, dominio y web: eliminar última fila impide avanzar sin un teléfono válido. |
| CA-009 | `saveGeneralData`, `resumeSupplierRegistration` | INV-34; US09 CA-009 | Aplicación y persistencia: salir o fallar sin guardar conserva teléfonos anteriores. |

### US10 — [US10-gestionar-cedula-fiscal](user-stories/US10-gestionar-cedula-fiscal.md)

| CA | Operación principal | Reglas o fuente | Verificación prevista |
| :--- | :--- | :--- | :--- |
| CA-001 | `uploadFiscalDocument` | US10 CA-001; INV-28 | Carga con asociación inmediata, UUID, descarga y visualización sin Siguiente. |
| CA-002 | `uploadFiscalDocument` | INV-27 | Aplicación y web. |
| CA-003 | `saveGeneralData` | INV-27 | Dominio y aplicación. |
| CA-004 | `uploadFiscalDocument` | INV-27 | Persistencia y concurrencia: máximo una cédula asociada. |
| CA-005 | `uploadFiscalDocument`, `resumeSupplierRegistration` | INV-28 | Archivo disponible tras volver o reanudar aunque no se pulse Siguiente. |
| CA-006 | `deleteFiscalDocument` | INV-29 | Confirmación; desasociación, marca no asociado y progreso en transacción sin disco; fallo hace rollback. |
| CA-007 | `deleteFiscalDocument`, `saveGeneralData` | US10 CA-007 | Eliminar exitosamente deja Datos Generales PENDING; fallo conserva progreso; CUCoP intacto. |
| CA-008 | `uploadFiscalDocument` | INV-27; ADR-0010, ADR-0004 | Documento y web: PDF de 5,242,880 bytes permitido, un byte adicional rechazado; variantes de mayúsculas en extensión. |
| CA-009 | `uploadFiscalDocument` | INV-27; ADR-0010, ADR-0004 | Documento y web: contenido no PDF renombrado y MIME declarado no evitan HTTP 415. |
| CA-010 | `saveGeneralData` | INV-27, INV-28; US10 CA-010 | Validar asociación existente sin UUID editable; fallo de sección conserva archivo cargado. |
| CA-011 | `deleteFiscalDocument`, `resumeSupplierRegistration`, `downloadFiscalDocument` | INV-29; ADR-0010 | Archivo desasociado inaccesible desde expediente; metadatos retenidos para limpieza, asociados excluidos. |

### US11 — [US11-capturar-participacion-y-partidas-cucop](user-stories/US11-capturar-participacion-y-partidas-cucop.md)

| CA | Operación principal | Reglas o fuente | Verificación prevista |
| :--- | :--- | :--- | :--- |
| CA-001 | `getCucopCatalog` | INV-30 | Interfaz y aplicación. |
| CA-002 | Sin petición propia | US11 CA-002 | Interfaz. |
| CA-003 | Sin petición propia | INV-31 | Interfaz. |
| CA-004 | `saveCucopParticipation` | INV-31 | Interfaz y dominio. |
| CA-005 | Sin petición propia | INV-31 | Interfaz. |
| CA-006 | Sin petición propia | US11 CA-006 | Interfaz. |
| CA-007 | Sin petición propia | INV-33 | Interfaz. |
| CA-008 | Sin petición propia | INV-33 | Interfaz. |
| CA-009 | Sin petición propia | US11 CA-009 | Interfaz: búsqueda local. |
| CA-010 | Sin petición propia | INV-35 | Interfaz: ausencia de guardado. |
| CA-011 | `saveCucopParticipation` | INV-31, INV-34 | Dominio, aplicación y persistencia. |
| CA-012 | `saveCucopParticipation` | INV-34 | Aplicación y web. |
| CA-013 | Sin petición propia | INV-33 | Interfaz. |
| CA-014 | Sin petición propia | INV-33 | Interfaz. |
| CA-015 | `getCucopCatalog` | US11 CA-015 | Web, catálogos e interfaz. |

## 4. Reglas expresadas en campos y contratos

Las tablas de campos de US08 también especifican reglas que no tienen un CA individual
exclusivo. Se verifican al guardar Datos Generales, sin afirmar que el número de CA por sí
solo demuestra toda la cobertura:

| Detalle | Fuente | Operación / verificación |
| :--- | :--- | :--- |
| Nacionalidad obligatoria y condicional, hasta 250 caracteres para extranjero. | RF-REG-05; INV-18; tabla de campos US08. | saveGeneralData: dominio y web. |
| Sector/Subsector requeridos para persona moral y relación entre ambos. | RF-REG-05; INV-21; tabla de campos US08. | getGeneralDataCatalogs y saveGeneralData. |
| Giro, estratificación, actividad preponderante, trabajadores y ventas obligatorios; estratificación es una sola opción sin asociación ni filtro por régimen. | RF-REG-05; INV-22; tabla de campos US08. | saveGeneralData con contexto de catálogos. |
| PDF descargable conservando el nombre original. | RF-REG-05; US10 CA-001; ADR-0010. | downloadFiscalDocument: contenido y nombre original sin ruta física. |
| Cuatro niveles CUCoP, IDs enteros y referencias al padre. | RF-REG-08; INV-32; catalogs.md; US11 campos y CA-015. | getCucopCatalog y resolución de específicas al guardar. |
| ResultadoFirma íntegro, propiedades opcionales y certificado/sello Base64. | RF-REG-01; US01; ADR-0016; OpenAPI. | identifyApplicant e integración del adaptador de firma. |

## 5. Comprobaciones transversales previstas

| Tema | Comprobación | Fuente |
| :--- | :--- | :--- |
| Token | JWT firmado; validar firma/claims/jti vigente. Solo emitir tras firma aceptada; vencer a los 60 minutos; invalidar tokens anteriores del mismo RFC ante nueva sesión; no afectar otros RFC. | ADR-0015; US01 CA-017 a CA-019. |
| Acceso | Rechazar token ausente/inválido/vencido/invalidado con 401; expediente ajeno con 403. | ADR-0004, ADR-0015; US05 CA-004. |
| Errores | HTTP y code por operación; INTERNAL_ERROR genérico sin rutas, trazas ni evidencia completa. | ADR-0004, ADR-0007. |
| Unicidad concurrente | No duplicar expediente por RFC; cada carga mantiene como máximo una cédula asociada al expediente. | INV-09, INV-27; ADR-0010. |
| Guardado | Datos y progreso se guardan conjuntamente; fallos no completan ni avanzan. Atrás no guarda. | INV-34, INV-35; US06. |
| Volumen | Ruta por fecha/RFC, UUID del archivo, nombre original en metadatos y coherencia de carga/eliminación. | ADR-0010. |
| Reinicio de pruebas | H2 pierde datos y referencias; archivos del volumen permanecen sin recuperación automática. | ADR-0005, ADR-0010. |
| Firma interoperable | node-forge RSA/SHA-256/PKCS#1 v1.5 y Java SHA256withRSA; UTF-8 y Base64; rechazar alteraciones. | ADR-0016. |
| Null | Admitir ausencia explícita en opcionales y rechazar null donde aplique obligatoriedad. | ADR-0009; US08 CA-021; US09 CA-006. |

La verificación técnica general de ADR-0010 incluye limpieza diaria con reloj fijo y coordinación con cargas y asociaciones: temporales de menos de 24 horas y exactamente 24 horas se conservan; los no asociados con más de 24 horas se eliminan; archivos asociados y archivos sin metadatos tras reiniciar se excluyen. Verificar también fallos y reintentos. No requiere una HU ni un endpoint de limpieza del usuario.

## 6. Etapa posterior y decisiones pendientes

- RF-REG-07 / US07 / INV-38 e INV-39 corresponden a finalización futura. No tienen endpoint
  en la primera etapa ni pruebas de implementación declaradas como realizadas.
- US06 CA-008 requiere consistencia antes de finalizar y se conserva como criterio futuro.
- Las decisiones abiertas del [contrato API](api/README.md) limitan las operaciones afectadas;
  esta matriz no autoriza resolverlas por suposición.
- Los nombres concretos de clases y archivos de prueba se fijarán al implementar. La
  convención es USxx_CAyyy_descriptionInEnglish, manteniendo la referencia al criterio.

## 7. Fuentes

- [Visión](vision.md), [RF](requirements/functional.md), [modelo](architecture/domain-model.md)
  y [arquitectura](architecture/architecture.md).
- [Ciclo de vida](domain/supplier-registration-lifecycle.md) y [catálogos](domain/catalogs.md).
- [Contrato OpenAPI](api/supplier-registration-openapi.yaml) e [índice de ADR](adr/README.md).
