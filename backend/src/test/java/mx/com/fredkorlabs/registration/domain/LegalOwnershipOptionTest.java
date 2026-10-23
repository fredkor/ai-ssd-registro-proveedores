package mx.com.fredkorlabs.registration.domain;

import mx.com.fredkorlabs.registration.domain.model.LegalOwnershipOption;
import mx.com.fredkorlabs.registration.domain.model.Origin;
import mx.com.fredkorlabs.registration.domain.model.TaxRegime;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LegalOwnershipOptionTest {

    private static final LegalOwnershipOption MX_MORAL =
        new LegalOwnershipOption(1, "MX00001", "1.- SOCIEDAD ANÓNIMA (S.A.)", "MX", TaxRegime.LEGAL_ENTITY);
    private static final LegalOwnershipOption MX_FISICA =
        new LegalOwnershipOption(6, "MX00006", "4.- PERSONA FÍSICA CON ACTIVIDAD EMPRESARIAL", "MX", TaxRegime.INDIVIDUAL);
    private static final LegalOwnershipOption AT_SECOND =
        new LegalOwnershipOption(123, "AT00002", "GESELLSCHAFT BÜRGERLICHEN RECHTS (GBR)", "AT", null);
    private static final LegalOwnershipOption AT_FIRST =
        new LegalOwnershipOption(122, "AT00001", "EINZELUNTERNEHMEN", "AT", null);
    private static final LegalOwnershipOption NOT_DEF =
        new LegalOwnershipOption(99, "NOT_DEF", "RNS.LEGALSTRUCTURE/NOTDEFINED", null, null);
    private static final List<LegalOwnershipOption> CANDIDATES = List.of(NOT_DEF, MX_FISICA, AT_SECOND, MX_MORAL, AT_FIRST);

    @Test
    void US08_CA003_national_keepsOnlyCountryAndRegime() {
        assertEquals(List.of(MX_MORAL),
            LegalOwnershipOption.applicable(CANDIDATES, Origin.NATIONAL, "MX", TaxRegime.LEGAL_ENTITY));
        assertEquals(List.of(MX_FISICA),
            LegalOwnershipOption.applicable(CANDIDATES, Origin.NATIONAL, "MX", TaxRegime.INDIVIDUAL));
    }

    @Test
    void US08_CA014_foreign_keepsCountryPlusNotDefOrderedByKey() {
        assertEquals(List.of(AT_FIRST, AT_SECOND, NOT_DEF),
            LegalOwnershipOption.applicable(CANDIDATES, Origin.FOREIGN, "AT", null));
    }

    @Test
    void US08_CA014_foreign_ignoresRegime() {
        assertEquals(List.of(MX_MORAL, MX_FISICA, NOT_DEF),
            LegalOwnershipOption.applicable(CANDIDATES, Origin.FOREIGN, "MX", null));
    }

    @Test
    void US08_CA014_foreignCountryWithoutOptions_getsOnlyNotDef() {
        assertEquals(List.of(NOT_DEF), LegalOwnershipOption.applicable(CANDIDATES, Origin.FOREIGN, "AF", null));
    }

    @Test
    void US08_CA003_nationalCountryWithoutOptions_isEmpty() {
        assertTrue(LegalOwnershipOption.applicable(CANDIDATES, Origin.NATIONAL, "AF", TaxRegime.LEGAL_ENTITY).isEmpty());
    }

    @Test
    void US08_CA003_notDef_neverAppliesToNational() {
        assertFalse(NOT_DEF.appliesTo(Origin.NATIONAL, "MX", TaxRegime.LEGAL_ENTITY));
        assertTrue(NOT_DEF.isNotDefined());
        assertFalse(MX_MORAL.isNotDefined());
    }
}
