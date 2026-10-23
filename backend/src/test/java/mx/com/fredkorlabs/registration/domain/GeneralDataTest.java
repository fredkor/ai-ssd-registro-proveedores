package mx.com.fredkorlabs.registration.domain;

import mx.com.fredkorlabs.registration.domain.exception.InvalidGeneralDataException;
import mx.com.fredkorlabs.registration.domain.model.GeneralData;
import mx.com.fredkorlabs.registration.domain.model.GeneralDataCatalogContext;
import mx.com.fredkorlabs.registration.domain.model.GeneralDataInput;
import mx.com.fredkorlabs.registration.domain.model.GeneralDataInput.PhoneInput;
import mx.com.fredkorlabs.registration.domain.model.GeneralDataViolation;
import mx.com.fredkorlabs.registration.domain.model.LegalOwnershipOption;
import mx.com.fredkorlabs.registration.domain.model.Origin;
import mx.com.fredkorlabs.registration.domain.model.PhoneNumber;
import mx.com.fredkorlabs.registration.domain.model.TaxRegime;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.*;

class GeneralDataTest {

    private static final LegalOwnershipOption MX_MORAL =
        new LegalOwnershipOption(1, "MX00001", "1.- SOCIEDAD ANÓNIMA (S.A.)", "MX", TaxRegime.LEGAL_ENTITY);
    private static final LegalOwnershipOption MX_FISICA =
        new LegalOwnershipOption(6, "MX00006", "4.- PERSONA FÍSICA CON ACTIVIDAD EMPRESARIAL", "MX", TaxRegime.INDIVIDUAL);
    private static final LegalOwnershipOption NOT_DEF =
        new LegalOwnershipOption(99, "NOT_DEF", "RNS.LEGALSTRUCTURE/NOTDEFINED", null, null);

    private static GeneralDataCatalogContext context(String countryCode, LegalOwnershipOption ownership) {
        return new GeneralDataCatalogContext(countryCode, ownership, true, 5, true, true, true, true, true);
    }

    private static GeneralDataInput nationalLegalEntity() {
        return new GeneralDataInput(Origin.NATIONAL, 151, "Mexicana", null, 1, 5, 21, 1, 2, 3, 4, 2,
            "https://empresa.com", List.of(new PhoneInput("5512345678", "123")));
    }

    private static GeneralDataInput foreignIndividual() {
        return new GeneralDataInput(Origin.FOREIGN, 4, "Alemana", "DE123456789", 99, null, null, 1, 2, 3, 4, 2,
            null, null);
    }

    private static List<GeneralDataViolation> violations(GeneralDataInput input, TaxRegime regime,
                                                         GeneralDataCatalogContext context) {
        return assertThrows(InvalidGeneralDataException.class, () -> GeneralData.validate(input, regime, context))
            .violations();
    }

    private static GeneralDataInput with(GeneralDataInput base, UnaryOperator<Object[]> change) {
        Object[] v = {base.origin(), base.countryId(), base.nationality(), base.foreignFiscalCode(),
            base.legalOwnershipId(), base.sectorId(), base.subsectorId(), base.businessActivityId(),
            base.stratificationId(), base.primaryActivityId(), base.workerCountRangeId(), base.annualSalesRangeId(),
            base.website(), base.phones()};
        Object[] c = change.apply(v);
        @SuppressWarnings("unchecked")
        List<PhoneInput> phones = (List<PhoneInput>) c[13];
        return new GeneralDataInput((Origin) c[0], (Integer) c[1], (String) c[2], (String) c[3], (Integer) c[4],
            (Integer) c[5], (Integer) c[6], (Integer) c[7], (Integer) c[8], (Integer) c[9], (Integer) c[10],
            (Integer) c[11], (String) c[12], phones);
    }

    private static GeneralDataInput set(GeneralDataInput base, int index, Object value) {
        return with(base, v -> {
            v[index] = value;
            return v;
        });
    }

