package mx.com.fredkorlabs.registration;

import mx.com.fredkorlabs.registration.adapter.out.persistence.SupplierRegistrationJpaRepository;
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
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static mx.com.fredkorlabs.registration.testsupport.SignatureFixtures.identifyRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** End to end: Next validates and saves the CUCoP selection and its progress in one transaction (US11, US06). */
@Import({PostgresTestcontainersConfiguration.class, DocumentsVolumeTestConfiguration.class})
@SpringBootTest(classes = SupplierRegistrationApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class SaveCucopParticipationIntegrationTest {

    private static final byte[] PDF = "%PDF-1.7\n%%EOF".getBytes(StandardCharsets.US_ASCII);
    private static final String GENERAL_DATA = """
        {"origin":"NATIONAL","countryId":151,"nationality":"Mexicana","foreignFiscalCode":null,
         "legalOwnershipId":1,"sectorId":5,"subsectorId":21,"businessActivityId":1,"stratificationId":2,
         "primaryActivityId":3,"workerCountRangeId":4,"annualSalesRangeId":2,"website":null,
         "phones":[{"number":"5512345678","extension":null}]}""";
    private static final String BOTH_TYPES_TWO_ITEMS = """
        {"participationTypes":["PUBLIC_WORKS_CONTRACTOR","GOODS_AND_SERVICES_SUPPLIER"],"specificItemIds":[2,1]}""";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SupplierRegistrationJpaRepository registrations;

    @Autowired
    private JdbcTemplate jdbc;

    private record Session(String token, UUID registrationId) {}

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

    private String identify(String variant, String rfc) throws Exception {
        String acceptance = JsonMapper.shared().readTree(mockMvc.perform(post("/api/v1/registration-notice-acceptances")
                .contentType(MediaType.APPLICATION_JSON).content("{\"version\":\"1.0\"}"))
            .andReturn().getResponse().getContentAsString()).get("acceptanceId").asString();
        return JsonMapper.shared().readTree(mockMvc.perform(post("/api/v1/applicant-identifications")
                .contentType(MediaType.APPLICATION_JSON).content(identifyRequest(acceptance, variant, rfc)))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("registrationToken").asString();
    }

    /** A legal entity registration with its fiscal document and, optionally, General Data saved. */
    private Session registration(boolean withGeneralData) throws Exception {
        String token = identify("legal-entity", "AAA010101AAA");
        UUID registrationId = UUID.fromString(JsonMapper.shared().readTree(mockMvc.perform(
                post("/api/v1/supplier-registrations").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asString());
        mockMvc.perform(multipart("/api/v1/supplier-registrations/" + registrationId + "/fiscal-document")
                .file(new MockMultipartFile("file", "cedula.pdf", "application/pdf", PDF))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk());
        if (withGeneralData) {
            mockMvc.perform(put("/api/v1/supplier-registrations/" + registrationId + "/general-data")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON).content(GENERAL_DATA))
                .andExpect(status().isOk());
        }
        return new Session(token, registrationId);
    }

    private ResultActions save(Session session, String body) throws Exception {
        return save(session.registrationId(), session.token(), body);
    }

    private ResultActions save(Object registrationId, String token, String body) throws Exception {
        return mockMvc.perform(put("/api/v1/supplier-registrations/" + registrationId + "/cucop")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private String progress(UUID registrationId, String section) {
        return jdbc.queryForObject("SELECT status FROM section_progress WHERE registration_id = ? AND section = ?",
            String.class, registrationId, section);
    }

    private List<Integer> storedItems(UUID registrationId) {
        return jdbc.queryForList("SELECT specific_item_id FROM registration_cucop_item WHERE registration_id = ? "
            + "ORDER BY specific_item_id", Integer.class, registrationId);
    }

    private List<String> storedTypes(UUID registrationId) {
        return jdbc.queryForList("SELECT participation_type FROM registration_participation_type "
            + "WHERE registration_id = ? ORDER BY participation_type", String.class, registrationId);
    }

    private static void assertValidationError(ResultActions result, String... pointers) throws Exception {
        result.andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        JsonNode errors = JsonMapper.shared().readTree(result.andReturn().getResponse().getContentAsString()).get("errors");
        List<String> actual = new ArrayList<>();
        errors.forEach(error -> actual.add(error.get("pointer").asString()));
        // Bean Validation reports field errors in no fixed order.
        assertEquals(Stream.of(pointers).sorted().toList(), actual.stream().sorted().toList());
    }

    private static void assertProblem(ResultActions result, int status, String code) throws Exception {
        result.andExpect(status().is(status))
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.code").value(code));
    }

    @Test
    void US11_CA011_validSelection_isSavedCompletedAndContinuesToContacts() throws Exception {
        Session session = registration(true);

        save(session, BOTH_TYPES_TWO_ITEMS)
            .andExpect(status().isOk())
            .andExpect(content().json("{\"registrationId\":\"" + session.registrationId() + "\",\"section\":\"CUCOP\","
                + "\"status\":\"COMPLETED\",\"nextSection\":\"CONTACTS\"}", JsonCompareMode.STRICT));

        assertEquals(List.of("GOODS_AND_SERVICES_SUPPLIER", "PUBLIC_WORKS_CONTRACTOR"),
            storedTypes(session.registrationId()));
        assertEquals(List.of(1, 2), storedItems(session.registrationId()));
        assertEquals("COMPLETED", progress(session.registrationId(), "CUCOP"));
        assertEquals("COMPLETED", progress(session.registrationId(), "GENERAL_DATA"));
    }

    @Test
    void US11_CA011_newSave_replacesTheWholeSelection() throws Exception {
        Session session = registration(true);
        save(session, BOTH_TYPES_TWO_ITEMS).andExpect(status().isOk());

        save(session, "{\"participationTypes\":[\"PUBLIC_WORKS_CONTRACTOR\"],\"specificItemIds\":[7]}")
            .andExpect(status().isOk());

        assertEquals(List.of("PUBLIC_WORKS_CONTRACTOR"), storedTypes(session.registrationId()));
        assertEquals(List.of(7), storedItems(session.registrationId()));
    }

    @Test
    void US05_CA002_resume_returnsEverySavedSectionWithItsValues() throws Exception {
        Session session = registration(true);
        save(session, BOTH_TYPES_TWO_ITEMS).andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/supplier-registrations/" + session.registrationId() + "/resume")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + identify("legal-entity", "AAA010101AAA")))
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {"id":"%s","status":"IN_PROGRESS",
                 "generalData":{"origin":"NATIONAL","countryId":151,"nationality":"Mexicana","sectorId":5,
                   "subsectorId":21,"phones":[{"number":"5512345678"}]},
                 "fiscalDocument":{"originalName":"cedula.pdf","extension":"pdf","sizeBytes":%d},
                 "cucopParticipation":{"participationTypes":["GOODS_AND_SERVICES_SUPPLIER","PUBLIC_WORKS_CONTRACTOR"],
                   "specificItems":[{"id":1},{"id":2}]}}"""
                .formatted(session.registrationId(), PDF.length), JsonCompareMode.LENIENT))
            .andExpect(jsonPath("$.progress[?(@.section == 'GENERAL_DATA')].status").value("COMPLETED"))
            .andExpect(jsonPath("$.progress[?(@.section == 'CUCOP')].status").value("COMPLETED"))
            .andExpect(jsonPath("$.progress[?(@.section == 'CONTACTS')].status").value("PENDING"));
    }

    @Test
    void US11_CA010_resume_returnsThePersistedSelection() throws Exception {
        Session session = registration(true);
        save(session, BOTH_TYPES_TWO_ITEMS).andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/supplier-registrations/" + session.registrationId() + "/resume")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + identify("legal-entity", "AAA010101AAA")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cucopParticipation").exists())
            .andExpect(content().json("""
                {"cucopParticipation":{
                  "participationTypes":["GOODS_AND_SERVICES_SUPPLIER","PUBLIC_WORKS_CONTRACTOR"],
                  "specificItems":[
                    {"id":1,"key":"21101","description":"MATERIALES Y ÚTILES DE OFICINA","level":4,"parentId":1},
                    {"id":2,"key":"21201","description":"MATERIALES Y ÚTILES DE IMPRESIÓN Y REPRODUCCIÓN","level":4,
                     "parentId":2}]}}""", JsonCompareMode.LENIENT));
    }

    @Test
    void US11_CA010_resumeWithoutCucop_omitsTheSection() throws Exception {
        Session session = registration(true);

        mockMvc.perform(post("/api/v1/supplier-registrations/" + session.registrationId() + "/resume")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + identify("legal-entity", "AAA010101AAA")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cucopParticipation").doesNotExist());
    }

    @Test
    void US11_CA001_withoutGeneralData_returns409AndSavesNothing() throws Exception {
        Session session = registration(false);

        assertProblem(save(session, BOTH_TYPES_TWO_ITEMS), 409, "INVALID_STATE_TRANSITION");
        assertEquals("PENDING", progress(session.registrationId(), "CUCOP"));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM cucop_participation", Integer.class));
    }

    @Test
    void US11_CA001_afterDeletingTheFiscalDocument_returns409() throws Exception {
        Session session = registration(true);
        mockMvc.perform(delete("/api/v1/supplier-registrations/" + session.registrationId() + "/fiscal-document")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + session.token()))
            .andExpect(status().is2xxSuccessful());

        assertProblem(save(session, BOTH_TYPES_TWO_ITEMS), 409, "INVALID_STATE_TRANSITION");
        assertEquals("PENDING", progress(session.registrationId(), "CUCOP"));
    }

    @Test
    void US06_CA006_deletingTheFiscalDocument_keepsTheSavedCucop() throws Exception {
        Session session = registration(true);
        save(session, BOTH_TYPES_TWO_ITEMS).andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/supplier-registrations/" + session.registrationId() + "/fiscal-document")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + session.token()))
            .andExpect(status().is2xxSuccessful());

        assertEquals("COMPLETED", progress(session.registrationId(), "CUCOP"));
        assertEquals(List.of(1, 2), storedItems(session.registrationId()));
    }

    @Test
    void US11_CA004_emptySelections_return400WithBothPointers() throws Exception {
        Session session = registration(true);

        assertValidationError(save(session, "{\"participationTypes\":[],\"specificItemIds\":[]}"),
            "/participationTypes", "/specificItemIds");
        assertEquals("PENDING", progress(session.registrationId(), "CUCOP"));
    }

    @Test
    void US11_CA012_idsThatAreNotSpecificItems_return400WithTheirIndexes() throws Exception {
        Session session = registration(true);

        // 361 is a generic item without a specific item of the same id; 999999 is in no level.
        assertValidationError(save(session,
                "{\"participationTypes\":[\"GOODS_AND_SERVICES_SUPPLIER\"],\"specificItemIds\":[1,361,999999]}"),
            "/specificItemIds/1", "/specificItemIds/2");
        assertEquals("PENDING", progress(session.registrationId(), "CUCOP"));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM cucop_participation", Integer.class));
    }

    @Test
    void INV31_repeatedSpecificItem_returns400WithTheRepetitionPointer() throws Exception {
        Session session = registration(true);

        assertValidationError(save(session,
                "{\"participationTypes\":[\"GOODS_AND_SERVICES_SUPPLIER\"],\"specificItemIds\":[1,2,1]}"),
            "/specificItemIds/2");
    }

    @Test
    void INV31_repeatedParticipationType_returns400WithTheRepetitionPointer() throws Exception {
        Session session = registration(true);

        assertValidationError(save(session, "{\"participationTypes\":[\"GOODS_AND_SERVICES_SUPPLIER\","
                + "\"GOODS_AND_SERVICES_SUPPLIER\"],\"specificItemIds\":[1]}"),
            "/participationTypes/1");
    }

    @Test
    void ADR0009_missingProperty_returns400WithItsPointer() throws Exception {
        assertValidationError(save(registration(true), "{\"participationTypes\":[\"GOODS_AND_SERVICES_SUPPLIER\"]}"),
            "/specificItemIds");
    }

    @Test
    void ADR0009_unknownProperty_returns400() throws Exception {
        assertValidationError(save(registration(true), "{\"participationTypes\":[\"GOODS_AND_SERVICES_SUPPLIER\"],"
                + "\"specificItemIds\":[1],\"genericItemIds\":[1]}"),
            "/genericItemIds");
    }

    @Test
    void ADR0019_unknownParticipationType_returns400() throws Exception {
        assertValidationError(save(registration(true),
                "{\"participationTypes\":[\"OTHER\"],\"specificItemIds\":[1]}"),
            "/participationTypes/0");
    }

    @Test
    void US11_registeredRegistration_returns409() throws Exception {
        Session session = registration(true);
        jdbc.update("UPDATE supplier_registration SET status = 'REGISTERED' WHERE id = ?", session.registrationId());

        assertProblem(save(session, BOTH_TYPES_TWO_ITEMS), 409, "INVALID_STATE_TRANSITION");
    }

    @Test
    void US11_registrationOfAnotherRfc_returns403() throws Exception {
        Session session = registration(true);

        assertProblem(save(session.registrationId(), identify("individual", "HEGT7610034S2"), BOTH_TYPES_TWO_ITEMS),
            403, "REGISTRATION_ACCESS_DENIED");
    }

    @Test
    void US11_unknownRegistration_returns404() throws Exception {
        assertProblem(save(UUID.randomUUID(), identify("legal-entity", "AAA010101AAA"), BOTH_TYPES_TWO_ITEMS),
            404, "REGISTRATION_NOT_FOUND");
    }

    @Test
    void ADR0019_registrationIdNotUuid_returns400WithParameter() throws Exception {
        save("not-a-uuid", identify("legal-entity", "AAA010101AAA"), BOTH_TYPES_TWO_ITEMS)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[0].parameter").value("registrationId"));
    }

    @Test
    void ADR0015_withoutToken_returns401() throws Exception {
        mockMvc.perform(put("/api/v1/supplier-registrations/" + UUID.randomUUID() + "/cucop")
                .contentType(MediaType.APPLICATION_JSON).content(BOTH_TYPES_TWO_ITEMS))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("REGISTRATION_TOKEN_INVALID"));
    }
}
