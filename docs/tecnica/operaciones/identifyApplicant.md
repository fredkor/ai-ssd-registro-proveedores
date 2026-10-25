<!-- Generado con /documentar desde specs/ y backend/src/. No editar a mano: cambia la spec y regenera. -->


# `identifyApplicant`

**POST `/api/v1/applicant-identifications`** — Procesar Firma Electrónica del solicitante.

- Contrato: [api.md](../api.md#identifyapplicant).
- Secuencia: [diagramas.md](../diagramas.md#31-identifyapplicant).
- Trazabilidad: [traceability.md §2](../../../specs/traceability.md#2-operaciones-de-la-primera-etapa).

## Historias y requerimientos

[RF-REG-01](../../../specs/requirements/functional.md), [RF-REG-03](../../../specs/requirements/functional.md), [US01](../../../specs/user-stories/US01-identificacion-mediante-efirma.md), [US03](../../../specs/user-stories/US03-aceptar-politicas.md).

## Criterios de aceptación

| Criterio | Reglas o fuente | Verificación prevista | Tipo (spec_sync) | Pruebas | Clases de prueba |
| :--- | :--- | :--- | :--- | :--- | :--- |
| US01 CA-002 | INV-02 | Aplicación, web y firma. | backend | 1 | IdentifyApplicantServiceTest |
| US01 CA-006 | INV-06 | Integración y persistencia. | backend | 1 | IdentifyApplicantIntegrationTest |
| US01 CA-010 | INV-01, INV-02 | Dominio, aplicación y web. | backend | 3 | ApplicantIdentificationsControllerTest, ProblemDetailsTest, RfcTest |
| US01 CA-011 | INV-01 | Dominio y web. | backend | 2 | ApplicantIdentificationsControllerTest, RfcTest |
| US01 CA-013 | INV-06 | Aplicación y persistencia. | backend | 4 | ApplicantIdentificationsControllerTest, IdentificationPersistenceAdapterTest, IdentifyApplicantIntegrationTest, IdentifyApplicantServiceTest |
| US01 CA-014 | INV-04; ADR-0016 | Adaptador de firma. | backend | 1 | JdkApplicantSignatureVerificationAdapterTest |
| US01 CA-015 | INV-04; ADR-0016 | Aplicación, firma y web. | backend | 5 | ApplicantIdentificationsControllerTest, IdentifyApplicantIntegrationTest, IdentifyApplicantServiceTest, JdkApplicantSignatureVerificationAdapterTest, ProblemDetailsTest |
| US01 CA-016 | US01 CA-016 | Aplicación y web. | backend | 2 | ApplicantIdentificationsControllerTest, IdentifyApplicantServiceTest |
| US01 CA-017 | ADR-0015 | Aplicación y web. | backend | 4 | ApplicantIdentificationsControllerTest, IdentifyApplicantIntegrationTest, IdentifyApplicantServiceTest, RegistrationTokenSecurityTest |
| US01 CA-019 | ADR-0015 | Contexto de token y aplicación: dos tokens vigentes del mismo RFC coexisten. | backend | 3 | IdentificationPersistenceAdapterTest, IdentifyApplicantIntegrationTest, IdentifyApplicantServiceTest |
| US01 CA-020 | INV-04; ADR-0016 | Firma y aplicación: extracción del primer RFC, sin alternativa del apoderado. | backend | 2 | CertificateIdentityTest, IdentifyApplicantIntegrationTest |
| US01 CA-021 | INV-01, INV-04; ADR-0004, ADR-0016 | Firma, aplicación y web: VALIDATION_ERROR. | backend | 3 | ApplicantIdentificationsControllerTest, CertificateIdentityTest, IdentifyApplicantIntegrationTest |
| US01 CA-022 | INV-04; ADR-0004, ADR-0016 | Firma, aplicación y web: SIGNATURE_VERIFICATION_FAILED. | backend | 4 | ApplicantIdentificationsControllerTest, CertificateIdentityTest, IdentifyApplicantIntegrationTest, IdentifyApplicantServiceTest |
| US01 CA-023 | INV-02; ADR-0016 | Firma y aplicación: primer atributo de nombre según orden del certificado. | backend | 1 | CertificateIdentityTest |
| US01 CA-024 | INV-02, INV-06; ADR-0016 | Aplicación y persistencia: nombre de identidad y evidencia original separados. | backend | 2 | CertificateIdentityTest, IdentifyApplicantServiceTest |
| US01 CA-025 | INV-02; ADR-0004, ADR-0016 | Firma, aplicación y web: nombre ausente o vacío produce VALIDATION_ERROR. | backend | 3 | CertificateIdentityTest, IdentifyApplicantIntegrationTest, IdentifyApplicantServiceTest |
| US01 CA-027 | ADR-0015; US01 CA-027 | Persistencia y aplicación: contexto JPA por jti en la misma BD, emisión tras commit, rollback y rechazo de un JWT sin contexto. | backend | 2 | IdentificationPersistenceAdapterTest, RegistrationTokenSecurityTest |

Criterios cuya operación principal es esta, según [traceability.md §3](../../../specs/traceability.md#3-cobertura-de-criterios-de-aceptación). Conteo de [`docs/sync-report.md`](../../sync-report.md).

## Caso de uso

| Elemento | Clase |
| :--- | :--- |
| Puerto de entrada | [`IdentifyApplicantUseCase.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/in/IdentifyApplicantUseCase.java) |
| Entrada | [`IdentifyApplicantCommand.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/in/IdentifyApplicantCommand.java) |
| Resultado | [`IdentifyApplicantResult.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/in/IdentifyApplicantResult.java) |
| Decorador transaccional | [`TransactionalIdentifyApplicantUseCase.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/transaction/TransactionalIdentifyApplicantUseCase.java) |
| Servicio | [`IdentifyApplicantService.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/service/IdentifyApplicantService.java) |

## Invariantes

INV-01 a INV-08, INV-14, INV-15; ADR-0015, ADR-0016. Detalle en [dominio.md](../dominio.md#invariantes).

## Archivos por capa

| Capa | Archivos |
| :--- | :--- |
| Web | [`ApplicantIdentificationsController.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/web/ApplicantIdentificationsController.java) implementa `ApplicantIdentificationsApi` (generada desde el contrato) |
| Errores | [`GlobalExceptionHandler.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/web/GlobalExceptionHandler.java) |
| Transacción | [`TransactionalIdentifyApplicantUseCase.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/in/transaction/TransactionalIdentifyApplicantUseCase.java) |
| Aplicación | [`IdentifyApplicantService.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/service/IdentifyApplicantService.java) |
| Dominio | [`ApplicantIdentity.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/domain/model/ApplicantIdentity.java), [`ApplicantSignatureEvidence.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/domain/model/ApplicantSignatureEvidence.java), [`CertificateIdentity.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/domain/model/CertificateIdentity.java) |
| Puertos de salida | [`NoticeAcceptancePersistencePort.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/out/NoticeAcceptancePersistencePort.java), [`ApplicantSignatureVerificationPort.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/out/ApplicantSignatureVerificationPort.java), [`ApplicantSignatureEvidencePersistencePort.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/out/ApplicantSignatureEvidencePersistencePort.java), [`RegistrationContextPort.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/application/port/out/RegistrationContextPort.java) |
| Adaptadores de salida | [`NoticeAcceptancePersistenceAdapter.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/out/persistence/NoticeAcceptancePersistenceAdapter.java), [`JdkApplicantSignatureVerificationAdapter.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/out/signature/JdkApplicantSignatureVerificationAdapter.java), [`ApplicantSignatureEvidencePersistenceAdapter.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/out/persistence/ApplicantSignatureEvidencePersistenceAdapter.java), [`RegistrationContextAdapter.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/adapter/out/persistence/RegistrationContextAdapter.java) |
| Reloj | [`ClockConfiguration.java`](../../../backend/src/main/java/mx/com/fredkorlabs/registration/configuration/ClockConfiguration.java) (UTC) |

## Pruebas

| Clase de prueba | Ubicación |
| :--- | :--- |
| `ApplicantIdentificationsControllerTest` | [mx/com/fredkorlabs/registration/adapter/in/web/ApplicantIdentificationsControllerTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/adapter/in/web/ApplicantIdentificationsControllerTest.java) |
| `CertificateIdentityTest` | [mx/com/fredkorlabs/registration/domain/CertificateIdentityTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/domain/CertificateIdentityTest.java) |
| `IdentificationPersistenceAdapterTest` | [mx/com/fredkorlabs/registration/adapter/out/persistence/IdentificationPersistenceAdapterTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/adapter/out/persistence/IdentificationPersistenceAdapterTest.java) |
| `IdentifyApplicantIntegrationTest` | [mx/com/fredkorlabs/registration/IdentifyApplicantIntegrationTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/IdentifyApplicantIntegrationTest.java) |
| `IdentifyApplicantServiceTest` | [mx/com/fredkorlabs/registration/application/service/IdentifyApplicantServiceTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/application/service/IdentifyApplicantServiceTest.java) |
| `JdkApplicantSignatureVerificationAdapterTest` | [mx/com/fredkorlabs/registration/adapter/out/signature/JdkApplicantSignatureVerificationAdapterTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/adapter/out/signature/JdkApplicantSignatureVerificationAdapterTest.java) |
| `ProblemDetailsTest` | [mx/com/fredkorlabs/registration/adapter/in/web/ProblemDetailsTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/adapter/in/web/ProblemDetailsTest.java) |
| `RegistrationTokenSecurityTest` | [mx/com/fredkorlabs/registration/RegistrationTokenSecurityTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/RegistrationTokenSecurityTest.java) |
| `RfcTest` | [mx/com/fredkorlabs/registration/domain/RfcTest.java](../../../backend/src/test/java/mx/com/fredkorlabs/registration/domain/RfcTest.java) |

Incluye las clases nombradas como el caso de uso o el controlador y las que verifican sus criterios.
