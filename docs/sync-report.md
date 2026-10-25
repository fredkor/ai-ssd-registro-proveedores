# Informe de sincronización specs ↔ implementación

Generado por `python3 tools/spec_sync.py --md`. No editar a mano.

**Resultado:** OK — errores: 0, advertencias: 0

Operaciones: 22 | Criterios: 136 (91 verificables en backend) | Pruebas USxx_CAyyy: 405

## Cobertura de criterios de aceptación

| Criterio | Verificación | Pruebas | Clases |
| :--- | :--- | :---: | :--- |
| US01_CA001 | interfaz / externa / futura | 0 | — |
| US01_CA002 | backend | 1 | IdentifyApplicantServiceTest |
| US01_CA003 | interfaz / externa / futura | 0 | — |
| US01_CA004 | interfaz / externa / futura | 0 | — |
| US01_CA005 | interfaz / externa / futura | 0 | — |
| US01_CA006 | backend | 1 | IdentifyApplicantIntegrationTest |
| US01_CA007 | interfaz / externa / futura | 0 | — |
| US01_CA008 | interfaz / externa / futura | 0 | — |
| US01_CA009 | interfaz / externa / futura | 0 | — |
| US01_CA010 | backend | 3 | ApplicantIdentificationsControllerTest, ProblemDetailsTest, RfcTest |
| US01_CA011 | backend | 2 | ApplicantIdentificationsControllerTest, RfcTest |
| US01_CA012 | interfaz / externa / futura | 0 | — |
| US01_CA013 | backend | 4 | ApplicantIdentificationsControllerTest, IdentificationPersistenceAdapterTest, IdentifyApplicantIntegrationTest, IdentifyApplicantServiceTest |
| US01_CA014 | backend | 1 | JdkApplicantSignatureVerificationAdapterTest |
| US01_CA015 | backend | 5 | ApplicantIdentificationsControllerTest, IdentifyApplicantIntegrationTest, IdentifyApplicantServiceTest, JdkApplicantSignatureVerificationAdapterTest, ProblemDetailsTest |
| US01_CA016 | backend | 2 | ApplicantIdentificationsControllerTest, IdentifyApplicantServiceTest |
| US01_CA017 | backend | 4 | ApplicantIdentificationsControllerTest, IdentifyApplicantIntegrationTest, IdentifyApplicantServiceTest, RegistrationTokenSecurityTest |
| US01_CA018 | backend | 1 | RegistrationTokenSecurityTest |
| US01_CA019 | backend | 3 | IdentificationPersistenceAdapterTest, IdentifyApplicantIntegrationTest, IdentifyApplicantServiceTest |
| US01_CA020 | backend | 2 | CertificateIdentityTest, IdentifyApplicantIntegrationTest |
| US01_CA021 | backend | 3 | ApplicantIdentificationsControllerTest, CertificateIdentityTest, IdentifyApplicantIntegrationTest |
| US01_CA022 | backend | 4 | ApplicantIdentificationsControllerTest, CertificateIdentityTest, IdentifyApplicantIntegrationTest, IdentifyApplicantServiceTest |
| US01_CA023 | backend | 1 | CertificateIdentityTest |
| US01_CA024 | backend | 2 | CertificateIdentityTest, IdentifyApplicantServiceTest |
| US01_CA025 | backend | 3 | CertificateIdentityTest, IdentifyApplicantIntegrationTest, IdentifyApplicantServiceTest |
| US01_CA026 | backend | 3 | IdentificationPersistenceAdapterTest, ProblemDetailsTest, RegistrationTokenSecurityTest |
| US01_CA027 | backend | 2 | IdentificationPersistenceAdapterTest, RegistrationTokenSecurityTest |
| US02_CA001 | backend | 4 | CheckRegistrationExistenceIntegrationTest, CheckRegistrationExistenceServiceTest, ResponsePropertyInclusionTest, SupplierRegistrationPersistenceAdapterTest |
| US02_CA002 | backend | 3 | CheckRegistrationExistenceIntegrationTest, CheckRegistrationExistenceServiceTest, SupplierRegistrationPersistenceAdapterTest |
| US02_CA003 | backend | 3 | CheckRegistrationExistenceIntegrationTest, CheckRegistrationExistenceServiceTest, SupplierRegistrationPersistenceAdapterTest |
| US02_CA004 | backend | 2 | CheckRegistrationExistenceServiceTest, SupplierRegistrationsControllerTest |
| US02_CA005 | backend | 3 | CheckRegistrationExistenceIntegrationTest, CheckRegistrationExistenceServiceTest, SupplierRegistrationPersistenceAdapterTest |
| US03_CA001 | interfaz / externa / futura | 0 | — |
| US03_CA002 | interfaz / externa / futura | 0 | — |
| US03_CA003 | backend | 5 | AcceptRegistrationNoticeIntegrationTest, AcceptRegistrationNoticeServiceTest, NoticeAcceptancePersistenceAdapterTest, NoticeAcceptanceTest, RegistrationNoticesControllerTest |
| US03_CA004 | backend | 7 | AcceptRegistrationNoticeIntegrationTest, AcceptRegistrationNoticeServiceTest, IdentificationPersistenceAdapterTest, IdentifyApplicantIntegrationTest, IdentifyApplicantServiceTest, ProblemDetailsTest, RegistrationNoticesControllerTest |
| US03_CA005 | backend | 4 | StartSupplierRegistrationIntegrationTest, StartSupplierRegistrationServiceTest, SupplierRegistrationPersistenceAdapterTest, SupplierRegistrationTest |
| US03_CA006 | backend | 3 | GetRegistrationNoticeServiceTest, RegistrationNoticesControllerTest, RegistrationTokenSecurityTest |
| US03_CA007 | backend | 8 | AcceptRegistrationNoticeServiceTest, IdentificationPersistenceAdapterTest, IdentifyApplicantIntegrationTest, IdentifyApplicantServiceTest, NoticeAcceptancePersistenceAdapterTest, NoticeAcceptanceTest, ResumeSupplierRegistrationIntegrationTest, ResumeSupplierRegistrationServiceTest |
| US03_CA008 | backend | 1 | ResumeSupplierRegistrationRollbackTest |
| US04_CA001 | backend | 4 | StartSupplierRegistrationIntegrationTest, StartSupplierRegistrationServiceTest, SupplierRegistrationPersistenceAdapterTest, SupplierRegistrationTest |
| US04_CA002 | backend | 4 | StartSupplierRegistrationIntegrationTest, StartSupplierRegistrationServiceTest, SupplierRegistrationPersistenceAdapterTest, SupplierRegistrationTest |
| US04_CA003 | backend | 3 | StartSupplierRegistrationIntegrationTest, StartSupplierRegistrationServiceTest, SupplierRegistrationPersistenceAdapterTest |
| US04_CA004 | backend | 2 | StartSupplierRegistrationServiceTest, SupplierRegistrationsControllerTest |
| US04_CA005 | backend | 3 | StartSupplierRegistrationIntegrationTest, StartSupplierRegistrationServiceTest, SupplierRegistrationTest |
| US04_CA006 | backend | 4 | IdentificationPersistenceAdapterTest, StartSupplierRegistrationIntegrationTest, StartSupplierRegistrationServiceTest, SupplierRegistrationTest |
| US05_CA001 | backend | 3 | ResumeSupplierRegistrationIntegrationTest, ResumeSupplierRegistrationServiceTest, SupplierRegistrationPersistenceAdapterTest |
| US05_CA002 | backend | 1 | SaveCucopParticipationIntegrationTest |
| US05_CA003 | backend | 4 | ResumeSupplierRegistrationIntegrationTest, ResumeSupplierRegistrationServiceTest, SupplierRegistrationPersistenceAdapterTest, SupplierRegistrationTest |
| US05_CA004 | backend | 2 | ResumeSupplierRegistrationIntegrationTest, ResumeSupplierRegistrationServiceTest |
| US05_CA005 | backend | 1 | ResumeSupplierRegistrationServiceTest |
| US05_CA006 | backend | 3 | ResumeSupplierRegistrationIntegrationTest, ResumeSupplierRegistrationServiceTest, SupplierRegistrationTest |
| US05_CA007 | interfaz / externa / futura | 0 | — |
| US05_CA008 | backend | 2 | ResumeSupplierRegistrationIntegrationTest, ResumeSupplierRegistrationServiceTest |
| US05_CA009 | backend | 3 | ResumeSupplierRegistrationIntegrationTest, ResumeSupplierRegistrationServiceTest, SupplierRegistrationTest |
| US05_CA010 | backend | 4 | ResumeSupplierRegistrationIntegrationTest, ResumeSupplierRegistrationServiceTest, SupplierRegistrationPersistenceAdapterTest, SupplierRegistrationTest |
| US05_CA011 | backend | 2 | ResumeSupplierRegistrationRollbackTest, ResumeSupplierRegistrationServiceTest |
| US06_CA001 | interfaz / externa / futura | 0 | — |
| US06_CA002 | interfaz / externa / futura | 0 | — |
| US06_CA003 | backend | 1 | SupplierRegistrationTest |
| US06_CA004 | backend | 2 | SaveGeneralDataRollbackTest, SaveGeneralDataServiceTest |
| US06_CA005 | backend | 2 | SaveGeneralDataIntegrationTest, SupplierRegistrationPersistenceAdapterTest |
| US06_CA006 | backend | 3 | SaveCucopParticipationIntegrationTest, SaveGeneralDataIntegrationTest, SupplierRegistrationTest |
| US06_CA007 | interfaz / externa / futura | 0 | — |
| US06_CA008 | interfaz / externa / futura | 0 | — |
| US06_CA009 | interfaz / externa / futura | 0 | — |
| US06_CA010 | backend | 1 | SaveGeneralDataIntegrationTest |
| US07_CA001 | interfaz / externa / futura | 0 | — |
| US07_CA002 | interfaz / externa / futura | 0 | — |
| US07_CA003 | interfaz / externa / futura | 0 | — |
| US07_CA004 | interfaz / externa / futura | 0 | — |
| US07_CA005 | interfaz / externa / futura | 0 | — |
| US07_CA006 | interfaz / externa / futura | 0 | — |
| US07_CA007 | interfaz / externa / futura | 0 | — |
| US08_CA001 | interfaz / externa / futura | 0 | — |
| US08_CA002 | interfaz / externa / futura | 0 | — |
| US08_CA003 | backend | 6 | GeneralDataCatalogAdapterTest, GeneralDataTest, GetLegalOwnershipsIntegrationTest, GetLegalOwnershipsServiceTest, LegalOwnershipOptionTest, SaveGeneralDataIntegrationTest |
| US08_CA004 | backend | 2 | GeneralDataTest, SaveGeneralDataIntegrationTest |
| US08_CA005 | interfaz / externa / futura | 0 | — |
| US08_CA006 | backend | 4 | GeneralDataTest, SaveGeneralDataIntegrationTest, SaveGeneralDataServiceTest, SupplierRegistrationTest |
| US08_CA007 | backend | 3 | GeneralDataTest, SaveGeneralDataIntegrationTest, SaveGeneralDataServiceTest |
| US08_CA008 | backend | 3 | GeneralDataCatalogAdapterTest, GeneralDataTest, SaveGeneralDataIntegrationTest |
| US08_CA009 | interfaz / externa / futura | 4 | GeneralDataCatalogAdapterTest, GeneralDataTest, GetCountriesIntegrationTest, SaveGeneralDataIntegrationTest |
| US08_CA010 | interfaz / externa / futura | 5 | GeneralDataCatalogAdapterTest, GeneralDataTest, GetCountriesIntegrationTest, GetCountriesServiceTest, SaveGeneralDataIntegrationTest |
| US08_CA011 | backend | 4 | ApplicantIdentityTest, GeneralDataCatalogAdapterTest, GetTaxRegimesIntegrationTest, GetTaxRegimesServiceTest |
| US08_CA012 | backend | 2 | ApplicantIdentityTest, GetTaxRegimesIntegrationTest |
| US08_CA013 | backend | 3 | GeneralDataTest, SaveGeneralDataIntegrationTest, WebsiteTest |
| US08_CA014 | backend | 6 | GeneralDataCatalogAdapterTest, GeneralDataTest, GetLegalOwnershipsIntegrationTest, GetLegalOwnershipsServiceTest, LegalOwnershipOptionTest, ResponsePropertyInclusionTest |
| US08_CA015 | interfaz / externa / futura | 2 | GeneralDataTest, SaveGeneralDataIntegrationTest |
| US08_CA016 | interfaz / externa / futura | 0 | — |
| US08_CA017 | interfaz / externa / futura | 0 | — |
| US08_CA018 | backend | 3 | ForeignFiscalCodeTest, GeneralDataTest, SaveGeneralDataIntegrationTest |
| US08_CA019 | interfaz / externa / futura | 1 | ApplicantIdentityTest |
| US08_CA020 | interfaz / externa / futura | 0 | — |
| US08_CA021 | backend | 2 | GeneralDataTest, SaveGeneralDataIntegrationTest |
| US08_CA022 | backend | 14 | GeneralDataCatalogAdapterTest, GetAnnualSalesRangesIntegrationTest, GetAnnualSalesRangesServiceTest, GetBusinessActivitiesIntegrationTest, GetBusinessActivitiesServiceTest, GetPrimaryActivitiesIntegrationTest, GetPrimaryActivitiesServiceTest, GetSectorsIntegrationTest, GetSectorsServiceTest, GetStratificationsIntegrationTest, GetStratificationsServiceTest, GetTaxRegimesIntegrationTest, GetWorkerCountRangesIntegrationTest, GetWorkerCountRangesServiceTest |
| US08_CA023 | backend | 3 | GeneralDataCatalogAdapterTest, GetSubsectorsIntegrationTest, GetSubsectorsServiceTest |
| US08_CA024 | backend | 1 | SaveGeneralDataIntegrationTest |
| US08_CA025 | backend | 2 | EditableTextFieldsTest, SaveGeneralDataIntegrationTest |
| US08_CA026 | backend | 3 | EditableTextFieldsTest, GeneralDataTest, SaveGeneralDataIntegrationTest |
| US08_CA027 | backend | 1 | EditableTextFieldsTest |
| US09_CA001 | interfaz / externa / futura | 2 | SaveGeneralDataIntegrationTest, SupplierRegistrationPersistenceAdapterTest |
| US09_CA002 | interfaz / externa / futura | 0 | — |
| US09_CA003 | backend | 3 | GeneralDataTest, PhoneNumberTest, SaveGeneralDataIntegrationTest |
| US09_CA004 | backend | 2 | GeneralDataTest, SaveGeneralDataIntegrationTest |
| US09_CA005 | backend | 1 | PhoneNumberTest |
| US09_CA006 | backend | 2 | GeneralDataTest, PhoneNumberTest |
| US09_CA007 | backend | 1 | SupplierRegistrationPersistenceAdapterTest |
| US09_CA008 | backend | 1 | SaveGeneralDataIntegrationTest |
| US09_CA009 | backend | 1 | SaveGeneralDataIntegrationTest |
| US10_CA001 | backend | 5 | FiscalDocumentFormatTest, SupplierRegistrationTest, UploadFiscalDocumentIntegrationTest, UploadFiscalDocumentServiceTest, VolumeFileStorageAdapterTest |
| US10_CA002 | backend | 2 | FiscalDocumentFormatTest, UploadFiscalDocumentIntegrationTest |
| US10_CA003 | backend | 3 | SaveGeneralDataIntegrationTest, SaveGeneralDataServiceTest, SupplierRegistrationTest |
| US10_CA004 | backend | 4 | SupplierRegistrationPersistenceAdapterTest, SupplierRegistrationTest, UploadFiscalDocumentIntegrationTest, UploadFiscalDocumentServiceTest |
| US10_CA005 | backend | 6 | DownloadFiscalDocumentIntegrationTest, DownloadFiscalDocumentServiceTest, FiscalDocumentContentDispositionTest, SupplierRegistrationPersistenceAdapterTest, UploadFiscalDocumentIntegrationTest, VolumeFileStorageAdapterTest |
| US10_CA006 | backend | 4 | DeleteFiscalDocumentIntegrationTest, DeleteFiscalDocumentRollbackTest, DeleteFiscalDocumentServiceTest, SupplierRegistrationPersistenceAdapterTest |
| US10_CA007 | backend | 3 | DeleteFiscalDocumentIntegrationTest, DeleteFiscalDocumentServiceTest, SupplierRegistrationTest |
| US10_CA008 | backend | 4 | FiscalDocumentFormatTest, UploadFiscalDocumentIntegrationTest, UploadFiscalDocumentServerTest, UploadFiscalDocumentServiceTest |
| US10_CA009 | backend | 3 | FiscalDocumentFormatTest, UploadFiscalDocumentIntegrationTest, UploadFiscalDocumentServiceTest |
| US10_CA010 | backend | 1 | SaveGeneralDataIntegrationTest |
| US10_CA011 | backend | 3 | DeleteFiscalDocumentIntegrationTest, DownloadFiscalDocumentIntegrationTest, DownloadFiscalDocumentServiceTest |
| US11_CA001 | backend | 3 | SaveCucopParticipationIntegrationTest, SaveCucopParticipationServiceTest, SupplierRegistrationTest |
| US11_CA002 | interfaz / externa / futura | 0 | — |
| US11_CA003 | interfaz / externa / futura | 0 | — |
| US11_CA004 | backend | 2 | CucopParticipationTest, SaveCucopParticipationIntegrationTest |
| US11_CA005 | interfaz / externa / futura | 0 | — |
| US11_CA006 | interfaz / externa / futura | 0 | — |
| US11_CA007 | interfaz / externa / futura | 0 | — |
| US11_CA008 | interfaz / externa / futura | 0 | — |
| US11_CA009 | interfaz / externa / futura | 0 | — |
| US11_CA010 | interfaz / externa / futura | 1 | SaveCucopParticipationIntegrationTest |
| US11_CA011 | backend | 5 | CucopParticipationTest, SaveCucopParticipationIntegrationTest, SaveCucopParticipationServiceTest, SupplierRegistrationPersistenceAdapterTest, SupplierRegistrationTest |
| US11_CA012 | backend | 5 | CucopCatalogAdapterTest, CucopParticipationTest, SaveCucopParticipationIntegrationTest, SaveCucopParticipationRollbackTest, SaveCucopParticipationServiceTest |
| US11_CA013 | interfaz / externa / futura | 0 | — |
| US11_CA014 | interfaz / externa / futura | 0 | — |
| US11_CA015 | backend | 3 | CucopCatalogAdapterTest, GetCucopCatalogIntegrationTest, GetCucopCatalogServiceTest |
