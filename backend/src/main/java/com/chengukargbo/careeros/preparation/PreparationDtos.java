package com.chengukargbo.careeros.preparation;

import java.time.OffsetDateTime;

import com.chengukargbo.careeros.preparation.PreparationEnums.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class PreparationDtos {
    private PreparationDtos() {}

    public record Response(
        PreparationCapability capability,
        Session session
    ) {}

    public record Session(
        Long id, Long applicationId, Long formTargetId, Long previousSessionId,
        SessionState state, String normalizedFormUrl,
        OffsetDateTime startedAt, OffsetDateTime lastProgressAt,
        OffsetDateTime completedAt, OffsetDateTime createdAt,
        OffsetDateTime updatedAt, String currentPage, String currentQuestion,
        String checkpoint, String snapshotHash, SessionState resumeState,
        OffsetDateTime pausedAt
    ) {
        static Session from(ApplicationPreparationSession session) {
            return new Session(
                session.getId(), session.getApplication().getId(),
                session.getFormTarget().getId(),
                session.getPreviousSession() == null ? null
                    : session.getPreviousSession().getId(),
                session.getState(), session.getFormTarget().getNormalizedFormUrl(),
                session.getStartedAt(), session.getLastProgressAt(),
                session.getCompletedAt(), session.getCreatedAt(), session.getUpdatedAt(),
                session.getCurrentPage(), session.getCurrentQuestion(), session.getCheckpoint(),
                session.getCheckpointSnapshotHash(), session.getResumeState(), session.getPausedAt()
            );
        }
    }

    public record Event(
        Long id, Long sessionId, EventType eventType, OffsetDateTime timestamp,
        boolean retryable, String safeUserMessage, String pageKey,
        String questionKey, ProviderFailureCode providerFailureCode
    ) {
        public Event(Long id, Long sessionId, EventType eventType,
            OffsetDateTime timestamp, boolean retryable, String safeUserMessage,
            String pageKey, String questionKey) {
            this(id, sessionId, eventType, timestamp, retryable,
                safeUserMessage, pageKey, questionKey, null);
        }

        static Event from(PreparationSessionEvent event) {
            return new Event(
                event.getId(), event.getSession().getId(), event.getEventType(),
                event.getOccurredAt(), event.isRetryable(),
                event.getSafeUserMessage(), event.getPageKey(),
                event.getQuestionKey(), event.getProviderFailureCode()
            );
        }
    }

    public record WorkerFailureRequest(
        @NotBlank @Size(max = 1000) String safeUserMessage,
        boolean retryable,
        ProviderFailureCode failureCode
    ) {
        public WorkerFailureRequest(String safeUserMessage, boolean retryable) {
            this(safeUserMessage, retryable, null);
        }
    }

    public record PauseRequest(
        @Size(max=200) String currentPage,
        @Size(max=200) String currentQuestion,
        @NotBlank @Size(max=4000) String checkpoint,
        @jakarta.validation.constraints.Pattern(regexp="^[0-9a-f]{64}$",
            message="Snapshot hash must be 64 lowercase hexadecimal characters")
        String snapshotHash
    ) {}
}
