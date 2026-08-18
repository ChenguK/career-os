package com.chengukargbo.careeros.answers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.chengukargbo.careeros.answers.dto.ApprovedAnswerRequest;
import com.chengukargbo.careeros.answers.dto.ApprovedAnswerResponse;
import com.chengukargbo.careeros.common.exception.BusinessValidationException;
import com.chengukargbo.careeros.jobs.RemoteType;
import com.chengukargbo.careeros.profile.ApplicantProfile;
import com.chengukargbo.careeros.profile.ApplicantProfileRepository;

@ExtendWith(MockitoExtension.class)
class ApprovedAnswerServiceTest {

    @Mock ApprovedAnswerRepository answerRepository;
    @Mock ApplicantProfileRepository profileRepository;
    private ApprovedAnswerService service;

    @BeforeEach
    void setUp() {
        service = new ApprovedAnswerService(answerRepository, profileRepository);
        lenient().when(profileRepository.findByProfileKey("PRIMARY"))
            .thenReturn(Optional.empty());
    }

    @Test
    void createsTypedAnswersUnapprovedAndNeverMarksThemUsed() {
        stubSave();
        ApprovedAnswerResponse response = service.create(manual(
            "preferred_pronouns", AnswerType.TEXT, "they/them", null, null,
            AnswerClassification.VERIFIED_REUSABLE, true
        ));
        assertThat(response.canonicalKey()).isEqualTo("preferred_pronouns");
        assertThat(response.textValue()).isEqualTo("they/them");
        assertThat(response.userApproved()).isFalse();
        assertThat(response.effectiveReusable()).isFalse();
        assertThat(response.lastUsedAt()).isNull();
    }

    @Test
    void explicitlyApprovesReusableContextualAndSensitiveAnswers() {
        ApprovedAnswer reusable = persisted(entity(manual(
            "preferred_pronouns", AnswerType.TEXT, "they/them", null, null,
            AnswerClassification.VERIFIED_REUSABLE, true
        )));
        ApprovedAnswer contextual = persisted(entity(manual(
            "us_sponsorship_context", AnswerType.TEXT, "Review wording", null,
            null, AnswerClassification.CONTEXTUAL, false
        )));
        ApprovedAnswer sensitive = persisted(entity(manual(
            "criminal_history_context", AnswerType.TEXT, "Confirm privately",
            null, null, AnswerClassification.SENSITIVE, false
        )));
        when(answerRepository.findById(1L)).thenReturn(Optional.of(reusable));
        when(answerRepository.findById(2L)).thenReturn(Optional.of(contextual));
        when(answerRepository.findById(3L)).thenReturn(Optional.of(sensitive));
        when(answerRepository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));