    @Test
    void US08_CA006_validNationalLegalEntity_isAccepted() {
        GeneralData data = GeneralData.validate(nationalLegalEntity(), TaxRegime.LEGAL_ENTITY, context("MX", MX_MORAL));

        assertEquals(Origin.NATIONAL, data.origin());
        assertEquals("Mexicana", data.nationality().value());
        assertNull(data.foreignFiscalCode());
        assertEquals(5, data.sectorId());
        assertEquals("https://empresa.com", data.website().value());
        assertEquals(List.of(new PhoneNumber("5512345678", "123")), data.phones());
    }

    @Test
    void US08_CA010_validForeignIndividual_isAcceptedWithoutPhones() {
        GeneralData data = GeneralData.validate(foreignIndividual(), TaxRegime.INDIVIDUAL, context("DE", NOT_DEF));

        assertEquals("DE123456789", data.foreignFiscalCode().value());
        assertNull(data.sectorId());
        assertNull(data.phones());
        assertNull(data.website());
    }

    @Test
    void US08_CA009_nationalOrigin_requiresMexico() {
        assertEquals(List.of(new GeneralDataViolation("countryId", "must be Mexico for NATIONAL origin")),
            violations(set(nationalLegalEntity(), 1, 4), TaxRegime.LEGAL_ENTITY, context("DE", MX_MORAL))
                .stream().filter(v -> v.field().equals("countryId")).toList());
    }

    @Test
    void US08_CA010_unknownCountry_isRejected() {
        assertTrue(violations(set(foreignIndividual(), 1, 999), TaxRegime.INDIVIDUAL, context(null, NOT_DEF))
            .contains(new GeneralDataViolation("countryId", "is not in the country catalog")));
    }

    @Test
    void INV18_nationalOrigin_requiresExactlyMexicana() {
        for (String nationality : List.of("mexicana", "Mexicano", "MEXICANA")) {
            assertEquals(List.of(new GeneralDataViolation("nationality", "must be Mexicana for NATIONAL origin")),
                violations(set(nationalLegalEntity(), 2, nationality), TaxRegime.LEGAL_ENTITY, context("MX", MX_MORAL)));
        }
    }

    @Test
    void INV18_foreignNationality_acceptsUpTo250Characters() {
        assertDoesNotThrow(() -> GeneralData.validate(set(foreignIndividual(), 2, "A".repeat(250)),
            TaxRegime.INDIVIDUAL, context("DE", NOT_DEF)));
        assertEquals(List.of(new GeneralDataViolation("nationality", "must not exceed 250 characters")),
            violations(set(foreignIndividual(), 2, "A".repeat(251)), TaxRegime.INDIVIDUAL, context("DE", NOT_DEF)));
    }

    @Test
    void US08_CA026_missingRequiredNationality_isRejected() {
        assertEquals(List.of(new GeneralDataViolation("nationality", "is required")),
            violations(set(foreignIndividual(), 2, null), TaxRegime.INDIVIDUAL, context("DE", NOT_DEF)));
    }

    @Test
    void US08_CA015_foreignOrigin_requiresForeignFiscalCode() {
        assertEquals(List.of(new GeneralDataViolation("foreignFiscalCode", "is required for FOREIGN origin")),
            violations(set(foreignIndividual(), 3, null), TaxRegime.INDIVIDUAL, context("DE", NOT_DEF)));
    }

    @Test
    void US08_CA018_foreignFiscalCodeOver20Characters_isRejected() {
        assertDoesNotThrow(() -> GeneralData.validate(set(foreignIndividual(), 3, "X".repeat(20)),
            TaxRegime.INDIVIDUAL, context("DE", NOT_DEF)));
        assertEquals(List.of(new GeneralDataViolation("foreignFiscalCode", "must not exceed 20 characters")),
            violations(set(foreignIndividual(), 3, "X".repeat(21)), TaxRegime.INDIVIDUAL, context("DE", NOT_DEF)));
    }

