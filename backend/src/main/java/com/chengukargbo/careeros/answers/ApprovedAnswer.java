package com.chengukargbo.careeros.answers;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "approved_answers")
public class ApprovedAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "canonical_key", nullable = false, unique = true, length = 80)
    private String canonicalKey;

    @Column(name = "representative_question", nullable = false, length = 500)
    private String representativeQuestion;

    @Enumerated(EnumType.STRING)
    @Column(name = "answer_type", nullable = false, length = 20)
    private AnswerType answerType;

    @Column(name = "text_value", columnDefinition = "TEXT")
    private String textValue;

    @Column(name = "boolean_value")
    private Boolean booleanValue;

    @Column(name = "number_value", precision = 14, scale = 2)
    private BigDecimal numberValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AnswerClassification classification;

    @Column(nullable = false)
    private boolean reusable;

    @Column(name = "user_approved", nullable = false)
    private boolean userApproved;

    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;

    @Column(name = "last_used_at")
    private OffsetDateTime lastUsedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "answer_source", nullable = false, length = 30)
    private AnswerSource answerSource;

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_field", length = 40)
    private ProfileAnswerField profileField;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected ApprovedAnswer() {
    }

    public ApprovedAnswer(
        String canonicalKey,
        String representativeQuestion,
        AnswerType answerType,
        String textValue,
        Boolean booleanValue,
        BigDecimal numberValue,
        AnswerClassification classification,
        boolean reusable,
        AnswerSource answerSource,
        ProfileAnswerField profileField,
        String notes
    ) {
        apply(
            canonicalKey, representativeQuestion, answerType, textValue,
            booleanValue, numberValue, classification, reusable,
            answerSource, profileField, notes
        );
    }

    public void update(
        String canonicalKey,
        String representativeQuestion,
        AnswerType answerType,
        String textValue,
        Boolean booleanValue,
        BigDecimal numberValue,
        AnswerClassification classification,
        boolean reusable,
        AnswerSource answerSource,
        ProfileAnswerField profileField,
        String notes
    ) {
        boolean approvalMeaningChanged =
            !Objects.equals(this.canonicalKey, canonicalKey)
            || this.answerType != answerType
            || !Objects.equals(this.textValue, textValue)
            || !Objects.equals(this.booleanValue, booleanValue)
            || !sameDecimal(this.numberValue, numberValue)
            || this.classification != classification
            || this.reusable != reusable
            || this.answerSource != answerSource
            || this.profileField != profileField;

        apply(
            canonicalKey, representativeQuestion, answerType, textValue,
            booleanValue, numberValue, classification, reusable,
            answerSource, profileField, notes
        );
        if (approvalMeaningChanged) {
            revokeApproval();
        }
    }

    public void approve() {
        userApproved = true;
        approvedAt = OffsetDateTime.now();
    }

    public void revokeApproval() {
        userApproved = false;
        approvedAt = null;
    }

    private void apply(
        String canonicalKey, String representativeQuestion,
        AnswerType answerType, String textValue, Boolean booleanValue,
        BigDecimal numberValue, AnswerClassification classification,
        boolean reusable, AnswerSource answerSource,
        ProfileAnswerField profileField, String notes
    ) {
        this.canonicalKey = canonicalKey;
        this.representativeQuestion = representativeQuestion;
        this.answerType = answerType;
        this.textValue = textValue;
        this.booleanValue = booleanValue;
        this.numberValue = numberValue;
        this.classification = classification;
        this.reusable = reusable;
        this.answerSource = answerSource;
        this.profileField = profileField;
        this.notes = notes;
    }

    private boolean sameDecimal(BigDecimal first, BigDecimal second) {
        if (first == null || second == null) {
            return first == second;
        }
        return first.compareTo(second) == 0;
    }

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public String getCanonicalKey() { return canonicalKey; }
    public String getRepresentativeQuestion() { return representativeQuestion; }
    public AnswerType getAnswerType() { return answerType; }
    public String getTextValue() { return textValue; }
    public Boolean getBooleanValue() { return booleanValue; }
    public BigDecimal getNumberValue() { return numberValue; }
    public AnswerClassification getClassification() { return classification; }
    public boolean isReusable() { return reusable; }
    public boolean isUserApproved() { return userApproved; }
    public OffsetDateTime getApprovedAt() { return approvedAt; }
    public OffsetDateTime getLastUsedAt() { return lastUsedAt; }
    public AnswerSource getAnswerSource() { return answerSource; }
    public ProfileAnswerField getProfileField() { return profileField; }
    public String getNotes() { return notes; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
