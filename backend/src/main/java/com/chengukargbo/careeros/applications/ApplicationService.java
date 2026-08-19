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
import com.chengukargbo.careeros.applications.history.ApplicationStatusHistoryService;
import com.chengukargbo.careeros.applications.history.ApplicationTransitionSource;
import com.chengukargbo.careeros.automation.ApplicationAutomationService;
import com.chengukargbo.careeros.materials.CareerMaterial;
import com.chengukargbo.careeros.materials.CareerMaterialService;
import com.chengukargbo.careeros.applications.lock.ApplicationLockService;
import com.chengukargbo.careeros.applications.lock.ApplicationLockGuard;

@Service
@Transactional
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobOpportunityRepository jobRepository;
    private final ApplicationStatusHistoryService historyService;
    private final ApplicationAutomationService automationService;
    private final CareerMaterialService materialService;
    private final ApplicationLockService lockService;
    private final ApplicationLockGuard lockGuard;

    public ApplicationService(
        ApplicationRepository applicationRepository,
        JobOpportunityRepository jobRepository,
        ApplicationStatusHistoryService historyService,
        ApplicationAutomationService automationService,
        CareerMaterialService materialService,
        ApplicationLockService lockService,
        ApplicationLockGuard lockGuard
    ) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.historyService = historyService;
        this.automationService = automationService;
        this.materialService = materialService;
        this.lockService = lockService;
        this.lockGuard = lockGuard;
    }

    public ApplicationResponse create(
        ApplicationRequest request
    ) {
        return create(request, ApplicationTransitionSource.USER);
    }

    public ApplicationResponse createFromImport(ApplicationRequest request) {
        return create(request, ApplicationTransitionSource.IMPORT);
    }

    private ApplicationResponse create(ApplicationRequest request,
        ApplicationTransitionSource source) {
        if (source == ApplicationTransitionSource.USER
            && request.status() == ApplicationStatus.APPLIED) {
            throw new BusinessValidationException(
                "Use Mark as Applied to record a submitted application"
            );
        }
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
            resolveMaterial(request.resumeMaterialId(), null),
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
        historyService.recordInitial(saved, source);
        automationService.initialize(saved);
        lockService.initialize(saved);

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
        ApplicationStatus previousStatus = application.getStatus();
        if (request.status() == ApplicationStatus.APPLIED
            && previousStatus != ApplicationStatus.APPLIED) {
            throw new BusinessValidationException(
                "Use Mark as Applied to record a submitted application"
            );
        }
        Long currentMaterialId = application.getResumeMaterial() == null
            ? null : application.getResumeMaterial().getId();
        String requestedLegacyResume = normalize(request.resumeVersion());
        if (!java.util.Objects.equals(currentMaterialId, request.resumeMaterialId())
            || (currentMaterialId == null
                && !java.util.Objects.equals(application.getResumeVersion(), requestedLegacyResume))) {
            lockGuard.requireMaterialChange(id);
        }
        if (currentMaterialId != null) requestedLegacyResume = application.getResumeVersion();

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
            requestedLegacyResume,
            resolveMaterial(request.resumeMaterialId(), application),
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
        historyService.recordTransition(saved, previousStatus,
            ApplicationTransitionSource.USER);

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

    private CareerMaterial resolveMaterial(Long id, Application application) {
        if (id == null) return null;
        if (application != null && application.getResumeMaterial() != null
            && application.getResumeMaterial().getId().equals(id)) return application.getResumeMaterial();
        return materialService.requireOwnedActive(id);
    }
}
