package com.chengukargbo.careeros.preparation;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.chengukargbo.careeros.answers.AnswerType;
import jakarta.persistence.*;

@Entity
@Table(name = "approved_field_plans")
public class ApprovedFieldPlan {
    public enum ValueSource { APPLICANT_PROFILE, APPROVED_ANSWER }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "session_id", nullable = false, unique = true)
    private ApplicationPreparationSession session;
    @Column(name = "generated_at", nullable = false)
    private OffsetDateTime generatedAt;
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC, id ASC")
    private List<ApprovedFieldPlanItem> items = new ArrayList<>();

    protected ApprovedFieldPlan() {}
    ApprovedFieldPlan(ApplicationPreparationSession session) {
        this.session = session;
        generatedAt = OffsetDateTime.now();
    }
    void add(String key, AnswerType type, String text, Boolean bool, BigDecimal number,
        ValueSource source, Long sourceId, OffsetDateTime verifiedAt) {
        items.add(new ApprovedFieldPlanItem(this, key, type, text, bool, number,
            source, sourceId, verifiedAt, items.size()));
    }
    @PrePersist void create() { createdAt = OffsetDateTime.now(); }
    public Long getId() { return id; }
    public ApplicationPreparationSession getSession() { return session; }
    public OffsetDateTime getGeneratedAt() { return generatedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public List<ApprovedFieldPlanItem> getItems() { return List.copyOf(items); }
}

@Entity
@Table(name = "approved_field_plan_items")
class ApprovedFieldPlanItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "plan_id", nullable = false)
    private ApprovedFieldPlan plan;
    @Column(name = "canonical_key", nullable = false, length = 80) private String canonicalKey;
    @Enumerated(EnumType.STRING) @Column(name = "answer_type", nullable = false, length = 20)
    private AnswerType answerType;
    @Column(name = "text_value", columnDefinition = "TEXT") private String textValue;
    @Column(name = "boolean_value") private Boolean booleanValue;
    @Column(name = "number_value", precision = 14, scale = 2) private BigDecimal numberValue;
    @Enumerated(EnumType.STRING) @Column(name = "value_source", nullable = false, length = 30)
    private ApprovedFieldPlan.ValueSource valueSource;
    @Column(name = "source_record_id") private Long sourceRecordId;
    @Column(name = "source_verified_at", nullable = false) private OffsetDateTime sourceVerifiedAt;
    @Column(name = "display_order", nullable = false) private int displayOrder;
    protected ApprovedFieldPlanItem() {}
    ApprovedFieldPlanItem(ApprovedFieldPlan plan, String key, AnswerType type,
        String text, Boolean bool, BigDecimal number, ApprovedFieldPlan.ValueSource source,
        Long sourceId, OffsetDateTime verifiedAt, int order) {
        this.plan=plan; canonicalKey=key; answerType=type; textValue=text;
        booleanValue=bool; numberValue=number; valueSource=source;
        sourceRecordId=sourceId; sourceVerifiedAt=verifiedAt; displayOrder=order;
    }
    public Long getId(){return id;} public String getCanonicalKey(){return canonicalKey;}
    public AnswerType getAnswerType(){return answerType;} public String getTextValue(){return textValue;}
    public Boolean getBooleanValue(){return booleanValue;} public BigDecimal getNumberValue(){return numberValue;}
    public ApprovedFieldPlan.ValueSource getValueSource(){return valueSource;}
    public Long getSourceRecordId(){return sourceRecordId;} public OffsetDateTime getSourceVerifiedAt(){return sourceVerifiedAt;}
}
