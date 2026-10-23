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
class GetSectorsIntegrationTest {

    private static final String PATH = "/api/v1/registration-catalogs/sectors";

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

    @Test
    void US08_CA022_sectors_returnsWholeCatalogOrderedByIdInItems() throws Exception {
        JsonNode response = JsonMapper.shared().readTree(mockMvc.perform(get(PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token()))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));

        assertEquals(Set.of("items"), Set.copyOf(response.propertyNames()));
        JsonNode items = response.get("items");
        assertEquals(20, items.size());
        List<Integer> ids = new ArrayList<>();
        items.forEach(item -> ids.add(item.get("id").asInt()));
        assertEquals(ids.stream().sorted().toList(), ids);
        JsonNode first = items.get(0);
        assertEquals(Set.of("id", "description"), Set.copyOf(first.propertyNames()));
        assertEquals(1, first.get("id").asInt());
        assertEquals("11 AGRICULTURA, CRÍA Y EXPLOTACIÓN DE ANIMALES, APROVECHAMIENTO FORESTAL, PESCA Y CAZA",
            first.get("description").asString());
        assertEquals("93 ACTIVIDADES LEGISLATIVAS, GUBERNAMENTALES, DE IMPARTICIÓN DE JUSTICIA Y DE ORGANISMOS "
            + "INTERNACIONALES Y EXTRATERRITORIALES", items.get(19).get("description").asString());
    }

    @Test
    void ADR0015_withoutToken_returns401() throws Exception {
        mockMvc.perform(get(PATH))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("REGISTRATION_TOKEN_INVALID"));
    }
}
