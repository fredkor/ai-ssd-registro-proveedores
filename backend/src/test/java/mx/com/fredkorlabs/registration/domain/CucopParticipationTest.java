package mx.com.fredkorlabs.registration.domain;

import mx.com.fredkorlabs.registration.domain.exception.InvalidCucopParticipationException;
import mx.com.fredkorlabs.registration.domain.model.CucopParticipation;
import mx.com.fredkorlabs.registration.domain.model.CucopSpecificItem;
import mx.com.fredkorlabs.registration.domain.model.ParticipationType;
import org.junit.jupiter.api.Test;

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
}
