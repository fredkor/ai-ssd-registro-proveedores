package mx.com.fredkorlabs.registration.adapter.in.web;

import mx.com.fredkorlabs.registration.application.port.in.GetRegistrationNoticeResult;
import mx.com.fredkorlabs.registration.application.port.in.GetRegistrationNoticeUseCase;
import mx.com.fredkorlabs.registration.SupplierRegistrationApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = SupplierRegistrationApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class RegistrationNoticesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetRegistrationNoticeUseCase getRegistrationNoticeUseCase;

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
}
