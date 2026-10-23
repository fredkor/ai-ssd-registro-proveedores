package mx.com.fredkorlabs.registration.adapter.in.web;

import mx.com.fredkorlabs.registration.adapter.in.web.generated.ApplicantIdentificationsApi;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.ApplicantIdentificationResponse;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.ApplicantIdentityResponse;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.IdentifyApplicantRequest;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.SignatureResultRequest;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.TaxRegime;
import mx.com.fredkorlabs.registration.application.port.in.IdentifyApplicantCommand;
import mx.com.fredkorlabs.registration.application.port.in.IdentifyApplicantResult;
import mx.com.fredkorlabs.registration.application.port.in.IdentifyApplicantUseCase;
import mx.com.fredkorlabs.registration.application.port.in.SignatureResultInput;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.json.JsonMapper;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@RestController
public class ApplicantIdentificationsController implements ApplicantIdentificationsApi {

    private static final String SIGNATURE_RESULT_PROPERTY = "signatureResult";

    private final IdentifyApplicantUseCase identifyApplicant;
    private final JsonMapper jsonMapper;

    public ApplicantIdentificationsController(IdentifyApplicantUseCase identifyApplicant, JsonMapper jsonMapper) {
        this.identifyApplicant = identifyApplicant;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public ResponseEntity<ApplicantIdentificationResponse> identifyApplicant(IdentifyApplicantRequest request) {
        SignatureResultRequest signature = request.getSignatureResult();
        String completeResult = RawJsonProperty.extract(
            jsonMapper, RawRequestBodyAdvice.currentRawBody(), SIGNATURE_RESULT_PROPERTY);
        IdentifyApplicantResult result = identifyApplicant.execute(new IdentifyApplicantCommand(
            request.getNoticeAcceptanceId(),
            new SignatureResultInput(
                completeResult,
                Boolean.TRUE.equals(signature.getResultado()),
                signature.getCadenaOriginal().getValue(),
                signature.getCertificado(),
                signature.getCadenaFirma(),
                signature.getRfc(),
                signature.getRazonSocial())));
        return ResponseEntity.ok(new ApplicantIdentificationResponse(
            new ApplicantIdentityResponse(result.rfc(), result.legalName(),
                TaxRegime.fromValue(result.taxRegime()), result.taxRegimeId(), result.username()),
            result.registrationToken(),
            OffsetDateTime.ofInstant(result.tokenExpiresAt(), ZoneOffset.UTC)));
    }
}
