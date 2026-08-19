package com.chengukargbo.careeros.preparation;

import static com.chengukargbo.careeros.preparation.BrowserPreparationIntelligenceDtos.*;
import static com.chengukargbo.careeros.questions.QuestionEnums.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.chengukargbo.careeros.answers.AnswerClassification;
import com.chengukargbo.careeros.answers.AnswerSource;
import com.chengukargbo.careeros.answers.ApprovedAnswerService;
import com.chengukargbo.careeros.answers.dto.ApprovedAnswerResponse;
import com.chengukargbo.careeros.applications.Application;
import com.chengukargbo.careeros.applications.ApplicationRepository;
import com.chengukargbo.careeros.jobs.RemoteType;
import com.chengukargbo.careeros.profile.ApplicantProfile;
import com.chengukargbo.careeros.profile.ApplicantProfileRepository;
import com.chengukargbo.careeros.questions.research.LikelyQuestion;
import com.chengukargbo.careeros.questions.research.QuestionResearchService;

@ExtendWith(MockitoExtension.class)
class BrowserPreparationIntelligenceServiceTest {
    @Mock ApplicationRepository applications;
    @Mock FormObservationSnapshotRepository snapshots;
    @Mock ObservedQuestionRepository observed;
    @Mock QuestionResearchService research;
    @Mock ApprovedAnswerService answers;
    @Mock ApplicantProfileRepository profiles;
    @Mock ObservedQuestionMappingRepository mappings;
    private BrowserPreparationIntelligenceService service;
    private FormObservationSnapshot snapshot;

    @BeforeEach
    void setUp() {
        service = new BrowserPreparationIntelligenceService(
            applications, snapshots, observed, research, answers, profiles, mappings);
        snapshot = mock(FormObservationSnapshot.class);
        when(applications.findById(7L)).thenReturn(Optional.of(mock(Application.class)));
        when(snapshot.getId()).thenReturn(31L);
        when(snapshots.findFirstByFormTargetApplicationIdOrderBySequenceNumberDesc(7L))
            .thenReturn(Optional.of(snapshot));
        when(profiles.findByProfileKey(ApplicantProfile.PRIMARY_PROFILE_KEY))
            .thenReturn(Optional.empty());
        when(answers.findAll()).thenReturn(List.of());
        when(mappings.findByFormTargetApplicationIdAndExternalQuestionId(eq(7L), anyString()))
            .thenReturn(Optional.empty());
    }

    @Test
    void comparesObservedQuestionsAndSuggestsOnlyTrustedProfileAndApprovedValues() {
        ApplicantProfile profile = profile(true);
        when(profiles.findByProfileKey(ApplicantProfile.PRIMARY_PROFILE_KEY))
            .thenReturn(Optional.of(profile));
        when(answers.findAll()).thenReturn(List.of(
            approved(42L, "why_role", "I value the mission."),
            approved(43L, "email", "stale@example.com")));
        when(research.research(JobFamily.SOFTWARE_ENGINEER, Seniority.MID_LEVEL, null))
            .thenReturn(List.of(
                likely("email", "What is your email?", "0.99", "0.95"),
                likely("why_role", "Why do you want this role?", "0.90", "0.90")));
        when(observed.findBySnapshotIdOrderByDisplayOrderAscExternalQuestionIdAsc(31L))
            .thenReturn(List.of(
                question(1L, "email", "Email address", 0, true),
                question(2L, "ats-why", "Why do you want this role?", 1, true)));
        when(mappings.findByFormTargetApplicationIdAndExternalQuestionId(7L, "email"))
            .thenReturn(Optional.of(mapping("email", "email", QuestionMappingEnums.MappingSource.USER, true, "1.000")));
        when(mappings.findByFormTargetApplicationIdAndExternalQuestionId(7L, "ats-why"))
            .thenReturn(Optional.of(mapping("ats-why", "why_role", QuestionMappingEnums.MappingSource.EXACT_TEXT, true, "0.950")));

        Response result = service.analyze(
            7L, JobFamily.SOFTWARE_ENGINEER, Seniority.MID_LEVEL, null);

        assertThat(result.observedQuestions()).hasSize(2);
        assertThat(result.observedQuestions().get(0).matchMethod()).isEqualTo(MatchMethod.EXPLICIT_CONFIRMED);
        assertThat(result.observedQuestions().get(0).suggestions()).singleElement().satisfies(s -> {
            assertThat(s.source()).isEqualTo(SuggestionSource.APPLICANT_PROFILE);
            assertThat(s.value()).isEqualTo("verified@example.com");
            assertThat(s.confidence()).isEqualByComparingTo("0.99");
        });
        assertThat(result.observedQuestions().get(1).matchMethod())
            .isEqualTo(MatchMethod.EXPLICIT_CONFIRMED);
        assertThat(result.observedQuestions().get(1).suggestions()).singleElement().satisfies(s -> {
            assertThat(s.source()).isEqualTo(SuggestionSource.APPROVED_ANSWER);
            assertThat(s.confidence()).isEqualByComparingTo("0.97");
        });
        assertThat(result.observedQuestions()).noneMatch(ObservedAssessment::missingAnswer);
        assertThat(result.preparationGaps()).isEmpty();
    }

