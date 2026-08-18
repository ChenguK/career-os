package com.chengukargbo.careeros.answers.dto;

import java.math.BigDecimal;

import com.chengukargbo.careeros.answers.AnswerClassification;
import com.chengukargbo.careeros.answers.AnswerSource;
import com.chengukargbo.careeros.answers.AnswerType;
import com.chengukargbo.careeros.answers.ProfileAnswerField;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ApprovedAnswerRequest(
    @NotBlank(message = "Canonical key is required")
    @Size(max = 80, message = "Canonical key must not exceed 80 characters")
    @Pattern(
        regexp = "^[A-Za-z][A-Za-z0-9_]{2,79}$",
        message = "Canonical key must use letters, numbers, and underscores"
    )
    String canonicalKey,

    @NotBlank(message = "Representative question is required")
    @Size(
        max = 500,
        message = "Representative question must not exceed 500 characters"
    )
    String representativeQuestion,

    @NotNull(message = "Answer type is required")
    AnswerType answerType,

    String textValue,
    Boolean booleanValue,

    @Digits(
        integer = 12,
        fraction = 2,
        message = "Number answer must have at most two decimal places"
    )
    BigDecimal numberValue,

    @NotNull(message = "Classification is required")
    AnswerClassification classification,

    boolean reusable,

    @NotNull(message = "Answer source is required")
    AnswerSource answerSource,

    ProfileAnswerField profileField,
    String notes
) {
}
