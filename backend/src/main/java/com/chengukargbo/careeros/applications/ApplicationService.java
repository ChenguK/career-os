package com.chengukargbo.careeros.applications;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chengukargbo.careeros.applications.dto.ApplicationRequest;
import com.chengukargbo.careeros.applications.dto.ApplicationResponse;
import com.chengukargbo.careeros.common.exception.BusinessValidationException;
import com.chengukargbo.careeros.jobs.JobOpportunity;
import com.chengukargbo.careeros.jobs.JobOpportunityNotFoundException;
import com.chengukargbo.careeros.jobs.JobOpportunityRepository;

@Service
@Transactional
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobOpportunityRepository jobRepository;

    public ApplicationService(
        ApplicationRepository applicationRepository,
        JobOpportunityRepository jobRepository
    ) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
    }

    public ApplicationResponse create(
        ApplicationRequest request
    ) {
        if (
            applicationRepository.existsByJobOpportunityId(
                request.jobOpportunityId()
            )
        ) {
            throw new BusinessValidationException(
                "An application already exists for this job opportunity"
            );
        }

        JobOpportunity job = findJob(request.jobOpportunityId());

        Application application = new Application(
            job,
            request.status() == null
                ? ApplicationStatus.SAVED
                : request.status(),
            normalize(request.resumeVersion()),
            request.coverLetterNeeded(),
            normalize(request.portfolioLink()),
            normalize(request.githubLink()),
            normalize(request.projectsToHighlight()),
            normalize(request.skillsToEmphasize()),
            normalize(request.interviewTopics()),
            normalize(request.recruiterName()),
            normalize(request.recruiterEmail()),
            request.applicationDate(),
            request.followUpDate(),
            request.phoneScreenAt(),
            request.interviewOneAt(),
            request.interviewTwoAt(),
            request.offerAt(),
            request.rejectedAt(),
            normalize(request.notes())
        );

        Application saved =
            applicationRepository.saveAndFlush(application);

        return ApplicationResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> findAll() {
        return applicationRepository
            .findAllByOrderByUpdatedAtDesc()
            .stream()
            .map(ApplicationResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public ApplicationResponse findById(Long id) {
        return ApplicationResponse.from(findEntityById(id));
    }

    public ApplicationResponse update(
        Long id,
        ApplicationRequest request
    ) {
        Application application = findEntityById(id);

        if (
            !application
                .getJobOpportunity()
                .getId()
                .equals(request.jobOpportunityId())
        ) {
            throw new BusinessValidationException(
                "The job opportunity cannot be changed after an application is created"
            );
        }

        application.update(
            request.status() == null
                ? ApplicationStatus.SAVED
                : request.status(),
            normalize(request.resumeVersion()),
            request.coverLetterNeeded(),
            normalize(request.portfolioLink()),
            normalize(request.githubLink()),
            normalize(request.projectsToHighlight()),
            normalize(request.skillsToEmphasize()),
            normalize(request.interviewTopics()),
            normalize(request.recruiterName()),
            normalize(request.recruiterEmail()),
            request.applicationDate(),
            request.followUpDate(),
            request.phoneScreenAt(),
            request.interviewOneAt(),
            request.interviewTwoAt(),
            request.offerAt(),
            request.rejectedAt(),
            normalize(request.notes())
        );

        Application saved =
            applicationRepository.saveAndFlush(application);

        return ApplicationResponse.from(saved);
    }

    public void delete(Long id) {
        applicationRepository.delete(findEntityById(id));
    }

    private Application findEntityById(Long id) {
        return applicationRepository.findById(id)
            .orElseThrow(
                () -> new ApplicationNotFoundException(id)
            );
    }

    private JobOpportunity findJob(Long id) {
        return jobRepository.findById(id)
            .orElseThrow(
                () -> new JobOpportunityNotFoundException(id)
            );
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty() ? null : trimmed;
    }
}