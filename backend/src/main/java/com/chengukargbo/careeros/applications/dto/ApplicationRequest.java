package com.chengukargbo.careeros.applications.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import com.chengukargbo.careeros.applications.ApplicationStatus;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ApplicationRequest(
    @NotNull(message = "Job opportunity is required")
    Long jobOpportunityId,

    ApplicationStatus status,

    @Size(
        max = 100,
        message = "Resume version must not exceed 100 characters"
    )
    String resumeVersion,

    boolean coverLetterNeeded,

    @Size(max = 1000)
    String portfolioLink,

    @Size(max = 1000)
    String githubLink,

    String projectsToHighlight,
    String skillsToEmphasize,
    String interviewTopics,

    @Size(max = 200)
    String recruiterName,

    @Email(message = "Recruiter email must be valid")
    @Size(max = 320)
    String recruiterEmail,

    LocalDate applicationDate,
    LocalDate followUpDate,

    OffsetDateTime phoneScreenAt,
    OffsetDateTime interviewOneAt,
    OffsetDateTime interviewTwoAt,
    OffsetDateTime offerAt,
    OffsetDateTime rejectedAt,

    String notes
) {
}