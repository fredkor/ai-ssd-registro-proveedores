package mx.com.fredkorlabs.registration.adapter.in.web;

import mx.com.fredkorlabs.registration.SupplierRegistrationApplication;
import mx.com.fredkorlabs.registration.application.port.in.IdentifyApplicantUseCase;
import mx.com.fredkorlabs.registration.application.port.in.SignatureVerificationFailedException;
import mx.com.fredkorlabs.registration.testsupport.PostgresTestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

import java.util.Base64;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** ADR-0019: every error is application/problem+json with type, title, status, detail, instance, code and errors. */
@Import(PostgresTestcontainersConfiguration.class)
@SpringBootTest(classes = SupplierRegistrationApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class ProblemDetailsTest {

    private static final String IDENTIFY = "/api/v1/applicant-identifications";
    private static final String SIGNATURE_RESULT = """
        {"resultado":true,"fechaVigencia":"2030-01-01T00:00:00Z","mensaje":"OK","cadenaOriginal":"Temporal",
         "certificado":"%s","cadenaFirma":"%s","rfc":"AAA010101AAA","curp":null,"razonSocial":"RAZON PKI",
         "numeroSerie":1,"tipoCertificado":1,"certificadoPem":"PEM","isVigente":true,"origen":"COMPONENTE"}"""
        .formatted(Base64.getEncoder().encodeToString(new byte[] {1}), Base64.getEncoder().encodeToString(new byte[] {2}));

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IdentifyApplicantUseCase identifyApplicantUseCase;

    private static String identifyWith(String property, String jsonValue) {
        ObjectNode signature = (ObjectNode) JsonMapper.shared().readTree(SIGNATURE_RESULT);
        signature.set(property, JsonMapper.shared().readTree(jsonValue));
        return "{\"noticeAcceptanceId\":\"acc-1\",\"signatureResult\":" + signature + "}";
    }

    private ResultActions postIdentify(String body) throws Exception {
        return mockMvc.perform(post(IDENTIFY).contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON).content(body));
    }

    private static void assertProblem(ResultActions result, int status, String code, String slug, String title,
                                      String instance) throws Exception {
        result.andExpect(status().is(status))
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("urn:fredkorlabs:registration:problem:" + slug))
            .andExpect(jsonPath("$.title").value(title))
            .andExpect(jsonPath("$.status").value(status))
            .andExpect(jsonPath("$.detail").isString())
            .andExpect(jsonPath("$.instance").value(instance))
            .andExpect(jsonPath("$.code").value(code))
            .andExpect(jsonPath("$.errors").isArray());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        "cadenaOriginal|\"Cadena original de prueba\"|must be one of [Temporal]",
        "certificado|\"string\"|has an invalid format",
        "resultado|\"true\"|has an invalid type",
        "rfc|\"AAA0101\"|size must be between 12 and 13",
        "razonSocial|null|must not be null",
    })
    void US01_CA010_invalidField_isReportedWithJsonPointer(String property, String value, String reason)
            throws Exception {
        ResultActions result = postIdentify(identifyWith(property, value));

        assertProblem(result, 400, "VALIDATION_ERROR", "validation-error", "Invalid request", IDENTIFY);
        result.andExpect(jsonPath("$.errors[0].pointer").value("/signatureResult/" + property))
            .andExpect(jsonPath("$.errors[0].detail").value(reason));
    }

    @Test
    void US01_CA010_unknownProperty_isReportedWithItsPointer() throws Exception {
        String body = identifyWith("origen", "\"COMPONENTE\"").replace("\"origen\":\"COMPONENTE\"",
            "\"origen\":\"COMPONENTE\",\"llavePrivada\":\"x\"");

        postIdentify(body)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[0].pointer").value("/signatureResult/llavePrivada"))
            .andExpect(jsonPath("$.errors[0].detail").value("is not a defined property"));
    }

    @Test
    void US01_CA010_invalidJson_hasNoFieldErrors() throws Exception {
        ResultActions result = postIdentify("{\"noticeAcceptanceId\":");

        assertProblem(result, 400, "VALIDATION_ERROR", "validation-error", "Invalid request", IDENTIFY);
        result.andExpect(jsonPath("$.detail").value("Request body is not valid JSON"))
            .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void US01_CA015_signatureFailure_isProblemWithoutFieldErrors() throws Exception {
        given(identifyApplicantUseCase.execute(any())).willThrow(new SignatureVerificationFailedException("Seal mismatch"));

        ResultActions result = postIdentify(identifyWith("mensaje", "\"OK\""));

        assertProblem(result, 422, "SIGNATURE_VERIFICATION_FAILED", "signature-verification-failed",
            "Signature verification failed", IDENTIFY);
        result.andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void ADR0007_unexpectedError_isGenericProblem() throws Exception {
        given(identifyApplicantUseCase.execute(any())).willThrow(new IllegalStateException("jdbc:postgresql down"));

        ResultActions result = postIdentify(identifyWith("mensaje", "\"OK\""));

        assertProblem(result, 500, "INTERNAL_ERROR", "internal-error", "Internal error", IDENTIFY);
        result.andExpect(jsonPath("$.detail").value("Unexpected error"))
            .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void US01_CA026_missingToken_isProblemWithBearerChallenge() throws Exception {
        ResultActions result = mockMvc.perform(get("/api/v1/supplier-registrations/existence"));

        assertProblem(result, 401, "REGISTRATION_TOKEN_INVALID", "registration-token-invalid",
            "Invalid registration token", "/api/v1/supplier-registrations/existence");
        result.andExpect(header().string("WWW-Authenticate", "Bearer"));
    }

    @Test
    void US03_CA004_noticeVersionError_isProblem() throws Exception {
        ResultActions result = mockMvc.perform(post("/api/v1/registration-notice-acceptances")
            .contentType(MediaType.APPLICATION_JSON).content("{\"version\":\"2.0\"}"));

        assertProblem(result, 400, "VALIDATION_ERROR", "validation-error", "Invalid request",
            "/api/v1/registration-notice-acceptances");
        result.andExpect(jsonPath("$.errors[0].pointer").value("/version"))
            .andExpect(jsonPath("$.errors[0].detail").value("must be one of [1.0]"));
    }
}