    @Test
    void ordersSuggestionsByConfidenceAndDoesNotUseUnverifiedProfileValues() {
        when(profiles.findByProfileKey(ApplicantProfile.PRIMARY_PROFILE_KEY))
            .thenReturn(Optional.of(profile(false)));
        when(answers.findAll()).thenReturn(List.of(
            approved(51L, "why_role", "Mission answer"),
            approved(52L, "email", "approved@example.com")));
        when(research.research(JobFamily.SOFTWARE_ENGINEER, Seniority.MID_LEVEL, null))
            .thenReturn(List.of(
                likely("email", "Email?", "0.99", "0.95"),
                likely("why_role", "Why role?", "0.90", "0.90")));
        when(observed.findBySnapshotIdOrderByDisplayOrderAscExternalQuestionIdAsc(31L))
            .thenReturn(List.of(
                question(1L, "ats-why", "Why role?", 0, true),
                question(2L, "email", "Email?", 1, true)));
        when(mappings.findByFormTargetApplicationIdAndExternalQuestionId(7L, "ats-why"))
            .thenReturn(Optional.of(mapping("ats-why", "why_role", QuestionMappingEnums.MappingSource.USER, true, "0.950")));
        when(mappings.findByFormTargetApplicationIdAndExternalQuestionId(7L, "email"))
            .thenReturn(Optional.of(mapping("email", "email", QuestionMappingEnums.MappingSource.ADAPTER, false, "1.000")));

        Response result = service.analyze(
            7L, JobFamily.SOFTWARE_ENGINEER, Seniority.MID_LEVEL, null);

        assertThat(result.suggestedAnswers()).extracting(Suggestion::canonicalKey)
            .containsExactly("email", "why_role");
        assertThat(result.suggestedAnswers()).extracting(Suggestion::confidence)
            .containsExactly(new BigDecimal("0.97"), new BigDecimal("0.97"));
        assertThat(result.suggestedAnswers()).allMatch(
            suggestion -> suggestion.source() == SuggestionSource.APPROVED_ANSWER);
        assertThat(result.observedQuestions().get(1).matchMethod())
            .isEqualTo(MatchMethod.ADAPTER_AUTHORITATIVE);
    }

    @Test
    void detectsMissingAnswersAndProducesDeterministicPreparationRecommendations() {
        when(research.research(JobFamily.SOFTWARE_ENGINEER, Seniority.MID_LEVEL, null))
            .thenReturn(List.of(
                likely("email", "Email?", "0.90", "0.90"),
                likely("portfolio_url", "Portfolio?", "0.80", "0.80")));
        when(observed.findBySnapshotIdOrderByDisplayOrderAscExternalQuestionIdAsc(31L))
            .thenReturn(List.of(
                question(1L, "email", "Email?", 0, true),
                question(2L, "custom-id", "Describe a recent project", 1, true),
                question(3L, "portfolio_url", "Portfolio?", 2, false)));

        Response result = service.analyze(
            7L, JobFamily.SOFTWARE_ENGINEER, Seniority.MID_LEVEL, null);

        assertThat(result.observedQuestions()).hasSize(2);
        assertThat(result.observedQuestions()).allMatch(ObservedAssessment::missingAnswer);
        assertThat(result.researchedNotObserved()).extracting(LikelyQuestion::canonicalKey)
            .containsExactly("portfolio_url");
        assertThat(result.preparationGaps()).extracting(Gap::code)
            .containsExactly(
                "UNCONFIRMED_QUESTION_MAPPING",
                "UNMAPPED_OBSERVED_QUESTION",
                "LIKELY_QUESTION_NOT_OBSERVED");
        assertThat(result.preparationGaps()).extracting(Gap::recommendation)
            .allMatch(message -> message != null && !message.isBlank());
    }

