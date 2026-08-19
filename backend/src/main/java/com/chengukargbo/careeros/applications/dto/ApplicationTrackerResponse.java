package com.chengukargbo.careeros.applications.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import com.chengukargbo.careeros.applications.Application;
import com.chengukargbo.careeros.applications.ApplicationStatus;
import com.chengukargbo.careeros.companies.Company;
import com.chengukargbo.careeros.jobs.JobOpportunity;
import com.chengukargbo.careeros.jobs.RemoteType;
import com.chengukargbo.careeros.automation.AutomationEnums.State;
import com.chengukargbo.careeros.applications.lock.ApplicationLockState;

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
    Long resumeMaterialId,
    String resumeMaterialDisplayName,
    Boolean resumeMaterialActive,
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
    OffsetDateTime applicationUpdatedAt,
    State automationState,
    OffsetDateTime statusDate,
    ApplicationLockState lockState
) {

    public ApplicationTrackerResponse(Long jobOpportunityId,Long companyId,String companyName,
        String positionTitle,String department,String location,RemoteType remoteType,String employmentType,
        BigDecimal salaryMin,BigDecimal salaryMax,String salaryCurrency,String salaryNotes,
        String applicationUrl,String source,LocalDate datePosted,LocalDate closingDate,short priority,
        BigDecimal matchScore,String jobDescription,String jobNotes,OffsetDateTime jobCreatedAt,
        OffsetDateTime jobUpdatedAt,Long applicationId,ApplicationStatus status,String resumeVersion,
        Boolean coverLetterNeeded,String portfolioLink,String githubLink,String projectsToHighlight,
        String skillsToEmphasize,String interviewTopics,String recruiterName,String recruiterEmail,
        LocalDate applicationDate,LocalDate followUpDate,OffsetDateTime phoneScreenAt,
        OffsetDateTime interviewOneAt,OffsetDateTime interviewTwoAt,OffsetDateTime offerAt,
        OffsetDateTime rejectedAt,String applicationNotes,OffsetDateTime applicationCreatedAt,
        OffsetDateTime applicationUpdatedAt){this(jobOpportunityId,companyId,companyName,positionTitle,
            department,location,remoteType,employmentType,salaryMin,salaryMax,salaryCurrency,salaryNotes,
            applicationUrl,source,datePosted,closingDate,priority,matchScore,jobDescription,jobNotes,
            jobCreatedAt,jobUpdatedAt,applicationId,status,resumeVersion,null,null,null,coverLetterNeeded,
            portfolioLink,githubLink,projectsToHighlight,skillsToEmphasize,interviewTopics,recruiterName,
            recruiterEmail,applicationDate,followUpDate,phoneScreenAt,interviewOneAt,interviewTwoAt,
            offerAt,rejectedAt,applicationNotes,applicationCreatedAt,applicationUpdatedAt,
            applicationId == null ? null : State.NOT_APPROVED,
            applicationId == null ? jobCreatedAt : resolveStatusDate(status, applicationDate,
                phoneScreenAt, interviewOneAt, interviewTwoAt, offerAt, rejectedAt,
                applicationCreatedAt, null), applicationId == null ? null : ApplicationLockState.NOT_SUBMITTED);}

    public static ApplicationTrackerResponse from(
        JobOpportunity job,
        Application application
    ) {
        return from(job, application, application == null ? null : State.NOT_APPROVED, null,
            application == null ? null : ApplicationLockState.NOT_SUBMITTED);
    }

    public static ApplicationTrackerResponse from(
        JobOpportunity job,
        Application application,
        State automationState,
        OffsetDateTime historyFallback,
        ApplicationLockState lockState
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
            application == null ? null : application.getResumeMaterial() == null
                ? application.getResumeVersion() : application.getResumeMaterial().getDisplayName(),
            application == null || application.getResumeMaterial() == null ? null : application.getResumeMaterial().getId(),
            application == null || application.getResumeMaterial() == null ? null : application.getResumeMaterial().getDisplayName(),
            application == null || application.getResumeMaterial() == null ? null : application.getResumeMaterial().isActive(),
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
            application == null ? null : application.getUpdatedAt(),
            application == null ? null : automationState,
            application == null ? job.getCreatedAt() : resolveStatusDate(
                application.getStatus(), application.getApplicationDate(), application.getPhoneScreenAt(),
                application.getInterviewOneAt(), application.getInterviewTwoAt(), application.getOfferAt(),
                application.getRejectedAt(), application.getCreatedAt(), historyFallback),
            application == null ? null : lockState
        );
    }

    private static OffsetDateTime resolveStatusDate(ApplicationStatus status, LocalDate applicationDate,
        OffsetDateTime phoneScreenAt, OffsetDateTime interviewOneAt, OffsetDateTime interviewTwoAt,
        OffsetDateTime offerAt, OffsetDateTime rejectedAt, OffsetDateTime createdAt,
        OffsetDateTime historyFallback) {
        OffsetDateTime milestone = switch (status) {
            case SAVED -> applicationDate == null ? createdAt : applicationDate.atStartOfDay().atOffset(java.time.ZoneOffset.UTC);
            case APPLIED -> applicationDate == null ? null : applicationDate.atStartOfDay().atOffset(java.time.ZoneOffset.UTC);
            case PHONE_SCREEN -> phoneScreenAt;
            case INTERVIEW_ONE -> interviewOneAt;
            case INTERVIEW_TWO -> interviewTwoAt;
            case OFFER -> offerAt;
            case REJECTED -> rejectedAt;
            default -> null;
        };
        return milestone == null ? historyFallback : milestone;
    }
}
