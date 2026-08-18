package com.chengukargbo.careeros.preparation;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import com.chengukargbo.careeros.answers.AnswerType;
import com.chengukargbo.careeros.preparation.ApprovedFieldPlan.ValueSource;
import com.chengukargbo.careeros.preparation.FieldPreparationResult.Outcome;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class FieldPreparationDtos {
    private FieldPreparationDtos() {}
    public record PlanResponse(Long id, Long sessionId, OffsetDateTime generatedAt,
        List<PlanItemResponse> fields) {}
    public record PlanItemResponse(Long id, String canonicalKey, AnswerType answerType,
        String textValue, Boolean booleanValue, BigDecimal numberValue,
        ValueSource source, Long sourceRecordId, OffsetDateTime sourceVerifiedAt) {}
    public record ResultsRequest(@NotNull List<@Valid ResultInput> results) {}
    public record ResultInput(@NotNull Long planItemId, @NotNull Outcome outcome,
        @Size(max=1000) String safeMessage, OffsetDateTime preparedAt) {}
    public record ResultsResponse(Long planId, long preparedCount, long skippedCount,
        long failedCount, OffsetDateTime recordedAt) {}
}