    @Test
    void producesIdenticalResultsForRepeatedReadOnlyAnalysis() {
        when(research.research(JobFamily.SOFTWARE_ENGINEER, Seniority.MID_LEVEL, null))
            .thenReturn(List.of(likely("email", "Email?", "0.90", "0.90")));
        when(observed.findBySnapshotIdOrderByDisplayOrderAscExternalQuestionIdAsc(31L))
            .thenReturn(List.of(question(1L, "email", "Email?", 0, true)));

        Response first = service.analyze(7L, JobFamily.SOFTWARE_ENGINEER, Seniority.MID_LEVEL, null);
        Response second = service.analyze(7L, JobFamily.SOFTWARE_ENGINEER, Seniority.MID_LEVEL, null);

        assertThat(second).isEqualTo(first);
        verify(applications, times(2)).findById(7L);
        verifyNoMoreInteractions(applications);
    }

    @Test
    void deterministicCandidateCannotUseApprovedAnswerBeforeMappingConfirmation() {
        when(answers.findAll()).thenReturn(List.of(
            approved(52L, "email", "approved@example.com")));
        when(research.research(JobFamily.SOFTWARE_ENGINEER, Seniority.MID_LEVEL, null))
            .thenReturn(List.of(likely("email", "Email?", "0.99", "0.95")));
        when(observed.findBySnapshotIdOrderByDisplayOrderAscExternalQuestionIdAsc(31L))
            .thenReturn(List.of(question(1L, "email", "Email?", 0, true)));

        Response result = service.analyze(
            7L, JobFamily.SOFTWARE_ENGINEER, Seniority.MID_LEVEL, null);

        assertThat(result.observedQuestions().getFirst().mappingTrusted()).isFalse();
        assertThat(result.observedQuestions().getFirst().suggestions()).isEmpty();
        assertThat(result.preparationGaps()).extracting(Gap::code)
            .contains("UNCONFIRMED_QUESTION_MAPPING");
    }

    private ObservedQuestion question(Long id, String externalId, String text,
        int order, boolean active) {
        ObservedQuestion question = new ObservedQuestion(
            snapshot, externalId, text, AnswerType.TEXT, true, active, order, "fingerprint-" + id);
        ReflectionTestUtils.setField(question, "id", id);
        return question;
    }

    private LikelyQuestion likely(String key, String text, String probability,
        String confidence) {
        return new LikelyQuestion(key, text, AnswerType.TEXT, Classification.CONTEXTUAL,
            true, new BigDecimal(probability), "static-careeros-templates",
            JobFamily.SOFTWARE_ENGINEER, Seniority.MID_LEVEL, new BigDecimal(confidence));
    }

    private ApplicantProfile profile(boolean verified) {
        ApplicantProfile profile = new ApplicantProfile(
            "Verified", "Applicant", null, "verified@example.com", null,
            null, null, null, null, null, null, null, null,
            RemoteType.REMOTE, null, "USD", null, null);
        ReflectionTestUtils.setField(profile, "id", 11L);
        if (verified) profile.verify();
        return profile;
    }

    private ApprovedAnswerResponse approved(Long id, String key, String value) {
        OffsetDateTime now = OffsetDateTime.now();
        return new ApprovedAnswerResponse(
            id, key, key, com.chengukargbo.careeros.answers.AnswerType.TEXT,
            value, null, null, AnswerClassification.VERIFIED_REUSABLE,
            true, true, now, null, AnswerSource.MANUAL, null, true, true,
            value, null, null, null, null, now, now);
    }

    private ObservedQuestionMapping mapping(String externalId, String canonicalKey,
        QuestionMappingEnums.MappingSource source, boolean userConfirmed, String confidence) {
        ObservedQuestionMapping mapping = new ObservedQuestionMapping(
            mock(ApplicationFormTarget.class), externalId);
        mapping.confirm(canonicalKey, source, new BigDecimal(confidence), userConfirmed);
        return mapping;
    }
}
