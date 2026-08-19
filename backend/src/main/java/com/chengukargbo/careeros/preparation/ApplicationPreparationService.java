package com.chengukargbo.careeros.preparation;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chengukargbo.careeros.applications.*;
import com.chengukargbo.careeros.automation.ApplicationAutomationService;
import com.chengukargbo.careeros.automation.AutomationEnums.State;
import com.chengukargbo.careeros.common.exception.BusinessValidationException;
import com.chengukargbo.careeros.common.url.ApplicationUrlService;
import com.chengukargbo.careeros.jobs.JobOpportunity;
import com.chengukargbo.careeros.jobs.JobOpportunityRepository;
import com.chengukargbo.careeros.preparation.PreparationDtos.*;
import com.chengukargbo.careeros.preparation.PreparationEnums.*;
import com.chengukargbo.careeros.applications.lock.ApplicationLockGuard;

@Service
@Transactional
public class ApplicationPreparationService {
    public static final PreparationCapability CAPABILITY =
        PreparationCapability.FIELD_PREPARATION;

    private final ApplicationRepository applications;
    private final JobOpportunityRepository jobs;
    private final ApplicationFormTargetRepository targets;
    private final ApplicationPreparationSessionRepository sessions;
    private final PreparationSessionEventRepository events;
    private final ApplicationAutomationService automation;
    private final ApplicationUrlService urls;
    private final FormObservationSnapshotRepository snapshots;
    private final ApplicationLockGuard lockGuard;

    public ApplicationPreparationService(
        ApplicationRepository applications,
        JobOpportunityRepository jobs,
        ApplicationFormTargetRepository targets,
        ApplicationPreparationSessionRepository sessions,
        PreparationSessionEventRepository events,
        ApplicationAutomationService automation,
        ApplicationUrlService urls,
        FormObservationSnapshotRepository snapshots,
        ApplicationLockGuard lockGuard
    ) {
        this.applications = applications;
        this.jobs = jobs;
        this.targets = targets;
        this.sessions = sessions;
        this.events = events;
        this.automation = automation;
        this.urls = urls;
        this.snapshots = snapshots;
        this.lockGuard = lockGuard;
    }

    public Response initialize(Long applicationId) {
        Application application = application(applicationId);
        lockGuard.requireLiveInteraction(applicationId);
        requirePreparationPermission(applicationId);
        if (active(applicationId) != null) {
            throw new BusinessValidationException(
                "An active preparation session already exists"
            );
        }
        return response(create(application, null, EventType.SESSION_INITIALIZED,
            "Preparation session initialized"));
    }

    public Response cancel(Long applicationId) {
        application(applicationId);
        ApplicationPreparationSession session = active(applicationId);
        if (session == null) {
            throw new BusinessValidationException(
                "No active preparation session exists"
            );
        }
        try {
            session.cancel();
        } catch (IllegalStateException exception) {
            throw new BusinessValidationException(exception.getMessage());
        }
        sessions.saveAndFlush(session);
        events.save(new PreparationSessionEvent(
            session, EventType.SESSION_CANCELLED, true,
            "Preparation session cancelled", null, null
        ));
        return response(session);
    }

    public Response retry(Long applicationId) {
        Application application = application(applicationId);
        lockGuard.requireLiveInteraction(applicationId);
        requirePreparationPermission(applicationId);
        if (active(applicationId) != null) {
            throw new BusinessValidationException(
                "Cancel the active preparation session before retrying"
            );
        }
        ApplicationPreparationSession previous = latest(applicationId);
        if (previous == null || (previous.getState() != SessionState.FAILED
            && previous.getState() != SessionState.CANCELLED)) {
            throw new BusinessValidationException(
                "Only a failed or cancelled preparation session can be retried"
            );
        }
        return response(create(
            application, previous, EventType.SESSION_RETRY_INITIALIZED,
            "Preparation retry initialized"
        ));
    }

