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

@Service
@Transactional(readOnly = true)
public class ApplicationTrackerService {

    private final JobOpportunityRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationTrackerQueryEngine queryEngine;

    public ApplicationTrackerService(
        JobOpportunityRepository jobRepository,
        ApplicationRepository applicationRepository,
        ApplicationTrackerQueryEngine queryEngine
    ) {
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.queryEngine = queryEngine;
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
            .map(job -> ApplicationTrackerResponse.from(
                job,
                applicationsByJobId.get(job.getId())
            ))
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

        return ApplicationTrackerResponse.from(job, application);
    }
}
