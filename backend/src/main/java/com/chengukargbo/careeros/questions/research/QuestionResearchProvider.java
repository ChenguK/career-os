package com.chengukargbo.careeros.questions.research;

import java.util.List;
import com.chengukargbo.careeros.questions.QuestionEnums.*;

public interface QuestionResearchProvider {
    String providerId();
    boolean canHandle(JobFamily jobFamily, Seniority seniority);
    List<LikelyQuestion> research(JobFamily jobFamily, Seniority seniority);
}
