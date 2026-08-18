package com.chengukargbo.careeros.preparation;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import com.chengukargbo.careeros.answers.AnswerType;
import com.chengukargbo.careeros.preparation.ApprovedFieldPlan.ValueSource;
import com.chengukargbo.careeros.preparation.FieldPreparationResult.Outcome;
import com.chengukargbo.careeros.questions.QuestionEnums;
import jakarta.persistence.*;

@Entity @Table(name="preparation_reviews")
public class PreparationReview {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @OneToOne(fetch=FetchType.LAZY) @JoinColumn(name="session_id",nullable=false,unique=true) private ApplicationPreparationSession session;
 @Column(name="snapshot_hash",nullable=false,length=64) private String snapshotHash;
 @Column(name="generated_at",nullable=false) private OffsetDateTime generatedAt;
 @Column(name="created_at",nullable=false,updatable=false) private OffsetDateTime createdAt;
 @OneToMany(mappedBy="review",cascade=CascadeType.ALL,orphanRemoval=true) @OrderBy("displayOrder ASC,id ASC") private List<PreparationReviewField> fields=new ArrayList<>();
 @OneToMany(mappedBy="review",cascade=CascadeType.ALL,orphanRemoval=true) @OrderBy("category ASC,displayOrder ASC,id ASC") private List<PreparationReviewQuestion> questions=new ArrayList<>();
 @OneToMany(mappedBy="review",cascade=CascadeType.ALL,orphanRemoval=true) @OrderBy("displayOrder ASC,id ASC") private List<PreparationReviewScreenshot> screenshots=new ArrayList<>();
 protected PreparationReview(){}
 PreparationReview(ApplicationPreparationSession session,String hash){this.session=session;snapshotHash=hash;generatedAt=OffsetDateTime.now();}
 void addField(ApprovedFieldPlanItem i,FieldPreparationResult r){fields.add(new PreparationReviewField(this,i,r,fields.size()));}
 void addQuestion(Long id,String key,String text,QuestionEnums.AnswerType type,boolean required,QuestionEnums.Status status,PreparationReviewQuestion.Category category){questions.add(new PreparationReviewQuestion(this,id,key,text,type,required,status,category,questions.size()));}
 void addScreenshot(String reference,String page,OffsetDateTime captured){screenshots.add(new PreparationReviewScreenshot(this,reference,page,captured,screenshots.size()));}
 @PrePersist void create(){createdAt=OffsetDateTime.now();}
 public Long getId(){return id;} public ApplicationPreparationSession getSession(){return session;} public String getSnapshotHash(){return snapshotHash;} public OffsetDateTime getGeneratedAt(){return generatedAt;}
 public List<PreparationReviewField> getFields(){return List.copyOf(fields);} public List<PreparationReviewQuestion> getQuestions(){return List.copyOf(questions);} public List<PreparationReviewScreenshot> getScreenshots(){return List.copyOf(screenshots);}
}

@Entity @Table(name="preparation_review_fields")
class PreparationReviewField {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="review_id",nullable=false) private PreparationReview review;
 @Column(name="canonical_key",nullable=false,length=80) private String canonicalKey; @Enumerated(EnumType.STRING) @Column(name="answer_type",nullable=false,length=20) private AnswerType answerType;
 @Column(name="text_value",columnDefinition="TEXT") private String textValue; @Column(name="boolean_value") private Boolean booleanValue; @Column(name="number_value") private BigDecimal numberValue;
 @Enumerated(EnumType.STRING) @Column(name="value_source",nullable=false,length=30) private ValueSource valueSource; @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Outcome outcome;
 @Column(name="safe_message",length=1000) private String safeMessage; @Column(name="prepared_at") private OffsetDateTime preparedAt; @Column(name="display_order",nullable=false) private int displayOrder;
 protected PreparationReviewField(){} PreparationReviewField(PreparationReview review,ApprovedFieldPlanItem i,FieldPreparationResult r,int order){this.review=review;canonicalKey=i.getCanonicalKey();answerType=i.getAnswerType();textValue=i.getTextValue();booleanValue=i.getBooleanValue();numberValue=i.getNumberValue();valueSource=i.getValueSource();outcome=r.getOutcome();safeMessage=r.getSafeMessage();preparedAt=r.getPreparedAt();displayOrder=order;}
 public String getCanonicalKey(){return canonicalKey;} public AnswerType getAnswerType(){return answerType;} public String getTextValue(){return textValue;} public Boolean getBooleanValue(){return booleanValue;} public BigDecimal getNumberValue(){return numberValue;} public ValueSource getValueSource(){return valueSource;} public Outcome getOutcome(){return outcome;} public String getSafeMessage(){return safeMessage;} public OffsetDateTime getPreparedAt(){return preparedAt;}
}

@Entity @Table(name="preparation_review_questions")
class PreparationReviewQuestion {
 enum Category { UNANSWERED, BLOCKED }
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="review_id",nullable=false) private PreparationReview review;
 @Column(name="application_question_id",nullable=false) private Long applicationQuestionId; @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Category category;
 @Column(name="canonical_key",length=80) private String canonicalKey; @Column(name="question_text",nullable=false,length=1000) private String questionText; @Enumerated(EnumType.STRING) @Column(name="answer_type",nullable=false,length=30) private QuestionEnums.AnswerType answerType;
 @Column(nullable=false) private boolean required; @Enumerated(EnumType.STRING) @Column(name="question_status",nullable=false,length=30) private QuestionEnums.Status questionStatus; @Column(name="display_order",nullable=false) private int displayOrder;
 protected PreparationReviewQuestion(){} PreparationReviewQuestion(PreparationReview r,Long id,String key,String text,QuestionEnums.AnswerType type,boolean req,QuestionEnums.Status status,Category category,int order){review=r;applicationQuestionId=id;canonicalKey=key;questionText=text;answerType=type;required=req;questionStatus=status;this.category=category;displayOrder=order;}
 public Long getApplicationQuestionId(){return applicationQuestionId;} public Category getCategory(){return category;} public String getCanonicalKey(){return canonicalKey;} public String getQuestionText(){return questionText;} public QuestionEnums.AnswerType getAnswerType(){return answerType;} public boolean isRequired(){return required;} public QuestionEnums.Status getQuestionStatus(){return questionStatus;}
}

@Entity @Table(name="preparation_review_screenshots")
class PreparationReviewScreenshot {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="review_id",nullable=false) private PreparationReview review;
 @Column(nullable=false,length=1000) private String reference; @Column(name="page_key",length=200) private String pageKey; @Column(name="captured_at",nullable=false) private OffsetDateTime capturedAt; @Column(name="display_order",nullable=false) private int displayOrder;
 protected PreparationReviewScreenshot(){} PreparationReviewScreenshot(PreparationReview r,String ref,String page,OffsetDateTime at,int order){review=r;reference=ref;pageKey=page;capturedAt=at;displayOrder=order;}
 public String getReference(){return reference;} public String getPageKey(){return pageKey;} public OffsetDateTime getCapturedAt(){return capturedAt;}
}
