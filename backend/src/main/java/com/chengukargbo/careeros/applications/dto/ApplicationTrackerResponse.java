package com.chengukargbo.careeros.applications.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import com.chengukargbo.careeros.applications.Application;
import com.chengukargbo.careeros.applications.ApplicationStatus;
import com.chengukargbo.careeros.companies.Company;
import com.chengukargbo.careeros.jobs.JobOpportunity;
import com.chengukargbo.careeros.jobs.RemoteType;

public record ApplicationTrackerResponse(
    Long jobOpportunityId,
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
    String jobNotes,
    OffsetDateTime jobCreatedAt,
    OffsetDateTime jobUpdatedAt,
    Long applicationId,
    ApplicationStatus status,
    String resumeVersion,
    Boolean coverLetterNeeded,
    String portfolioLink,
    String githubLink,
    String projectsToHighlight,
    String skillsToEmphasize,
    String interviewTopics,
    String recruiterName,
    String recruiterEmail,
    LocalDate applicationDate,
    LocalDate followUpDate,
    OffsetDateTime phoneScreenAt,
    OffsetDateTime interviewOneAt,
    OffsetDateTime interviewTwoAt,
    OffsetDateTime offerAt,
    OffsetDateTime rejectedAt,
    String applicationNotes,
    OffsetDateTime applicationCreatedAt,
    OffsetDateTime applicationUpdatedAt
) {

    public static ApplicationTrackerResponse from(
        JobOpportunity job,
        Application application
    ) {
        Company company = job.getCompany();

        return new ApplicationTrackerResponse(
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
            job.getUpdatedAt(),
            application == null ? null : application.getId(),
            application == null ? null : application.getStatus(),
            application == null ? null : application.getResumeVersion(),
            application == null
                ? null
                : application.isCoverLetterNeeded(),
            application == null ? null : application.getPortfolioLink(),
            application == null ? null : application.getGithubLink(),
            application == null
                ? null
                : application.getProjectsToHighlight(),
            application == null
                ? null
                : application.getSkillsToEmphasize(),
            application == null
                ? null
                : application.getInterviewTopics(),
            application == null ? null : application.getRecruiterName(),
            application == null ? null : application.getRecruiterEmail(),
            application == null ? null : application.getApplicationDate(),
            application == null ? null : application.getFollowUpDate(),
            application == null ? null : application.getPhoneScreenAt(),
            application == null ? null : application.getInterviewOneAt(),
            application == null ? null : application.getInterviewTwoAt(),
            application == null ? null : application.getOfferAt(),
            application == null ? null : application.getRejectedAt(),
            application == null ? null : application.getNotes(),
            application == null ? null : application.getCreatedAt(),
            application == null ? null : application.getUpdatedAt()
        );
    }
}
