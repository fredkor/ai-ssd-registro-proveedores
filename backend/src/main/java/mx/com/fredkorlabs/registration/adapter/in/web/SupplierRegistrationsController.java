package mx.com.fredkorlabs.registration.adapter.in.web;

import mx.com.fredkorlabs.registration.adapter.in.web.generated.SupplierRegistrationsApi;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.ApplicantIdentityResponse;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.CucopParticipationRequest;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.GeneralDataRequest;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.NoticeAcceptanceResponse;
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
import mx.com.fredkorlabs.registration.application.port.in.StartSupplierRegistrationCommand;
import mx.com.fredkorlabs.registration.application.port.in.StartSupplierRegistrationUseCase;
import mx.com.fredkorlabs.registration.application.port.in.SupplierRegistrationResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.ZoneOffset;
import java.util.UUID;

@RestController
public class SupplierRegistrationsController implements SupplierRegistrationsApi {

    private static final String REGISTRATIONS_PATH = "/api/v1/supplier-registrations/";

    private final CheckRegistrationExistenceUseCase checkRegistrationExistence;
    private final StartSupplierRegistrationUseCase startSupplierRegistration;

    public SupplierRegistrationsController(CheckRegistrationExistenceUseCase checkRegistrationExistence,
                                           StartSupplierRegistrationUseCase startSupplierRegistration) {
        this.checkRegistrationExistence = checkRegistrationExistence;
        this.startSupplierRegistration = startSupplierRegistration;
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
        throw new UnsupportedOperationException("Not implemented in this iteration");
    }

    @Override
    public ResponseEntity<SectionSaveResponse> saveGeneralData(UUID registrationId, GeneralDataRequest request) {
        throw new UnsupportedOperationException("Not implemented in this iteration");
    }

    @Override
    public ResponseEntity<SectionSaveResponse> saveCucopParticipation(UUID registrationId,
                                                                     CucopParticipationRequest request) {
        throw new UnsupportedOperationException("Not implemented in this iteration");
    }

    private static SupplierRegistrationResponse toResponse(SupplierRegistrationResult result) {
        return new SupplierRegistrationResponse(
            result.id(),
            RegistrationStatus.fromValue(result.status()),
            new ApplicantIdentityResponse(result.rfc(), result.legalName(), TaxRegime.fromValue(result.taxRegime()),
                result.username()),
            new NoticeAcceptanceResponse(NoticeAcceptanceResponse.VersionEnum.fromValue(result.noticeVersion()),
                result.noticeAcceptedAt().atOffset(ZoneOffset.UTC), result.noticeAcceptanceId()),
            result.progress().stream()
                .map(item -> new SectionProgressResponse(RegistrationSection.fromValue(item.section()),
                    SectionStatus.fromValue(item.status())))
                .toList());
    }
}
