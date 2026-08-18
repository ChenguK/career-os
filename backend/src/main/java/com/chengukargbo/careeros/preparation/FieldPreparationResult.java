package com.chengukargbo.careeros.preparation;

import java.time.OffsetDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "field_preparation_results")
public class FieldPreparationResult {
    public enum Outcome { PREPARED, SKIPPED, FAILED }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "plan_item_id", nullable = false, unique = true)
    private ApprovedFieldPlanItem planItem;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Outcome outcome;
    @Column(name = "safe_message", length = 1000) private String safeMessage;
    @Column(name = "prepared_at") private OffsetDateTime preparedAt;
    @Column(name = "recorded_at", nullable = false) private OffsetDateTime recordedAt;
    @Column(name = "created_at", nullable = false, updatable = false) private OffsetDateTime createdAt;
    protected FieldPreparationResult() {}
    FieldPreparationResult(ApprovedFieldPlanItem item, Outcome outcome, String message,
        OffsetDateTime preparedAt) {
        planItem=item; this.outcome=outcome; safeMessage=message;
        this.preparedAt=outcome==Outcome.PREPARED ? preparedAt : null;
        recordedAt=OffsetDateTime.now();
    }
    @PrePersist void create(){createdAt=OffsetDateTime.now();}
    public Long getId(){return id;} public ApprovedFieldPlanItem getPlanItem(){return planItem;}
    public Outcome getOutcome(){return outcome;} public String getSafeMessage(){return safeMessage;}
    public OffsetDateTime getPreparedAt(){return preparedAt;} public OffsetDateTime getRecordedAt(){return recordedAt;}
}
