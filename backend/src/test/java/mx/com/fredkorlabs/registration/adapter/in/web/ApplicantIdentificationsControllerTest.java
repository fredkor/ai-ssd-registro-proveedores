package mx.com.fredkorlabs.registration.adapter.in.web;

import mx.com.fredkorlabs.registration.SupplierRegistrationApplication;
import mx.com.fredkorlabs.registration.application.port.in.IdentifyApplicantCommand;
import mx.com.fredkorlabs.registration.application.port.in.IdentifyApplicantResult;
import mx.com.fredkorlabs.registration.application.port.in.IdentifyApplicantUseCase;
import mx.com.fredkorlabs.registration.application.port.in.InvalidIdentificationDataException;
import mx.com.fredkorlabs.registration.application.port.in.SignatureVerificationFailedException;
import mx.com.fredkorlabs.registration.domain.exception.ApplicantRfcMismatchException;
import mx.com.fredkorlabs.registration.domain.exception.InvalidCertificateIdentityException;
import mx.com.fredkorlabs.registration.testsupport.PostgresTestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.time.Instant;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(PostgresTestcontainersConfiguration.class)
@SpringBootTest(classes = SupplierRegistrationApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class ApplicantIdentificationsControllerTest {

    private static final String PATH = "/api/v1/applicant-identifications";
    private static final String CERTIFICATE = Base64.getEncoder().encodeToString(new byte[] {48, 1, 2});
    private static final String SEAL = Base64.getEncoder().encodeToString(new byte[] {9, 8, 7});
    private static final String SIGNATURE_RESULT = """
        {"resultado":true,"fechaVigencia":"2030-01-01T00:00:00Z","mensaje":"OK","cadenaOriginal":"Temporal",
         "certificado":"%s","cadenaFirma":"%s","rfc":"AAA010101AAA","curp":null,
         "razonSocial":"RAZON PKI","numeroSerie":30001000000500003416.000,"tipoCertificado":1,
         "certificadoPem":"-----BEGIN CERTIFICATE-----","isVigente":true,"origen":"COMPONENTE"}"""
        .formatted(CERTIFICATE, SEAL);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IdentifyApplicantUseCase identifyApplicantUseCase;

    private static String body(String signatureResult) {
        return "{\"noticeAcceptanceId\":\"acc-1\",\"signatureResult\":" + signatureResult + "}";
    }

    private static String withSignatureProperty(String property, String jsonValue) {
        ObjectNode signature = (ObjectNode) JsonMapper.shared().readTree(SIGNATURE_RESULT);
        signature.set(property, JsonMapper.shared().readTree(jsonValue));
        return body(signature.toString());
    }

    private static String withoutSignatureProperty(String property) {
        ObjectNode signature = (ObjectNode) JsonMapper.shared().readTree(SIGNATURE_RESULT);
        signature.remove(property);
        return body(signature.toString());
    }

    private ResultActions perform(String json) throws Exception {
        return mockMvc.perform(post(PATH).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private void givenSuccess() {
        given(identifyApplicantUseCase.execute(any())).willReturn(new IdentifyApplicantResult(
            "AAA010101AAA", "EMPRESA CERTIFICADO SA", "LEGAL_ENTITY", "aaa010101aaa",
            "jwt-token", Instant.parse("2026-10-21T16:30:00Z")));
    }

    @Test
    void US01_CA017_identifyApplicant_returns200WithIdentityTokenAndExpiry() throws Exception {
        givenSuccess();

        perform(body(SIGNATURE_RESULT))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.identity.rfc").value("AAA010101AAA"))
            .andExpect(jsonPath("$.identity.legalName").value("EMPRESA CERTIFICADO SA"))
            .andExpect(jsonPath("$.identity.taxRegime").value("LEGAL_ENTITY"))
            .andExpect(jsonPath("$.identity.username").value("aaa010101aaa"))
            .andExpect(jsonPath("$.registrationToken").value("jwt-token"))
            .andExpect(jsonPath("$.tokenExpiresAt").value("2026-10-21T16:30:00Z"));
    }

    @Test
    void US01_CA013_commandCarriesOriginalSignatureResultTextAndProjections() throws Exception {
        givenSuccess();

        perform(body(SIGNATURE_RESULT)).andExpect(status().isOk());

        ArgumentCaptor<IdentifyApplicantCommand> captor = ArgumentCaptor.forClass(IdentifyApplicantCommand.class);
        verify(identifyApplicantUseCase).execute(captor.capture());
        IdentifyApplicantCommand command = captor.getValue();
        assertEquals("acc-1", command.noticeAcceptanceId());
        assertEquals(SIGNATURE_RESULT, command.signatureResult().completeResult());
        assertTrue(command.signatureResult().successful());
        assertEquals("Temporal", command.signatureResult().signedContent());
        assertArrayEquals(new byte[] {48, 1, 2}, command.signatureResult().certificate());
        assertArrayEquals(new byte[] {9, 8, 7}, command.signatureResult().signature());
        assertEquals("AAA010101AAA", command.signatureResult().rfc());
        assertEquals("RAZON PKI", command.signatureResult().legalName());
    }

    @Test
    void US01_CA013_curpNull_isAccepted() throws Exception {
        givenSuccess();

        perform(body(SIGNATURE_RESULT)).andExpect(status().isOk());
    }

    @Test
    void US01_CA016_resultadoFalse_isPassedToApplicationAsUnsuccessful() throws Exception {
        given(identifyApplicantUseCase.execute(any()))
            .willThrow(new InvalidIdentificationDataException("Signature result is not successful"));

        perform(withSignatureProperty("resultado", "false"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"resultado", "fechaVigencia", "mensaje", "cadenaOriginal", "certificado", "cadenaFirma",
        "rfc", "curp", "razonSocial", "numeroSerie", "tipoCertificado", "certificadoPem", "isVigente", "origen"})
    void US01_CA010_omittedSignatureProperty_returns400WithPath(String property) throws Exception {
        perform(withoutSignatureProperty(property))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.errors[0].pointer").value("/signatureResult/" + property))
            .andExpect(jsonPath("$.errors[0].detail").value("is required"));
        verifyNoInteractions(identifyApplicantUseCase);
    }

    @ParameterizedTest
    @ValueSource(strings = {"resultado", "fechaVigencia", "mensaje", "cadenaOriginal", "certificado", "cadenaFirma",
        "rfc", "razonSocial", "numeroSerie", "tipoCertificado", "certificadoPem", "isVigente", "origen"})
    void US01_CA010_nullInRequiredSignatureProperty_returns400(String property) throws Exception {
        perform(withSignatureProperty(property, "null"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        verifyNoInteractions(identifyApplicantUseCase);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "{\"signatureResult\":{}}",
        "{\"noticeAcceptanceId\":\"acc-1\"}",
        "{\"noticeAcceptanceId\":\"\",\"signatureResult\":%s}",
        "{\"noticeAcceptanceId\":\"acc-1\",\"signatureResult\":%s,\"extra\":1}",
        "{\"noticeAcceptanceId\":\"acc-1\",\"signatureResult\":%s",
    })
    void US01_CA010_invalidEnvelope_returns400(String template) throws Exception {
        perform(template.formatted(SIGNATURE_RESULT))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        verifyNoInteractions(identifyApplicantUseCase);
    }

    @Test
    void US01_CA010_unknownNestedProperty_returns400() throws Exception {
        perform(body(SIGNATURE_RESULT.replace("\"origen\":\"COMPONENTE\"", "\"origen\":\"COMPONENTE\",\"llavePrivada\":\"x\"")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        verifyNoInteractions(identifyApplicantUseCase);
    }

    @Test
    void US01_CA010_emptyLegalName_returns400() throws Exception {
        perform(withSignatureProperty("razonSocial", "\"\""))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        verifyNoInteractions(identifyApplicantUseCase);
    }

    @ParameterizedTest
    @ValueSource(strings = {"\"AAA010101AA\"", "\"AAA010101AAAAA\""})
    void US01_CA011_rfcWithInvalidLength_returns400(String rfc) throws Exception {
        perform(withSignatureProperty("rfc", rfc))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        verifyNoInteractions(identifyApplicantUseCase);
    }

    @ParameterizedTest
    @ValueSource(strings = {"cadenaOriginal|\"temporal\"", "certificado|\"%%%\"", "cadenaFirma|\"%%%\"",
        "certificado|\"\"", "certificadoPem|\"\"", "fechaVigencia|\"ayer\"", "resultado|\"true\"",
        "resultado|1", "isVigente|\"true\"", "numeroSerie|\"123\"", "tipoCertificado|true", "mensaje|5",
        "rfc|123456789012", "origen|false"})
    void US01_CA015_malformedSignatureData_returns400(String propertyAndValue) throws Exception {
        String[] parts = propertyAndValue.split("\\|", 2);
        perform(withSignatureProperty(parts[0], parts[1]))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        verifyNoInteractions(identifyApplicantUseCase);
    }

    @Test
    void US01_CA021_invalidCertificateIdentity_returns400() throws Exception {
        given(identifyApplicantUseCase.execute(any()))
            .willThrow(new InvalidCertificateIdentityException("Certificate has no RFC attribute"));

        perform(body(SIGNATURE_RESULT))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void US01_CA015_invalidSeal_returns422() throws Exception {
        given(identifyApplicantUseCase.execute(any()))
            .willThrow(new SignatureVerificationFailedException("Seal does not match"));

        perform(body(SIGNATURE_RESULT))
            .andExpect(status().isUnprocessableContent())
            .andExpect(jsonPath("$.code").value("SIGNATURE_VERIFICATION_FAILED"))
            .andExpect(jsonPath("$.registrationToken").doesNotExist());
    }

    @Test
    void US01_CA022_rfcMismatch_returns422() throws Exception {
        given(identifyApplicantUseCase.execute(any()))
            .willThrow(new ApplicantRfcMismatchException("Received RFC does not match"));

        perform(body(SIGNATURE_RESULT))
            .andExpect(status().isUnprocessableContent())
            .andExpect(jsonPath("$.code").value("SIGNATURE_VERIFICATION_FAILED"));
    }

    @Test
    void US01_CA015_technicalFailure_returns500WithoutDetails() throws Exception {
        given(identifyApplicantUseCase.execute(any())).willThrow(new IllegalStateException("db down"));

        perform(body(SIGNATURE_RESULT))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
            .andExpect(jsonPath("$.detail").value("Unexpected error"))
            .andExpect(jsonPath("$.errors").isEmpty());
    }
}
