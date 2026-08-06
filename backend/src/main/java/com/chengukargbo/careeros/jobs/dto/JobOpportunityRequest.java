package com.chengukargbo.careeros.jobs.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.chengukargbo.careeros.jobs.RemoteType;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JobOpportunityRequest(
    Long companyId,

    @NotBlank(message = "Position title is required")
    @Size(
        max = 200,
        message = "Position title must not exceed 200 characters"
    )
    String positionTitle,

    @Size(
        max = 150,
        message = "Department must not exceed 150 characters"
    )
    String department,

    @Size(
        max = 200,
        message = "Location must not exceed 200 characters"
    )
    String location,

    RemoteType remoteType,

    @Size(
        max = 50,
        message = "Employment type must not exceed 50 characters"
    )
    String employmentType,

    @DecimalMin(
        value = "0.00",
        message = "Minimum salary cannot be negative"
    )
    BigDecimal salaryMin,

    @DecimalMin(
        value = "0.00",
        message = "Maximum salary cannot be negative"
    )
    BigDecimal salaryMax,

    @Size(
        min = 3,
        max = 3,
        message = "Salary currency must use a three-letter code"
    )
    String salaryCurrency,

    String salaryNotes,

    @Size(
        max = 1000,
        message = "Application URL must not exceed 1000 characters"
    )
    String applicationUrl,

    @Size(
        max = 150,
        message = "Source must not exceed 150 characters"
    )
    String source,

    LocalDate datePosted,
    LocalDate closingDate,

    @Min(value = 1, message = "Priority must be between 1 and 5")
    @Max(value = 5, message = "Priority must be between 1 and 5")
    Short priority,

    @DecimalMin(
        value = "0.0",
        message = "Match score must be between 0 and 10"
    )
    @DecimalMax(
        value = "10.0",
        message = "Match score must be between 0 and 10"
    )
    BigDecimal matchScore,

    String jobDescription,
    String notes
) {
}