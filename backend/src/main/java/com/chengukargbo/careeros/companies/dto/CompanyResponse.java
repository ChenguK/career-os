package com.chengukargbo.careeros.companies.dto;

import java.time.OffsetDateTime;

import com.chengukargbo.careeros.companies.Company;

public record CompanyResponse(
    Long id,
    String name,
    String websiteUrl,
    String careersUrl,
    String industry,
    String companyType,
    String mission,
    String products,
    String techStack,
    String remotePolicy,
    String salaryNotes,
    String generalNotes,
    boolean dreamCompany,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {

    public static CompanyResponse from(Company company) {
        return new CompanyResponse(
            company.getId(),
            company.getName(),
            company.getWebsiteUrl(),
            company.getCareersUrl(),
            company.getIndustry(),
            company.getCompanyType(),
            company.getMission(),
            company.getProducts(),
            company.getTechStack(),
            company.getRemotePolicy(),
            company.getSalaryNotes(),
            company.getGeneralNotes(),
            company.isDreamCompany(),
            company.getCreatedAt(),
            company.getUpdatedAt()
        );
    }
}