package com.chengukargbo.careeros.preparation;

import java.time.OffsetDateTime;

import com.chengukargbo.careeros.preparation.PreparationEnums.EventType;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "preparation_session_events")
public class PreparationSessionEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "session_id", nullable = false)
    private ApplicationPreparationSession session;
    @Enumerated(EnumType.STRING) @Column(name = "event_type", nullable = false, length = 50)
    private EventType eventType;
    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;
    @Column(nullable = false)
    private boolean retryable;
    @Column(name = "safe_user_message", nullable = false, length = 1000)
    private String safeUserMessage;
    @Column(name = "page_key", length = 200)
    private String pageKey;
    @Column(name = "question_key", length = 200)
    private String questionKey;

    protected PreparationSessionEvent() {}

    PreparationSessionEvent(ApplicationPreparationSession session, EventType type,
        boolean retryable, String safeUserMessage, String pageKey, String questionKey) {
        this.session = session;
        eventType = type;
        this.retryable = retryable;
        this.safeUserMessage = safeUserMessage;
        this.pageKey = pageKey;
        this.questionKey = questionKey;
        occurredAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public ApplicationPreparationSession getSession() { return session; }
    public EventType getEventType() { return eventType; }
    public OffsetDateTime getOccurredAt() { return occurredAt; }
    public boolean isRetryable() { return retryable; }
    public String getSafeUserMessage() { return safeUserMessage; }
    public String getPageKey() { return pageKey; }
    public String getQuestionKey() { return questionKey; }
}
