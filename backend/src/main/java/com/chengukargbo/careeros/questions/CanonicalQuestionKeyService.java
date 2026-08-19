package com.chengukargbo.careeros.questions;

import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.chengukargbo.careeros.answers.ApprovedAnswerRepository;
import com.chengukargbo.careeros.questions.QuestionEnums.*;

@Service @Transactional(readOnly=true)
public class CanonicalQuestionKeyService {
    public record CanonicalKey(String key,String representativeQuestion,AnswerType answerType,Classification classification,Set<String> sources) {}
    private static final Map<String,CanonicalKey> PROFILE_KEYS=profileKeys();
    private final QuestionTemplateRepository templates; private final ApprovedAnswerRepository answers;
    public CanonicalQuestionKeyService(QuestionTemplateRepository templates,ApprovedAnswerRepository answers){this.templates=templates;this.answers=answers;}
    public List<CanonicalKey> all(){
        Map<String,CanonicalKey> keys=new TreeMap<>(PROFILE_KEYS);
        templates.findByActiveTrueOrderByCanonicalQuestionKeyAsc().forEach(t->merge(keys,t.getCanonicalQuestionKey(),t.getRepresentativeQuestion(),t.getAnswerType(),t.getClassification(),"QUESTION_TEMPLATE"));
        answers.findAllByOrderByCanonicalKeyAsc().forEach(a->merge(keys,a.getCanonicalKey(),a.getRepresentativeQuestion(),convert(a.getAnswerType()),classification(a.getClassification()),"APPROVED_ANSWER"));
        return List.copyOf(keys.values());
    }
    public CanonicalKey require(String raw){String key=normalize(raw);return all().stream().filter(x->x.key().equals(key)).findFirst().orElseThrow(()->new com.chengukargbo.careeros.common.exception.BusinessValidationException("Canonical question key is not registered: "+key));}
    private void merge(Map<String,CanonicalKey> values,String key,String question,AnswerType type,Classification classification,String source){CanonicalKey prior=values.get(key);Set<String> sources=new TreeSet<>();if(prior!=null)sources.addAll(prior.sources());sources.add(source);values.put(key,new CanonicalKey(key,prior==null?question:prior.representativeQuestion(),prior==null?type:prior.answerType(),prior==null?classification:prior.classification(),Set.copyOf(sources)));}
    private String normalize(String value){if(value==null||!value.matches("^[a-z][a-z0-9_]{2,79}$"))throw new com.chengukargbo.careeros.common.exception.BusinessValidationException("Canonical question key is invalid");return value;}
    private AnswerType convert(com.chengukargbo.careeros.answers.AnswerType type){return switch(type){case TEXT->AnswerType.TEXT;case BOOLEAN->AnswerType.BOOLEAN;case NUMBER->AnswerType.NUMBER;};}
    private Classification classification(com.chengukargbo.careeros.answers.AnswerClassification value){return switch(value){case VERIFIED_REUSABLE->Classification.VERIFIED_REUSABLE;case CONTEXTUAL->Classification.CONTEXTUAL;case SENSITIVE->Classification.SENSITIVE;case UNKNOWN->Classification.UNKNOWN;};}
    private static Map<String,CanonicalKey> profileKeys(){Map<String,CanonicalKey> out=new HashMap<>();add(out,"first_name","First name",AnswerType.TEXT);add(out,"last_name","Last name",AnswerType.TEXT);add(out,"preferred_name","Preferred name",AnswerType.TEXT);add(out,"email","Email address",AnswerType.TEXT);add(out,"phone","Phone number",AnswerType.TEXT);add(out,"city","City",AnswerType.TEXT);add(out,"state_region","State or region",AnswerType.TEXT);add(out,"country","Country",AnswerType.TEXT);add(out,"postal_code","Postal code",AnswerType.TEXT);add(out,"portfolio_url","Portfolio URL",AnswerType.TEXT);add(out,"github_url","GitHub URL",AnswerType.TEXT);add(out,"linkedin_url","LinkedIn URL",AnswerType.TEXT);add(out,"willing_to_relocate","Willing to relocate",AnswerType.BOOLEAN);add(out,"willing_to_travel","Willing to travel",AnswerType.BOOLEAN);add(out,"salary_expectation","Salary expectation",AnswerType.NUMBER);return Map.copyOf(out);}
    private static void add(Map<String,CanonicalKey> out,String key,String label,AnswerType type){out.put(key,new CanonicalKey(key,label,type,Classification.VERIFIED_REUSABLE,Set.of("APPLICANT_PROFILE")));}
}
