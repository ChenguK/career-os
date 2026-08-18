package com.chengukargbo.careeros.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.chengukargbo.careeros.common.exception.BusinessValidationException;
import com.chengukargbo.careeros.applications.ApplicationRepository;
import com.chengukargbo.careeros.jobs.JobOpportunityRepository;
import com.chengukargbo.careeros.jobs.RemoteType;
import com.chengukargbo.careeros.profile.dto.ApplicantProfileRequest;
import com.chengukargbo.careeros.profile.dto.ApplicantProfileResponse;

@ExtendWith(MockitoExtension.class)
class ApplicantProfileServiceTest {

    @Mock
    private ApplicantProfileRepository profileRepository;

    @Mock
    private JobOpportunityRepository jobRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private ApplicantProfileService profileService;

    @Test
    void returnsAnExplicitEmptyStateBeforeProfileCreation() {
        when(profileRepository.findByProfileKey("PRIMARY"))
            .thenReturn(Optional.empty());

        ApplicantProfileResponse response = profileService.findCurrent();

        assertThat(response.exists()).isFalse();
        assertThat(response.id()).isNull();
        assertThat(response.preferredWorkArrangement())
            .isEqualTo(RemoteType.UNKNOWN);
        assertThat(response.salaryCurrency()).isEqualTo("USD");
        assertThat(response.verified()).isFalse();
    }

    @Test
    void createsAndNormalizesTheSinglePrimaryProfile() {
        when(profileRepository.findByProfileKey("PRIMARY"))
            .thenReturn(Optional.empty());
        when(profileRepository.saveAndFlush(any(ApplicantProfile.class)))
            .thenAnswer(invocation -> persisted(invocation.getArgument(0)));

        ApplicantProfileResponse response = profileService.save(request());

        assertThat(response.exists()).isTrue();
        assertThat(response.id()).isEqualTo(91L);
        assertThat(response.firstName()).isEqualTo("Chengu");
        assertThat(response.preferredName()).isNull();
        assertThat(response.email()).isEqualTo("chengu@example.com");
        assertThat(response.portfolioUrl())
            .isEqualTo("https://portfolio.example");
        assertThat(response.preferredWorkArrangement())
            .isEqualTo(RemoteType.REMOTE);
        assertThat(response.minimumSalary())
            .isEqualByComparingTo("120000");
        assertThat(response.salaryCurrency()).isEqualTo("USD");
        assertThat(response.createdAt()).isNotNull();
        assertThat(response.updatedAt()).isNotNull();
        assertThat(response.verified()).isFalse();
        verify(profileRepository).findByProfileKey("PRIMARY");
    }

    @Test
    void explicitVerificationIsSeparateFromOrdinaryProfileUpdates() {
        ApplicantProfile profile = persisted(profile(request()));
        when(profileRepository.findByProfileKey("PRIMARY"))
            .thenReturn(Optional.of(profile));
        when(profileRepository.saveAndFlush(profile)).thenReturn(profile);

        ApplicantProfileResponse verified = profileService.verifyCurrent();

        assertThat(verified.verified()).isTrue();
        assertThat(verified.lastVerifiedAt()).isNotNull();

        ApplicantProfileRequest changed = new ApplicantProfileRequest(
            "Chengu", "Kargbo", "CK", "chengu@example.com", null,
            "New York", "NY", "United States", "10001",
            "https://portfolio.example", "https://github.com/chengu",
            "https://linkedin.com/in/chengu", "Software Engineering",
            RemoteType.HYBRID, new BigDecimal("125000"), "USD", true,
            false
        );
        ApplicantProfileResponse updated = profileService.save(changed);

        assertThat(updated.verified()).isFalse();
        assertThat(updated.lastVerifiedAt()).isNull();
        assertThat(updated.preferredName()).isEqualTo("CK");
    }

    @Test
    void savingUnchangedValuesPreservesExplicitVerification() {
        ApplicantProfile profile = persisted(profile(request()));
        ReflectionTestUtils.setField(
            profile, "minimumSalary", new BigDecimal("120000.00")
        );
        profile.verify();
        when(profileRepository.findByProfileKey("PRIMARY"))
            .thenReturn(Optional.of(profile));
        when(profileRepository.saveAndFlush(profile)).thenReturn(profile);

        ApplicantProfileResponse response = profileService.save(request());

        assertThat(response.verified()).isTrue();
        assertThat(response.lastVerifiedAt()).isNotNull();
    }

    @Test
    void cannotVerifyAnUnsavedProfile() {
        when(profileRepository.findByProfileKey("PRIMARY"))
            .thenReturn(Optional.empty());

        assertThatThrownBy(profileService::verifyCurrent)
            .isInstanceOf(BusinessValidationException.class)
            .hasMessage("Save the applicant profile before verifying it");
    }

    @Test
    void profileChangesDoNotMutateJobsOrApplications() {
        when(profileRepository.findByProfileKey("PRIMARY"))
            .thenReturn(Optional.empty());
        when(profileRepository.saveAndFlush(any(ApplicantProfile.class)))
            .thenAnswer(invocation -> persisted(invocation.getArgument(0)));

        profileService.save(request());

        verifyNoInteractions(jobRepository, applicationRepository);
    }

    private ApplicantProfileRequest request() {
        return new ApplicantProfileRequest(
            "  Chengu  ", " Kargbo ", " ", " chengu@example.com ",
            null, " New York ", " NY ", " United States ", " 10001 ",
            " https://portfolio.example ", " https://github.com/chengu ",
            " https://linkedin.com/in/chengu ", " Software Engineering ",
            RemoteType.REMOTE, new BigDecimal("120000"), "usd", true,
            false
        );
    }

    private ApplicantProfile profile(ApplicantProfileRequest request) {
        return new ApplicantProfile(
            request.firstName().trim(), request.lastName().trim(), null,
            request.email().trim(), null, request.city().trim(),
            request.stateRegion().trim(), request.country().trim(),
            request.postalCode().trim(), request.portfolioUrl().trim(),
            request.githubUrl().trim(), request.linkedinUrl().trim(),
            request.defaultResumeVersion().trim(),
            request.preferredWorkArrangement(), request.minimumSalary(),
            request.salaryCurrency().toUpperCase(),
            request.willingToRelocate(), request.willingToTravel()
        );
    }

    private ApplicantProfile persisted(ApplicantProfile profile) {
        if (profile.getId() == null) {
            ReflectionTestUtils.setField(profile, "id", 91L);
            profile.onCreate();
        }
        return profile;
    }
}
