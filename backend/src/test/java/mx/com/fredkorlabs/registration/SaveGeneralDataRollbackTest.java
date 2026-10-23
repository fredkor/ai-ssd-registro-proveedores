package mx.com.fredkorlabs.registration;

import mx.com.fredkorlabs.registration.adapter.out.persistence.SupplierRegistrationJpaRepository;
import mx.com.fredkorlabs.registration.adapter.out.persistence.SupplierRegistrationPersistenceAdapter;
import mx.com.fredkorlabs.registration.testsupport.DocumentsVolumeTestConfiguration;
import mx.com.fredkorlabs.registration.testsupport.PostgresTestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static mx.com.fredkorlabs.registration.testsupport.SignatureFixtures.identifyRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** A failure after writing keeps the previously saved data, phones and progress (US06 CA-004, US09 CA-009). */
@Import({PostgresTestcontainersConfiguration.class, DocumentsVolumeTestConfiguration.class})
@SpringBootTest(classes = SupplierRegistrationApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class SaveGeneralDataRollbackTest {

    private static final String FIRST = """
        {"origin":"NATIONAL","countryId":151,"nationality":"Mexicana","foreignFiscalCode":null,
         "legalOwnershipId":1,"sectorId":5,"subsectorId":21,"businessActivityId":1,"stratificationId":2,
         "primaryActivityId":3,"workerCountRangeId":4,"annualSalesRangeId":2,"website":"https://empresa.com",
         "phones":[{"number":"5512345678","extension":null},{"number":"5587654321","extension":null}]}""";
    private static final String SECOND = """
        {"origin":"NATIONAL","countryId":151,"nationality":"Mexicana","foreignFiscalCode":null,
         "legalOwnershipId":2,"sectorId":5,"subsectorId":21,"businessActivityId":2,"stratificationId":3,
         "primaryActivityId":3,"workerCountRangeId":4,"annualSalesRangeId":2,"website":null,
         "phones":[{"number":"5511111111","extension":null}]}""";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SupplierRegistrationJpaRepository registrations;

    @Autowired
    private JdbcTemplate jdbc;

    @MockitoSpyBean
    private SupplierRegistrationPersistenceAdapter persistence;

    @AfterEach
    void removeRegistrations() {
        jdbc.update("DELETE FROM registration_phone");
        jdbc.update("DELETE FROM general_data");
        jdbc.update("DELETE FROM registration_fiscal_document");
        jdbc.update("DELETE FROM file_metadata");
        registrations.deleteAll();
    }

    @Test
    void US06_CA004_failureAfterWriting_keepsThePreviousDataPhonesAndProgress() throws Exception {
        String acceptance = JsonMapper.shared().readTree(mockMvc.perform(post("/api/v1/registration-notice-acceptances")
                .contentType(MediaType.APPLICATION_JSON).content("{\"version\":\"1.0\"}"))
            .andReturn().getResponse().getContentAsString()).get("acceptanceId").asString();
        String token = JsonMapper.shared().readTree(mockMvc.perform(post("/api/v1/applicant-identifications")
                .contentType(MediaType.APPLICATION_JSON).content(identifyRequest(acceptance, "legal-entity", "AAA010101AAA")))
            .andReturn().getResponse().getContentAsString()).get("registrationToken").asString();
        UUID registrationId = UUID.fromString(JsonMapper.shared().readTree(mockMvc.perform(
                post("/api/v1/supplier-registrations").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andReturn().getResponse().getContentAsString()).get("id").asString());
        mockMvc.perform(multipart("/api/v1/supplier-registrations/" + registrationId + "/fiscal-document")
                .file(new MockMultipartFile("file", "cedula.pdf", "application/pdf",
                    "%PDF-1.7\n%%EOF".getBytes(StandardCharsets.US_ASCII)))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk());
        String path = "/api/v1/supplier-registrations/" + registrationId + "/general-data";
        mockMvc.perform(put(path).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(FIRST))
            .andExpect(status().isOk());
        willAnswer(invocation -> {
            invocation.callRealMethod();
            throw new IllegalStateException("Commit failure");
        }).given(persistence).saveGeneralData(any());

        mockMvc.perform(put(path).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(SECOND))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));

        assertEquals(1, jdbc.queryForObject("SELECT legal_ownership_id FROM general_data WHERE registration_id = ?",
            Integer.class, registrationId));
        assertEquals("https://empresa.com", jdbc.queryForObject(
            "SELECT website FROM general_data WHERE registration_id = ?", String.class, registrationId));
        assertEquals(List.of("5512345678", "5587654321"), jdbc.queryForList(
            "SELECT number FROM registration_phone WHERE registration_id = ? ORDER BY position", String.class,
            registrationId));
        assertEquals("COMPLETED", jdbc.queryForObject(
            "SELECT status FROM section_progress WHERE registration_id = ? AND section = 'GENERAL_DATA'",
            String.class, registrationId));
        assertEquals(1, jdbc.queryForObject(
            "SELECT COUNT(*) FROM registration_fiscal_document WHERE registration_id = ?", Integer.class, registrationId));
    }
}
