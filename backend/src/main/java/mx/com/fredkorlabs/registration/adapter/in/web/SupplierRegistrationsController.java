package mx.com.fredkorlabs.registration.adapter.in.web;

import mx.com.fredkorlabs.registration.adapter.in.web.generated.SupplierRegistrationsApi;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.ApplicantIdentityResponse;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.CucopNodeResponse;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.CucopParticipationRequest;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.CucopParticipationResponse;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.FiscalDocumentResponse;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.GeneralDataRequest;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.GeneralDataResponse;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.NoticeAcceptanceResponse;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.Origin;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.ParticipationType;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.PhoneRequest;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.RegistrationExistenceResponse;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.RegistrationSection;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.RegistrationStatus;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.SectionProgressResponse;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.SectionSaveResponse;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.SectionStatus;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.SupplierRegistrationResponse;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.TaxRegime;
import mx.com.fredkorlabs.registration.application.port.in.CheckRegistrationExistenceQuery;
import mx.com.fredkorlabs.registration.application.port.in.CheckRegistrationExistenceResult;
import mx.com.fredkorlabs.registration.application.port.in.CheckRegistrationExistenceUseCase;
import mx.com.fredkorlabs.registration.application.port.in.ResumeSupplierRegistrationCommand;
import mx.com.fredkorlabs.registration.application.port.in.ResumeSupplierRegistrationUseCase;
import mx.com.fredkorlabs.registration.application.port.in.SaveCucopParticipationCommand;
import mx.com.fredkorlabs.registration.application.port.in.SaveCucopParticipationUseCase;
import mx.com.fredkorlabs.registration.application.port.in.SaveGeneralDataCommand;
import mx.com.fredkorlabs.registration.application.port.in.SaveGeneralDataUseCase;
import mx.com.fredkorlabs.registration.application.port.in.SectionSaveResult;
import mx.com.fredkorlabs.registration.application.port.in.StartSupplierRegistrationCommand;
import mx.com.fredkorlabs.registration.application.port.in.StartSupplierRegistrationUseCase;
import mx.com.fredkorlabs.registration.application.port.in.SupplierRegistrationResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
public class SupplierRegistrationsController implements SupplierRegistrationsApi {

    private static final String REGISTRATIONS_PATH = "/api/v1/supplier-registrations/";

    private final CheckRegistrationExistenceUseCase checkRegistrationExistence;
    private final StartSupplierRegistrationUseCase startSupplierRegistration;
    private final ResumeSupplierRegistrationUseCase resumeSupplierRegistration;
    private final SaveGeneralDataUseCase saveGeneralData;
    private final SaveCucopParticipationUseCase saveCucopParticipation;

    public SupplierRegistrationsController(CheckRegistrationExistenceUseCase checkRegistrationExistence,
                                           StartSupplierRegistrationUseCase startSupplierRegistration,
                                           ResumeSupplierRegistrationUseCase resumeSupplierRegistration,
                                           SaveGeneralDataUseCase saveGeneralData,
                                           SaveCucopParticipationUseCase saveCucopParticipation) {
        this.checkRegistrationExistence = checkRegistrationExistence;
        this.startSupplierRegistration = startSupplierRegistration;
        this.resumeSupplierRegistration = resumeSupplierRegistration;
        this.saveGeneralData = saveGeneralData;
        this.saveCucopParticipation = saveCucopParticipation;
    }

