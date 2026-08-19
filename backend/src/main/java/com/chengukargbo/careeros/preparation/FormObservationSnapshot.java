package com.chengukargbo.careeros.preparation;

import java.time.OffsetDateTime;
import java.util.*;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "form_observation_snapshots")
public class FormObservationSnapshot {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "form_target_id", nullable = false)
    private ApplicationFormTarget formTarget;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "preparation_session_id")
    private ApplicationPreparationSession preparationSession;
    @Column(name = "sequence_number", nullable = false)
    private int sequenceNumber;
    @Column(name = "snapshot_fingerprint", nullable = false, length = 64)
    private String snapshotFingerprint;
    @Column(name = "observed_at", nullable = false)
    private OffsetDateTime observedAt;
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    @OneToMany(mappedBy = "snapshot", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC, externalQuestionId ASC")
    private List<ObservedQuestion> questions = new ArrayList<>();
    @OneToMany(mappedBy = "snapshot", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC, externalFieldId ASC")
    private List<ObservedMaterialRequirement> materialRequirements = new ArrayList<>();

    protected FormObservationSnapshot() {}

    FormObservationSnapshot(ApplicationFormTarget target,
        ApplicationPreparationSession preparationSession, int sequenceNumber,
        String fingerprint) {
        formTarget = target;
        this.preparationSession = preparationSession;
        this.sequenceNumber = sequenceNumber;
        snapshotFingerprint = fingerprint;
        observedAt = createdAt = OffsetDateTime.now();
    }

    void add(ObservedQuestion question) { questions.add(question); }
    void add(ObservedMaterialRequirement requirement) { materialRequirements.add(requirement); }

    public Long getId() { return id; }
    public ApplicationFormTarget getFormTarget() { return formTarget; }
    public ApplicationPreparationSession getPreparationSession() { return preparationSession; }
    public int getSequenceNumber() { return sequenceNumber; }
    public String getSnapshotFingerprint() { return snapshotFingerprint; }
    public OffsetDateTime getObservedAt() { return observedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public List<ObservedQuestion> getQuestions() { return List.copyOf(questions); }
    public List<ObservedMaterialRequirement> getMaterialRequirements() {
        return List.copyOf(materialRequirements);
    }
}
