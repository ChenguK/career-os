package com.chengukargbo.careeros.preparation;

import java.time.OffsetDateTime;

import com.chengukargbo.careeros.applications.Application;
import com.chengukargbo.careeros.preparation.PreparationEnums.SessionState;

import jakarta.persistence.*;

@Entity
@Table(name = "application_preparation_sessions")
public class ApplicationPreparationSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "application_id", nullable = false)
    private Application application;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "form_target_id", nullable = false)
    private ApplicationFormTarget formTarget;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "previous_session_id")
    private ApplicationPreparationSession previousSession;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 40)
    private SessionState state;
    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;
    @Column(name = "last_progress_at", nullable = false)
    private OffsetDateTime lastProgressAt;
    @Column(name = "completed_at")
    private OffsetDateTime completedAt;
    @Column(name = "current_page", length = 200) private String currentPage;
    @Column(name = "current_question", length = 200) private String currentQuestion;
    @Column(columnDefinition = "TEXT") private String checkpoint;
    @Column(name = "checkpoint_snapshot_hash", length = 64) private String checkpointSnapshotHash;
    @Enumerated(EnumType.STRING) @Column(name = "resume_state", length = 40)
    private SessionState resumeState;
    @Column(name = "paused_at") private OffsetDateTime pausedAt;
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected ApplicationPreparationSession() {}

    ApplicationPreparationSession(Application application, ApplicationFormTarget target,
        ApplicationPreparationSession previousSession) {
        this.application = application;
        this.formTarget = target;
        this.previousSession = previousSession;
        state = SessionState.INITIALIZED;
        startedAt = lastProgressAt = OffsetDateTime.now();
    }

    void cancel() {
        if (!state.active()) {
            throw new IllegalStateException("Only an active preparation session can be cancelled");
        }
        state = SessionState.CANCELLED;
        completedAt = lastProgressAt = OffsetDateTime.now();
    }

    void beginOpening() { transition(SessionState.INITIALIZED, SessionState.OPENING); }
    void beginCollectingQuestions() {
        transition(SessionState.OPENING, SessionState.COLLECTING_QUESTIONS);
    }
    void waitForUser(String snapshotHash) {
        pauseFrom(SessionState.COLLECTING_QUESTIONS, SessionState.PREPARING_FIELDS,
            null, null, "questions-reviewed", snapshotHash);
    }
    void beginPreparingFields() {
        transition(SessionState.WAITING_FOR_USER, SessionState.PREPARING_FIELDS);
    }
    void readyForReview() {
        transition(SessionState.PREPARING_FIELDS, SessionState.READY_FOR_REVIEW);
        completedAt = lastProgressAt;
    }
    void pause(String page, String question, String checkpoint, String snapshotHash) {
        if (state == SessionState.WAITING_FOR_USER || !state.active())
            throw new IllegalStateException("Only running preparation can be paused");
        pauseFrom(state, state, page, question, checkpoint, snapshotHash);
    }
    void resume() {
        if (state != SessionState.WAITING_FOR_USER || resumeState == null)
            throw new IllegalStateException("Only a paused preparation session can be resumed");
        state = resumeState;
        resumeState = null;
        pausedAt = null;
        lastProgressAt = OffsetDateTime.now();
    }
    private void pauseFrom(SessionState expected, SessionState resume,
        String page, String question, String value, String snapshotHash) {
        if (state != expected) throw new IllegalStateException("Preparation session cannot pause from " + state);
        state = SessionState.WAITING_FOR_USER; resumeState = resume;
        currentPage = page; currentQuestion = question; checkpoint = value;
        checkpointSnapshotHash = snapshotHash; pausedAt = lastProgressAt = OffsetDateTime.now();
    }
    void fail() {
        if (!state.active()) {
            throw new IllegalStateException("Only an active preparation session can fail");
        }
        state = SessionState.FAILED;
        completedAt = lastProgressAt = OffsetDateTime.now();
    }

    private void transition(SessionState expected, SessionState next) {
        if (state != expected) {
            throw new IllegalStateException(
                "Preparation session must be " + expected + " before " + next
            );
        }
        state = next;
        lastProgressAt = OffsetDateTime.now();
    }

    @PrePersist void create() { createdAt = updatedAt = OffsetDateTime.now(); }
    @PreUpdate void update() { updatedAt = OffsetDateTime.now(); }

    public Long getId() { return id; }
    public Application getApplication() { return application; }
    public ApplicationFormTarget getFormTarget() { return formTarget; }
    public ApplicationPreparationSession getPreviousSession() { return previousSession; }
    public SessionState getState() { return state; }
    public OffsetDateTime getStartedAt() { return startedAt; }
    public OffsetDateTime getLastProgressAt() { return lastProgressAt; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
    public String getCurrentPage(){return currentPage;} public String getCurrentQuestion(){return currentQuestion;}
    public String getCheckpoint(){return checkpoint;} public String getCheckpointSnapshotHash(){return checkpointSnapshotHash;}
    public SessionState getResumeState(){return resumeState;} public OffsetDateTime getPausedAt(){return pausedAt;}
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
