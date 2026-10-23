package mx.com.fredkorlabs.registration.adapter.in.web;

import mx.com.fredkorlabs.registration.SupplierRegistrationApplication;
import mx.com.fredkorlabs.registration.application.port.out.SupplierRegistrationPersistencePort;
import mx.com.fredkorlabs.registration.testsupport.PostgresTestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import static mx.com.fredkorlabs.registration.testsupport.SignatureFixtures.identifyRequest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(PostgresTestcontainersConfiguration.class)
@SpringBootTest(classes = SupplierRegistrationApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class SupplierRegistrationsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SupplierRegistrationPersistencePort supplierRegistrationPersistence;

    private String token() throws Exception {
        String acceptance = JsonMapper.shared().readTree(mockMvc.perform(post("/api/v1/registration-notice-acceptances")
                .contentType(MediaType.APPLICATION_JSON).content("{\"version\":\"1.0\"}"))
            .andReturn().getResponse().getContentAsString()).get("acceptanceId").asString();
        return JsonMapper.shared().readTree(mockMvc.perform(post("/api/v1/applicant-identifications")
                .contentType(MediaType.APPLICATION_JSON).content(identifyRequest(acceptance, "legal-entity", "AAA010101AAA")))
            .andReturn().getResponse().getContentAsString()).get("registrationToken").asString();
    }

    @Test
    void US04_CA004_insertFailure_returns500WithoutReportingCreation() throws Exception {
        willThrow(new IllegalStateException("db down")).given(supplierRegistrationPersistence)
            .insert(any(), any(), any());

        mockMvc.perform(post("/api/v1/supplier-registrations").header(HttpHeaders.AUTHORIZATION, "Bearer " + token()))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
            .andExpect(jsonPath("$.detail").value("Unexpected error"))
            .andExpect(jsonPath("$.id").doesNotExist())
            .andExpect(header().doesNotExist(HttpHeaders.LOCATION));
    }

    @Test
    void US04_CA004_lookupFailure_returns500WithoutInserting() throws Exception {
        given(supplierRegistrationPersistence.findStatusByRfc(any())).willThrow(new IllegalStateException("db down"));

        mockMvc.perform(post("/api/v1/supplier-registrations").header(HttpHeaders.AUTHORIZATION, "Bearer " + token()))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));
        then(supplierRegistrationPersistence).should(never()).insert(any(), any(), any());
    }

    @Test
    void US02_CA004_lookupFailure_returns500InsteadOfExistsFalse() throws Exception {
        given(supplierRegistrationPersistence.findStatusByRfc(any())).willThrow(new IllegalStateException("db down"));
        String token = token();

        mockMvc.perform(get("/api/v1/supplier-registrations/existence").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
            .andExpect(jsonPath("$.exists").doesNotExist());
    }
}
