package mx.com.fredkorlabs.registration.domain.model;

import mx.com.fredkorlabs.registration.domain.exception.InvalidCucopParticipationException;

import java.util.Set;

public record CucopParticipation(
    Set<ParticipationType> participationTypes,
    Set<CucopSpecificItem> specificItems
) {
    public CucopParticipation {
        if (participationTypes == null || participationTypes.isEmpty()) {
            throw new InvalidCucopParticipationException("At least one participation type is required");
        }
        if (specificItems == null || specificItems.isEmpty()) {
            throw new InvalidCucopParticipationException("At least one specific item is required");
        }
        participationTypes = Set.copyOf(participationTypes);
        specificItems = Set.copyOf(specificItems);
    }
}
