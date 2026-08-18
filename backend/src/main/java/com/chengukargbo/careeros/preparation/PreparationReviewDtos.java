package com.chengukargbo.careeros.preparation;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import com.chengukargbo.careeros.answers.AnswerType;
import com.chengukargbo.careeros.preparation.ApprovedFieldPlan.ValueSource;
import com.chengukargbo.careeros.preparation.FieldPreparationResult.Outcome;
import com.chengukargbo.careeros.preparation.PreparationReviewQuestion.Category;
import com.chengukargbo.careeros.questions.QuestionEnums;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public final class PreparationReviewDtos {private PreparationReviewDtos(){}
 public record CreateRequest(@NotEmpty List<@Valid ScreenshotInput> screenshots){}
 public record ScreenshotInput(@NotBlank @Size(max=1000) String reference,@Size(max=200) String pageKey,@NotNull OffsetDateTime capturedAt){}
 public record Response(Long id,Long sessionId,String snapshotHash,OffsetDateTime generatedAt,List<Field> preparedFields,List<Question> unansweredQuestions,List<Question> blockedQuestions,List<Screenshot> screenshots){}
 public record Field(String canonicalKey,AnswerType answerType,String textValue,Boolean booleanValue,BigDecimal numberValue,ValueSource source,Outcome outcome,String safeMessage,OffsetDateTime preparedAt){}
 public record Question(Long applicationQuestionId,String canonicalKey,String questionText,QuestionEnums.AnswerType answerType,boolean required,QuestionEnums.Status status,Category category){}
 public record Screenshot(String reference,String pageKey,OffsetDateTime capturedAt){}
}
