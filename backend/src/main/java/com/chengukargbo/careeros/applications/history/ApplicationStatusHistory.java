package com.chengukargbo.careeros.applications.history;

import java.time.OffsetDateTime;

import com.chengukargbo.careeros.applications.Application;
import com.chengukargbo.careeros.applications.ApplicationStatus;

import jakarta.persistence.*;

@Entity
@Table(name = "application_status_history")
public class ApplicationStatusHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;
    @Enumerated(EnumType.STRING) @Column(name = "previous_status", length = 40)
    private ApplicationStatus previousStatus;
    @Enumerated(EnumType.STRING) @Column(name = "new_status", nullable = false, length = 40)
    private ApplicationStatus newStatus;
    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private ApplicationTransitionSource source;
    @Column(columnDefinition = "TEXT") private String note;
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected ApplicationStatusHistory() {}

    public ApplicationStatusHistory(Application application,
        ApplicationStatus previousStatus, ApplicationStatus newStatus,
        ApplicationTransitionSource source, String note) {
        this.application = application;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.source = source;
        this.note = note;
        this.occurredAt = OffsetDateTime.now();
    }

    @PrePersist void onCreate() {
        if (occurredAt == null) occurredAt = OffsetDateTime.now();
        createdAt = OffsetDateTime.now();
    }
    public Long getId() { return id; }
    public Application getApplication() { return application; }
    public ApplicationStatus getPreviousStatus() { return previousStatus; }
    public ApplicationStatus getNewStatus() { return newStatus; }
    public OffsetDateTime getOccurredAt() { return occurredAt; }
    public ApplicationTransitionSource getSource() { return source; }
    public String getNote() { return note; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
