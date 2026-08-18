package com.chengukargbo.careeros.questions;
import java.time.OffsetDateTime; import java.util.*; import jakarta.persistence.*; import com.chengukargbo.careeros.applications.Application; import com.chengukargbo.careeros.answers.ApprovedAnswer; import com.chengukargbo.careeros.questions.QuestionEnums.*;
@Entity @Table(name="application_questions")
public class ApplicationQuestion { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
 @ManyToOne(fetch=FetchType.LAZY) Application application; @ManyToOne(fetch=FetchType.LAZY) QuestionTemplate template;
 String externalQuestionKey; String canonicalQuestionKey; String questionText; @Enumerated(EnumType.STRING) AnswerType answerType;
 boolean required; @Enumerated(EnumType.STRING) Classification classification;
 @OneToMany(mappedBy="question",fetch=FetchType.LAZY,cascade=CascadeType.ALL,orphanRemoval=true)
 @OrderBy("displayOrder ASC, id ASC") List<ApplicationQuestionOption> options=new ArrayList<>();
 @Enumerated(EnumType.STRING) Status status; String proposedAnswer; String approvedAnswer;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="approved_answer_id") ApprovedAnswer approvedAnswerLink; @Enumerated(EnumType.STRING) Source source;
 String notes; OffsetDateTime answeredAt; OffsetDateTime reviewedAt; OffsetDateTime createdAt; OffsetDateTime updatedAt;
 protected ApplicationQuestion(){}
 ApplicationQuestion(Application a, QuestionTemplate t, String key, String text, AnswerType type, boolean req, Classification c, Source source){application=a;template=t;canonicalQuestionKey=key;questionText=text;answerType=type;required=req;classification=c;this.source=source;status=Status.UNANSWERED; createdAt=updatedAt=OffsetDateTime.now();}
 void suggest(ApprovedAnswer link,String value){approvedAnswerLink=link;proposedAnswer=value;status=Status.NEEDS_REVIEW;updatedAt=OffsetDateTime.now();}
 void answer(String value){proposedAnswer=value;approvedAnswer=null;status=Status.ANSWERED;answeredAt=OffsetDateTime.now();updatedAt=answeredAt;}
 void approve(){if(proposedAnswer==null||proposedAnswer.isBlank())throw new IllegalStateException("Answer is required before approval");approvedAnswer=proposedAnswer;status=Status.APPROVED;reviewedAt=OffsetDateTime.now();updatedAt=reviewedAt;}
 void rejectSuggestion(){proposedAnswer=null;approvedAnswerLink=null;status=Status.UNANSWERED;updatedAt=OffsetDateTime.now();}
 void block(){status=Status.BLOCKED;updatedAt=OffsetDateTime.now();} void unblock(){status=proposedAnswer==null?Status.UNANSWERED:Status.NEEDS_REVIEW;updatedAt=OffsetDateTime.now();}
 public Long getId(){return id;} public Application getApplication(){return application;} public String getExternalQuestionKey(){return externalQuestionKey;} public String getCanonicalQuestionKey(){return canonicalQuestionKey;} public String getQuestionText(){return questionText;} public AnswerType getAnswerType(){return answerType;} public boolean isRequired(){return required;} public List<ApplicationQuestionOption> getOptions(){return List.copyOf(options);} public Classification getClassification(){return classification;} public Status getStatus(){return status;} public String getProposedAnswer(){return proposedAnswer;} public String getApprovedAnswer(){return approvedAnswer;} public ApprovedAnswer getApprovedAnswerLink(){return approvedAnswerLink;} public Source getSource(){return source;} public String getNotes(){return notes;} public OffsetDateTime getCreatedAt(){return createdAt;} public OffsetDateTime getUpdatedAt(){return updatedAt;} public OffsetDateTime getAnsweredAt(){return answeredAt;} public OffsetDateTime getReviewedAt(){return reviewedAt;}
}
