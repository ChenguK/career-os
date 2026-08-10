package com.chengukargbo.careeros.importing;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import com.chengukargbo.careeros.applications.ApplicationStatus;
import com.chengukargbo.careeros.jobs.RemoteType;

public record CanonicalImportRow(
    String positionTitle,
    String companyName,
    String department,
    String location,
    RemoteType workArrangement,
    String employmentType,
    BigDecimal salaryMin,
    BigDecimal salaryMax,
    String salaryCurrency,
    String salaryNotes,
    String applicationUrl,
    String source,
    LocalDate datePosted,
    LocalDate closingDate,
    Short priority,
    BigDecimal matchScore,
    String jobDescription,
    String jobNotes,
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
    String applicationNotes
) {
}
