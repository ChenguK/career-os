package com.chengukargbo.careeros.automation;

import java.time.OffsetDateTime;
import com.chengukargbo.careeros.applications.Application;
import com.chengukargbo.careeros.automation.AutomationEnums.*;
import jakarta.persistence.*;

@Entity @Table(name="application_automation")
public class ApplicationAutomation {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="application_id",unique=true) private Application application;
 @Enumerated(EnumType.STRING) private State state;
 @Enumerated(EnumType.STRING) private SubmissionMode submissionMode;
 @Enumerated(EnumType.STRING) private AtsType atsType;
 private OffsetDateTime approvedForPrepAt; private OffsetDateTime readyForReviewAt;
 private OffsetDateTime approvedToSubmitAt; private OffsetDateTime blockedAt;
 private String blockReason; private OffsetDateTime createdAt; private OffsetDateTime updatedAt;
 protected ApplicationAutomation(){}
 public ApplicationAutomation(Application application){this.application=application;state=State.NOT_APPROVED;submissionMode=SubmissionMode.PREPARE_ONLY;atsType=AtsType.UNKNOWN;createdAt=updatedAt=OffsetDateTime.now();}
 void transition(State next){state=next;OffsetDateTime now=OffsetDateTime.now();updatedAt=now;if(next==State.APPROVED_FOR_PREP)approvedForPrepAt=now;if(next==State.READY_FOR_REVIEW)readyForReviewAt=now;if(next==State.APPROVED_TO_SUBMIT)approvedToSubmitAt=now;if(next==State.BLOCKED)blockedAt=now;if(next!=State.BLOCKED){blockedAt=null;blockReason=null;}}
 void setAtsType(AtsType value){atsType=value;updatedAt=OffsetDateTime.now();}
 void setBlockReason(String value){blockReason=value;}
 public Long getId(){return id;} public Application getApplication(){return application;} public State getState(){return state;} public SubmissionMode getSubmissionMode(){return submissionMode;} public AtsType getAtsType(){return atsType;} public OffsetDateTime getApprovedForPrepAt(){return approvedForPrepAt;} public OffsetDateTime getReadyForReviewAt(){return readyForReviewAt;} public OffsetDateTime getApprovedToSubmitAt(){return approvedToSubmitAt;} public OffsetDateTime getBlockedAt(){return blockedAt;} public String getBlockReason(){return blockReason;} public OffsetDateTime getCreatedAt(){return createdAt;} public OffsetDateTime getUpdatedAt(){return updatedAt;}
}
