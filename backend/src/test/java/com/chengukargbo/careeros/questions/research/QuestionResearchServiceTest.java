package com.chengukargbo.careeros.questions.research;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import com.chengukargbo.careeros.questions.*;
import com.chengukargbo.careeros.questions.QuestionDtos.TemplateResponse;
import com.chengukargbo.careeros.questions.QuestionEnums.*;

class QuestionResearchServiceTest {
    @Test void selectsRequestedProviderWithoutCallingFallback() {
        StaticCareerOSTemplates fallback=mock(StaticCareerOSTemplates.class);
        QuestionResearchProvider selected=provider("FUTURE",List.of(question("future",new BigDecimal("0.80"),new BigDecimal("0.90"),"FUTURE")));
        QuestionResearchService service=new QuestionResearchService(List.of(fallback,selected),fallback);
        assertEquals("FUTURE",service.research(JobFamily.SOFTWARE_ENGINEER,Seniority.SENIOR,"future").getFirst().source());
        verify(fallback,never()).research(any(),any());
    }

    @Test void ordersDeterministicallyByProbabilityConfidenceAndKey() {
        StaticCareerOSTemplates fallback=mock(StaticCareerOSTemplates.class);
        when(fallback.research(any(),any())).thenReturn(List.of(
            question("z",new BigDecimal("0.70"),new BigDecimal("0.90"),StaticCareerOSTemplates.ID),
            question("a",new BigDecimal("0.80"),new BigDecimal("0.80"),StaticCareerOSTemplates.ID),
            question("b",new BigDecimal("0.80"),new BigDecimal("0.90"),StaticCareerOSTemplates.ID)));
        var result=new QuestionResearchService(List.of(fallback),fallback).research(JobFamily.SOFTWARE_ENGINEER,Seniority.MID_LEVEL,null);
        assertEquals(List.of("b","a","z"),result.stream().map(LikelyQuestion::canonicalKey).toList());
    }

    @Test void eliminatesDuplicatesKeepingHighestQualityResult() {
        StaticCareerOSTemplates fallback=mock(StaticCareerOSTemplates.class);
        when(fallback.research(any(),any())).thenReturn(List.of(
            question("email",new BigDecimal("0.60"),new BigDecimal("0.90"),StaticCareerOSTemplates.ID),
            question("email",new BigDecimal("0.85"),new BigDecimal("0.80"),StaticCareerOSTemplates.ID)));
        var result=new QuestionResearchService(List.of(fallback),fallback).research(JobFamily.SOFTWARE_ENGINEER,Seniority.ENTRY_LEVEL,null);
        assertEquals(1,result.size()); assertEquals(new BigDecimal("0.85"),result.getFirst().probability());
    }

    @Test void fallsBackToStaticTemplatesWhenSelectedProviderHasNoResults() {
        StaticCareerOSTemplates fallback=mock(StaticCareerOSTemplates.class);
        when(fallback.research(any(),any())).thenReturn(List.of(question("fallback",new BigDecimal("0.75"),new BigDecimal("0.85"),StaticCareerOSTemplates.ID)));
        QuestionResearchProvider empty=provider("EMPTY",List.of());
        var result=new QuestionResearchService(List.of(fallback,empty),fallback).research(JobFamily.FRONTEND_ENGINEER,Seniority.SENIOR,"EMPTY");
        assertEquals("fallback",result.getFirst().canonicalKey()); verify(fallback).research(JobFamily.FRONTEND_ENGINEER,Seniority.SENIOR);
    }

    @Test void staticTemplatesUseExplainableConfidenceAndProbabilityScores() {
        QuestionQueueService templates=mock(QuestionQueueService.class);
        when(templates.templates(JobFamily.SOFTWARE_ENGINEER,Seniority.SENIOR)).thenReturn(List.of(
            new TemplateResponse(1L,JobFamily.SOFTWARE_ENGINEER,null,"common","Common?",AnswerType.TEXT,Classification.VERIFIED_REUSABLE,false,true),
            new TemplateResponse(2L,JobFamily.SOFTWARE_ENGINEER,Seniority.SENIOR,"required","Required?",AnswerType.BOOLEAN,Classification.CONTEXTUAL,true,true)));
        var result=new StaticCareerOSTemplates(templates).research(JobFamily.SOFTWARE_ENGINEER,Seniority.SENIOR);
        assertEquals(new BigDecimal("0.75"),result.get(0).probability()); assertEquals(new BigDecimal("0.85"),result.get(0).confidence());
        assertEquals(new BigDecimal("0.98"),result.get(1).probability()); assertEquals(new BigDecimal("0.95"),result.get(1).confidence());
    }

    private QuestionResearchProvider provider(String id,List<LikelyQuestion> values){return new QuestionResearchProvider(){public String providerId(){return id;}public boolean canHandle(JobFamily f,Seniority s){return true;}public List<LikelyQuestion> research(JobFamily f,Seniority s){return values;}};}
    private LikelyQuestion question(String key,BigDecimal probability,BigDecimal confidence,String source){return new LikelyQuestion(key,key,AnswerType.TEXT,Classification.UNKNOWN,false,probability,source,JobFamily.SOFTWARE_ENGINEER,Seniority.MID_LEVEL,confidence);}
}