    @Test
    void US08_CA015_nationalOrigin_rejectsForeignFiscalCode() {
        assertEquals(List.of(new GeneralDataViolation("foreignFiscalCode", "must be null for NATIONAL origin")),
            violations(set(nationalLegalEntity(), 3, "ABC"), TaxRegime.LEGAL_ENTITY, context("MX", MX_MORAL)));
    }

    @Test
    void US08_CA003_nationalLegalOwnership_mustMatchCountryAndRegime() {
        assertEquals(List.of(new GeneralDataViolation("legalOwnershipId",
                "does not apply to the selected origin, country and tax regime")),
            violations(set(nationalLegalEntity(), 4, 6), TaxRegime.LEGAL_ENTITY, context("MX", MX_FISICA)));
        assertEquals(List.of(new GeneralDataViolation("legalOwnershipId",
                "does not apply to the selected origin, country and tax regime")),
            violations(set(nationalLegalEntity(), 4, 99), TaxRegime.LEGAL_ENTITY, context("MX", NOT_DEF)));
    }

    @Test
    void US08_CA014_foreignLegalOwnership_acceptsNotDefAndCountryOptionsWithoutRegime() {
        assertDoesNotThrow(() -> GeneralData.validate(foreignIndividual(), TaxRegime.INDIVIDUAL, context("DE", NOT_DEF)));
        assertDoesNotThrow(() -> GeneralData.validate(set(foreignIndividual(), 1, 151), TaxRegime.INDIVIDUAL,
            context("MX", MX_MORAL)));
        LegalOwnershipOption austrian = new LegalOwnershipOption(122, "AT00001", "EINZELUNTERNEHMEN", "AT", null);
        assertEquals(List.of(new GeneralDataViolation("legalOwnershipId",
                "does not apply to the selected origin, country and tax regime")),
            violations(foreignIndividual(), TaxRegime.INDIVIDUAL, context("DE", austrian)));
    }

    @Test
    void US08_CA008_unknownLegalOwnership_isRejected() {
        assertEquals(List.of(new GeneralDataViolation("legalOwnershipId", "is not in the legal ownership catalog")),
            violations(set(nationalLegalEntity(), 4, 9999), TaxRegime.LEGAL_ENTITY, context("MX", null)));
    }

    @Test
    void US08_CA004_individual_mustSendSectorSubsectorAndPhonesAsNull() {
        GeneralDataInput withMoralFields = with(foreignIndividual(), v -> {
            v[5] = 5;
            v[6] = 21;
            v[13] = List.of();
            return v;
        });

        assertEquals(List.of(
                new GeneralDataViolation("sectorId", "must be null for individuals"),
                new GeneralDataViolation("subsectorId", "must be null for individuals"),
                new GeneralDataViolation("phones", "must be null for individuals")),
            violations(withMoralFields, TaxRegime.INDIVIDUAL, context("DE", NOT_DEF)));
    }

    @Test
    void INV21_legalEntity_requiresSectorAndSubsectorOfThatSector() {
        GeneralDataInput withoutSector = with(nationalLegalEntity(), v -> {
            v[5] = null;
            v[6] = null;
            return v;
        });
        assertEquals(List.of(
                new GeneralDataViolation("sectorId", "is required for legal entities"),
                new GeneralDataViolation("subsectorId", "is required for legal entities")),
            violations(withoutSector, TaxRegime.LEGAL_ENTITY, context("MX", MX_MORAL)));

        GeneralDataCatalogContext otherSector =
            new GeneralDataCatalogContext("MX", MX_MORAL, true, 6, true, true, true, true, true);
        assertEquals(List.of(new GeneralDataViolation("subsectorId", "does not belong to the selected sector")),
            violations(nationalLegalEntity(), TaxRegime.LEGAL_ENTITY, otherSector));
    }

