package com.chengukargbo.careeros.preparation;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import jakarta.persistence.*;
import com.chengukargbo.careeros.preparation.QuestionMappingEnums.*;

@Entity
@Table(name = "observed_question_mappings", uniqueConstraints = @UniqueConstraint(
    columnNames = {"form_target_id", "external_question_id"}))
public class ObservedQuestionMapping {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "form_target_id", nullable = false)
    private ApplicationFormTarget formTarget;
    @Column(name = "external_question_id", nullable = false, length = 200)
    private String externalQuestionId;
    @Column(name = "canonical_question_key", length = 80) private String canonicalQuestionKey;
    @Enumerated(EnumType.STRING) @Column(name = "mapping_source", length = 30)
    private MappingSource mappingSource;
    @Enumerated(EnumType.STRING) @Column(name = "mapping_state", nullable = false, length = 20)
    private MappingState mappingState;
    @Column(precision = 4, scale = 3) private BigDecimal confidence;
    @Column(name = "user_confirmed", nullable = false) private boolean userConfirmed;
    @Column(name = "confirmed_at") private OffsetDateTime confirmedAt;
    @Column(name = "revoked_at") private OffsetDateTime revokedAt;
    @Column(name = "created_at", nullable = false, updatable = false) private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt;

    protected ObservedQuestionMapping() {}
    ObservedQuestionMapping(ApplicationFormTarget target, String externalQuestionId) {
        formTarget = target; this.externalQuestionId = externalQuestionId;
        mappingState = MappingState.UNCONFIRMED;
    }
    String confirm(String key, MappingSource source, BigDecimal confidence, boolean userConfirmed) {
        String previous = canonicalQuestionKey;
        canonicalQuestionKey = key; mappingSource = source; this.confidence = confidence;
        this.userConfirmed = userConfirmed; mappingState = MappingState.CONFIRMED;
        confirmedAt = OffsetDateTime.now(); revokedAt = null;
        return previous;
    }
    String revoke() {
        String previous = canonicalQuestionKey; mappingState = MappingState.REVOKED;
        userConfirmed = false; revokedAt = OffsetDateTime.now(); return previous;
    }
    @PrePersist void create() { createdAt = updatedAt = OffsetDateTime.now(); }
    @PreUpdate void update() { updatedAt = OffsetDateTime.now(); }
    public Long getId(){return id;} public ApplicationFormTarget getFormTarget(){return formTarget;}
    public String getExternalQuestionId(){return externalQuestionId;}
    public String getCanonicalQuestionKey(){return canonicalQuestionKey;}
    public MappingSource getMappingSource(){return mappingSource;}
    public MappingState getMappingState(){return mappingState;}
    public BigDecimal getConfidence(){return confidence;} public boolean isUserConfirmed(){return userConfirmed;}
    public OffsetDateTime getConfirmedAt(){return confirmedAt;} public OffsetDateTime getRevokedAt(){return revokedAt;}
    public OffsetDateTime getCreatedAt(){return createdAt;} public OffsetDateTime getUpdatedAt(){return updatedAt;}
}
