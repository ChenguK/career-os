package com.chengukargbo.careeros.questions.research;

import java.math.BigDecimal;
import com.chengukargbo.careeros.questions.QuestionEnums.*;

public record LikelyQuestion(
    String canonicalKey,
    String question,
    AnswerType answerType,
    Classification classification,
    boolean required,
    BigDecimal probability,
    String source,
    JobFamily jobFamily,
    Seniority seniority,
    BigDecimal confidence
) {}
