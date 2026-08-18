package com.chengukargbo.careeros.preparation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.chengukargbo.careeros.applications.*;
import com.chengukargbo.careeros.automation.ApplicationAutomationService;
import com.chengukargbo.careeros.automation.AutomationDtos.Response;
import com.chengukargbo.careeros.automation.AutomationEnums.*;
import com.chengukargbo.careeros.common.exception.BusinessValidationException;
import com.chengukargbo.careeros.common.url.ApplicationUrlService;
import com.chengukargbo.careeros.jobs.*;
import com.chengukargbo.careeros.preparation.PreparationEnums.*;

class ApplicationPreparationServiceTest {
    private final ApplicationRepository applications = mock(ApplicationRepository.class);
    private final JobOpportunityRepository jobs = mock(JobOpportunityRepository.class);
    private final ApplicationFormTargetRepository targets = mock(ApplicationFormTargetRepository.class);
    private final ApplicationPreparationSessionRepository sessions = mock(ApplicationPreparationSessionRepository.class);
    private final PreparationSessionEventRepository events = mock(PreparationSessionEventRepository.class);
    private final ApplicationAutomationService automation = mock(ApplicationAutomationService.class);
    private final ApplicationUrlService urls = new ApplicationUrlService();
    private final FormObservationSnapshotRepository snapshots = mock(FormObservationSnapshotRepository.class);
    private ApplicationPreparationService service;
    private Application application;
    private JobOpportunity job;

