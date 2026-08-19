package com.chengukargbo.careeros.applications;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chengukargbo.careeros.applications.dto.ApplicationTrackerResponse;
import com.chengukargbo.careeros.applications.tracker.ApplicationTrackerPageResponse;
import com.chengukargbo.careeros.applications.tracker.ApplicationTrackerQuery;
import com.chengukargbo.careeros.applications.tracker.ApplicationTrackerQueryEngine;
import com.chengukargbo.careeros.jobs.JobOpportunity;
import com.chengukargbo.careeros.jobs.JobOpportunityNotFoundException;
import com.chengukargbo.careeros.jobs.JobOpportunityRepository;
import com.chengukargbo.careeros.automation.ApplicationAutomationService;
import com.chengukargbo.careeros.applications.history.ApplicationStatusHistoryService;
import com.chengukargbo.careeros.applications.lock.ApplicationLockGuard;

@Service
@Transactional(readOnly = true)
public class ApplicationTrackerService {

    private final JobOpportunityRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationTrackerQueryEngine queryEngine;
    private final ApplicationAutomationService automationService;
    private final ApplicationStatusHistoryService statusHistoryService;
    private final ApplicationLockGuard lockGuard;

    public ApplicationTrackerService(
        JobOpportunityRepository jobRepository,
        ApplicationRepository applicationRepository,
        ApplicationTrackerQueryEngine queryEngine,
        ApplicationAutomationService automationService,
        ApplicationStatusHistoryService statusHistoryService,
        ApplicationLockGuard lockGuard
    ) {
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.queryEngine = queryEngine;
        this.automationService = automationService;
        this.statusHistoryService = statusHistoryService;
        this.lockGuard = lockGuard;
    }

    public List<ApplicationTrackerResponse> findAll() {
        Map<Long, Application> applicationsByJobId =
            applicationRepository.findAll()
                .stream()
                .collect(
                    Collectors.toMap(
                        application -> application
                            .getJobOpportunity()
                            .getId(),
                        Function.identity()
                    )
                );

        return jobRepository
            .findAllByOrderByPriorityAscCreatedAtDescIdDesc()
            .stream()
            .map(job -> trackerResponse(job, applicationsByJobId.get(job.getId())))
            .toList();
    }

    public ApplicationTrackerPageResponse findAll(
        ApplicationTrackerQuery query
    ) {
        return queryEngine.execute(findAll(), query);
    }

    public ApplicationTrackerResponse findByJobOpportunityId(
        Long jobOpportunityId
    ) {
        JobOpportunity job = jobRepository.findById(jobOpportunityId)
            .orElseThrow(
                () -> new JobOpportunityNotFoundException(jobOpportunityId)
            );
        Application application = applicationRepository
            .findByJobOpportunityId(jobOpportunityId)
            .orElse(null);

        return trackerResponse(job, application);
    }

    private ApplicationTrackerResponse trackerResponse(JobOpportunity job, Application application) {
        if (application == null) return ApplicationTrackerResponse.from(job, null);
        return ApplicationTrackerResponse.from(
            job,
            application,
            automationService.findExistingState(application.getId()),
            statusHistoryService.latestEventForStatus(application.getId(), application.getStatus()),
            lockGuard.state(application.getId())
        );
    }
}
