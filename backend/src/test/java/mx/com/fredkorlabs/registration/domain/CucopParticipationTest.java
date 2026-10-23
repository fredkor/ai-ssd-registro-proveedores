package mx.com.fredkorlabs.registration.domain;

import mx.com.fredkorlabs.registration.domain.exception.InvalidCucopParticipationException;
import mx.com.fredkorlabs.registration.domain.model.CucopParticipation;
import mx.com.fredkorlabs.registration.domain.model.CucopParticipationViolation;
import mx.com.fredkorlabs.registration.domain.model.CucopSpecificItem;
import mx.com.fredkorlabs.registration.domain.model.ParticipationType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CucopParticipationTest {

    private static final CucopSpecificItem ITEM = new CucopSpecificItem(1, 1, "10101001", "Item test", 4);

    @Test
    void US11_CA004_emptyParticipationTypes_isRejected() {
        assertThrows(InvalidCucopParticipationException.class,
            () -> new CucopParticipation(Set.of(), Set.of(ITEM)));
    }

    @Test
    void US11_CA004_emptySpecificItems_isRejected() {
        assertThrows(InvalidCucopParticipationException.class,
            () -> new CucopParticipation(Set.of(ParticipationType.GOODS_AND_SERVICES_SUPPLIER), Set.of()));
    }

    @Test
    void US11_CA004_nullParticipationTypes_isRejected() {
        assertThrows(InvalidCucopParticipationException.class,
            () -> new CucopParticipation(null, Set.of(ITEM)));
    }

    @Test
    void US11_CA004_validParticipationWithBothTypes_isAccepted() {
        CucopParticipation participation = new CucopParticipation(
            Set.of(ParticipationType.GOODS_AND_SERVICES_SUPPLIER, ParticipationType.PUBLIC_WORKS_CONTRACTOR),
            Set.of(ITEM));
        assertEquals(2, participation.participationTypes().size());
        assertEquals(1, participation.specificItems().size());
    }

    private static final Map<Integer, CucopSpecificItem> CATALOG = Map.of(
        1, ITEM, 2, new CucopSpecificItem(2, 1, "10101002", "Otra partida", 4));

    private static List<CucopParticipationViolation> violationsOf(List<ParticipationType> types, List<Integer> ids) {
        return assertThrows(InvalidCucopParticipationException.class,
            () -> CucopParticipation.validate(types, ids, CATALOG)).violations();
    }

    @Test
    void US11_CA011_validate_acceptsBothTypesAndSeveralSpecificItems() {
        CucopParticipation participation = CucopParticipation.validate(
            List.of(ParticipationType.PUBLIC_WORKS_CONTRACTOR, ParticipationType.GOODS_AND_SERVICES_SUPPLIER),
            List.of(2, 1), CATALOG);

        assertEquals(Set.of(ParticipationType.values()), participation.participationTypes());
        assertEquals(Set.copyOf(CATALOG.values()), participation.specificItems());
    }

    @Test
    void US11_CA004_validate_withoutTypesNorItems_reportsBothFields() {
        assertEquals(List.of(
                new CucopParticipationViolation("participationTypes", "must have at least one item"),
                new CucopParticipationViolation("specificItemIds", "must have at least one item")),
            violationsOf(List.of(), null));
    }

    @Test
    void US11_CA012_validate_idOutsideTheSpecificItems_isReportedWithItsIndex() {
        assertEquals(List.of(new CucopParticipationViolation("specificItemIds/1",
                "is not a specific item of the CUCoP catalog")),
            violationsOf(List.of(ParticipationType.GOODS_AND_SERVICES_SUPPLIER), List.of(1, 99)));
    }

    @Test
    void INV31_validate_repeatedTypeOrItem_isRejectedWithoutDeduplicating() {
        assertEquals(List.of(
                new CucopParticipationViolation("participationTypes/1", "is repeated"),
                new CucopParticipationViolation("specificItemIds/2", "is repeated")),
            violationsOf(List.of(ParticipationType.GOODS_AND_SERVICES_SUPPLIER,
                ParticipationType.GOODS_AND_SERVICES_SUPPLIER), List.of(1, 2, 1)));
    }

    @Test
    void US11_CA012_validate_nullItems_areRequired() {
        assertEquals(List.of(
                new CucopParticipationViolation("participationTypes/0", "is required"),
                new CucopParticipationViolation("specificItemIds/0", "is required")),
            violationsOf(Arrays.asList((ParticipationType) null), Arrays.asList((Integer) null)));
    }
}
