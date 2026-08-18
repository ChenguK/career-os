package com.chengukargbo.careeros.automation;
import java.time.OffsetDateTime; import com.chengukargbo.careeros.automation.AutomationEnums.*; import com.chengukargbo.careeros.questions.QuestionReadinessService.Readiness;
public final class AutomationDtos {private AutomationDtos(){}
 public record Response(Long id,Long applicationId,State state,SubmissionMode submissionMode,AtsType atsType,long unresolvedRequiredCount,long needsReviewCount,long blockerCount,String blockReason,OffsetDateTime approvedForPrepAt,OffsetDateTime readyForReviewAt,OffsetDateTime approvedToSubmitAt,OffsetDateTime updatedAt){static Response from(ApplicationAutomation a,Readiness r){return new Response(a.getId(),a.getApplication().getId(),a.getState(),a.getSubmissionMode(),a.getAtsType(),r.requiredUnresolved(),r.needsReview(),r.blockers(),a.getBlockReason(),a.getApprovedForPrepAt(),a.getReadyForReviewAt(),a.getApprovedToSubmitAt(),a.getUpdatedAt());}}
 public record HistoryResponse(Long id,Long applicationId,State previousState,State newState,Source source,OffsetDateTime occurredAt,String reason){static HistoryResponse from(ApplicationAutomationHistory h){return new HistoryResponse(h.getId(),h.getApplicationId(),h.getPreviousState(),h.getNewState(),h.getSource(),h.getOccurredAt(),h.getReason());}}
 public record AtsRequest(AtsType atsType){}
}
