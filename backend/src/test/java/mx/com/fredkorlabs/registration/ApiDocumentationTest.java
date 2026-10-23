package mx.com.fredkorlabs.registration;

import mx.com.fredkorlabs.registration.testsupport.PostgresTestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The published API documentation lists only contract operations, not the internal error path. */
@Import(PostgresTestcontainersConfiguration.class)
@SpringBootTest(classes = SupplierRegistrationApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class ApiDocumentationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void ADR0019_apiDocs_excludeInternalErrorPath() throws Exception {
        String body = mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        JsonNode paths = JsonMapper.shared().readTree(body).get("paths");
        assertFalse(paths.has("/error"));
        assertTrue(paths.has("/api/v1/supplier-registrations/existence"));
    }

    @Test
    void ADR0008_swaggerUi_loadsTheAuthoritativeContract() throws Exception {
        String body = mockMvc.perform(get("/v3/api-docs/swagger-config"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        assertEquals("/supplier-registration-openapi.yaml", JsonMapper.shared().readTree(body).get("url").asString());
    }

    @Test
    void ADR0015_contract_isServedWithoutTokenAndDeclaresBearerScheme() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/supplier-registration-openapi.yaml"))
            .andExpect(status().isOk()).andReturn().getResponse();
        String contract = response.getContentAsString(StandardCharsets.UTF_8);

        assertTrue(contract.contains("operationId: startSupplierRegistration"));
        assertTrue(contract.matches("(?s).*\\n {4}RegistrationToken:\\n {6}type: http\\n {6}scheme: bearer\\n {6}bearerFormat: JWT\\n.*"));
    }
}
