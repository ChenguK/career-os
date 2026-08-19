package com.chengukargbo.careeros.preparation;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import com.chengukargbo.careeros.preparation.QuestionMappingEnums.*;

@Entity @Immutable @Table(name="observed_question_mapping_history")
public class ObservedQuestionMappingHistory {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="mapping_id",nullable=false) private ObservedQuestionMapping mapping;
    @Enumerated(EnumType.STRING) @Column(name="event_type",nullable=false,length=20) private MappingEventType eventType;
    @Column(name="previous_canonical_key",length=80) private String previousCanonicalKey;
    @Column(name="canonical_question_key",length=80) private String canonicalQuestionKey;
    @Enumerated(EnumType.STRING) @Column(name="mapping_source",length=30) private MappingSource mappingSource;
    @Column(precision=4,scale=3) private BigDecimal confidence;
    @Column(name="user_confirmed",nullable=false) private boolean userConfirmed;
    @Column(name="occurred_at",nullable=false) private OffsetDateTime occurredAt;
    protected ObservedQuestionMappingHistory() {}
    ObservedQuestionMappingHistory(ObservedQuestionMapping mapping,MappingEventType event,String previous){this.mapping=mapping;eventType=event;previousCanonicalKey=previous;canonicalQuestionKey=mapping.getCanonicalQuestionKey();mappingSource=mapping.getMappingSource();confidence=mapping.getConfidence();userConfirmed=mapping.isUserConfirmed();occurredAt=OffsetDateTime.now();}
    public Long getId(){return id;} public MappingEventType getEventType(){return eventType;}
    public String getPreviousCanonicalKey(){return previousCanonicalKey;} public String getCanonicalQuestionKey(){return canonicalQuestionKey;}
    public MappingSource getMappingSource(){return mappingSource;} public BigDecimal getConfidence(){return confidence;}
    public boolean isUserConfirmed(){return userConfirmed;} public OffsetDateTime getOccurredAt(){return occurredAt;}
}
