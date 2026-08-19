package com.chengukargbo.careeros.preparation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chengukargbo.careeros.common.exception.BusinessValidationException;
import com.chengukargbo.careeros.preparation.ObservationDtos.*;
import com.chengukargbo.careeros.preparation.PreparationDtos.*;
import com.chengukargbo.careeros.preparation.PreparationEnums.*;

@Service
@Transactional
public class PreparationWorkerService {
    private final ApplicationPreparationSessionRepository sessions;
    private final PreparationSessionEventRepository events;
    private final FormObservationService observations;
    private final FormObservationSnapshotRepository snapshots;

    public PreparationWorkerService(
        ApplicationPreparationSessionRepository sessions,
        PreparationSessionEventRepository events,
        FormObservationService observations,
        FormObservationSnapshotRepository snapshots
    ) {
        this.sessions = sessions;
        this.events = events;
        this.observations = observations;
        this.snapshots = snapshots;
    }

    public Response opening(Long applicationId, Long sessionId) {
        ApplicationPreparationSession session = session(applicationId, sessionId);
        transition(session, session::beginOpening, EventType.FORM_OPENING,
            "Application form opening started", false);
        return response(session);
    }

    public Response collectingQuestions(Long applicationId, Long sessionId) {
        ApplicationPreparationSession session = session(applicationId, sessionId);
        transition(session, session::beginCollectingQuestions,
            EventType.COLLECTING_QUESTIONS,
            "Application question collection started", false);
        return response(session);
    }

    public SnapshotResponse observations(Long applicationId, Long sessionId,
        SnapshotInput input) {
        ApplicationPreparationSession session = session(applicationId, sessionId);
        if (session.getState() != SessionState.COLLECTING_QUESTIONS) {
            throw new BusinessValidationException(
                "Preparation session must be collecting questions before recording observations"
            );
        }
        SnapshotResponse snapshot = observations.reconcile(
            applicationId, session, input
        );
        events.save(new PreparationSessionEvent(
            session, EventType.OBSERVATION_CAPTURED, false,
            "Application form observation captured", null, null
        ));
        transition(session, () -> session.waitForUser(snapshot.fingerprint()), EventType.WAITING_FOR_USER,
            "Question observation complete; waiting for user review", false);
        return snapshot;
    }

    public Response pause(Long applicationId, Long sessionId, PauseRequest request) {
        ApplicationPreparationSession session = session(applicationId, sessionId);
        validateSnapshot(applicationId, request.snapshotHash());
        try {
            session.pause(trim(request.currentPage()), trim(request.currentQuestion()),
                request.checkpoint().trim(), request.snapshotHash());
        } catch (IllegalStateException exception) {
            throw new BusinessValidationException(exception.getMessage());
        }
        sessions.saveAndFlush(session);
        events.save(new PreparationSessionEvent(session, EventType.SESSION_PAUSED, false,
            "Preparation paused at the last safe checkpoint", session.getCurrentPage(),
            session.getCurrentQuestion()));
        return response(session);
    }

    private void validateSnapshot(Long applicationId, String hash) {
        if (hash == null) return;
        String latest = snapshots.findFirstByFormTargetApplicationIdOrderBySequenceNumberDesc(applicationId)
            .map(FormObservationSnapshot::getSnapshotFingerprint).orElse(null);
        if (!hash.equals(latest)) throw new BusinessValidationException("Preparation checkpoint is stale");
    }

    private String trim(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    public Response failed(Long applicationId, Long sessionId,
        WorkerFailureRequest request) {
        ApplicationPreparationSession session = session(applicationId, sessionId);
        try {
            session.fail();
        } catch (IllegalStateException exception) {
            throw new BusinessValidationException(exception.getMessage());
        }
        sessions.saveAndFlush(session);
        events.save(new PreparationSessionEvent(
            session, EventType.SESSION_FAILED, request.retryable(),
            request.safeUserMessage().trim(), null, null, request.failureCode()
        ));
        return response(session);
    }

    private void transition(ApplicationPreparationSession session,
        Runnable action, EventType type, String message, boolean retryable) {
        try {
            action.run();
        } catch (IllegalStateException exception) {
            throw new BusinessValidationException(exception.getMessage());
        }
        sessions.saveAndFlush(session);
        events.save(new PreparationSessionEvent(
            session, type, retryable, message, null, null
        ));
    }

    private ApplicationPreparationSession session(Long applicationId,
        Long sessionId) {
        return sessions.findByIdAndApplicationId(sessionId, applicationId)
            .orElseThrow(() -> new BusinessValidationException(
                "Preparation session not found for application"
            ));
    }

    private Response response(ApplicationPreparationSession session) {
        return new Response(
            PreparationCapability.FIELD_PREPARATION,
            PreparationDtos.Session.from(session)
        );
    }
}
