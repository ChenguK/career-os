package com.chengukargbo.careeros.companies.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyRequest(
    @NotBlank(message = "Company name is required")
    @Size(max = 200, message = "Company name must not exceed 200 characters")
    String name,

    @Size(max = 500, message = "Website URL must not exceed 500 characters")
    String websiteUrl,

    @Size(max = 500, message = "Careers URL must not exceed 500 characters")
    String careersUrl,

    @Size(max = 150, message = "Industry must not exceed 150 characters")
    String industry,

    @Size(max = 100, message = "Company type must not exceed 100 characters")
    String companyType,

    String mission,
    String products,
    String techStack,
    String remotePolicy,
    String salaryNotes,
    String generalNotes,
    boolean dreamCompany
) {
}