package com.chengukargbo.careeros.applications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.chengukargbo.careeros.applications.dto.ApplicationRequest;
import com.chengukargbo.careeros.applications.history.*;
import com.chengukargbo.careeros.jobs.*;
import com.chengukargbo.careeros.automation.ApplicationAutomationService;
import com.chengukargbo.careeros.materials.CareerMaterialService;
import com.chengukargbo.careeros.applications.lock.*;

@ExtendWith(MockitoExtension.class)
class ApplicationLifecycleServiceTest {
    @Mock ApplicationRepository applicationRepository;
    @Mock JobOpportunityRepository jobRepository;
    @Mock ApplicationStatusHistoryService historyService;
    @Mock ApplicationAutomationService automationService;
    @Mock CareerMaterialService materialService;
    @Mock ApplicationLockService lockService;
    @Mock ApplicationLockGuard lockGuard;
    @InjectMocks ApplicationService service;

    @Test void manualAndImportCreationAssignTrustedSources() {
        JobOpportunity job = job();
        when(jobRepository.findById(5L)).thenReturn(Optional.of(job));
        when(applicationRepository.saveAndFlush(any())).thenAnswer(invocation -> {
            Application value = invocation.getArgument(0);
            ReflectionTestUtils.setField(value, "id", 7L);
            value.onCreate();
            return value;
        });
        service.create(request(ApplicationStatus.SAVED, "first"));
        service.createFromImport(request(ApplicationStatus.APPLIED, "imported"));
        verify(historyService).recordInitial(any(Application.class),
            eq(ApplicationTransitionSource.USER));
        verify(historyService).recordInitial(any(Application.class),
            eq(ApplicationTransitionSource.IMPORT));
        verify(automationService, times(2)).initialize(any(Application.class));
        verify(lockService, times(2)).initialize(any(Application.class));
    }

    @Test void updatePassesThePersistedPreviousStatusToCentralHistory() {
        Application application = application(ApplicationStatus.SAVED);
        when(applicationRepository.findById(7L)).thenReturn(Optional.of(application));
        when(applicationRepository.saveAndFlush(application)).thenReturn(application);
        service.update(7L, request(ApplicationStatus.PREPARING, "changed"));
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.PREPARING);
        verify(historyService).recordTransition(application, ApplicationStatus.SAVED,
            ApplicationTransitionSource.USER);
        assertThat(application.getPhoneScreenAt()).isNull();
    }

    @Test void genericUserEditingCannotCreateAppliedWithoutCoordination() {
        assertThatThrownBy(() -> service.create(request(ApplicationStatus.APPLIED, "manual")))
            .isInstanceOf(com.chengukargbo.careeros.common.exception.BusinessValidationException.class)
            .hasMessageContaining("Mark as Applied");

        Application application = application(ApplicationStatus.SAVED);
        when(applicationRepository.findById(7L)).thenReturn(Optional.of(application));
        assertThatThrownBy(() -> service.update(7L, request(ApplicationStatus.APPLIED, "manual")))
            .isInstanceOf(com.chengukargbo.careeros.common.exception.BusinessValidationException.class)
            .hasMessageContaining("Mark as Applied");
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.SAVED);
    }

    private ApplicationRequest request(ApplicationStatus status, String notes) {
        return new ApplicationRequest(5L, status, null, null, false, null, null,
            null, null, null, null, null, null, null, null, null, null, null,
            null, notes);
    }
    private JobOpportunity job() {
        JobOpportunity job = new JobOpportunity(null, "Engineer", null, null,
            RemoteType.UNKNOWN, null, null, null, "USD", null, null, null,
            null, null, (short) 3, null, null, null);
        ReflectionTestUtils.setField(job, "id", 5L); return job;
    }
    private Application application(ApplicationStatus status) {
        Application app = new Application(job(), status, null, false, null, null,
            null, null, null, null, null, null, null, null, null, null, null,
            null, "old");
        ReflectionTestUtils.setField(app, "id", 7L); return app;
    }
}