    public Response resume(Long applicationId) {
        application(applicationId);
        lockGuard.requireLiveInteraction(applicationId);
        requirePreparationPermission(applicationId);
        ApplicationPreparationSession session = active(applicationId);
        if (session == null || session.getState() != SessionState.WAITING_FOR_USER)
            throw new BusinessValidationException("No paused preparation session exists");
        String checkpointHash = session.getCheckpointSnapshotHash();
        if (checkpointHash != null) {
            String latestHash = snapshots
                .findFirstByFormTargetApplicationIdOrderBySequenceNumberDesc(applicationId)
                .map(FormObservationSnapshot::getSnapshotFingerprint).orElse(null);
            if (!checkpointHash.equals(latestHash))
                throw new BusinessValidationException("Preparation checkpoint is stale; inspect the form again");
        }
        try { session.resume(); }
        catch (IllegalStateException exception) { throw new BusinessValidationException(exception.getMessage()); }
        sessions.saveAndFlush(session);
        events.save(new PreparationSessionEvent(session, EventType.SESSION_RESUMED, false,
            "Preparation resumed from the last safe checkpoint", session.getCurrentPage(),
            session.getCurrentQuestion()));
        return response(session);
    }

    @Transactional(readOnly = true)
    public Response get(Long applicationId) {
        application(applicationId);
        return new Response(CAPABILITY, session(latest(applicationId)));
    }

    @Transactional(readOnly = true)
    public List<Event> events(Long applicationId) {
        application(applicationId);
        return events.findBySessionApplicationIdOrderByOccurredAtAscIdAsc(
            applicationId
        ).stream().map(Event::from).toList();
    }

    private ApplicationPreparationSession create(
        Application application,
        ApplicationPreparationSession previous,
        EventType type,
        String message
    ) {
        ApplicationFormTarget target = target(application);
        ApplicationPreparationSession session =
            new ApplicationPreparationSession(application, target, previous);
        try {
            session = sessions.saveAndFlush(session);
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessValidationException(
                "An active preparation session already exists"
            );
        }
        events.save(new PreparationSessionEvent(
            session, type, false, message, null, null
        ));
        return session;
    }

    private ApplicationFormTarget target(Application application) {
        JobOpportunity job = application.getJobOpportunity();
        String normalized = urls.normalize(job.getApplicationUrl());
        if (normalized == null) {
            throw new BusinessValidationException(
                "A valid application URL is required to initialize preparation"
            );
        }
        if (!normalized.equals(job.getNormalizedApplicationUrl())) {
            job.setNormalizedApplicationUrl(normalized);
            jobs.save(job);
        }
        return targets.findByApplicationId(application.getId())
            .map(existing -> {
                existing.confirm(normalized);
                return targets.save(existing);
            })
            .orElseGet(() -> targets.save(
                new ApplicationFormTarget(application, normalized)
            ));
    }

    private void requirePreparationPermission(Long applicationId) {
        State state = automation.get(applicationId).state();
        if (state == State.NOT_APPROVED || state == State.BLOCKED) {
            throw new BusinessValidationException(
                "Application must be approved for preparation"
            );
        }
    }

    private Application application(Long id) {
        return applications.findById(id)
            .orElseThrow(() -> new ApplicationNotFoundException(id));
    }

    private ApplicationPreparationSession active(Long applicationId) {
        return sessions.findByApplicationIdOrderByCreatedAtDescIdDesc(
            applicationId
        ).stream().filter(session -> session.getState().active())
            .findFirst().orElse(null);
    }

    public void cancelActiveForLock(Long applicationId, String lockState) {
        ApplicationPreparationSession session = active(applicationId);
        if (session == null) return;
        session.cancel();
        sessions.saveAndFlush(session);
        events.save(new PreparationSessionEvent(session, EventType.SESSION_CANCELLED, false,
            "Preparation cancelled because application lock changed to " + lockState,
            session.getCurrentPage(), session.getCurrentQuestion()));
    }

    private ApplicationPreparationSession latest(Long applicationId) {
        return sessions.findFirstByApplicationIdOrderByCreatedAtDescIdDesc(
            applicationId
        ).orElse(null);
    }

    private Session session(ApplicationPreparationSession session) {
        return session == null ? null : Session.from(session);
    }

    private Response response(ApplicationPreparationSession session) {
        return new Response(CAPABILITY, Session.from(session));
    }
}
