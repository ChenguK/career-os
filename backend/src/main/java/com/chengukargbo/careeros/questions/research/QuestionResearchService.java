package com.chengukargbo.careeros.questions.research;

import java.util.*;
import org.springframework.stereotype.Service;
import com.chengukargbo.careeros.common.exception.BusinessValidationException;
import com.chengukargbo.careeros.questions.QuestionEnums.*;

@Service
public class QuestionResearchService {
    private final List<QuestionResearchProvider> providers;
    private final StaticCareerOSTemplates fallback;
    public QuestionResearchService(List<QuestionResearchProvider> providers,
        StaticCareerOSTemplates fallback) {
        this.providers = List.copyOf(providers); this.fallback = fallback;
    }
    public List<LikelyQuestion> research(JobFamily family, Seniority seniority,
        String preferredProvider) {
        QuestionResearchProvider selected = select(family, seniority, preferredProvider);
        List<LikelyQuestion> researched = selected.research(family, seniority);
        if (researched.isEmpty() && selected != fallback) researched = fallback.research(family, seniority);
        Map<String,LikelyQuestion> unique = new HashMap<>();
        for (LikelyQuestion question : researched) unique.merge(question.canonicalKey(), question,
            (a,b) -> compareQuality(a,b) <= 0 ? a : b);
        return unique.values().stream().sorted(Comparator
            .comparing(LikelyQuestion::probability).reversed()
            .thenComparing(LikelyQuestion::confidence, Comparator.reverseOrder())
            .thenComparing(LikelyQuestion::canonicalKey)
            .thenComparing(LikelyQuestion::question)).toList();
    }
    private QuestionResearchProvider select(JobFamily family, Seniority seniority,
        String preferred) {
        if (preferred == null || preferred.isBlank()) return fallback;
        return providers.stream().filter(p -> preferred.trim().equalsIgnoreCase(p.providerId()))
            .filter(p -> p.canHandle(family, seniority)).findFirst()
            .orElseThrow(() -> new BusinessValidationException("Question research provider is unavailable: " + preferred.trim()));
    }
    private int compareQuality(LikelyQuestion a, LikelyQuestion b) {
        int probability = b.probability().compareTo(a.probability());
        return probability != 0 ? probability : b.confidence().compareTo(a.confidence());
    }
}
