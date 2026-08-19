package com.chengukargbo.careeros.applications.lock;

import java.time.OffsetDateTime;
import jakarta.persistence.*;

@Entity @Table(name="application_lock_history")
public class ApplicationLockHistory {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="application_id",nullable=false) private Long applicationId;
 @Enumerated(EnumType.STRING) @Column(name="previous_lock",length=30) private ApplicationLockState previousLock;
 @Enumerated(EnumType.STRING) @Column(name="new_lock",nullable=false,length=30) private ApplicationLockState newLock;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private ApplicationLockSource source;
 @Column(name="occurred_at",nullable=false,updatable=false) private OffsetDateTime occurredAt;
 @Column(columnDefinition="TEXT") private String reason;
 protected ApplicationLockHistory(){}
 ApplicationLockHistory(Long applicationId,ApplicationLockState previous,ApplicationLockState next,ApplicationLockSource source,String reason){this.applicationId=applicationId;previousLock=previous;newLock=next;this.source=source;this.reason=reason;occurredAt=OffsetDateTime.now();}
 public Long getId(){return id;} public Long getApplicationId(){return applicationId;} public ApplicationLockState getPreviousLock(){return previousLock;} public ApplicationLockState getNewLock(){return newLock;} public ApplicationLockSource getSource(){return source;} public OffsetDateTime getOccurredAt(){return occurredAt;} public String getReason(){return reason;}
}
