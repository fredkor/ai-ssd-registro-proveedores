package mx.com.fredkorlabs.registration.adapter.in.web;

import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.GeneralDataRequest;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.SignatureResultRequest;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.*;

class EditableTextFieldsTest {

    private static JsonNode normalized(String json, Class<?> model) {
        JsonNode tree = JsonMapper.shared().readTree(json);
        EditableTextFields.normalize(tree, model);
        return tree;
    }

    @Test
    void US08_CA025_editableTexts_areTrimmedKeepingInnerSpaces() {
        JsonNode tree = normalized("""
            {"nationality":"  Alemana del Norte \\t","foreignFiscalCode":" DE 123 ","website":" https://empresa.com ",
             "phones":[{"number":" 5512345678 ","extension":" 12 "}]}""", GeneralDataRequest.class);

        assertEquals("Alemana del Norte", tree.get("nationality").asString());
        assertEquals("DE 123", tree.get("foreignFiscalCode").asString());
        assertEquals("https://empresa.com", tree.get("website").asString());
        assertEquals("5512345678", tree.get("phones").get(0).get("number").asString());
        assertEquals("12", tree.get("phones").get(0).get("extension").asString());
    }

    @Test
    void US08_CA026_blankTexts_becomeNull() {
        JsonNode tree = normalized("""
            {"nationality":"   ","foreignFiscalCode":"","website":"  ","phones":[{"number":" ","extension":"  "}]}""",
            GeneralDataRequest.class);

        assertTrue(tree.get("nationality").isNull());
        assertTrue(tree.get("foreignFiscalCode").isNull());
        assertTrue(tree.get("website").isNull());
        assertTrue(tree.get("phones").get(0).get("number").isNull());
        assertTrue(tree.get("phones").get(0).get("extension").isNull());
    }

    @Test
    void US08_CA027_identifiersAndNonTextValues_areNotTouched() {
        JsonNode tree = normalized("""
            {"origin":" NATIONAL ","countryId":151,"nationality":5,"phones":null}""", GeneralDataRequest.class);

        assertEquals(" NATIONAL ", tree.get("origin").asString());
        assertEquals(151, tree.get("countryId").asInt());
        assertEquals(5, tree.get("nationality").asInt());
        assertTrue(tree.get("phones").isNull());
    }

    @Test
    void US08_CA027_otherRequests_areNotNormalized() {
        assertFalse(EditableTextFields.apply(SignatureResultRequest.class));
        JsonNode tree = normalized("{\"rfc\":\" AAA010101AAA \"}", SignatureResultRequest.class);

        assertEquals(" AAA010101AAA ", tree.get("rfc").asString());
    }
}
