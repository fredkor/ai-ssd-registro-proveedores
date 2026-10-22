package mx.com.fredkorlabs.registration.adapter.in.web;

import mx.com.fredkorlabs.registration.adapter.in.web.generated.SupplierRegistrationsApi;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.CucopParticipationRequest;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.GeneralDataRequest;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.RegistrationExistenceResponse;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.RegistrationStatus;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.SectionSaveResponse;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.SupplierRegistrationResponse;
import mx.com.fredkorlabs.registration.application.port.in.CheckRegistrationExistenceQuery;
import mx.com.fredkorlabs.registration.application.port.in.CheckRegistrationExistenceResult;
import mx.com.fredkorlabs.registration.application.port.in.CheckRegistrationExistenceUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class SupplierRegistrationsController implements SupplierRegistrationsApi {

    private final CheckRegistrationExistenceUseCase checkRegistrationExistence;

    public SupplierRegistrationsController(CheckRegistrationExistenceUseCase checkRegistrationExistence) {
        this.checkRegistrationExistence = checkRegistrationExistence;
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
        throw new UnsupportedOperationException("Not implemented in this iteration");
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
}