    @Override
    public ResponseEntity<RegistrationExistenceResponse> checkRegistrationExistence() {
        CheckRegistrationExistenceResult result = checkRegistrationExistence.execute(
            new CheckRegistrationExistenceQuery(RegistrationTokenPrincipal.currentRfc()));
        RegistrationExistenceResponse response = new RegistrationExistenceResponse(result.exists());
        if (result.exists()) {
            response.registrationId(result.registrationId()).status(RegistrationStatus.fromValue(result.status()));
        }
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<SupplierRegistrationResponse> startSupplierRegistration() {
        SupplierRegistrationResult result = startSupplierRegistration.execute(
            new StartSupplierRegistrationCommand(RegistrationTokenPrincipal.currentTokenId()));
        return ResponseEntity.created(URI.create(REGISTRATIONS_PATH + result.id())).body(toResponse(result));
    }

    @Override
    public ResponseEntity<SupplierRegistrationResponse> resumeSupplierRegistration(UUID registrationId) {
        SupplierRegistrationResult result = resumeSupplierRegistration.execute(
            new ResumeSupplierRegistrationCommand(registrationId, RegistrationTokenPrincipal.currentTokenId()));
        return ResponseEntity.ok(toResponse(result));
    }

    @Override
    public ResponseEntity<SectionSaveResponse> saveGeneralData(UUID registrationId, GeneralDataRequest request) {
        return toResponse(saveGeneralData.execute(new SaveGeneralDataCommand(registrationId,
            RegistrationTokenPrincipal.currentTokenId(), request.getOrigin().getValue(), request.getCountryId(),
            request.getNationality(), request.getForeignFiscalCode(), request.getLegalOwnershipId(),
            request.getSectorId(), request.getSubsectorId(), request.getBusinessActivityId(),
            request.getStratificationId(), request.getPrimaryActivityId(), request.getWorkerCountRangeId(),
            request.getAnnualSalesRangeId(), request.getWebsite(),
            request.getPhones() == null ? null : request.getPhones().stream()
                .map(phone -> new SaveGeneralDataCommand.Phone(phone.getNumber(), phone.getExtension()))
                .toList())));
    }

    // Repeated items were already rejected on the raw JSON, so the generated Sets keep the request order.
    @Override
    public ResponseEntity<SectionSaveResponse> saveCucopParticipation(UUID registrationId,
                                                                     CucopParticipationRequest request) {
        return toResponse(saveCucopParticipation.execute(new SaveCucopParticipationCommand(registrationId,
            RegistrationTokenPrincipal.currentTokenId(),
            request.getParticipationTypes().stream().map(type -> type == null ? null : type.getValue()).toList(),
            new ArrayList<>(request.getSpecificItemIds()))));
    }

    private static ResponseEntity<SectionSaveResponse> toResponse(SectionSaveResult result) {
        return ResponseEntity.ok(new SectionSaveResponse(result.registrationId(),
            RegistrationSection.fromValue(result.section()), SectionSaveResponse.StatusEnum.fromValue(result.status()),
            RegistrationSection.fromValue(result.nextSection())));
    }

    private static SupplierRegistrationResponse toResponse(SupplierRegistrationResult result) {
        SupplierRegistrationResponse response = new SupplierRegistrationResponse(
            result.id(),
            RegistrationStatus.fromValue(result.status()),
            new ApplicantIdentityResponse(result.rfc(), result.legalName(), TaxRegime.fromValue(result.taxRegime()),
                result.taxRegimeId(), result.username()),
            new NoticeAcceptanceResponse(NoticeAcceptanceResponse.VersionEnum.fromValue(result.noticeVersion()),
                result.noticeAcceptedAt().atOffset(ZoneOffset.UTC), result.noticeAcceptanceId()),
            result.progress().stream()
                .map(item -> new SectionProgressResponse(RegistrationSection.fromValue(item.section()),
                    SectionStatus.fromValue(item.status())))
                .toList());
        if (result.generalData() != null) {
            response.generalData(toResponse(result.generalData()));
        }
        if (result.cucopParticipation() != null) {
            response.cucopParticipation(toResponse(result.cucopParticipation()));
        }
        SupplierRegistrationResult.FiscalDocumentItem document = result.fiscalDocument();
        if (document != null) {
            response.fiscalDocument(new FiscalDocumentResponse(document.id(), document.originalName(),
                FiscalDocumentResponse.ExtensionEnum.fromValue(document.extension()), document.sizeBytes(),
                document.uploadedAt().atOffset(ZoneOffset.UTC)));
        }
        return response;
    }

    private static CucopParticipationResponse toResponse(
            SupplierRegistrationResult.CucopParticipationItem participation) {
        return new CucopParticipationResponse(
            participation.participationTypes().stream().map(ParticipationType::fromValue)
                .collect(Collectors.toCollection(LinkedHashSet::new)),
            participation.specificItems().stream()
                .map(item -> new CucopNodeResponse(item.id(), item.key(), item.description(),
                    CucopNodeResponse.LevelEnum.fromValue(item.level())).parentId(item.parentId()))
                .toList());
    }

    private static GeneralDataResponse toResponse(SupplierRegistrationResult.GeneralDataItem data) {
        GeneralDataResponse response = new GeneralDataResponse(Origin.fromValue(data.origin()), data.countryId(),
            data.nationality(), data.legalOwnershipId(), data.businessActivityId(), data.stratificationId(),
            data.primaryActivityId(), data.workerCountRangeId(), data.annualSalesRangeId());
        response.foreignFiscalCode(data.foreignFiscalCode()).sectorId(data.sectorId()).subsectorId(data.subsectorId())
            .website(data.website())
            .phones(data.phones() == null ? null : data.phones().stream()
                .map(phone -> new PhoneRequest(phone.number(), phone.extension()))
                .toList());
        return response;
    }
}
