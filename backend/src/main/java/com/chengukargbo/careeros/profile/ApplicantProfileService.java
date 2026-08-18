package com.chengukargbo.careeros.profile;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chengukargbo.careeros.common.exception.BusinessValidationException;
import com.chengukargbo.careeros.jobs.RemoteType;
import com.chengukargbo.careeros.profile.dto.ApplicantProfileRequest;
import com.chengukargbo.careeros.profile.dto.ApplicantProfileResponse;

@Service
@Transactional
public class ApplicantProfileService {

    private final ApplicantProfileRepository profileRepository;

    public ApplicantProfileService(
        ApplicantProfileRepository profileRepository
    ) {
        this.profileRepository = profileRepository;
    }

    @Transactional(readOnly = true)
    public ApplicantProfileResponse findCurrent() {
        return profileRepository
            .findByProfileKey(ApplicantProfile.PRIMARY_PROFILE_KEY)
            .map(ApplicantProfileResponse::from)
            .orElseGet(ApplicantProfileResponse::empty);
    }

    public ApplicantProfileResponse save(ApplicantProfileRequest request) {
        ApplicantProfile profile = profileRepository
            .findByProfileKey(ApplicantProfile.PRIMARY_PROFILE_KEY)
            .orElse(null);

        if (profile == null) {
            profile = create(request);
        } else {
            update(profile, request);
        }

        return ApplicantProfileResponse.from(
            profileRepository.saveAndFlush(profile)
        );
    }

    public ApplicantProfileResponse verifyCurrent() {
        ApplicantProfile profile = profileRepository
            .findByProfileKey(ApplicantProfile.PRIMARY_PROFILE_KEY)
            .orElseThrow(() -> new BusinessValidationException(
                "Save the applicant profile before verifying it"
            ));
        profile.verify();
        return ApplicantProfileResponse.from(
            profileRepository.saveAndFlush(profile)
        );
    }

    private ApplicantProfile create(ApplicantProfileRequest request) {
        return new ApplicantProfile(
            required(request.firstName()),
            required(request.lastName()),
            normalize(request.preferredName()),
            required(request.email()),
            normalize(request.phone()),
            normalize(request.city()),
            normalize(request.stateRegion()),
            normalize(request.country()),
            normalize(request.postalCode()),
            normalize(request.portfolioUrl()),
            normalize(request.githubUrl()),
            normalize(request.linkedinUrl()),
            normalize(request.defaultResumeVersion()),
            arrangement(request.preferredWorkArrangement()),
            request.minimumSalary(),
            currency(request.salaryCurrency()),
            request.willingToRelocate(),
            request.willingToTravel()
        );
    }

    private void update(
        ApplicantProfile profile,
        ApplicantProfileRequest request
    ) {
        profile.update(
            required(request.firstName()),
            required(request.lastName()),
            normalize(request.preferredName()),
            required(request.email()),
            normalize(request.phone()),
            normalize(request.city()),
            normalize(request.stateRegion()),
            normalize(request.country()),
            normalize(request.postalCode()),
            normalize(request.portfolioUrl()),
            normalize(request.githubUrl()),
            normalize(request.linkedinUrl()),
            normalize(request.defaultResumeVersion()),
            arrangement(request.preferredWorkArrangement()),
            request.minimumSalary(),
            currency(request.salaryCurrency()),
            request.willingToRelocate(),
            request.willingToTravel()
        );
    }

    private RemoteType arrangement(RemoteType value) {
        return value == null ? RemoteType.UNKNOWN : value;
    }

    private String currency(String value) {
        return required(value).toUpperCase();
    }

    private String required(String value) {
        return value.trim();
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
