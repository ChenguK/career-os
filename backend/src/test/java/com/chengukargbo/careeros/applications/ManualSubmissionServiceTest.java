package com.chengukargbo.careeros.applications;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.chengukargbo.careeros.applications.history.ApplicationStatusHistoryService;
import com.chengukargbo.careeros.applications.history.ApplicationTransitionSource;
import com.chengukargbo.careeros.applications.lock.ApplicationLockDtos;
import com.chengukargbo.careeros.applications.lock.ApplicationLockService;
import com.chengukargbo.careeros.applications.lock.ApplicationLockState;
import com.chengukargbo.careeros.common.exception.BusinessValidationException;
import com.chengukargbo.careeros.jobs.JobOpportunity;
import com.chengukargbo.careeros.jobs.RemoteType;

class ManualSubmissionServiceTest {
    private final ApplicationRepository applications = mock(ApplicationRepository.class);
    private final ApplicationStatusHistoryService history = mock(ApplicationStatusHistoryService.class);
    private final ApplicationLockService locks = mock(ApplicationLockService.class);
    private final ManualSubmissionService service = new ManualSubmissionService(applications, history, locks);
    private Application application;

    @BeforeEach
    void setUp() {
        JobOpportunity job = new JobOpportunity(null, "Engineer", null, null,
            RemoteType.UNKNOWN, null, null, null, "USD", null, null, null,
            null, null, (short) 3, null, null, null);
        ReflectionTestUtils.setField(job, "id", 5L);
        application = new Application(job, ApplicationStatus.SAVED, null, false,
            null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null);
        ReflectionTestUtils.setField(application, "id", 9L);
        when(applications.findById(9L)).thenReturn(Optional.of(application));
        when(applications.saveAndFlush(application)).thenReturn(application);
        when(locks.recordManualSubmission(9L)).thenReturn(new ApplicationLockDtos.Response(
            1L, 9L, ApplicationLockState.SUBMITTED, null,
            "User recorded manual application submission", null, null));
    }

    @Test
    void coordinatesSavedApplicationDateHistoryAndLock() {
        LocalDate date = LocalDate.now().minusDays(8);

        var response = service.markApplied(9L, date);

        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.APPLIED);
        assertThat(application.getApplicationDate()).isEqualTo(date);
        assertThat(response.lock().lockState()).isEqualTo(ApplicationLockState.SUBMITTED);
        verify(locks).requireManualSubmission(9L);
        verify(history).recordTransition(application, ApplicationStatus.SAVED,
            ApplicationTransitionSource.USER);
        verify(locks).recordManualSubmission(9L);
    }

    @Test
    void allowsPreparingAndToday() {
        ReflectionTestUtils.setField(application, "status", ApplicationStatus.PREPARING);
        service.markApplied(9L, LocalDate.now());
        verify(history).recordTransition(application, ApplicationStatus.PREPARING,
            ApplicationTransitionSource.USER);
    }

    @Test
    void rejectsFutureDateBeforeMutation() {
        assertThatThrownBy(() -> service.markApplied(9L, LocalDate.now().plusDays(1)))
            .isInstanceOf(BusinessValidationException.class)
            .hasMessageContaining("future");
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.SAVED);
        verifyNoInteractions(history, locks);
    }

    @Test
    void rejectsDuplicateAndLaterLifecycleStates() {
        for (ApplicationStatus status : new ApplicationStatus[] {
            ApplicationStatus.APPLIED, ApplicationStatus.PHONE_SCREEN,
            ApplicationStatus.INTERVIEW_ONE, ApplicationStatus.OFFER,
            ApplicationStatus.REJECTED
        }) {
            ReflectionTestUtils.setField(application, "status", status);
            assertThatThrownBy(() -> service.markApplied(9L, LocalDate.now()))
                .isInstanceOf(BusinessValidationException.class);
        }
        verifyNoInteractions(history, locks);
    }
}
