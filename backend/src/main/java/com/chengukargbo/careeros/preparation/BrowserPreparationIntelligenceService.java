package com.chengukargbo.careeros.preparation;

import static com.chengukargbo.careeros.preparation.BrowserPreparationIntelligenceDtos.*;
import java.math.*;
import java.text.Normalizer;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.chengukargbo.careeros.answers.*;
import com.chengukargbo.careeros.answers.dto.ApprovedAnswerResponse;
import com.chengukargbo.careeros.applications.*;
import com.chengukargbo.careeros.common.exception.BusinessValidationException;
import com.chengukargbo.careeros.profile.*;
import com.chengukargbo.careeros.questions.QuestionEnums.*;
import com.chengukargbo.careeros.questions.research.*;

@Service @Transactional(readOnly=true)
public class BrowserPreparationIntelligenceService {
 private final ApplicationRepository applications; private final FormObservationSnapshotRepository snapshots;
 private final ObservedQuestionRepository observed; private final QuestionResearchService research;
 private final ApprovedAnswerService answers; private final ApplicantProfileRepository profiles; private final ObservedQuestionMappingRepository mappings;
 public BrowserPreparationIntelligenceService(ApplicationRepository a,FormObservationSnapshotRepository s,ObservedQuestionRepository o,QuestionResearchService r,ApprovedAnswerService answers,ApplicantProfileRepository p,ObservedQuestionMappingRepository mappings){applications=a;snapshots=s;observed=o;research=r;this.answers=answers;profiles=p;this.mappings=mappings;}
 public Response analyze(Long applicationId,JobFamily family,Seniority seniority,String provider){
  applications.findById(applicationId).orElseThrow(()->new ApplicationNotFoundException(applicationId));
  FormObservationSnapshot snapshot=snapshots.findFirstByFormTargetApplicationIdOrderBySequenceNumberDesc(applicationId).orElseThrow(()->new BusinessValidationException("No observed application questions are available"));
  List<LikelyQuestion> likely=research.research(family,seniority,provider);
  Map<String,LikelyQuestion> byKey=new HashMap<>(),byText=new HashMap<>(); likely.forEach(q->{byKey.put(normalizeKey(q.canonicalKey()),q);byText.put(normalizeText(q.question()),q);});
  Map<String,Suggestion> trusted=trustedValues(); Set<String> matched=new HashSet<>(); List<ObservedAssessment> assessments=new ArrayList<>(); List<Gap> gaps=new ArrayList<>();
  for(ObservedQuestion question:observed.findBySnapshotIdOrderByDisplayOrderAscExternalQuestionIdAsc(snapshot.getId())){
   if(!question.isActive())continue;ObservedQuestionMapping explicit=mappings.findByFormTargetApplicationIdAndExternalQuestionId(applicationId,question.getExternalQuestionId()).filter(m->m.getMappingState()==QuestionMappingEnums.MappingState.CONFIRMED).filter(m->m.getMappingSource()==QuestionMappingEnums.MappingSource.ADAPTER||m.isUserConfirmed()).orElse(null);boolean mappingTrusted=explicit!=null;QuestionMappingEnums.MappingSource mappingSource=explicit==null?null:explicit.getMappingSource();String key=explicit==null?null:explicit.getCanonicalQuestionKey();LikelyQuestion match=key==null?null:byKey.get(normalizeKey(key));MatchMethod method=explicit==null?MatchMethod.NONE:(mappingSource==QuestionMappingEnums.MappingSource.ADAPTER?MatchMethod.ADAPTER_AUTHORITATIVE:MatchMethod.EXPLICIT_CONFIRMED);BigDecimal matchConfidence=explicit==null?BigDecimal.ZERO:explicit.getConfidence();
   if(explicit==null){match=byKey.get(normalizeKey(question.getExternalQuestionId()));method=MatchMethod.EXTERNAL_ID;matchConfidence=new BigDecimal("1.00");if(match==null){match=byText.get(normalizeText(question.getQuestionText()));method=MatchMethod.REPRESENTATIVE_QUESTION;matchConfidence=new BigDecimal("0.95");}key=match==null?null:match.canonicalKey();}
   if(key==null){method=MatchMethod.NONE;matchConfidence=BigDecimal.ZERO;gaps.add(new Gap("UNMAPPED_OBSERVED_QUESTION",null,question.getQuestionText(),"Review this ATS question and assign a canonical key before preparing an answer",new BigDecimal("1.00"),mappingPath(applicationId)));}else if(!mappingTrusted){gaps.add(new Gap("UNCONFIRMED_QUESTION_MAPPING",key,question.getQuestionText(),"Review and confirm the deterministic mapping before using an answer",matchConfidence,mappingPath(applicationId)));}
   if(key!=null)matched.add(key);Suggestion suggestion=!mappingTrusted||key==null?null:trusted.get(key);List<Suggestion> suggestions=suggestion==null?List.of():List.of(suggestion);boolean missing=suggestion==null;
   if(mappingTrusted&&missing)gaps.add(new Gap("MISSING_TRUSTED_ANSWER",key,question.getQuestionText(),"Provide and explicitly approve an answer before field preparation",matchConfidence,null));
   assessments.add(new ObservedAssessment(question.getId(),question.getExternalQuestionId(),question.getQuestionText(),question.getAnswerType(),question.isRequired(),key,method,mappingSource,mappingTrusted,matchConfidence,match==null?null:match.probability(),match==null?null:match.confidence(),suggestions,missing));
  }
  List<LikelyQuestion> notObserved=likely.stream().filter(q->!matched.contains(q.canonicalKey())).toList();
  notObserved.forEach(q->gaps.add(new Gap("LIKELY_QUESTION_NOT_OBSERVED",q.canonicalKey(),q.question(),"Prepare this likely question before visiting the application form",q.probability().multiply(q.confidence()).setScale(2,RoundingMode.HALF_UP),null)));
  List<Suggestion> suggested=assessments.stream().flatMap(a->a.suggestions().stream()).sorted(Comparator.comparing(Suggestion::confidence).reversed().thenComparing(Suggestion::source).thenComparing(Suggestion::canonicalKey)).toList();
  gaps.sort(Comparator.comparing(Gap::confidence).reversed().thenComparing(Gap::code).thenComparing(g->Objects.toString(g.canonicalKey(),"")));
  String actualProvider=likely.isEmpty()?(provider==null||provider.isBlank()?StaticCareerOSTemplates.ID:provider.trim()):likely.getFirst().source();
  return new Response(applicationId,snapshot.getId(),actualProvider,List.copyOf(assessments),notObserved,suggested,List.copyOf(gaps));
 }
 private Map<String,Suggestion> trustedValues(){Map<String,Suggestion> out=new HashMap<>();ApplicantProfile profile=profiles.findByProfileKey(ApplicantProfile.PRIMARY_PROFILE_KEY).orElse(null);if(profile!=null&&profile.isVerified())addProfile(out,profile);for(ApprovedAnswerResponse answer:answers.findAll()){if(!answer.effectiveReusable()||out.containsKey(answer.canonicalKey()))continue;String value=display(answer.resolvedTextValue(),answer.resolvedBooleanValue(),answer.resolvedNumberValue(),answer.resolvedCurrency());if(value!=null)out.put(answer.canonicalKey(),new Suggestion(answer.canonicalKey(),SuggestionSource.APPROVED_ANSWER,answer.id(),value,new BigDecimal("0.97")));}return out;}
 private void addProfile(Map<String,Suggestion> out,ApplicantProfile p){put(out,"first_name",p.getFirstName(),p);put(out,"last_name",p.getLastName(),p);put(out,"preferred_name",p.getPreferredName(),p);put(out,"email",p.getEmail(),p);put(out,"phone",p.getPhone(),p);put(out,"city",p.getCity(),p);put(out,"state_region",p.getStateRegion(),p);put(out,"country",p.getCountry(),p);put(out,"postal_code",p.getPostalCode(),p);put(out,"portfolio_url",p.getPortfolioUrl(),p);put(out,"github_url",p.getGithubUrl(),p);put(out,"linkedin_url",p.getLinkedinUrl(),p);put(out,"willing_to_relocate",p.getWillingToRelocate()==null?null:p.getWillingToRelocate()?"Yes":"No",p);put(out,"willing_to_travel",p.getWillingToTravel()==null?null:p.getWillingToTravel()?"Yes":"No",p);put(out,"salary_expectation",p.getMinimumSalary()==null?null:p.getMinimumSalary().toPlainString()+(p.getSalaryCurrency()==null?"":" "+p.getSalaryCurrency()),p);}
 private void put(Map<String,Suggestion> out,String key,String value,ApplicantProfile p){if(value!=null&&!value.isBlank())out.put(key,new Suggestion(key,SuggestionSource.APPLICANT_PROFILE,p.getId(),value,new BigDecimal("0.99")));}
 private String display(String text,Boolean bool,BigDecimal number,String currency){if(text!=null)return text;if(bool!=null)return bool?"Yes":"No";if(number!=null)return number.toPlainString()+(currency==null?"":" "+currency);return null;}
 private String normalizeKey(String value){return value==null?"":value.trim().toLowerCase(Locale.ROOT).replace('-','_').replace(' ','_');}
 private String normalizeText(String value){if(value==null)return "";return Normalizer.normalize(value,Normalizer.Form.NFKC).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+"," ").trim().replaceAll("\\s+"," ");}
 private String mappingPath(Long applicationId){return "/questions?applicationId="+applicationId+"#question-mapping-review";}
}
