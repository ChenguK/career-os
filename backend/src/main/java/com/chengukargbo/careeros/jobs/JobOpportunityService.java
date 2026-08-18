package com.chengukargbo.careeros.jobs;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chengukargbo.careeros.companies.Company;
import com.chengukargbo.careeros.companies.CompanyNotFoundException;
import com.chengukargbo.careeros.companies.CompanyRepository;
import com.chengukargbo.careeros.jobs.dto.JobOpportunityRequest;
import com.chengukargbo.careeros.jobs.dto.JobOpportunityResponse;
import com.chengukargbo.careeros.common.exception.BusinessValidationException;
import com.chengukargbo.careeros.common.url.ApplicationUrlService;

@Service
@Transactional
public class JobOpportunityService {

    private final JobOpportunityRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final ApplicationUrlService urlService;

    public JobOpportunityService(
        JobOpportunityRepository jobRepository,
        CompanyRepository companyRepository,
        ApplicationUrlService urlService
    ) {
        this.jobRepository = jobRepository;
        this.companyRepository = companyRepository;
        this.urlService = urlService;
    }

    public JobOpportunityResponse create(
        JobOpportunityRequest request
    ) {
        validateSalaryRange(request);
        validateDates(request);

        Company company = findOptionalCompany(request.companyId());

        JobOpportunity job = new JobOpportunity(
            company,
            request.positionTitle().trim(),
            normalize(request.department()),
            normalize(request.location()),
            request.remoteType() == null
                ? RemoteType.UNKNOWN
                : request.remoteType(),
            normalize(request.employmentType()),
            request.salaryMin(),
            request.salaryMax(),
            normalizeCurrency(request.salaryCurrency()),
            normalize(request.salaryNotes()),
            normalize(request.applicationUrl()),
            normalize(request.source()),
            request.datePosted(),
            request.closingDate(),
            request.priority() == null
                ? (short) 3
                : request.priority().shortValue(),
            request.matchScore(),
            normalize(request.jobDescription()),
            normalize(request.notes())
        );
        job.setNormalizedApplicationUrl(
            urlService.normalize(request.applicationUrl())
        );

        JobOpportunity savedJob = jobRepository.saveAndFlush(job);

        return JobOpportunityResponse.from(savedJob);
    }

    public JobOpportunityResponse update(
        Long id,
        JobOpportunityRequest request
    ) {
        validateSalaryRange(request);
        validateDates(request);

        JobOpportunity job = findEntityById(id);
        Company company = findOptionalCompany(request.companyId());

        job.update(
            company,
            request.positionTitle().trim(),
            normalize(request.department()),
            normalize(request.location()),
            request.remoteType() == null
                ? RemoteType.UNKNOWN
                : request.remoteType(),
            normalize(request.employmentType()),
            request.salaryMin(),
            request.salaryMax(),
            normalizeCurrency(request.salaryCurrency()),
            normalize(request.salaryNotes()),
            normalize(request.applicationUrl()),
            normalize(request.source()),
            request.datePosted(),
            request.closingDate(),
            request.priority() == null
                ? (short) 3
                : request.priority().shortValue(),
            request.matchScore(),
            normalize(request.jobDescription()),
            normalize(request.notes())
        );
        job.setNormalizedApplicationUrl(
            urlService.normalize(request.applicationUrl())
        );

        JobOpportunity updatedJob =
            jobRepository.saveAndFlush(job);

        return JobOpportunityResponse.from(updatedJob);
    }

    public void delete(Long id) {
        JobOpportunity job = findEntityById(id);
        jobRepository.delete(job);
    }

    @Transactional(readOnly = true)
    public List<JobOpportunityResponse> search(String searchTerm) {
        String normalizedSearch = normalize(searchTerm);

        if (normalizedSearch == null) {
            return findAll();
        }

        return jobRepository
            .findByPositionTitleContainingIgnoreCaseOrderByPriorityAscCreatedAtDesc(
                normalizedSearch
            )
            .stream()
            .map(JobOpportunityResponse::from)
            .toList();
    }

    private JobOpportunity findEntityById(Long id) {
        return jobRepository.findById(id)
            .orElseThrow(
                () -> new JobOpportunityNotFoundException(id)
            );
    }

    @Transactional(readOnly = true)
    public List<JobOpportunityResponse> findAll() {
        return jobRepository
            .findAllByOrderByPriorityAscCreatedAtDesc()
            .stream()
            .map(JobOpportunityResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public JobOpportunityResponse findById(Long id) {
        return JobOpportunityResponse.from(findEntityById(id));
    }
    private Company findOptionalCompany(Long companyId) {
        if (companyId == null) {
            return null;
        }

        return companyRepository.findById(companyId)
            .orElseThrow(() -> new CompanyNotFoundException(companyId));
    }

    private void validateSalaryRange(
        JobOpportunityRequest request
    ) {
        BigDecimal minimum = request.salaryMin();
        BigDecimal maximum = request.salaryMax();

        if (
            minimum != null
                && maximum != null
                && maximum.compareTo(minimum) < 0
        ) {
            throw new BusinessValidationException(
                "Maximum salary must be greater than or equal to minimum salary"
            );
        }
    }

    private void validateDates(JobOpportunityRequest request) {
        if (
            request.datePosted() != null
                && request.closingDate() != null
                && request.closingDate()
                    .isBefore(request.datePosted())
        ) {
            throw new BusinessValidationException(
                "Closing date cannot be before the posting date"
            );
        }
    }

    private String normalizeCurrency(String value) {
        String normalized = normalize(value);

        return normalized == null
            ? "USD"
            : normalized.toUpperCase(Locale.ROOT);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty() ? null : trimmed;
    }
}
