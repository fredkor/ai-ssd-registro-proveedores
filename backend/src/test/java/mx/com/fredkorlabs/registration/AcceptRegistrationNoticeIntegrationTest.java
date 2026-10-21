package mx.com.fredkorlabs.registration;

import mx.com.fredkorlabs.registration.adapter.out.persistence.NoticeAcceptanceJpaEntity;
import mx.com.fredkorlabs.registration.adapter.out.persistence.NoticeAcceptanceJpaRepository;
import mx.com.fredkorlabs.registration.testsupport.PostgresTestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(PostgresTestcontainersConfiguration.class)
@SpringBootTest(classes = SupplierRegistrationApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class AcceptRegistrationNoticeIntegrationTest {

    private static final Pattern ACCEPTANCE_ID = Pattern.compile("\"acceptanceId\":\"([^\"]+)\"");
    private static final Pattern ACCEPTED_AT = Pattern.compile("\"acceptedAt\":\"([^\"]+)\"");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NoticeAcceptanceJpaRepository repository;

    @Test
    void US03_CA003_acceptRegistrationNotice_persistsAcceptanceBeforeResponding() throws Exception {
        Instant before = Instant.now();

        MvcResult result = mockMvc.perform(post("/api/v1/registration-notice-acceptances")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"version\":\"1.0\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.version").value("1.0"))
            .andReturn();

        String body = result.getResponse().getContentAsString();
        UUID id = UUID.fromString(extract(ACCEPTANCE_ID, body));
        Instant acceptedAt = Instant.parse(extract(ACCEPTED_AT, body));

        NoticeAcceptanceJpaEntity stored = repository.findById(id).orElseThrow();
        assertEquals("1.0", stored.getVersion());
        assertEquals(acceptedAt, stored.getAcceptedAt());
        assertFalse(acceptedAt.isBefore(before.minusSeconds(1)));
        assertTrue(extract(ACCEPTED_AT, body).endsWith("Z"));
    }

    @Test
    void US03_CA004_acceptRegistrationNotice_invalidVersion_persistsNothing() throws Exception {
        long count = repository.count();

        mockMvc.perform(post("/api/v1/registration-notice-acceptances")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"version\":\"2.0\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        assertEquals(count, repository.count());
    }

    private static String extract(Pattern pattern, String body) {
        Matcher matcher = pattern.matcher(body);
        assertTrue(matcher.find(), "missing value in " + body);
        return matcher.group(1);
    }
}
