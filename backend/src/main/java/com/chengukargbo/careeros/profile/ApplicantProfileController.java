package com.chengukargbo.careeros.profile;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chengukargbo.careeros.profile.dto.ApplicantProfileRequest;
import com.chengukargbo.careeros.profile.dto.ApplicantProfileResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/applicant-profile")
public class ApplicantProfileController {

    private final ApplicantProfileService profileService;

    public ApplicantProfileController(ApplicantProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ApplicantProfileResponse findCurrent() {
        return profileService.findCurrent();
    }

    @PutMapping
    public ApplicantProfileResponse save(
        @Valid @RequestBody ApplicantProfileRequest request
    ) {
        return profileService.save(request);
    }

    @PostMapping("/verify")
    public ApplicantProfileResponse verifyCurrent() {
        return profileService.verifyCurrent();
    }
}
