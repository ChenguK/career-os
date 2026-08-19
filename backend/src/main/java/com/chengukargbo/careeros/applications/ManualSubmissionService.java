package com.chengukargbo.careeros.applications;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chengukargbo.careeros.applications.ManualSubmissionDtos.Response;
import com.chengukargbo.careeros.applications.dto.ApplicationResponse;
import com.chengukargbo.careeros.applications.history.ApplicationStatusHistoryService;
import com.chengukargbo.careeros.applications.history.ApplicationTransitionSource;
import com.chengukargbo.careeros.applications.lock.ApplicationLockService;
import com.chengukargbo.careeros.common.exception.BusinessValidationException;

@Service
@Transactional
public class ManualSubmissionService {
    private final ApplicationRepository applications;
    private final ApplicationStatusHistoryService statusHistory;
    private final ApplicationLockService locks;

    public ManualSubmissionService(
        ApplicationRepository applications,
        ApplicationStatusHistoryService statusHistory,
        ApplicationLockService locks
    ) {
        this.applications = applications;
        this.statusHistory = statusHistory;
        this.locks = locks;
    }

    public Response markApplied(Long applicationId, LocalDate applicationDate) {
        if (applicationDate == null) {
            throw new BusinessValidationException("Application date is required");
        }
        if (applicationDate.isAfter(LocalDate.now())) {
            throw new BusinessValidationException("Application date cannot be in the future");
        }

        Application application = applications.findById(applicationId)
            .orElseThrow(() -> new ApplicationNotFoundException(applicationId));
        ApplicationStatus previous = application.getStatus();
        if (previous != ApplicationStatus.SAVED
            && previous != ApplicationStatus.PREPARING) {
            throw new BusinessValidationException(
                "Only Saved or Preparing applications can be marked as Applied"
            );
        }

        locks.requireManualSubmission(applicationId);
        application.recordManualSubmission(applicationDate);
        Application saved = applications.saveAndFlush(application);
        statusHistory.recordTransition(saved, previous, ApplicationTransitionSource.USER);
        var lock = locks.recordManualSubmission(applicationId);
        return new Response(ApplicationResponse.from(saved), lock);
    }
}
