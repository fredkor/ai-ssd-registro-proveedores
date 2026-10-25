# Resumen de pruebas (surefire)

Generado el 2026-10-25 02:02 -0600 a partir de `backend/target/surefire-reports` tras `./mvnw clean test` (commit `8dd6108`, openjdk version "25.0.3" 2026-04-21 LTS).

**Total:** 83 clases, 646 pruebas, 0 fallos, 0 errores, 0 omitidas; 71.3 s.

## Por capa

| Capa | Clases | Pruebas | Fallos + errores |
| :--- | :--- | :--- | :--- |
| Dominio | 12 | 115 | 0 |
| Aplicación | 23 | 117 | 0 |
| Adaptadores de entrada | 8 | 104 | 0 |
| Adaptadores de salida | 8 | 112 | 0 |
| Integración (aplicación completa) | 31 | 196 | 0 |
| Configuración | 1 | 2 | 0 |

## Por clase

| Clase | Capa | Pruebas | Fallos | Errores | Omitidas | Tiempo (s) |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `domain.ApplicantIdentityTest` | Dominio | 4 | 0 | 0 | 0 | 0.01 |
| `domain.CertificateIdentityTest` | Dominio | 14 | 0 | 0 | 0 | 0.02 |
| `domain.CucopParticipationTest` | Dominio | 9 | 0 | 0 | 0 | 0.02 |
| `domain.FiscalDocumentFormatTest` | Dominio | 6 | 0 | 0 | 0 | 0.04 |
| `domain.ForeignFiscalCodeTest` | Dominio | 3 | 0 | 0 | 0 | 0.01 |
| `domain.GeneralDataTest` | Dominio | 22 | 0 | 0 | 0 | 0.06 |
| `domain.LegalOwnershipOptionTest` | Dominio | 6 | 0 | 0 | 0 | 0.01 |
| `domain.NoticeAcceptanceTest` | Dominio | 4 | 0 | 0 | 0 | 0.01 |
| `domain.PhoneNumberTest` | Dominio | 7 | 0 | 0 | 0 | 0.03 |
| `domain.RfcTest` | Dominio | 5 | 0 | 0 | 0 | 0.01 |
| `domain.SupplierRegistrationTest` | Dominio | 27 | 0 | 0 | 0 | 0.06 |
| `domain.WebsiteTest` | Dominio | 8 | 0 | 0 | 0 | 0.04 |
| `application.service.AcceptRegistrationNoticeServiceTest` | Aplicación | 7 | 0 | 0 | 0 | 0.02 |
| `application.service.CheckRegistrationExistenceServiceTest` | Aplicación | 5 | 0 | 0 | 0 | 0.02 |
| `application.service.CleanUpUnassociatedFilesServiceTest` | Aplicación | 6 | 0 | 0 | 0 | 0.01 |
| `application.service.DeleteFiscalDocumentServiceTest` | Aplicación | 6 | 0 | 0 | 0 | 0.02 |
| `application.service.DownloadFiscalDocumentServiceTest` | Aplicación | 5 | 0 | 0 | 0 | 0.01 |
| `application.service.GetAnnualSalesRangesServiceTest` | Aplicación | 2 | 0 | 0 | 0 | 0.01 |
| `application.service.GetBusinessActivitiesServiceTest` | Aplicación | 2 | 0 | 0 | 0 | 0.00 |
| `application.service.GetCountriesServiceTest` | Aplicación | 2 | 0 | 0 | 0 | 0.01 |
| `application.service.GetCucopCatalogServiceTest` | Aplicación | 3 | 0 | 0 | 0 | 0.01 |
| `application.service.GetLegalOwnershipsServiceTest` | Aplicación | 5 | 0 | 0 | 0 | 0.01 |
| `application.service.GetPrimaryActivitiesServiceTest` | Aplicación | 2 | 0 | 0 | 0 | 0.00 |
| `application.service.GetRegistrationNoticeServiceTest` | Aplicación | 2 | 0 | 0 | 0 | 0.00 |
| `application.service.GetSectorsServiceTest` | Aplicación | 2 | 0 | 0 | 0 | 0.02 |
| `application.service.GetStratificationsServiceTest` | Aplicación | 2 | 0 | 0 | 0 | 0.01 |
| `application.service.GetSubsectorsServiceTest` | Aplicación | 2 | 0 | 0 | 0 | 0.01 |
| `application.service.GetTaxRegimesServiceTest` | Aplicación | 2 | 0 | 0 | 0 | 0.01 |
| `application.service.GetWorkerCountRangesServiceTest` | Aplicación | 2 | 0 | 0 | 0 | 0.01 |
| `application.service.IdentifyApplicantServiceTest` | Aplicación | 13 | 0 | 0 | 0 | 0.04 |
| `application.service.ResumeSupplierRegistrationServiceTest` | Aplicación | 14 | 0 | 0 | 0 | 0.04 |
| `application.service.SaveCucopParticipationServiceTest` | Aplicación | 7 | 0 | 0 | 0 | 0.02 |
| `application.service.SaveGeneralDataServiceTest` | Aplicación | 7 | 0 | 0 | 0 | 0.02 |
| `application.service.StartSupplierRegistrationServiceTest` | Aplicación | 11 | 0 | 0 | 0 | 0.05 |
| `application.service.UploadFiscalDocumentServiceTest` | Aplicación | 8 | 0 | 0 | 0 | 0.01 |
| `adapter.in.scheduling.DailyFileCleanupSchedulerTest` | Adaptadores de entrada | 3 | 0 | 0 | 0 | 0.01 |
| `adapter.in.web.ApplicantIdentificationsControllerTest` | Adaptadores de entrada | 58 | 0 | 0 | 0 | 0.71 |
| `adapter.in.web.EditableTextFieldsTest` | Adaptadores de entrada | 4 | 0 | 0 | 0 | 0.01 |
| `adapter.in.web.FiscalDocumentContentDispositionTest` | Adaptadores de entrada | 3 | 0 | 0 | 0 | 0.01 |
| `adapter.in.web.ProblemDetailsTest` | Adaptadores de entrada | 16 | 0 | 0 | 0 | 1.91 |
| `adapter.in.web.RegistrationNoticesControllerTest` | Adaptadores de entrada | 16 | 0 | 0 | 0 | 2.17 |
| `adapter.in.web.RegistrationSessionUnavailableTest` | Adaptadores de entrada | 1 | 0 | 0 | 0 | 1.75 |
| `adapter.in.web.SupplierRegistrationsControllerTest` | Adaptadores de entrada | 3 | 0 | 0 | 0 | 1.79 |
| `adapter.out.catalog.CucopCatalogAdapterTest` | Adaptadores de salida | 8 | 0 | 0 | 0 | 1.06 |
| `adapter.out.catalog.GeneralDataCatalogAdapterTest` | Adaptadores de salida | 34 | 0 | 0 | 0 | 1.27 |
| `adapter.out.catalog.SupplierCatalogsMigrationTest` | Adaptadores de salida | 3 | 0 | 0 | 0 | 1.68 |
| `adapter.out.document.VolumeFileStorageAdapterTest` | Adaptadores de salida | 10 | 0 | 0 | 0 | 0.05 |
| `adapter.out.persistence.IdentificationPersistenceAdapterTest` | Adaptadores de salida | 9 | 0 | 0 | 0 | 2.11 |
| `adapter.out.persistence.NoticeAcceptancePersistenceAdapterTest` | Adaptadores de salida | 4 | 0 | 0 | 0 | 0.74 |
| `adapter.out.persistence.SupplierRegistrationPersistenceAdapterTest` | Adaptadores de salida | 36 | 0 | 0 | 0 | 1.72 |
| `adapter.out.signature.JdkApplicantSignatureVerificationAdapterTest` | Adaptadores de salida | 8 | 0 | 0 | 0 | 0.01 |
| `AcceptRegistrationNoticeIntegrationTest` | Integración (aplicación completa) | 2 | 0 | 0 | 0 | 0.06 |
| `ApiDocumentationTest` | Integración (aplicación completa) | 3 | 0 | 0 | 0 | 1.12 |
| `CheckRegistrationExistenceIntegrationTest` | Integración (aplicación completa) | 6 | 0 | 0 | 0 | 0.23 |
| `DailyFileCleanupIntegrationTest` | Integración (aplicación completa) | 3 | 0 | 0 | 0 | 4.03 |
| `DeleteFiscalDocumentIntegrationTest` | Integración (aplicación completa) | 10 | 0 | 0 | 0 | 0.96 |
| `DeleteFiscalDocumentRollbackTest` | Integración (aplicación completa) | 1 | 0 | 0 | 0 | 1.71 |
| `DownloadFiscalDocumentIntegrationTest` | Integración (aplicación completa) | 10 | 0 | 0 | 0 | 0.68 |
| `GetAnnualSalesRangesIntegrationTest` | Integración (aplicación completa) | 2 | 0 | 0 | 0 | 0.05 |
| `GetBusinessActivitiesIntegrationTest` | Integración (aplicación completa) | 2 | 0 | 0 | 0 | 0.05 |
| `GetCountriesIntegrationTest` | Integración (aplicación completa) | 3 | 0 | 0 | 0 | 0.30 |
| `GetCucopCatalogIntegrationTest` | Integración (aplicación completa) | 4 | 0 | 0 | 0 | 0.20 |
| `GetLegalOwnershipsIntegrationTest` | Integración (aplicación completa) | 13 | 0 | 0 | 0 | 0.91 |
| `GetPrimaryActivitiesIntegrationTest` | Integración (aplicación completa) | 2 | 0 | 0 | 0 | 0.08 |
| `GetSectorsIntegrationTest` | Integración (aplicación completa) | 2 | 0 | 0 | 0 | 0.04 |
| `GetStratificationsIntegrationTest` | Integración (aplicación completa) | 2 | 0 | 0 | 0 | 0.04 |
| `GetSubsectorsIntegrationTest` | Integración (aplicación completa) | 5 | 0 | 0 | 0 | 0.16 |
| `GetTaxRegimesIntegrationTest` | Integración (aplicación completa) | 4 | 0 | 0 | 0 | 0.13 |
| `GetWorkerCountRangesIntegrationTest` | Integración (aplicación completa) | 2 | 0 | 0 | 0 | 0.04 |
| `IdentifyApplicantIntegrationTest` | Integración (aplicación completa) | 13 | 0 | 0 | 0 | 0.33 |
| `RegistrationTokenSecurityTest` | Integración (aplicación completa) | 9 | 0 | 0 | 0 | 0.47 |
| `ResumeSupplierRegistrationIntegrationTest` | Integración (aplicación completa) | 13 | 0 | 0 | 0 | 5.45 |
| `ResumeSupplierRegistrationRollbackTest` | Integración (aplicación completa) | 2 | 0 | 0 | 0 | 1.47 |
| `SaveCucopParticipationIntegrationTest` | Integración (aplicación completa) | 20 | 0 | 0 | 0 | 3.55 |
| `SaveCucopParticipationRollbackTest` | Integración (aplicación completa) | 1 | 0 | 0 | 0 | 0.22 |
| `SaveGeneralDataIntegrationTest` | Integración (aplicación completa) | 29 | 0 | 0 | 0 | 2.19 |
| `SaveGeneralDataRollbackTest` | Integración (aplicación completa) | 1 | 0 | 0 | 0 | 20.06 |
| `StartSupplierRegistrationIntegrationTest` | Integración (aplicación completa) | 12 | 0 | 0 | 0 | 0.62 |
| `SupplierRegistrationApplicationTests` | Integración (aplicación completa) | 1 | 0 | 0 | 0 | 3.78 |
| `UploadFiscalDocumentIntegrationTest` | Integración (aplicación completa) | 13 | 0 | 0 | 0 | 0.68 |
| `UploadFiscalDocumentRollbackTest` | Integración (aplicación completa) | 1 | 0 | 0 | 0 | 0.17 |
| `UploadFiscalDocumentServerTest` | Integración (aplicación completa) | 5 | 0 | 0 | 0 | 3.87 |
| `configuration.ResponsePropertyInclusionTest` | Configuración | 2 | 0 | 0 | 0 | 0.03 |
