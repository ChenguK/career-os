package com.chengukargbo.careeros.questions;
import java.time.OffsetDateTime; import jakarta.persistence.*; import com.chengukargbo.careeros.questions.QuestionEnums.*;
@Entity @Table(name="question_templates")
public class QuestionTemplate { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
 @Enumerated(EnumType.STRING) JobFamily jobFamily; @Enumerated(EnumType.STRING) Seniority seniority;
 String canonicalQuestionKey; String representativeQuestion; @Enumerated(EnumType.STRING) AnswerType answerType;
 @Enumerated(EnumType.STRING) Classification classification; boolean requiredByDefault; boolean common; boolean active;
 String notes; OffsetDateTime createdAt; OffsetDateTime updatedAt;
 public Long getId(){return id;} public JobFamily getJobFamily(){return jobFamily;} public Seniority getSeniority(){return seniority;}
 public String getCanonicalQuestionKey(){return canonicalQuestionKey;} public String getRepresentativeQuestion(){return representativeQuestion;}
 public AnswerType getAnswerType(){return answerType;} public Classification getClassification(){return classification;}
 public boolean isRequiredByDefault(){return requiredByDefault;} public boolean isCommon(){return common;} public boolean isActive(){return active;}
}
