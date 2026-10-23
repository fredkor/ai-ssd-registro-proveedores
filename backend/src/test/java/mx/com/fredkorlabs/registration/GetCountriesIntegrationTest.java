package mx.com.fredkorlabs.registration;

import mx.com.fredkorlabs.registration.testsupport.PostgresTestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static mx.com.fredkorlabs.registration.testsupport.SignatureFixtures.identifyRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(PostgresTestcontainersConfiguration.class)
@SpringBootTest(classes = SupplierRegistrationApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class GetCountriesIntegrationTest {

    private static final String PATH = "/api/v1/registration-catalogs/countries";

    @Autowired
    private MockMvc mockMvc;

    private String token() throws Exception {
        String acceptance = JsonMapper.shared().readTree(mockMvc.perform(post("/api/v1/registration-notice-acceptances")
                .contentType(MediaType.APPLICATION_JSON).content("{\"version\":\"1.0\"}"))
            .andReturn().getResponse().getContentAsString()).get("acceptanceId").asString();
        return JsonMapper.shared().readTree(mockMvc.perform(post("/api/v1/applicant-identifications")
                .contentType(MediaType.APPLICATION_JSON).content(identifyRequest(acceptance, "legal-entity", "AAA010101AAA")))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("registrationToken").asString();
    }

    private JsonNode countries() throws Exception {
        String body = mockMvc.perform(get(PATH).header(HttpHeaders.AUTHORIZATION, "Bearer " + token()))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return JsonMapper.shared().readTree(body);
    }

    @Test
    void US08_CA010_countries_returnsWholeCatalogOrderedByIdInItems() throws Exception {
        JsonNode response = countries();

        assertEquals(Set.of("items"), Set.copyOf(response.propertyNames()));
        JsonNode items = response.get("items");
        assertEquals(249, items.size());
        List<Integer> ids = new ArrayList<>();
        items.forEach(item -> ids.add(item.get("id").asInt()));
        assertEquals(ids.stream().sorted().toList(), ids);
        JsonNode first = items.get(0);
        assertEquals(Set.of("id", "description", "countryCode"), Set.copyOf(first.propertyNames()));
        assertEquals(1, first.get("id").asInt());
        assertEquals("AFGANISTÁN", first.get("description").asString());
        assertEquals("AF", first.get("countryCode").asString());
    }

    @Test
    void US08_CA009_mexico_isAvailableWithCodeMx() throws Exception {
        JsonNode mexico = null;
        for (JsonNode item : countries().get("items")) {
            if ("MX".equals(item.get("countryCode").asString())) mexico = item;
        }

        assertNotNull(mexico);
        assertEquals(151, mexico.get("id").asInt());
        assertEquals("MÉXICO", mexico.get("description").asString());
    }

    @Test
    void ADR0015_withoutToken_returns401() throws Exception {
        mockMvc.perform(get(PATH))
            .andExpect(status().isUnauthorized())
            .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
            .andExpect(jsonPath("$.code").value("REGISTRATION_TOKEN_INVALID"));
    }
}
