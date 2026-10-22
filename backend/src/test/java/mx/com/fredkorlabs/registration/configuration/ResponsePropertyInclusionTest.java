package mx.com.fredkorlabs.registration.configuration;

import mx.com.fredkorlabs.registration.SupplierRegistrationApplication;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.LegalOwnershipOptionResponse;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.RegistrationExistenceResponse;
import mx.com.fredkorlabs.registration.testsupport.PostgresTestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.*;

/** Optional response properties are omitted without a value; required nullable ones are sent as null. */
@Import(PostgresTestcontainersConfiguration.class)
@SpringBootTest(classes = SupplierRegistrationApplication.class)
class ResponsePropertyInclusionTest {

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void US02_CA001_optionalPropertiesWithoutValue_areOmitted() {
        assertEquals("{\"exists\":false}", jsonMapper.writeValueAsString(new RegistrationExistenceResponse(false)));
    }

    @Test
    void US08_CA014_requiredNullableProperty_isSentAsNull() {
        String json = jsonMapper.writeValueAsString(
            new LegalOwnershipOptionResponse("NOT_DEF", 99, "RNS.LEGALSTRUCTURE/NOTDEFINED", null));

        assertTrue(json.contains("\"countryCode\":null"), json);
        assertFalse(json.contains("taxRegime"), json);
    }
}
