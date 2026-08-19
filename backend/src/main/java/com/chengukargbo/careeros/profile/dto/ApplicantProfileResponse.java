package com.chengukargbo.careeros.profile.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.chengukargbo.careeros.jobs.RemoteType;
import com.chengukargbo.careeros.profile.ApplicantProfile;

public record ApplicantProfileResponse(
    boolean exists,
    Long id,
    String firstName,
    String lastName,
    String preferredName,
    String email,
    String phone,
    String city,
    String stateRegion,
    String country,
    String postalCode,
    String portfolioUrl,
    String githubUrl,
    String linkedinUrl,
    String defaultResumeVersion,
    Long defaultResumeMaterialId,
    RemoteType preferredWorkArrangement,
    BigDecimal minimumSalary,
    String salaryCurrency,
    Boolean willingToRelocate,
    Boolean willingToTravel,
    boolean verified,
    OffsetDateTime lastVerifiedAt,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
    public ApplicantProfileResponse(boolean exists,Long id,String firstName,String lastName,
        String preferredName,String email,String phone,String city,String stateRegion,String country,
        String postalCode,String portfolioUrl,String githubUrl,String linkedinUrl,String defaultResumeVersion,
        RemoteType preferredWorkArrangement,BigDecimal minimumSalary,String salaryCurrency,
        Boolean willingToRelocate,Boolean willingToTravel,boolean verified,OffsetDateTime lastVerifiedAt,
        OffsetDateTime createdAt,OffsetDateTime updatedAt){this(exists,id,firstName,lastName,preferredName,
            email,phone,city,stateRegion,country,postalCode,portfolioUrl,githubUrl,linkedinUrl,
            defaultResumeVersion,null,preferredWorkArrangement,minimumSalary,salaryCurrency,
            willingToRelocate,willingToTravel,verified,lastVerifiedAt,createdAt,updatedAt);}
    public static ApplicantProfileResponse empty() {
        return new ApplicantProfileResponse(
            false, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, RemoteType.UNKNOWN, null, "USD",
            null, null, false, null, null, null
        );
    }

    public static ApplicantProfileResponse from(ApplicantProfile profile) {
        return new ApplicantProfileResponse(
            true,
            profile.getId(),
            profile.getFirstName(),
            profile.getLastName(),
            profile.getPreferredName(),
            profile.getEmail(),
            profile.getPhone(),
            profile.getCity(),
            profile.getStateRegion(),
            profile.getCountry(),
            profile.getPostalCode(),
            profile.getPortfolioUrl(),
            profile.getGithubUrl(),
            profile.getLinkedinUrl(),
            profile.getDefaultResumeVersion(),
            profile.getDefaultResumeMaterial() == null ? null : profile.getDefaultResumeMaterial().getId(),
            profile.getPreferredWorkArrangement(),
            profile.getMinimumSalary(),
            profile.getSalaryCurrency(),
            profile.getWillingToRelocate(),
            profile.getWillingToTravel(),
            profile.isVerified(),
            profile.getLastVerifiedAt(),
            profile.getCreatedAt(),
            profile.getUpdatedAt()
        );
    }
}
