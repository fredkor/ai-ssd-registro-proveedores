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
class GetLegalOwnershipsIntegrationTest {

    private static final String PATH = "/api/v1/registration-catalogs/legal-ownerships";

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

    private ResultActions request(String query) throws Exception {
        return mockMvc.perform(get(PATH + "?" + query).header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
    }

    private JsonNode items(String query) throws Exception {
        return JsonMapper.shared().readTree(request(query).andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8)).get("items");
    }

    private static List<String> keys(JsonNode items) {
        List<String> keys = new ArrayList<>();
        items.forEach(item -> keys.add(item.get("key").asString()));
        return keys;
    }

    private void assertParameterError(String query, String parameter) throws Exception {
        request(query)
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.errors[0].parameter").value(parameter))
            .andExpect(jsonPath("$.errors[0].pointer").doesNotExist());
    }

    @Test
    void US08_CA003_nationalLegalEntity_returnsMexicanMoralOptionsOrderedByKey() throws Exception {
        JsonNode items = items("origin=NATIONAL&countryCode=MX&taxRegime=LEGAL_ENTITY");

        assertEquals(27, items.size());
        assertEquals(keys(items).stream().sorted().toList(), keys(items));
        JsonNode first = items.get(0);
        assertEquals(Set.of("key", "id", "description", "countryCode", "taxRegime"), Set.copyOf(first.propertyNames()));
        assertEquals("MX00001", first.get("key").asString());
        assertEquals(1, first.get("id").asInt());
        assertEquals("1.- SOCIEDAD ANÓNIMA (S.A.)", first.get("description").asString());
        assertEquals("MX", first.get("countryCode").asString());
        items.forEach(item -> assertEquals("LEGAL_ENTITY", item.get("taxRegime").asString()));
    }

    @Test
    void US08_CA003_nationalIndividual_returnsMexicanFisicaOptions() throws Exception {
        assertEquals(List.of("MX00006", "MX00026", "MX00027", "MX00029"),
            keys(items("origin=NATIONAL&countryCode=MX&taxRegime=INDIVIDUAL")));
    }

    @Test
    void US08_CA014_foreign_returnsCountryOptionsPlusNotDefOrderedByKey() throws Exception {
        JsonNode items = items("origin=FOREIGN&countryCode=AT");

        List<String> keys = keys(items);
        assertEquals(23, keys.size());
        assertEquals("AT00001", keys.getFirst());
        // By key NOT_DEF (id 99) goes after AT00022 (id 143): the order is not the id order.
        assertEquals("NOT_DEF", keys.getLast());
        JsonNode notDefined = items.get(22);
        assertEquals(Set.of("key", "id", "description", "countryCode"), Set.copyOf(notDefined.propertyNames()));
        assertTrue(notDefined.get("countryCode").isNull());
        assertFalse(items.get(0).has("taxRegime"));
    }

    @Test
    void US08_CA014_foreignCountryWithoutOptions_returnsOnlyNotDef() throws Exception {
        assertEquals(List.of("NOT_DEF"), keys(items("origin=FOREIGN&countryCode=AF")));
    }

    @Test
    void US08_CA014_foreignMexico_includesBothRegimesAndNotDef() throws Exception {
        List<String> keys = keys(items("origin=FOREIGN&countryCode=MX"));

        assertEquals(32, keys.size());
        assertTrue(keys.containsAll(List.of("MX00001", "MX00006", "NOT_DEF")));
    }

    @Test
    void US08_CA003_nationalCountryWithoutOptions_returnsEmptyList() throws Exception {
        assertEquals(0, items("origin=NATIONAL&countryCode=AF&taxRegime=LEGAL_ENTITY").size());
    }

    @Test
    void ADR0020_nationalWithoutTaxRegime_returns400() throws Exception {
        assertParameterError("origin=NATIONAL&countryCode=MX", "taxRegime");
    }

    @Test
    void ADR0020_foreignWithTaxRegime_returns400() throws Exception {
        assertParameterError("origin=FOREIGN&countryCode=AT&taxRegime=INDIVIDUAL", "taxRegime");
    }

    @Test
    void ADR0020_unknownCountryCode_returns400() throws Exception {
        assertParameterError("origin=FOREIGN&countryCode=ZZ", "countryCode");
        assertParameterError("origin=NATIONAL&countryCode=mx&taxRegime=LEGAL_ENTITY", "countryCode");
    }

    @Test
    void ADR0020_missingParameters_return400() throws Exception {
        assertParameterError("countryCode=MX&taxRegime=LEGAL_ENTITY", "origin");
        assertParameterError("origin=FOREIGN", "countryCode");
    }

    @Test
    void ADR0020_invalidParameterValues_return400() throws Exception {
        assertParameterError("origin=national&countryCode=MX&taxRegime=LEGAL_ENTITY", "origin");
        assertParameterError("origin=NATIONAL&countryCode=MX&taxRegime=MORAL", "taxRegime");
        assertParameterError("origin=FOREIGN&countryCode=", "countryCode");
    }

    @Test
    void ADR0020_undefinedOrRepeatedParameter_returns400() throws Exception {
        assertParameterError("origin=FOREIGN&countryCode=AT&page=1", "page");
        assertParameterError("origin=FOREIGN&countryCode=AT&countryCode=MX", "countryCode");
    }

    @Test
    void ADR0015_withoutToken_returns401() throws Exception {
        mockMvc.perform(get(PATH + "?origin=FOREIGN&countryCode=AT"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("REGISTRATION_TOKEN_INVALID"));
    }
}
