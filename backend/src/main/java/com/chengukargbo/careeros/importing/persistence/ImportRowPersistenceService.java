package com.chengukargbo.careeros.importing.persistence;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.chengukargbo.careeros.applications.ApplicationService;
import com.chengukargbo.careeros.applications.ApplicationStatus;
import com.chengukargbo.careeros.applications.dto.ApplicationRequest;
import com.chengukargbo.careeros.applications.dto.ApplicationResponse;
import com.chengukargbo.careeros.companies.Company;
import com.chengukargbo.careeros.companies.CompanyRepository;
import com.chengukargbo.careeros.importing.ApplicationUrlNormalizer;
import com.chengukargbo.careeros.importing.CanonicalImportRow;
import com.chengukargbo.careeros.importing.ImportRowResult;
import com.chengukargbo.careeros.jobs.JobOpportunity;
import com.chengukargbo.careeros.jobs.JobOpportunityRepository;
import com.chengukargbo.careeros.jobs.JobOpportunityService;
import com.chengukargbo.careeros.jobs.dto.JobOpportunityRequest;
import com.chengukargbo.careeros.jobs.dto.JobOpportunityResponse;

@Service
public class ImportRowPersistenceService {

    private final CompanyRepository companyRepository;
    private final JobOpportunityRepository jobRepository;
    private final JobOpportunityService jobService;
    private final ApplicationService applicationService;
    private final ApplicationUrlNormalizer urlNormalizer;

    public ImportRowPersistenceService(
        CompanyRepository companyRepository,
        JobOpportunityRepository jobRepository,
        JobOpportunityService jobService,
        ApplicationService applicationService,
        ApplicationUrlNormalizer urlNormalizer
    ) {
        this.companyRepository = companyRepository;
        this.jobRepository = jobRepository;
        this.jobService = jobService;
        this.applicationService = applicationService;
        this.urlNormalizer = urlNormalizer;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ImportRowPersistenceResult persist(ImportRowResult analyzed) {
        CanonicalImportRow row = analyzed.values();
        JobOpportunity duplicate = findUrlDuplicate(row.applicationUrl());
        if (duplicate != null) {
            return new ImportRowPersistenceResult(
                analyzed.rowNumber(),
                ImportRowOutcomeStatus.SKIPPED_DUPLICATE,
                duplicate.getCompany() == null
                    ? null : duplicate.getCompany().getId(),
                null, null, duplicate.getId(),
                List.of("Application URL already exists"), List.of()
            );
        }

        Company company = resolveCompany(row.companyName());
        JobOpportunityResponse job = jobService.create(new JobOpportunityRequest(
            company == null ? null : company.getId(),
            row.positionTitle(),
            row.department(),
            row.location(),
            row.workArrangement(),
            row.employmentType(),
            row.salaryMin(),
            row.salaryMax(),
            row.salaryCurrency(),
            row.salaryNotes(),
            row.applicationUrl(),
            row.source(),
            row.datePosted(),
            row.closingDate(),
            row.priority(),
            row.matchScore(),
            row.jobDescription(),
            row.jobNotes()
        ));

        ApplicationResponse application = applicationService.createFromImport(
            new ApplicationRequest(
                job.id(),
                row.status() == null ? ApplicationStatus.SAVED : row.status(),
                row.resumeVersion(),
                Boolean.TRUE.equals(row.coverLetterNeeded()),
                row.portfolioLink(),
                row.githubLink(),
                row.projectsToHighlight(),
                row.skillsToEmphasize(),
                row.interviewTopics(),
                row.recruiterName(),
                row.recruiterEmail(),
                row.applicationDate(),
                row.followUpDate(),
                row.phoneScreenAt(),
                row.interviewOneAt(),
                row.interviewTwoAt(),
                row.offerAt(),
                row.rejectedAt(),
                row.applicationNotes()
            )
        );

        List<String> warnings = analyzed.warnings().stream()
            .map(issue -> issue.message())
            .toList();
        return new ImportRowPersistenceResult(
            analyzed.rowNumber(),
            warnings.isEmpty()
                ? ImportRowOutcomeStatus.CREATED
                : ImportRowOutcomeStatus.CREATED_WITH_WARNING,
            company == null ? null : company.getId(),
            job.id(),
            application.id(),
            null,
            warnings,
            List.of()
        );
    }

    private Company resolveCompany(String name) {
        if (name == null) {
            return null;
        }
        return companyRepository.findFirstByNameIgnoreCase(name)
            .orElseGet(() -> createCompany(name));
    }

    private Company createCompany(String name) {
        try {
            return companyRepository.saveAndFlush(new Company(
                name, null, null, null, null, null, null, null, null,
                null, null, false
            ));
        } catch (DataIntegrityViolationException exception) {
            throw new CompanyResolutionRaceException(exception);
        }
    }

    private JobOpportunity findUrlDuplicate(String applicationUrl) {
        String normalized = urlNormalizer.normalize(applicationUrl);
        if (normalized == null) {
            return null;
        }
        return jobRepository.findAll().stream()
            .filter(job -> normalized.equals(
                urlNormalizer.normalize(job.getApplicationUrl())
            ))
            .findFirst()
            .orElse(null);
    }
}
