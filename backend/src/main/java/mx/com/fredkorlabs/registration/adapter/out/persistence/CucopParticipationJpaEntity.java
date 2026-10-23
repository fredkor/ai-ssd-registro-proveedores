package mx.com.fredkorlabs.registration.adapter.out.persistence;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import mx.com.fredkorlabs.registration.domain.model.ParticipationType;
import org.springframework.data.domain.Persistable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Saved CUCoP selection of a registration (design §3); types and specific items are replaced as a whole. */
@Entity
@Table(name = "cucop_participation")
public class CucopParticipationJpaEntity implements Persistable<UUID> {

    @Id
    @Column(name = "registration_id", nullable = false, updatable = false)
    private UUID registrationId;

    @ElementCollection
    @CollectionTable(name = "registration_participation_type", joinColumns = @JoinColumn(name = "registration_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "participation_type", nullable = false, length = 32)
    private Set<ParticipationType> participationTypes = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "registration_cucop_item", joinColumns = @JoinColumn(name = "registration_id"))
    @Column(name = "specific_item_id", nullable = false)
    private Set<Integer> specificItemIds = new HashSet<>();

    @Transient
    private boolean persisted;

    protected CucopParticipationJpaEntity() {
    }

    public CucopParticipationJpaEntity(UUID registrationId) {
        this.registrationId = registrationId;
    }

    public void replace(Collection<ParticipationType> participationTypes, Collection<Integer> specificItemIds) {
        this.participationTypes.clear();
        this.participationTypes.addAll(participationTypes);
        this.specificItemIds.clear();
        this.specificItemIds.addAll(specificItemIds);
    }

    @Override
    public UUID getId() { return registrationId; }

    @Override
    public boolean isNew() { return !persisted; }

    @PostLoad
    @PostPersist
    void markPersisted() { persisted = true; }

    public Set<ParticipationType> getParticipationTypes() { return participationTypes; }

    public Set<Integer> getSpecificItemIds() { return specificItemIds; }
}
