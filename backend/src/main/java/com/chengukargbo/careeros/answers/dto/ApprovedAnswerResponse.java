package com.chengukargbo.careeros.answers.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.chengukargbo.careeros.answers.AnswerClassification;
import com.chengukargbo.careeros.answers.AnswerSource;
import com.chengukargbo.careeros.answers.AnswerType;
import com.chengukargbo.careeros.answers.ApprovedAnswer;
import com.chengukargbo.careeros.answers.ProfileAnswerField;

public record ApprovedAnswerResponse(
    Long id,
    String canonicalKey,
    String representativeQuestion,
    AnswerType answerType,
    String textValue,
    Boolean booleanValue,
    BigDecimal numberValue,
    AnswerClassification classification,
    boolean reusable,
    boolean userApproved,
    OffsetDateTime approvedAt,
    OffsetDateTime lastUsedAt,
    AnswerSource answerSource,
    ProfileAnswerField profileField,
    boolean authorityAvailable,
    boolean effectiveReusable,
    String resolvedTextValue,
    Boolean resolvedBooleanValue,
    BigDecimal resolvedNumberValue,
    String resolvedCurrency,
    String notes,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
    public static ApprovedAnswerResponse from(
        ApprovedAnswer answer,
        boolean authorityAvailable,
        String resolvedTextValue,
        Boolean resolvedBooleanValue,
        BigDecimal resolvedNumberValue,
        String resolvedCurrency
    ) {
        boolean effectiveReusable = answer.isUserApproved()
            && answer.isReusable()
            && answer.getClassification()
                == AnswerClassification.VERIFIED_REUSABLE
            && authorityAvailable;
        return new ApprovedAnswerResponse(
            answer.getId(),
            answer.getCanonicalKey(),
            answer.getRepresentativeQuestion(),
            answer.getAnswerType(),
            answer.getTextValue(),
            answer.getBooleanValue(),
            answer.getNumberValue(),
            answer.getClassification(),
            answer.isReusable(),
            answer.isUserApproved(),
            answer.getApprovedAt(),
            answer.getLastUsedAt(),
            answer.getAnswerSource(),
            answer.getProfileField(),
            authorityAvailable,
            effectiveReusable,
            resolvedTextValue,
            resolvedBooleanValue,
            resolvedNumberValue,
            resolvedCurrency,
            answer.getNotes(),
            answer.getCreatedAt(),
            answer.getUpdatedAt()
        );
    }
}
