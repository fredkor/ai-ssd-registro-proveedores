package mx.com.fredkorlabs.registration.domain.model;

import mx.com.fredkorlabs.registration.domain.exception.InvalidCucopParticipationException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Selected participation types and specific items (INV-31); upper CUCoP levels are not selections. */
public record CucopParticipation(
    Set<ParticipationType> participationTypes,
    Set<CucopSpecificItem> specificItems
) {
    public CucopParticipation {
        if (participationTypes == null || participationTypes.isEmpty()) {
            throw new InvalidCucopParticipationException(
                List.of(new CucopParticipationViolation("participationTypes", "must have at least one item")));
        }
        if (specificItems == null || specificItems.isEmpty()) {
            throw new InvalidCucopParticipationException(
                List.of(new CucopParticipationViolation("specificItemIds", "must have at least one item")));
        }
        participationTypes = Set.copyOf(participationTypes);
        specificItems = Set.copyOf(specificItems);
    }

    /**
     * Validates a whole submission (INV-31) and reports every broken rule. {@code catalogItems} holds the specific
     * items of the catalog among the submitted ids; an id missing from it is not a specific item (INV-31).
     */
    public static CucopParticipation validate(List<ParticipationType> participationTypes,
                                              List<Integer> specificItemIds,
                                              Map<Integer, CucopSpecificItem> catalogItems) {
        List<CucopParticipationViolation> violations = new ArrayList<>();
        Set<ParticipationType> types = new LinkedHashSet<>();
        if (participationTypes == null || participationTypes.isEmpty()) {
            violations.add(new CucopParticipationViolation("participationTypes", "must have at least one item"));
        } else {
            for (int i = 0; i < participationTypes.size(); i++) {
                ParticipationType type = participationTypes.get(i);
                if (type == null) {
                    violations.add(new CucopParticipationViolation("participationTypes/" + i, "is required"));
                } else if (!types.add(type)) {
                    violations.add(new CucopParticipationViolation("participationTypes/" + i, "is repeated"));
                }
            }
        }

        Set<CucopSpecificItem> items = new LinkedHashSet<>();
        if (specificItemIds == null || specificItemIds.isEmpty()) {
            violations.add(new CucopParticipationViolation("specificItemIds", "must have at least one item"));
        } else {
            Set<Integer> seen = new HashSet<>();
            for (int i = 0; i < specificItemIds.size(); i++) {
                Integer id = specificItemIds.get(i);
                if (id == null) {
                    violations.add(new CucopParticipationViolation("specificItemIds/" + i, "is required"));
                } else if (!seen.add(id)) {
                    violations.add(new CucopParticipationViolation("specificItemIds/" + i, "is repeated"));
                } else if (!catalogItems.containsKey(id)) {
                    violations.add(new CucopParticipationViolation("specificItemIds/" + i,
                        "is not a specific item of the CUCoP catalog"));
                } else {
                    items.add(catalogItems.get(id));
                }
            }
        }

        if (!violations.isEmpty()) {
            throw new InvalidCucopParticipationException(violations);
        }
        return new CucopParticipation(types, items);
    }
}