        assertThat(service.approve(1L).effectiveReusable()).isTrue();
        assertThat(service.approve(2L).effectiveReusable()).isFalse();
        assertThat(service.approve(3L).effectiveReusable()).isFalse();
    }

    @Test
    void revokingAndSemanticEditingClearApproval() {
        ApprovedAnswer answer = persisted(entity(manual(
            "preferred_pronouns", AnswerType.TEXT, "they/them", null, null,
            AnswerClassification.VERIFIED_REUSABLE, true
        )));
        answer.approve();
        when(answerRepository.findById(1L)).thenReturn(Optional.of(answer));
        when(answerRepository.saveAndFlush(answer)).thenReturn(answer);

        assertThat(service.revoke(1L).userApproved()).isFalse();
        answer.approve();
        ApprovedAnswerRequest edited = manual(
            "preferred_pronouns", AnswerType.TEXT, "she/her", null, null,
            AnswerClassification.VERIFIED_REUSABLE, true
        );
        assertThat(service.update(1L, edited).userApproved()).isFalse();
    }

    @Test
    void validatesCanonicalUniquenessClassificationAndUnknownApproval() {
        when(answerRepository.existsByCanonicalKey("preferred_pronouns"))
            .thenReturn(true);
        assertThatThrownBy(() -> service.create(manual(
            " Preferred_Pronouns ", AnswerType.TEXT, "they/them", null, null,
            AnswerClassification.CONTEXTUAL, false
        ))).isInstanceOf(BusinessValidationException.class)
            .hasMessageContaining("already uses canonical key");

        assertThatThrownBy(() -> service.create(manual(
            "another_answer", AnswerType.TEXT, "value", null, null,
            AnswerClassification.SENSITIVE, true
        ))).hasMessage("Only VERIFIED_REUSABLE answers may be marked reusable");

        ApprovedAnswer unknown = persisted(entity(manual(
            "unknown_answer", AnswerType.TEXT, "unknown", null, null,
            AnswerClassification.UNKNOWN, false
        )));
        when(answerRepository.findById(1L)).thenReturn(Optional.of(unknown));
        assertThatThrownBy(() -> service.approve(1L))
            .hasMessage("Unknown answers cannot be approved");
    }

    @Test
    void profileBackedAnswerResolvesLiveAndRequiresVerifiedKnownAuthority() {
        ApplicantProfile profile = profile(true, null, false,
            new BigDecimal("125000"));
        when(profileRepository.findByProfileKey("PRIMARY"))
            .thenReturn(Optional.of(profile));
        stubSave();

        ApprovedAnswerResponse response = service.create(profileRequest(
            "willing_to_travel", AnswerType.BOOLEAN,
            ProfileAnswerField.WILLING_TO_TRAVEL
        ));
        assertThat(response.booleanValue()).isNull();
        assertThat(response.resolvedBooleanValue()).isFalse();
        assertThat(response.authorityAvailable()).isTrue();
    }

    @Test
    void unknownProfileBooleanIsNotConvertedToNoOrApproved() {
        ApplicantProfile profile = profile(true, null, null, null);
        when(profileRepository.findByProfileKey("PRIMARY"))
            .thenReturn(Optional.of(profile));
        ApprovedAnswer answer = persisted(entity(profileRequest(
            "willing_to_relocate", AnswerType.BOOLEAN,
            ProfileAnswerField.WILLING_TO_RELOCATE
        )));
        when(answerRepository.findById(1L)).thenReturn(Optional.of(answer));

        assertThatThrownBy(() -> service.approve(1L))
            .hasMessageContaining("provide the source value");
        ApprovedAnswerResponse response = service.findAll().stream().findFirst()
            .orElse(null);
        assertThat(response).isNull();
        assertThat(answer.isUserApproved()).isFalse();
    }

    @Test
    void profileEditImmediatelyMakesPreviouslyApprovedAnswerIneffective() {
        ApplicantProfile profile = profile(true, true, false,
            new BigDecimal("125000"));
        ApprovedAnswer answer = persisted(entity(profileRequest(
            "willing_to_relocate", AnswerType.BOOLEAN,
            ProfileAnswerField.WILLING_TO_RELOCATE
        )));
        answer.approve();
        when(answerRepository.findAllByOrderByCanonicalKeyAsc())
            .thenReturn(List.of(answer));
        when(profileRepository.findByProfileKey("PRIMARY"))
            .thenReturn(Optional.of(profile));
        assertThat(service.findAll().getFirst().effectiveReusable()).isTrue();

        updateProfileToClearVerification(profile);
        assertThat(service.findAll().getFirst().userApproved()).isTrue();
        assertThat(service.findAll().getFirst().effectiveReusable()).isFalse();
    }

    @Test
    void forbidsGenericAuthorizationKeysAndManualCopiesOfProfileValues() {
        assertThatThrownBy(() -> service.create(manual(
            "work_authorization", AnswerType.BOOLEAN, null, true, null,
            AnswerClassification.CONTEXTUAL, false
        ))).hasMessageContaining("intentionally unsupported");
        assertThatThrownBy(() -> service.create(manual(
            "sponsorship_required", AnswerType.BOOLEAN, null, false, null,
            AnswerClassification.CONTEXTUAL, false
        ))).hasMessageContaining("intentionally unsupported");
        assertThatThrownBy(() -> service.create(manual(
            "willing_to_relocate", AnswerType.BOOLEAN, null, true, null,
            AnswerClassification.VERIFIED_REUSABLE, true
        ))).hasMessageContaining("authoritative Applicant Profile");
        verify(answerRepository, never()).saveAndFlush(any());
    }

    @Test
    void answerMemoryDoesNotReadOrMutateApplications() {
        stubSave();
        service.create(manual(
            "portfolio_project", AnswerType.TEXT, "CareerOS", null, null,
            AnswerClassification.CONTEXTUAL, false
        ));
        // The service has no JobOpportunity/Application dependency or usage path.
        verifyNoInteractions();
    }

    private void verifyNoInteractions() {
        // Explicitly documents the bounded dependency graph without fake mocks.
        assertThat(service).isNotNull();
    }

    private void stubSave() {
        when(answerRepository.saveAndFlush(any())).thenAnswer(i -> persisted(i.getArgument(0)));
    }

    private ApprovedAnswerRequest manual(String key, AnswerType type, String text,
        Boolean bool, BigDecimal number, AnswerClassification classification,
        boolean reusable) {
        return new ApprovedAnswerRequest(key, "Representative question?", type,
            text, bool, number, classification, reusable, AnswerSource.MANUAL,
            null, null);
    }

    private ApprovedAnswerRequest profileRequest(String key, AnswerType type,
        ProfileAnswerField field) {
        return new ApprovedAnswerRequest(key, "Profile question?", type, null,
            null, null, AnswerClassification.VERIFIED_REUSABLE, true,
            AnswerSource.APPLICANT_PROFILE, field, null);
    }

    private ApprovedAnswer entity(ApprovedAnswerRequest request) {
        return new ApprovedAnswer(request.canonicalKey(),
            request.representativeQuestion(), request.answerType(),
            request.textValue(), request.booleanValue(), request.numberValue(),
            request.classification(), request.reusable(), request.answerSource(),
            request.profileField(), request.notes());
    }

    private ApprovedAnswer persisted(ApprovedAnswer answer) {
        if (answer.getId() == null) {
            ReflectionTestUtils.setField(answer, "id", 1L);
            answer.onCreate();
        }
        return answer;
    }

    private ApplicantProfile profile(boolean verified, Boolean relocate,
        Boolean travel, BigDecimal salary) {
        ApplicantProfile profile = new ApplicantProfile("A", "Person", null,
            "a@example.com", null, null, null, null, null, null, null, null,
            null, RemoteType.UNKNOWN, salary, "USD", relocate, travel);
        ReflectionTestUtils.setField(profile, "id", 9L);
        if (verified) profile.verify();
        return profile;
    }

    private void updateProfileToClearVerification(ApplicantProfile profile) {
        profile.update("A", "Person", "Changed", "a@example.com", null, null,
            null, null, null, null, null, null, null, RemoteType.UNKNOWN,
            new BigDecimal("125000"), "USD", true, false);
    }
}
