package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.GetLegalOwnershipsQuery;
import mx.com.fredkorlabs.registration.application.port.in.GetLegalOwnershipsResult;
import mx.com.fredkorlabs.registration.application.port.in.GetLegalOwnershipsResult.LegalOwnershipItem;
import mx.com.fredkorlabs.registration.application.port.in.InvalidQueryParameterException;
import mx.com.fredkorlabs.registration.domain.model.LegalOwnershipOption;
import mx.com.fredkorlabs.registration.domain.model.TaxRegime;
import mx.com.fredkorlabs.registration.testsupport.UnusedGeneralDataCatalog;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GetLegalOwnershipsServiceTest {

    private final FakeCatalog catalog = new FakeCatalog();
    private final GetLegalOwnershipsService service = new GetLegalOwnershipsService(catalog);

    private GetLegalOwnershipsResult query(String origin, String countryCode, String taxRegime) {
        return service.execute(new GetLegalOwnershipsQuery(origin, countryCode, taxRegime));
    }

    @Test
    void US08_CA003_national_returnsCountryAndRegimeOptionsWithRegimeCode() {
        assertEquals(List.of(new LegalOwnershipItem(1, "MX00001", "SOCIEDAD ANÓNIMA", "MX", "LEGAL_ENTITY")),
            query("NATIONAL", "MX", "LEGAL_ENTITY").items());
        assertEquals(List.of("MX"), catalog.candidatesFor);
    }

    @Test
    void US08_CA014_foreign_returnsCountryPlusNotDefWithNullCountryAndRegime() {
        assertEquals(List.of(new LegalOwnershipItem(122, "AT00001", "EINZELUNTERNEHMEN", "AT", null),
                new LegalOwnershipItem(99, "NOT_DEF", "RNS.LEGALSTRUCTURE/NOTDEFINED", null, null)),
            query("FOREIGN", "AT", null).items());
    }

    @Test
    void ADR0020_national_withoutTaxRegime_isRejected() {
        InvalidQueryParameterException error =
            assertThrows(InvalidQueryParameterException.class, () -> query("NATIONAL", "MX", null));
        assertEquals("taxRegime", error.getParameter());
        assertTrue(catalog.candidatesFor.isEmpty());
    }

    @Test
    void ADR0020_foreign_withTaxRegime_isRejected() {
        InvalidQueryParameterException error =
            assertThrows(InvalidQueryParameterException.class, () -> query("FOREIGN", "AT", "INDIVIDUAL"));
        assertEquals("taxRegime", error.getParameter());
    }

    @Test
    void ADR0020_unknownCountryCode_isRejected() {
        InvalidQueryParameterException error =
            assertThrows(InvalidQueryParameterException.class, () -> query("FOREIGN", "ZZ", null));
        assertEquals("countryCode", error.getParameter());
        assertTrue(catalog.candidatesFor.isEmpty());
    }

    private static final class FakeCatalog extends UnusedGeneralDataCatalog {
        private final List<String> candidatesFor = new ArrayList<>();

        @Override
        public boolean countryExists(String countryCode) {
            return Set.of("MX", "AT", "AF").contains(countryCode);
        }

        @Override
        public List<LegalOwnershipOption> findLegalOwnershipCandidates(String countryCode) {
            candidatesFor.add(countryCode);
            return List.of(
                new LegalOwnershipOption(99, "NOT_DEF", "RNS.LEGALSTRUCTURE/NOTDEFINED", null, null),
                new LegalOwnershipOption(6, "MX00006", "PERSONA FÍSICA", "MX", TaxRegime.INDIVIDUAL),
                new LegalOwnershipOption(1, "MX00001", "SOCIEDAD ANÓNIMA", "MX", TaxRegime.LEGAL_ENTITY),
                new LegalOwnershipOption(122, "AT00001", "EINZELUNTERNEHMEN", "AT", null));
        }
    }
}
