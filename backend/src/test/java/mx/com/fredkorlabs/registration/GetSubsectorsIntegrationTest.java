package mx.com.fredkorlabs.registration;

import mx.com.fredkorlabs.registration.testsupport.PostgresTestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import static mx.com.fredkorlabs.registration.testsupport.SignatureFixtures.identifyRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(PostgresTestcontainersConfiguration.class)
@SpringBootTest(classes = SupplierRegistrationApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class GetSubsectorsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String token;

    @BeforeEach
    void identify() throws Exception {
        String acceptance = JsonMapper.shared().readTree(mockMvc.perform(post("/api/v1/registration-notice-acceptances")
                .contentType(MediaType.APPLICATION_JSON).content("{\"version\":\"1.0\"}"))
            .andReturn().getResponse().getContentAsString()).get("acceptanceId").asString();
        token = JsonMapper.shared().readTree(mockMvc.perform(post("/api/v1/applicant-identifications")
                .contentType(MediaType.APPLICATION_JSON).content(identifyRequest(acceptance, "legal-entity", "AAA010101AAA")))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("registrationToken").asString();
    }

    private ResultActions subsectors(String sectorId) throws Exception {
        return mockMvc.perform(get("/api/v1/registration-catalogs/sectors/" + sectorId + "/subsectors")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
    }

    private JsonNode body(String sectorId) throws Exception {
        return JsonMapper.shared().readTree(subsectors(sectorId).andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    @Test
    void US08_CA023_subsectors_ofTheSelectedSectorOrderedById() throws Exception {
        JsonNode response = body("1");

        assertEquals(Set.of("items"), Set.copyOf(response.propertyNames()));
        JsonNode items = response.get("items");
        assertEquals(5, items.size());
        assertEquals(Set.of("id", "description"), Set.copyOf(items.get(0).propertyNames()));
        assertEquals(1, items.get(0).get("id").asInt());
        assertEquals("111 AGRICULTURA", items.get(0).get("description").asString());
        assertEquals(5, items.get(4).get("id").asInt());
    }

    @Test
    void US08_CA023_otherSector_returnsOnlyItsSubsectors() throws Exception {
        JsonNode items = body("2").get("items");

        assertEquals(3, items.size());
        assertEquals("211 EXTRACCIÓN DE PETRÓLEO Y GAS", items.get(0).get("description").asString());
    }

    @Test
    void ADR0020_unknownSector_returnsEmptyItems() throws Exception {
        assertEquals(0, body("999").get("items").size());
    }

    @Test
    void ADR0020_sectorIdNotInteger_returns400WithParameter() throws Exception {
        subsectors("abc")
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.errors[0].parameter").value("sectorId"));
    }

    @Test
    void ADR0015_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/registration-catalogs/sectors/1/subsectors"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("REGISTRATION_TOKEN_INVALID"));
    }
}
