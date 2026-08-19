package com.chengukargbo.careeros.preparation;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import com.chengukargbo.careeros.preparation.QuestionMappingEnums.*;
import com.chengukargbo.careeros.questions.QuestionEnums.*;
import jakarta.validation.constraints.*;

public final class QuestionMappingDtos { private QuestionMappingDtos() {}
    public record MappingSuggestion(String canonicalKey,String representativeQuestion,MappingSource source,BigDecimal confidence,String rationale) {}
    public record CanonicalKeyOption(String canonicalKey,String representativeQuestion,AnswerType answerType,Classification classification,List<String> sources) {}
    public record ReviewItem(Long mappingId,Long observedQuestionId,String externalQuestionId,String questionText,AnswerType answerType,boolean required,List<ObservationDtos.OptionResponse> options,FormIdentity formIdentity,MappingState mappingState,String canonicalQuestionKey,MappingSource mappingSource,BigDecimal mappingConfidence,boolean userConfirmed,OffsetDateTime confirmedAt,OffsetDateTime revokedAt,List<MappingSuggestion> suggestions) {}
    public record ReviewResponse(Long applicationId,Long snapshotId,List<CanonicalKeyOption> canonicalKeys,List<ReviewItem> questions) {}
    public record ConfirmRequest(@NotBlank @Size(max=200) String externalQuestionId,@NotBlank @Pattern(regexp="^[a-z][a-z0-9_]{2,79}$") String canonicalQuestionKey,@NotNull JobFamily jobFamily,Seniority seniority) {}
    public record MappingResponse(Long id,Long applicationId,String externalQuestionId,String canonicalQuestionKey,MappingSource mappingSource,MappingState mappingState,BigDecimal confidence,boolean userConfirmed,OffsetDateTime confirmedAt,OffsetDateTime revokedAt,OffsetDateTime createdAt,OffsetDateTime updatedAt) {static MappingResponse from(ObservedQuestionMapping value){return new MappingResponse(value.getId(),value.getFormTarget().getApplication().getId(),value.getExternalQuestionId(),value.getCanonicalQuestionKey(),value.getMappingSource(),value.getMappingState(),value.getConfidence(),value.isUserConfirmed(),value.getConfirmedAt(),value.getRevokedAt(),value.getCreatedAt(),value.getUpdatedAt());}}
    public record HistoryResponse(Long id,MappingEventType eventType,String previousCanonicalKey,String canonicalQuestionKey,MappingSource mappingSource,BigDecimal confidence,boolean userConfirmed,OffsetDateTime occurredAt){static HistoryResponse from(ObservedQuestionMappingHistory value){return new HistoryResponse(value.getId(),value.getEventType(),value.getPreviousCanonicalKey(),value.getCanonicalQuestionKey(),value.getMappingSource(),value.getConfidence(),value.isUserConfirmed(),value.getOccurredAt());}}
}
