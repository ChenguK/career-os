package com.chengukargbo.careeros.applications.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import com.chengukargbo.careeros.applications.Application;
import com.chengukargbo.careeros.applications.ApplicationStatus;
import com.chengukargbo.careeros.jobs.JobOpportunity;

public record ApplicationResponse(
    Long id,
    Long jobOpportunityId,
    String positionTitle,
    Long companyId,
    String companyName,
    ApplicationStatus status,
    String resumeVersion,
    boolean coverLetterNeeded,
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
    String notes,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {

    public static ApplicationResponse from(
        Application application
    ) {
        JobOpportunity job = application.getJobOpportunity();

        return new ApplicationResponse(
            application.getId(),
            job.getId(),
            job.getPositionTitle(),
            job.getCompany() == null
                ? null
                : job.getCompany().getId(),
            job.getCompany() == null
                ? null
                : job.getCompany().getName(),
            application.getStatus(),
            application.getResumeVersion(),
            application.isCoverLetterNeeded(),
            application.getPortfolioLink(),
            application.getGithubLink(),
            application.getProjectsToHighlight(),
            application.getSkillsToEmphasize(),
            application.getInterviewTopics(),
            application.getRecruiterName(),
            application.getRecruiterEmail(),
            application.getApplicationDate(),
            application.getFollowUpDate(),
            application.getPhoneScreenAt(),
            application.getInterviewOneAt(),
            application.getInterviewTwoAt(),
            application.getOfferAt(),
            application.getRejectedAt(),
            application.getNotes(),
            application.getCreatedAt(),
            application.getUpdatedAt()
        );
    }
}