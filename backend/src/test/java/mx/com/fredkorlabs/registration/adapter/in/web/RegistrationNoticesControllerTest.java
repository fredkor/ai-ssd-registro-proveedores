package mx.com.fredkorlabs.registration.adapter.in.web;

import mx.com.fredkorlabs.registration.application.port.in.AcceptRegistrationNoticeCommand;
import mx.com.fredkorlabs.registration.application.port.in.AcceptRegistrationNoticeResult;
import mx.com.fredkorlabs.registration.application.port.in.AcceptRegistrationNoticeUseCase;
import mx.com.fredkorlabs.registration.application.port.in.GetRegistrationNoticeResult;
import mx.com.fredkorlabs.registration.application.port.in.GetRegistrationNoticeUseCase;
import mx.com.fredkorlabs.registration.SupplierRegistrationApplication;
import mx.com.fredkorlabs.registration.testsupport.PostgresTestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(PostgresTestcontainersConfiguration.class)
@SpringBootTest(classes = SupplierRegistrationApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class RegistrationNoticesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetRegistrationNoticeUseCase getRegistrationNoticeUseCase;

    @MockitoBean
    private AcceptRegistrationNoticeUseCase acceptRegistrationNoticeUseCase;

    private static final String ACCEPTANCES_PATH = "/api/v1/registration-notice-acceptances";

    @Test
    void US03_CA006_getRegistrationNotice_returns200WithVersionAndText() throws Exception {
        given(getRegistrationNoticeUseCase.execute())
            .willReturn(new GetRegistrationNoticeResult("1.0", "Notice text"));

        mockMvc.perform(get("/api/v1/registration-notices/current"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.version").value("1.0"))
            .andExpect(jsonPath("$.text").value("Notice text"));
    }

    @Test
    void US03_CA006_getRegistrationNotice_doesNotRequireAuthorizationToken() throws Exception {
        given(getRegistrationNoticeUseCase.execute())
            .willReturn(new GetRegistrationNoticeResult("1.0", "Notice text"));

        mockMvc.perform(get("/api/v1/registration-notices/current"))
            .andExpect(status().isOk());
    }

    @Test
    void ADR0007_getRegistrationNotice_unexpectedException_returns500WithInternalError() throws Exception {
        given(getRegistrationNoticeUseCase.execute()).willThrow(new RuntimeException("DB down"));

        mockMvc.perform(get("/api/v1/registration-notices/current"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
            .andExpect(jsonPath("$.message").value("Unexpected error"))
            .andExpect(jsonPath("$.details").isEmpty());
    }

    @Test
    void US03_CA003_acceptRegistrationNotice_returns200WithVersionBackendTimeAndReference() throws Exception {
        given(acceptRegistrationNoticeUseCase.execute(new AcceptRegistrationNoticeCommand("1.0")))
            .willReturn(new AcceptRegistrationNoticeResult(
                "3f1c2a9e-0d5b-4a43-9a51-6d2f3c8b7e10", "1.0", Instant.parse("2026-10-20T15:30:00Z")));

        mockMvc.perform(post(ACCEPTANCES_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"version\":\"1.0\"}"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.version").value("1.0"))
            .andExpect(jsonPath("$.acceptedAt").value("2026-10-20T15:30:00Z"))
            .andExpect(jsonPath("$.acceptanceId").value("3f1c2a9e-0d5b-4a43-9a51-6d2f3c8b7e10"));

        verify(acceptRegistrationNoticeUseCase).execute(new AcceptRegistrationNoticeCommand("1.0"));
    }

    @Test
    void US03_CA003_acceptRegistrationNotice_doesNotRequireAuthorizationToken() throws Exception {
        given(acceptRegistrationNoticeUseCase.execute(any()))
            .willReturn(new AcceptRegistrationNoticeResult("ref", "1.0", Instant.parse("2026-10-20T15:30:00Z")));

        mockMvc.perform(post(ACCEPTANCES_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"version\":\"1.0\"}"))
            .andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "{}",
        "{\"version\":null}",
        "{\"version\":\"2.0\"}",
        "{\"version\":\" 1.0\"}",
        "{\"version\":\"\"}",
        "{\"version\":1.0}",
        "{\"version\":\"1.0\",\"acceptedAt\":\"2026-10-20T15:30:00Z\"}",
        "{\"version\":",
        "[]"
    })
    void US03_CA004_acceptRegistrationNotice_invalidBody_returns400WithoutAcceptance(String body) throws Exception {
        mockMvc.perform(post(ACCEPTANCES_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message").isString())
            .andExpect(jsonPath("$.details").isArray());

        verifyNoInteractions(acceptRegistrationNoticeUseCase);
    }

    @Test
    void US03_CA004_acceptRegistrationNotice_emptyBody_returns400WithoutAcceptance() throws Exception {
        mockMvc.perform(post(ACCEPTANCES_PATH)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verifyNoInteractions(acceptRegistrationNoticeUseCase);
    }

    @Test
    void US03_CA004_acceptRegistrationNotice_persistenceFailure_returns500WithoutDetails() throws Exception {
        given(acceptRegistrationNoticeUseCase.execute(any())).willThrow(new RuntimeException("jdbc:h2:mem:supplier down"));

        mockMvc.perform(post(ACCEPTANCES_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"version\":\"1.0\"}"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
            .andExpect(jsonPath("$.message").value("Unexpected error"))
            .andExpect(jsonPath("$.details").isEmpty())
            .andExpect(jsonPath("$.acceptanceId").doesNotExist());
    }
}
