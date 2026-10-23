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
class GetCucopCatalogIntegrationTest {

    private static final String PATH = "/api/v1/registration-catalogs/cucop";
    private static final Set<String> NODE_PROPERTIES = Set.of("id", "key", "description", "level", "items");

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

    private JsonNode chapters() throws Exception {
        JsonNode response = JsonMapper.shared().readTree(mockMvc.perform(get(PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token()))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertEquals(Set.of("items"), Set.copyOf(response.propertyNames()));
        return response.get("items");
    }

    private static JsonNode child(JsonNode node, String key) {
        for (JsonNode item : node.get("items")) {
            if (item.get("key").asString().equals(key)) return item;
        }
        throw new AssertionError("Missing child " + key + " under " + node.get("key").asString());
    }

    // Walks the whole tree checking shape, level and key order; counts nodes per level.
    private static void walk(JsonNode siblings, int expectedLevel, int[] perLevel) {
        String previousKey = "";
        for (JsonNode node : siblings) {
            assertEquals(NODE_PROPERTIES, Set.copyOf(node.propertyNames()));
            assertEquals(expectedLevel, node.get("level").asInt());
            String key = node.get("key").asString();
            assertTrue(key.compareTo(previousKey) > 0, "Siblings out of order at " + key);
            previousKey = key;
            perLevel[expectedLevel]++;
            if (expectedLevel == 4) {
                assertTrue(node.get("items").isEmpty(), "Specific item " + key + " must have no children");
            } else {
                assertFalse(node.get("items").isEmpty(), "Node " + key + " must have at least one child");
                walk(node.get("items"), expectedLevel + 1, perLevel);
            }
        }
    }

    @Test
    void US11_CA015_catalog_isTheWholeTreeNestedInOneResponse() throws Exception {
        int[] perLevel = new int[5];

        walk(chapters(), 1, perLevel);

        assertArrayEquals(new int[] {0, 7, 39, 206, 322}, perLevel);
    }

    @Test
    void US11_CA015_chapters_areTheRootOrderedByKey() throws Exception {
        List<String> keys = new ArrayList<>();
        chapters().forEach(chapter -> keys.add(chapter.get("key").asString()));

        assertEquals(List.of("1000", "2000", "3000", "4000", "5000", "6000", "8000"), keys);
    }

    @Test
    void US11_CA015_branch_keepsIdsAndDescriptionsOfEachLevel() throws Exception {
        JsonNode chapter = chapters().get(0);
        JsonNode concept = child(chapter, "1200");
        JsonNode generic = child(concept, "1220");
        JsonNode specific = child(generic, "12201");

        assertEquals(6, chapter.get("id").asInt());
        assertEquals("SERVICIOS PERSONALES", chapter.get("description").asString());
        assertEquals(38, concept.get("id").asInt());
        assertEquals(242, generic.get("id").asInt());
        assertEquals("SUELDOS BASE AL PERSONAL EVENTUAL", generic.get("description").asString());
        assertEquals(367, specific.get("id").asInt());
        assertEquals("REMUNERACIONES AL PERSONAL EVENTUAL", specific.get("description").asString());
        assertTrue(specific.get("items").isArray());
    }

    @Test
    void ADR0015_withoutToken_returns401() throws Exception {
        mockMvc.perform(get(PATH))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("REGISTRATION_TOKEN_INVALID"));
    }
}
