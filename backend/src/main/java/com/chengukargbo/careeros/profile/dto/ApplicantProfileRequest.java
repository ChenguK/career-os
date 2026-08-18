package com.chengukargbo.careeros.profile.dto;

import java.math.BigDecimal;

import com.chengukargbo.careeros.jobs.RemoteType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ApplicantProfileRequest(
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    String firstName,

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    String lastName,

    @Size(max = 100, message = "Preferred name must not exceed 100 characters")
    String preferredName,

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 320, message = "Email must not exceed 320 characters")
    String email,

    @Size(max = 50, message = "Phone must not exceed 50 characters")
    String phone,

    @Size(max = 100, message = "City must not exceed 100 characters")
    String city,

    @Size(max = 100, message = "State or region must not exceed 100 characters")
    String stateRegion,

    @Size(max = 100, message = "Country must not exceed 100 characters")
    String country,

    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    String postalCode,

    @Size(max = 1000, message = "Portfolio URL must not exceed 1000 characters")
    @Pattern(regexp = "^$|https?://.+", message = "Portfolio URL must use HTTP or HTTPS")
    String portfolioUrl,

    @Size(max = 1000, message = "GitHub URL must not exceed 1000 characters")
    @Pattern(regexp = "^$|https?://.+", message = "GitHub URL must use HTTP or HTTPS")
    String githubUrl,

    @Size(max = 1000, message = "LinkedIn URL must not exceed 1000 characters")
    @Pattern(regexp = "^$|https?://.+", message = "LinkedIn URL must use HTTP or HTTPS")
    String linkedinUrl,

    @Size(max = 100, message = "Resume version must not exceed 100 characters")
    String defaultResumeVersion,

    RemoteType preferredWorkArrangement,

    @DecimalMin(value = "0", message = "Minimum salary must not be negative")
    @Digits(integer = 10, fraction = 2, message = "Minimum salary must have at most two decimal places")
    BigDecimal minimumSalary,

    @NotBlank(message = "Salary currency is required")
    @Pattern(regexp = "^[A-Za-z]{3}$", message = "Salary currency must be a three-letter code")
    String salaryCurrency,

    Boolean willingToRelocate,
    Boolean willingToTravel
) {
}
