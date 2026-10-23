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

/** A failure after writing keeps the previously saved selection and progress (US11 CA-012, US06 CA-004). */
@Import({PostgresTestcontainersConfiguration.class, DocumentsVolumeTestConfiguration.class})
@SpringBootTest(classes = SupplierRegistrationApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class SaveCucopParticipationRollbackTest {

    private static final String GENERAL_DATA = """
        {"origin":"NATIONAL","countryId":151,"nationality":"Mexicana","foreignFiscalCode":null,
         "legalOwnershipId":1,"sectorId":5,"subsectorId":21,"businessActivityId":1,"stratificationId":2,
         "primaryActivityId":3,"workerCountRangeId":4,"annualSalesRangeId":2,"website":null,
         "phones":[{"number":"5512345678","extension":null}]}""";

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
        jdbc.update("DELETE FROM registration_cucop_item");
        jdbc.update("DELETE FROM registration_participation_type");
        jdbc.update("DELETE FROM cucop_participation");
        jdbc.update("DELETE FROM registration_phone");
        jdbc.update("DELETE FROM general_data");
        jdbc.update("DELETE FROM registration_fiscal_document");
        jdbc.update("DELETE FROM file_metadata");
        registrations.deleteAll();
    }

    @Test
    void US11_CA012_failureAfterWriting_keepsThePreviousSelectionAndProgress() throws Exception {
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
        mockMvc.perform(put("/api/v1/supplier-registrations/" + registrationId + "/general-data")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(GENERAL_DATA))
            .andExpect(status().isOk());
        String path = "/api/v1/supplier-registrations/" + registrationId + "/cucop";
        mockMvc.perform(put(path).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"participationTypes\":[\"GOODS_AND_SERVICES_SUPPLIER\"],\"specificItemIds\":[1,2]}"))
            .andExpect(status().isOk());
        willAnswer(invocation -> {
            invocation.callRealMethod();
            throw new IllegalStateException("Commit failure");
        }).given(persistence).saveCucopParticipation(any());

        mockMvc.perform(put(path).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"participationTypes\":[\"PUBLIC_WORKS_CONTRACTOR\"],\"specificItemIds\":[7]}"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));

        assertEquals(List.of("GOODS_AND_SERVICES_SUPPLIER"), jdbc.queryForList(
            "SELECT participation_type FROM registration_participation_type WHERE registration_id = ?", String.class,
            registrationId));
        assertEquals(List.of(1, 2), jdbc.queryForList(
            "SELECT specific_item_id FROM registration_cucop_item WHERE registration_id = ? ORDER BY specific_item_id",
            Integer.class, registrationId));
        assertEquals("COMPLETED", jdbc.queryForObject(
            "SELECT status FROM section_progress WHERE registration_id = ? AND section = 'CUCOP'",
            String.class, registrationId));
    }
}
