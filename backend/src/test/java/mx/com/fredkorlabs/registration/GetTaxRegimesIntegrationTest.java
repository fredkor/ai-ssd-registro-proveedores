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
import java.util.Set;

import static mx.com.fredkorlabs.registration.testsupport.SignatureFixtures.identifyRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** The applicant's taxRegimeId selects the description shown from the tax regime catalog (US08 CA-011, CA-012). */
@Import(PostgresTestcontainersConfiguration.class)
@SpringBootTest(classes = SupplierRegistrationApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class GetTaxRegimesIntegrationTest {

    private static final String PATH = "/api/v1/registration-catalogs/tax-regimes";

    @Autowired
    private MockMvc mockMvc;

    private JsonNode identify(String variant, String rfc) throws Exception {
        String acceptance = JsonMapper.shared().readTree(mockMvc.perform(post("/api/v1/registration-notice-acceptances")
                .contentType(MediaType.APPLICATION_JSON).content("{\"version\":\"1.0\"}"))
            .andReturn().getResponse().getContentAsString()).get("acceptanceId").asString();
        return JsonMapper.shared().readTree(mockMvc.perform(post("/api/v1/applicant-identifications")
                .contentType(MediaType.APPLICATION_JSON).content(identifyRequest(acceptance, variant, rfc)))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    }

    private JsonNode taxRegimes(String token) throws Exception {
        return JsonMapper.shared().readTree(mockMvc.perform(get(PATH).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private static String descriptionOf(JsonNode catalog, int id) {
        for (JsonNode item : catalog.get("items")) {
            if (item.get("id").asInt() == id) return item.get("description").asString();
        }
        throw new AssertionError("Tax regime " + id + " is not in the catalog");
    }

    @Test
    void US08_CA011_legalEntityTaxRegimeId_showsPersonaMoral() throws Exception {
        JsonNode identification = identify("legal-entity", "AAA010101AAA");

        int taxRegimeId = identification.get("identity").get("taxRegimeId").asInt();
        assertEquals("PERSONA MORAL",
            descriptionOf(taxRegimes(identification.get("registrationToken").asString()), taxRegimeId));
    }

    @Test
    void US08_CA012_individualTaxRegimeId_showsPersonaFisica() throws Exception {
        JsonNode identification = identify("individual", "HEGT7610034S2");

        int taxRegimeId = identification.get("identity").get("taxRegimeId").asInt();
        assertEquals("PERSONA FÍSICA",
            descriptionOf(taxRegimes(identification.get("registrationToken").asString()), taxRegimeId));
    }

    @Test
    void US08_CA022_taxRegimes_returnsWholeCatalogOrderedByIdSameForEveryApplicant() throws Exception {
        JsonNode response = taxRegimes(identify("legal-entity", "AAA010101AAA").get("registrationToken").asString());

        assertEquals(Set.of("items"), Set.copyOf(response.propertyNames()));
        JsonNode items = response.get("items");
        assertEquals(2, items.size());
        assertEquals(Set.of("id", "description"), Set.copyOf(items.get(0).propertyNames()));
        assertEquals(1, items.get(0).get("id").asInt());
        assertEquals("PERSONA MORAL", items.get(0).get("description").asString());
        assertEquals(2, items.get(1).get("id").asInt());
        assertEquals("PERSONA FÍSICA", items.get(1).get("description").asString());
        assertEquals(response, taxRegimes(identify("individual", "HEGT7610034S2").get("registrationToken").asString()));
    }

    @Test
    void ADR0015_withoutToken_returns401() throws Exception {
        mockMvc.perform(get(PATH))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("REGISTRATION_TOKEN_INVALID"));
    }
}