    @BeforeEach
    void setUp() {
        service = new ApplicationPreparationService(
            applications, jobs, targets, sessions, events, automation, urls, snapshots
        );
        application = mock(Application.class);
        job = mock(JobOpportunity.class);
        when(application.getId()).thenReturn(12L);
        when(application.getJobOpportunity()).thenReturn(job);
        when(job.getApplicationUrl()).thenReturn("https://jobs.example.com/apply/12");
        when(job.getNormalizedApplicationUrl()).thenReturn(
            "https://jobs.example.com/apply/12"
        );
        when(applications.findById(12L)).thenReturn(Optional.of(application));
        when(automation.get(12L)).thenReturn(automation(State.APPROVED_FOR_PREP));
        when(sessions.findByApplicationIdOrderByCreatedAtDescIdDesc(12L))
            .thenReturn(List.of());
        when(sessions.findFirstByApplicationIdOrderByCreatedAtDescIdDesc(12L))
            .thenReturn(Optional.empty());
        when(targets.findByApplicationId(12L)).thenReturn(Optional.empty());
        when(targets.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(sessions.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(events.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void initializesOneSessionAndReportsSessionOnlyCapability() {
        var response = service.initialize(12L);

        assertEquals(PreparationCapability.FIELD_PREPARATION, response.capability());
        assertEquals(SessionState.INITIALIZED, response.session().state());
        assertEquals("https://jobs.example.com/apply/12",
            response.session().normalizedFormUrl());
        verify(events).save(argThat(event ->
            event.getEventType() == EventType.SESSION_INITIALIZED));
        verifyNoInteractionsBeyondPreparationScope();
    }

    @Test
    void rejectsDuplicateInitializationAndMissingPermission() {
        ApplicationFormTarget target = new ApplicationFormTarget(
            application, "https://jobs.example.com/apply/12"
        );
        ApplicationPreparationSession active =
            new ApplicationPreparationSession(application, target, null);
        when(sessions.findByApplicationIdOrderByCreatedAtDescIdDesc(12L))
            .thenReturn(List.of(active));
        assertThrows(BusinessValidationException.class,
            () -> service.initialize(12L));

        when(sessions.findByApplicationIdOrderByCreatedAtDescIdDesc(12L))
            .thenReturn(List.of());
        when(automation.get(12L)).thenReturn(automation(State.NOT_APPROVED));
        assertThrows(BusinessValidationException.class,
            () -> service.initialize(12L));
    }

    @Test
    void cancellationIsHistoricalAndRetryCreatesANewSession() {
        ApplicationFormTarget target = new ApplicationFormTarget(
            application, "https://jobs.example.com/apply/12"
        );
        ApplicationPreparationSession first =
            new ApplicationPreparationSession(application, target, null);
        when(sessions.findByApplicationIdOrderByCreatedAtDescIdDesc(12L))
            .thenReturn(List.of(first));

        var cancelled = service.cancel(12L);
        assertEquals(SessionState.CANCELLED, cancelled.session().state());
        assertNotNull(cancelled.session().completedAt());
        verify(events).save(argThat(event ->
            event.getEventType() == EventType.SESSION_CANCELLED
                && event.isRetryable()));

        when(sessions.findByApplicationIdOrderByCreatedAtDescIdDesc(12L))
            .thenReturn(List.of());
        when(sessions.findFirstByApplicationIdOrderByCreatedAtDescIdDesc(12L))
            .thenReturn(Optional.of(first));
        when(targets.findByApplicationId(12L)).thenReturn(Optional.of(target));
        var retried = service.retry(12L);

        assertEquals(SessionState.INITIALIZED, retried.session().state());
        verify(sessions).saveAndFlush(argThat(session ->
            session != first && session.getPreviousSession() == first));
        verify(events).save(argThat(event ->
            event.getEventType() == EventType.SESSION_RETRY_INITIALIZED));
    }

    @Test
    void retryRejectsNonterminalAndReadySessions() {
        assertThrows(BusinessValidationException.class,
            () -> service.retry(12L));

        ApplicationFormTarget target = new ApplicationFormTarget(
            application, "https://jobs.example.com/apply/12"
        );
        ApplicationPreparationSession active =
            new ApplicationPreparationSession(application, target, null);
        when(sessions.findByApplicationIdOrderByCreatedAtDescIdDesc(12L))
            .thenReturn(List.of(active));
        assertThrows(BusinessValidationException.class,
            () -> service.retry(12L));
    }

    @Test
    void returnsAppendOnlyEventsInRepositoryOrder() {
        ApplicationFormTarget target = new ApplicationFormTarget(
            application, "https://jobs.example.com/apply/12"
        );
        ApplicationPreparationSession session =
            new ApplicationPreparationSession(application, target, null);
        PreparationSessionEvent event = new PreparationSessionEvent(
            session, EventType.SESSION_INITIALIZED, false,
            "Preparation session initialized", null, null
        );
        when(events.findBySessionApplicationIdOrderByOccurredAtAscIdAsc(12L))
            .thenReturn(List.of(event));

        var result = service.events(12L);
        assertEquals(1, result.size());
        assertEquals("Preparation session initialized",
            result.getFirst().safeUserMessage());
        verify(events, never()).delete(any());
    }

    @Test
    void resumesFromCheckpointAndPreservesItThroughCancellation() {
        ApplicationFormTarget target = new ApplicationFormTarget(application,
            "https://jobs.example.com/apply/12");
        ApplicationPreparationSession paused = new ApplicationPreparationSession(application,target,null);
        paused.pause("page-2","question-4","checkpoint-4",null);
        when(sessions.findByApplicationIdOrderByCreatedAtDescIdDesc(12L)).thenReturn(List.of(paused));

        var resumed = service.resume(12L);
        assertEquals(SessionState.INITIALIZED,resumed.session().state());
        assertEquals("checkpoint-4",resumed.session().checkpoint());
        verify(events).save(argThat(event->event.getEventType()==EventType.SESSION_RESUMED));

        var cancelled=service.cancel(12L);
        assertEquals(SessionState.CANCELLED,cancelled.session().state());
        assertEquals("checkpoint-4",cancelled.session().checkpoint());
    }

    @Test
    void rejectsResumeWhenCheckpointSnapshotIsStale() {
        ApplicationFormTarget target = new ApplicationFormTarget(application,
            "https://jobs.example.com/apply/12");
        ApplicationPreparationSession paused = new ApplicationPreparationSession(application,target,null);
        paused.pause("page","question","checkpoint","a".repeat(64));
        when(sessions.findByApplicationIdOrderByCreatedAtDescIdDesc(12L)).thenReturn(List.of(paused));
        FormObservationSnapshot latest=mock(FormObservationSnapshot.class);
        when(latest.getSnapshotFingerprint()).thenReturn("b".repeat(64));
        when(snapshots.findFirstByFormTargetApplicationIdOrderBySequenceNumberDesc(12L))
            .thenReturn(Optional.of(latest));

        assertThrows(BusinessValidationException.class,()->service.resume(12L));
        assertEquals(SessionState.WAITING_FOR_USER,paused.getState());
        verify(events,never()).save(argThat(event->event.getEventType()==EventType.SESSION_RESUMED));
    }

    private Response automation(State state) {
        return new Response(
            1L, 12L, state, SubmissionMode.PREPARE_ONLY, AtsType.UNKNOWN,
            0, 0, 0, null, OffsetDateTime.now(), null, null,
            OffsetDateTime.now()
        );
    }

    private void verifyNoInteractionsBeyondPreparationScope() {
        verifyNoInteractions(jobs);
    }
}
