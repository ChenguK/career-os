package com.chengukargbo.careeros.preparation;

import java.math.BigDecimal;
import java.util.List;
import com.chengukargbo.careeros.questions.QuestionEnums.*;
import com.chengukargbo.careeros.questions.research.LikelyQuestion;

public final class BrowserPreparationIntelligenceDtos { private BrowserPreparationIntelligenceDtos() {}
 public enum MatchMethod { EXPLICIT_CONFIRMED, ADAPTER_AUTHORITATIVE, EXTERNAL_ID, REPRESENTATIVE_QUESTION, NONE }
 public enum SuggestionSource { APPLICANT_PROFILE, APPROVED_ANSWER }
 public record Response(Long applicationId,Long observationSnapshotId,String provider,
    List<ObservedAssessment> observedQuestions,List<LikelyQuestion> researchedNotObserved,
    List<Suggestion> suggestedAnswers,List<Gap> preparationGaps) {}
 public record ObservedAssessment(Long observedQuestionId,String externalQuestionId,
    String questionText,AnswerType answerType,boolean required,String canonicalKey,
    MatchMethod matchMethod,QuestionMappingEnums.MappingSource mappingSource,boolean mappingTrusted,BigDecimal matchConfidence,BigDecimal researchProbability,
    BigDecimal researchConfidence,List<Suggestion> suggestions,boolean missingAnswer) {}
 public record Suggestion(String canonicalKey,SuggestionSource source,Long sourceId,
    String value,BigDecimal confidence) {}
 public record Gap(String code,String canonicalKey,String question,String recommendation,
    BigDecimal confidence,String actionPath) {}
}
