package com.chengukargbo.careeros.preparation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.chengukargbo.careeros.applications.Application;
import com.chengukargbo.careeros.common.exception.BusinessValidationException;
import com.chengukargbo.careeros.preparation.ObservationDtos.*;
import com.chengukargbo.careeros.preparation.PreparationDtos.WorkerFailureRequest;
import com.chengukargbo.careeros.preparation.PreparationEnums.*;

class PreparationWorkerServiceTest {
    private final ApplicationPreparationSessionRepository sessions =
        mock(ApplicationPreparationSessionRepository.class);
    private final PreparationSessionEventRepository events =
        mock(PreparationSessionEventRepository.class);
    private final FormObservationService observations = mock(FormObservationService.class);
    private final FormObservationSnapshotRepository snapshots = mock(FormObservationSnapshotRepository.class);
    private PreparationWorkerService service;
    private ApplicationPreparationSession session;

    @BeforeEach
    void setUp() {
        service = new PreparationWorkerService(sessions, events, observations, snapshots);
        Application application = mock(Application.class);
        when(application.getId()).thenReturn(12L);
        ApplicationFormTarget target = new ApplicationFormTarget(
            application, "https://jobs.ashbyhq.com/acme/job-1"
        );
        session = new ApplicationPreparationSession(application, target, null);
        when(sessions.findByIdAndApplicationId(7L, 12L))
            .thenReturn(Optional.of(session));
        when(sessions.saveAndFlush(any())).thenAnswer(call -> call.getArgument(0));
        when(events.save(any())).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void enforcesInspectionSequenceAndRecordsOneSnapshot() {
        SnapshotInput input = new SnapshotInput(null, List.of());
        when(observations.reconcile(12L, session, input)).thenReturn(
            new SnapshotResponse(3L, 7L, null, 1, "a".repeat(64),
                java.time.OffsetDateTime.now(), 0, 0)
        );

        assertEquals(SessionState.OPENING,
            service.opening(12L, 7L).session().state());
        assertEquals(SessionState.COLLECTING_QUESTIONS,
            service.collectingQuestions(12L, 7L).session().state());
        assertEquals(3L, service.observations(12L, 7L, input).id());
        assertEquals(SessionState.WAITING_FOR_USER, session.getState());
        verify(observations).reconcile(12L, session, input);
        verify(events, times(4)).save(any());
    }

    @Test
    void rejectsOutOfOrderAndWrongSessionCommands() {
        assertThrows(BusinessValidationException.class,
            () -> service.collectingQuestions(12L, 7L));
        when(sessions.findByIdAndApplicationId(99L, 12L))
            .thenReturn(Optional.empty());
        assertThrows(BusinessValidationException.class,
            () -> service.opening(12L, 99L));
        verifyNoInteractions(observations);
    }

    @Test
    void recordsSafeRetryableFailureWithoutBrowserDetails() {
        var response = service.failed(
            12L, 7L, new WorkerFailureRequest("Form could not be inspected", true)
        );
        assertEquals(SessionState.FAILED, response.session().state());
        assertNotNull(session.getCompletedAt());
        verify(events).save(argThat(event ->
            event.getEventType() == EventType.SESSION_FAILED
                && event.isRetryable()
                && event.getSafeUserMessage().equals("Form could not be inspected")));
    }

    @Test
    void pausesWithDurableCheckpointAndRejectsStaleSnapshot() {
        service.opening(12L, 7L);
        FormObservationSnapshot snapshot = mock(FormObservationSnapshot.class);
        when(snapshot.getSnapshotFingerprint()).thenReturn("a".repeat(64));
        when(snapshots.findFirstByFormTargetApplicationIdOrderBySequenceNumberDesc(12L))
            .thenReturn(Optional.of(snapshot));

        var paused = service.pause(12L, 7L, new PreparationDtos.PauseRequest(
            "application", "email", "field:email", "a".repeat(64)));

        assertEquals(SessionState.WAITING_FOR_USER, paused.session().state());
        assertEquals("application", paused.session().currentPage());
        assertEquals("email", paused.session().currentQuestion());
        assertEquals("field:email", paused.session().checkpoint());
        assertEquals(SessionState.OPENING, paused.session().resumeState());
        verify(events).save(argThat(event -> event.getEventType() == EventType.SESSION_PAUSED));

        Application application = mock(Application.class); when(application.getId()).thenReturn(13L);
        ApplicationPreparationSession stale = new ApplicationPreparationSession(application,
            new ApplicationFormTarget(application,"https://jobs.ashbyhq.com/acme/2"),null);
        when(sessions.findByIdAndApplicationId(8L,13L)).thenReturn(Optional.of(stale));
        when(snapshots.findFirstByFormTargetApplicationIdOrderBySequenceNumberDesc(13L))
            .thenReturn(Optional.empty());
        assertThrows(BusinessValidationException.class, () -> service.pause(13L,8L,
            new PreparationDtos.PauseRequest(null,null,"opening","b".repeat(64))));
    }
}
