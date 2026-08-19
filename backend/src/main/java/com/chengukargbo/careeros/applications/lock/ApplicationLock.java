package com.chengukargbo.careeros.applications.lock;

import java.time.OffsetDateTime;
import com.chengukargbo.careeros.applications.Application;
import jakarta.persistence.*;

@Entity @Table(name="application_locks")
public class ApplicationLock {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="application_id",nullable=false,unique=true) private Application application;
 @Enumerated(EnumType.STRING) @Column(name="lock_state",nullable=false,length=30) private ApplicationLockState state;
 @Column(name="changed_at",nullable=false) private OffsetDateTime changedAt;
 @Column(columnDefinition="TEXT") private String reason;
 @Column(name="created_at",nullable=false,updatable=false) private OffsetDateTime createdAt;
 @Column(name="updated_at",nullable=false) private OffsetDateTime updatedAt;
 protected ApplicationLock(){}
 public ApplicationLock(Application application,ApplicationLockState state,String reason){this.application=application;this.state=state;this.reason=reason;changedAt=OffsetDateTime.now();}
 void transition(ApplicationLockState next,String reason){state=next;this.reason=reason;changedAt=OffsetDateTime.now();}
 @PrePersist void create(){createdAt=updatedAt=OffsetDateTime.now();} @PreUpdate void update(){updatedAt=OffsetDateTime.now();}
 public Long getId(){return id;} public Application getApplication(){return application;} public ApplicationLockState getState(){return state;} public OffsetDateTime getChangedAt(){return changedAt;} public String getReason(){return reason;} public OffsetDateTime getCreatedAt(){return createdAt;} public OffsetDateTime getUpdatedAt(){return updatedAt;}
}
