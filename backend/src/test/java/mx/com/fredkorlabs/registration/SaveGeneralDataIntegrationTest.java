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
import tools.jackson.databind.node.ObjectNode;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static mx.com.fredkorlabs.registration.testsupport.SignatureFixtures.identifyRequest;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** End to end: Next validates and saves General Data and its progress in one transaction (US08, US09, US10). */
@Import({PostgresTestcontainersConfiguration.class, DocumentsVolumeTestConfiguration.class})
@SpringBootTest(classes = SupplierRegistrationApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class SaveGeneralDataIntegrationTest {

    private static final byte[] PDF = "%PDF-1.7\n%%EOF".getBytes(StandardCharsets.US_ASCII);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SupplierRegistrationJpaRepository registrations;

    @Autowired
    private JdbcTemplate jdbc;

    private record Session(String token, UUID registrationId) {}

    @AfterEach
    void removeRegistrations() {
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

    private Session registration(String variant, String rfc, boolean withDocument) throws Exception {
        String token = identify(variant, rfc);
        UUID registrationId = UUID.fromString(JsonMapper.shared().readTree(mockMvc.perform(
                post("/api/v1/supplier-registrations").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asString());
        if (withDocument) {
            mockMvc.perform(multipart("/api/v1/supplier-registrations/" + registrationId + "/fiscal-document")
                    .file(new MockMultipartFile("file", "cedula.pdf", "application/pdf", PDF))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
        }
        return new Session(token, registrationId);
    }

    private Session legalEntity() throws Exception {
        return registration("legal-entity", "AAA010101AAA", true);
    }

    private Session individual() throws Exception {
        return registration("individual", "HEGT7610034S2", true);
    }

    /** A valid national legal entity submission; tests change single properties on top of it. */
    private static ObjectNode nationalLegalEntity() {
        return (ObjectNode) JsonMapper.shared().readTree("""
            {"origin":"NATIONAL","countryId":151,"nationality":"Mexicana","foreignFiscalCode":null,
             "legalOwnershipId":1,"sectorId":5,"subsectorId":21,"businessActivityId":1,"stratificationId":2,
             "primaryActivityId":3,"workerCountRangeId":4,"annualSalesRangeId":2,"website":"https://empresa.com",
             "phones":[{"number":"5512345678","extension":"12"},{"number":"0012345678","extension":null}]}""");
    }

    /** A valid foreign individual submission: sector, subsector and phones are sent as null. */
    private static ObjectNode foreignIndividual() {
        return (ObjectNode) JsonMapper.shared().readTree("""
            {"origin":"FOREIGN","countryId":4,"nationality":"Alemana","foreignFiscalCode":"DE123456789",
             "legalOwnershipId":99,"sectorId":null,"subsectorId":null,"businessActivityId":1,"stratificationId":2,
             "primaryActivityId":3,"workerCountRangeId":4,"annualSalesRangeId":2,"website":null,"phones":null}""");
    }

    private ResultActions save(Session session, JsonNode body) throws Exception {
        return save(session.registrationId(), session.token(), body.toString());
    }

    private ResultActions save(Object registrationId, String token, String body) throws Exception {
        return mockMvc.perform(put("/api/v1/supplier-registrations/" + registrationId + "/general-data")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private ResultActions resume(Session session, String variant, String rfc) throws Exception {
        return mockMvc.perform(post("/api/v1/supplier-registrations/" + session.registrationId() + "/resume")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + identify(variant, rfc)));
    }

    private String progress(UUID registrationId, String section) {
        return jdbc.queryForObject("SELECT status FROM section_progress WHERE registration_id = ? AND section = ?",
            String.class, registrationId, section);
    }

    private static void assertValidationError(ResultActions result, String... pointers) throws Exception {
        result.andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        JsonNode errors = JsonMapper.shared().readTree(result.andReturn().getResponse().getContentAsString()).get("errors");
        List<String> actual = new ArrayList<>();
        errors.forEach(error -> actual.add(error.get("pointer").asString()));
        assertEquals(List.of(pointers), actual);
    }

    @Test
    void US08_CA006_validSection_returnsCompletedAndContinuesToCucop() throws Exception {
        Session session = legalEntity();

        save(session, nationalLegalEntity())
            .andExpect(status().isOk())
            .andExpect(content().json("{\"registrationId\":\"" + session.registrationId() + "\",\"section\":\"GENERAL_DATA\","
                + "\"status\":\"COMPLETED\",\"nextSection\":\"CUCOP\"}", JsonCompareMode.STRICT));

        Map<String, Object> row = jdbc.queryForMap("SELECT * FROM general_data WHERE registration_id = ?",
            session.registrationId());
        assertEquals(1, row.get("origin_id"));
        assertEquals(151, row.get("country_id"));
        assertEquals("Mexicana", row.get("nationality"));
        assertEquals("https://empresa.com", row.get("website"));
        assertEquals(List.of("5512345678", "0012345678"), jdbc.queryForList(
            "SELECT number FROM registration_phone WHERE registration_id = ? ORDER BY position", String.class,
            session.registrationId()));
        assertEquals("COMPLETED", progress(session.registrationId(), "GENERAL_DATA"));
    }

    @Test
    void US08_CA010_foreignIndividual_isSavedWithoutSectorNorPhones() throws Exception {
        Session session = individual();

        save(session, foreignIndividual()).andExpect(status().isOk());

        Map<String, Object> row = jdbc.queryForMap("SELECT * FROM general_data WHERE registration_id = ?",
            session.registrationId());
        assertEquals(2, row.get("origin_id"));
        assertEquals("DE123456789", row.get("foreign_fiscal_code"));
        assertNull(row.get("sector_id"));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM registration_phone WHERE registration_id = ?",
            Integer.class, session.registrationId()));
    }

    @Test
    void US06_CA005_resume_showsTheSavedGeneralData() throws Exception {
        Session moral = legalEntity();
        save(moral, nationalLegalEntity()).andExpect(status().isOk());

        resume(moral, "legal-entity", "AAA010101AAA")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.generalData.origin").value("NATIONAL"))
            .andExpect(jsonPath("$.generalData.countryId").value(151))
            .andExpect(jsonPath("$.generalData.sectorId").value(5))
            .andExpect(jsonPath("$.generalData.website").value("https://empresa.com"))
            .andExpect(jsonPath("$.generalData.phones[1].number").value("0012345678"))
            .andExpect(jsonPath("$.progress[0].status").value("COMPLETED"));
    }

    @Test
    void US09_CA001_resume_ofIndividual_omitsSectorAndPhones() throws Exception {
        Session session = individual();
        save(session, foreignIndividual()).andExpect(status().isOk());

        resume(session, "individual", "HEGT7610034S2")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.generalData.foreignFiscalCode").value("DE123456789"))
            .andExpect(jsonPath("$.generalData.sectorId").doesNotExist())
            .andExpect(jsonPath("$.generalData.phones").doesNotExist());
    }

    @Test
    void US08_CA025_editableTexts_areTrimmedBeforeValidating() throws Exception {
        Session session = legalEntity();
        ObjectNode body = nationalLegalEntity();
        body.put("nationality", "  Mexicana \t");
        body.put("website", "   ");
        ((ObjectNode) body.get("phones").get(0)).put("number", " 5512345678 ");
        ((ObjectNode) body.get("phones").get(0)).put("extension", "  ");

        save(session, body).andExpect(status().isOk());

        Map<String, Object> row = jdbc.queryForMap("SELECT * FROM general_data WHERE registration_id = ?",
            session.registrationId());
        assertEquals("Mexicana", row.get("nationality"));
        assertNull(row.get("website"));
        assertNull(jdbc.queryForObject(
            "SELECT extension FROM registration_phone WHERE registration_id = ? AND position = 0", String.class,
            session.registrationId()));
    }

    @Test
    void US08_CA026_blankRequiredText_isValidationError() throws Exception {
        Session session = legalEntity();
        ObjectNode body = nationalLegalEntity();
        body.put("nationality", "   ");

        assertValidationError(save(session, body), "/nationality");
    }

    @Test
    void US08_CA007_everyBrokenRule_isReportedAndNothingIsSaved() throws Exception {
        Session session = legalEntity();
        ObjectNode body = nationalLegalEntity();
        body.put("nationality", "Alemana");
        body.put("website", "empresa.com");
        body.putArray("phones");

        assertValidationError(save(session, body), "/nationality", "/phones", "/website");
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM general_data WHERE registration_id = ?",
            Integer.class, session.registrationId()));
        assertEquals("PENDING", progress(session.registrationId(), "GENERAL_DATA"));
    }

    @Test
    void US08_CA009_nationalOriginWithAnotherCountry_isRejected() throws Exception {
        Session session = legalEntity();
        ObjectNode body = nationalLegalEntity();
        body.put("countryId", 4);

        assertValidationError(save(session, body), "/countryId", "/legalOwnershipId");
    }

    @Test
    void US08_CA003_legalOwnershipOfAnotherRegime_isRejected() throws Exception {
        Session session = legalEntity();
        ObjectNode body = nationalLegalEntity();
        body.put("legalOwnershipId", 6);

        assertValidationError(save(session, body), "/legalOwnershipId");
    }

    @Test
    void US08_CA004_individualSendingSectorAndPhones_isRejected() throws Exception {
        Session session = individual();
        ObjectNode body = foreignIndividual();
        body.put("sectorId", 5);
        body.put("subsectorId", 21);
        body.putArray("phones");

        assertValidationError(save(session, body), "/sectorId", "/subsectorId", "/phones");
    }

    @Test
    void US08_CA015_foreignWithoutFiscalCode_isRejected() throws Exception {
        Session session = individual();
        ObjectNode body = foreignIndividual();
        body.putNull("foreignFiscalCode");

        assertValidationError(save(session, body), "/foreignFiscalCode");
    }

    @Test
    void US08_CA018_foreignFiscalCodeOver20Characters_isRejected() throws Exception {
        Session session = individual();
        ObjectNode body = foreignIndividual();
        body.put("foreignFiscalCode", "X".repeat(21));

        assertValidationError(save(session, body), "/foreignFiscalCode");
    }

    @Test
    void US08_CA013_websiteWithoutProtocol_isRejected() throws Exception {
        Session session = legalEntity();
        ObjectNode body = nationalLegalEntity();
        body.put("website", "empresa.com");

        assertValidationError(save(session, body), "/website");
    }

    @Test
    void US08_CA008_inactiveStratification_isRejected() throws Exception {
        Session session = legalEntity();
        ObjectNode body = nationalLegalEntity();
        body.put("stratificationId", 5);

        assertValidationError(save(session, body), "/stratificationId");
    }

    @Test
    void US09_CA003_invalidPhoneRows_areRejectedWithTheirPointers() throws Exception {
        Session session = legalEntity();
        ObjectNode body = nationalLegalEntity();
        ((ObjectNode) body.get("phones").get(1)).put("number", "55123");
        ((ObjectNode) body.get("phones").get(0)).put("extension", "123456");

        save(session, body)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.errors[*].pointer", containsInAnyOrder(
                "/phones/0/extension", "/phones/1/number")));
    }

    @Test
    void US09_CA004_legalEntityWithoutPhones_isRejected() throws Exception {
        Session session = legalEntity();
        ObjectNode body = nationalLegalEntity();
        body.putNull("phones");

        assertValidationError(save(session, body), "/phones");
    }

    @Test
    void US08_CA024_omittedOrUndefinedProperty_isValidationError() throws Exception {
        Session session = legalEntity();
        ObjectNode withoutWebsite = nationalLegalEntity();
        withoutWebsite.remove("website");
        ObjectNode withExtra = nationalLegalEntity();
        withExtra.put("comments", "x");

        assertValidationError(save(session, withoutWebsite), "/website");
        assertValidationError(save(session, withExtra), "/comments");
    }

    @Test
    void US10_CA003_withoutFiscalDocument_isRejectedWithoutFieldErrors() throws Exception {
        Session session = registration("legal-entity", "AAA010101AAA", false);

        save(session, nationalLegalEntity())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.errors").isEmpty());
        assertEquals("PENDING", progress(session.registrationId(), "GENERAL_DATA"));
    }

    @Test
    void US06_CA006_savingGeneralData_leavesCucopProgressIntact() throws Exception {
        Session session = legalEntity();
        jdbc.update("UPDATE section_progress SET status = 'COMPLETED' WHERE registration_id = ? AND section = 'CUCOP'",
            session.registrationId());

        save(session, nationalLegalEntity()).andExpect(status().isOk());

        assertEquals("COMPLETED", progress(session.registrationId(), "CUCOP"));
    }

    @Test
    void US08_CA021_nullOptional_isSavedWithoutValueInsteadOfKeepingThePrevious() throws Exception {
        Session session = legalEntity();
        save(session, nationalLegalEntity()).andExpect(status().isOk());
        ObjectNode withoutWebsite = nationalLegalEntity();
        withoutWebsite.putNull("website");

        save(session, withoutWebsite).andExpect(status().isOk());

        assertNull(jdbc.queryForObject("SELECT website FROM general_data WHERE registration_id = ?", String.class,
            session.registrationId()));
        assertEquals("COMPLETED", progress(session.registrationId(), "GENERAL_DATA"));
    }

    @Test
    void US08_registeredRegistration_returns409() throws Exception {
        Session session = legalEntity();
        jdbc.update("UPDATE supplier_registration SET status = 'REGISTERED' WHERE id = ?", session.registrationId());

        save(session, nationalLegalEntity())
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("INVALID_STATE_TRANSITION"));
    }

    @Test
    void US08_registrationOfAnotherRfc_returns403() throws Exception {
        Session owner = individual();

        save(owner.registrationId(), identify("legal-entity", "AAA010101AAA"), nationalLegalEntity().toString())
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("REGISTRATION_ACCESS_DENIED"));
    }

    @Test
    void US08_unknownRegistration_returns404() throws Exception {
        save(UUID.randomUUID(), identify("legal-entity", "AAA010101AAA"), nationalLegalEntity().toString())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("REGISTRATION_NOT_FOUND"));
    }

    @Test
    void ADR0019_registrationIdNotUuid_returns400WithParameter() throws Exception {
        save("not-a-uuid", identify("legal-entity", "AAA010101AAA"), nationalLegalEntity().toString())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[0].parameter").value("registrationId"));
    }

    @Test
    void ADR0015_withoutToken_returns401() throws Exception {
        mockMvc.perform(put("/api/v1/supplier-registrations/" + UUID.randomUUID() + "/general-data")
                .contentType(MediaType.APPLICATION_JSON).content(nationalLegalEntity().toString()))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("REGISTRATION_TOKEN_INVALID"));
    }
}
