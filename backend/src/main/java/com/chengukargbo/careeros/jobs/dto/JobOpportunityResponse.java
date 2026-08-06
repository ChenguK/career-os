package com.chengukargbo.careeros.jobs.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import com.chengukargbo.careeros.companies.Company;
import com.chengukargbo.careeros.jobs.JobOpportunity;
import com.chengukargbo.careeros.jobs.RemoteType;

public record JobOpportunityResponse(
    Long id,
    Long companyId,
    String companyName,
    String positionTitle,
    String department,
    String location,
    RemoteType remoteType,
    String employmentType,
    BigDecimal salaryMin,
    BigDecimal salaryMax,
    String salaryCurrency,
    String salaryNotes,
    String applicationUrl,
    String source,
    LocalDate datePosted,
    LocalDate closingDate,
    short priority,
    BigDecimal matchScore,
    String jobDescription,
    String notes,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {

    public static JobOpportunityResponse from(
        JobOpportunity job
    ) {
        Company company = job.getCompany();

        return new JobOpportunityResponse(
            job.getId(),
            company == null ? null : company.getId(),
            company == null ? null : company.getName(),
            job.getPositionTitle(),
            job.getDepartment(),
            job.getLocation(),
            job.getRemoteType(),
            job.getEmploymentType(),
            job.getSalaryMin(),
            job.getSalaryMax(),
            job.getSalaryCurrency(),
            job.getSalaryNotes(),
            job.getApplicationUrl(),
            job.getSource(),
            job.getDatePosted(),
            job.getClosingDate(),
            job.getPriority(),
            job.getMatchScore(),
            job.getJobDescription(),
            job.getNotes(),
            job.getCreatedAt(),
            job.getUpdatedAt()
        );
    }
}