package com.chengukargbo.careeros.questions.research;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;
import com.chengukargbo.careeros.questions.*;
import com.chengukargbo.careeros.questions.QuestionDtos.TemplateResponse;
import com.chengukargbo.careeros.questions.QuestionEnums.*;

@Component
public class StaticCareerOSTemplates implements QuestionResearchProvider {
    public static final String ID = "STATIC_CAREEROS_TEMPLATES";
    private final QuestionQueueService templates;
    public StaticCareerOSTemplates(QuestionQueueService templates) { this.templates = templates; }
    @Override public String providerId() { return ID; }
    @Override public boolean canHandle(JobFamily family, Seniority seniority) { return family != null; }
    @Override public List<LikelyQuestion> research(JobFamily family, Seniority seniority) {
        return templates.templates(family, seniority).stream().map(template -> likely(template, family, seniority)).toList();
    }
    private LikelyQuestion likely(TemplateResponse template, JobFamily family, Seniority requestedSeniority) {
        boolean exact = template.seniority() != null && template.seniority() == requestedSeniority;
        BigDecimal probability = template.requiredByDefault() ? new BigDecimal("0.90")
            : template.common() ? new BigDecimal("0.75") : new BigDecimal("0.55");
        if (exact) probability = probability.add(new BigDecimal("0.08")).min(new BigDecimal("0.98"));
        return new LikelyQuestion(template.canonicalQuestionKey(), template.representativeQuestion(),
            template.answerType(), template.classification(), template.requiredByDefault(),
            probability, ID, family, requestedSeniority,
            exact ? new BigDecimal("0.95") : new BigDecimal("0.85"));
    }
}