    @Test
    void US09_CA004_legalEntity_requiresAtLeastOnePhone() {
        for (List<PhoneInput> phones : Arrays.asList(null, List.<PhoneInput>of())) {
            assertEquals(List.of(new GeneralDataViolation("phones", "must contain at least one phone for legal entities")),
                violations(set(nationalLegalEntity(), 13, phones), TaxRegime.LEGAL_ENTITY, context("MX", MX_MORAL)));
        }
    }

    @Test
    void US09_CA003_invalidPhoneRows_areReportedWithTheirPosition() {
        GeneralDataInput badPhones = set(nationalLegalEntity(), 13, List.of(
            new PhoneInput("5512345678", null),
            new PhoneInput("551234", "123456"),
            new PhoneInput(null, null)));

        assertEquals(List.of(
                new GeneralDataViolation("phones/1/number", "must contain exactly 10 digits"),
                new GeneralDataViolation("phones/1/extension", "must not exceed 5 characters"),
                new GeneralDataViolation("phones/2/number", "is required")),
            violations(badPhones, TaxRegime.LEGAL_ENTITY, context("MX", MX_MORAL)));
    }

    @Test
    void US09_CA006_phoneWithoutExtension_isAccepted() {
        GeneralData data = GeneralData.validate(set(nationalLegalEntity(), 13,
            List.of(new PhoneInput("0012345678", null))), TaxRegime.LEGAL_ENTITY, context("MX", MX_MORAL));

        assertEquals(List.of(new PhoneNumber("0012345678", null)), data.phones());
    }

    @Test
    void US08_CA013_website_mustBeAbsoluteHttpOrHttpsWithHost() {
        for (String website : List.of("empresa.com", "https://", "ftp://empresa.com", "javascript:alert(1)")) {
            assertEquals(List.of(new GeneralDataViolation("website",
                    "must be an absolute http or https URL with a valid host of at most 2048 characters")),
                violations(set(nationalLegalEntity(), 12, website), TaxRegime.LEGAL_ENTITY, context("MX", MX_MORAL)),
                website);
        }
        assertDoesNotThrow(() -> GeneralData.validate(set(nationalLegalEntity(), 12, "http://empresa.com/ruta"),
            TaxRegime.LEGAL_ENTITY, context("MX", MX_MORAL)));
    }

    @Test
    void US08_CA021_optionalWebsiteNull_isSavedWithoutValue() {
        GeneralData data = GeneralData.validate(set(nationalLegalEntity(), 12, null), TaxRegime.LEGAL_ENTITY,
            context("MX", MX_MORAL));

        assertNull(data.website());
    }

    @Test
    void US08_CA008_selectionsMustBeCurrentCatalogEntries() {
        GeneralDataCatalogContext nothingCurrent =
            new GeneralDataCatalogContext("MX", MX_MORAL, false, null, false, false, false, false, false);

        assertEquals(List.of(
                new GeneralDataViolation("sectorId", "is not in the sector catalog"),
                new GeneralDataViolation("subsectorId", "is not in the subsector catalog"),
                new GeneralDataViolation("businessActivityId", "is not a current catalog entry"),
                new GeneralDataViolation("stratificationId", "is not a current catalog entry"),
                new GeneralDataViolation("primaryActivityId", "is not a current catalog entry"),
                new GeneralDataViolation("workerCountRangeId", "is not a current catalog entry"),
                new GeneralDataViolation("annualSalesRangeId", "is not a current catalog entry")),
            violations(nationalLegalEntity(), TaxRegime.LEGAL_ENTITY, nothingCurrent));
    }

    @Test
    void US08_CA007_everyBrokenRule_isReportedTogether() {
        GeneralDataInput broken = with(nationalLegalEntity(), v -> {
            v[2] = "Alemana";
            v[3] = "ABC";
            v[12] = "empresa.com";
            v[13] = List.of();
            return v;
        });

        List<String> fields = violations(broken, TaxRegime.LEGAL_ENTITY, context("MX", MX_MORAL)).stream()
            .map(GeneralDataViolation::field).toList();

        assertEquals(List.of("nationality", "foreignFiscalCode", "phones", "website"), fields);
    }
}
