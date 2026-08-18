package com.chengukargbo.careeros.applications.history;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import com.chengukargbo.careeros.applications.*;

@ExtendWith(MockitoExtension.class)
class ApplicationStatusHistoryServiceTest {
    @Mock ApplicationStatusHistoryRepository repository;
    @Mock ApplicationRepository applicationRepository;
    @InjectMocks ApplicationStatusHistoryService service;

    @Test void initialEventHasNullPreviousStatusAndExplicitSource() {
        Application app = application(ApplicationStatus.SAVED);
        service.recordInitial(app, ApplicationTransitionSource.IMPORT);
        ArgumentCaptor<ApplicationStatusHistory> captor =
            ArgumentCaptor.forClass(ApplicationStatusHistory.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getPreviousStatus()).isNull();
        assertThat(captor.getValue().getNewStatus()).isEqualTo(ApplicationStatus.SAVED);
        assertThat(captor.getValue().getSource()).isEqualTo(ApplicationTransitionSource.IMPORT);
    }

    @Test void recordsOnlyActualTransitionsAndPreservesSequence() {
        Application app = application(ApplicationStatus.SAVED);
        service.recordTransition(app, ApplicationStatus.SAVED,
            ApplicationTransitionSource.USER);
        verifyNoInteractions(repository);
        app.update(ApplicationStatus.PREPARING, null, false, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null);
        service.recordTransition(app, ApplicationStatus.SAVED,
            ApplicationTransitionSource.USER);
        app.update(ApplicationStatus.APPLIED, null, false, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null);
        service.recordTransition(app, ApplicationStatus.PREPARING,
            ApplicationTransitionSource.USER);
        ArgumentCaptor<ApplicationStatusHistory> captor =
            ArgumentCaptor.forClass(ApplicationStatusHistory.class);
        verify(repository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues()).extracting(ApplicationStatusHistory::getNewStatus)
            .containsExactly(ApplicationStatus.PREPARING, ApplicationStatus.APPLIED);
    }

    @Test void readsOldestFirstAndRejectsMissingApplication() {
        when(applicationRepository.existsById(7L)).thenReturn(true);
        when(repository.findByApplicationIdOrderByOccurredAtAscIdAsc(7L))
            .thenReturn(List.of());
        assertThat(service.findForApplication(7L)).isEmpty();
        when(applicationRepository.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> service.findForApplication(99L))
            .isInstanceOf(ApplicationNotFoundException.class);
    }

    private Application application(ApplicationStatus status) {
        Application app = new Application(null, status, null, false, null, null,
            null, null, null, null, null, null, null, null, null, null, null,
            null, null);
        ReflectionTestUtils.setField(app, "id", 7L); return app;
    }
}
