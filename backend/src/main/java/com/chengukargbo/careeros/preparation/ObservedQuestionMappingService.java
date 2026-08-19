package com.chengukargbo.careeros.preparation;

import static com.chengukargbo.careeros.preparation.QuestionMappingDtos.*;
import static com.chengukargbo.careeros.preparation.QuestionMappingEnums.*;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.chengukargbo.careeros.common.exception.BusinessValidationException;
import com.chengukargbo.careeros.questions.*;
import com.chengukargbo.careeros.questions.QuestionEnums.*;
import com.chengukargbo.careeros.questions.research.*;

@Service @Transactional
public class ObservedQuestionMappingService {
    private final ApplicationFormTargetRepository targets; private final FormObservationSnapshotRepository snapshots;
    private final ObservedQuestionRepository observed; private final ObservedQuestionMappingRepository mappings;
    private final ObservedQuestionMappingHistoryRepository history; private final CanonicalQuestionKeyService catalog;
    private final QuestionResearchService research; private final QuestionQueueService questionQueue;
    public ObservedQuestionMappingService(ApplicationFormTargetRepository targets,FormObservationSnapshotRepository snapshots,ObservedQuestionRepository observed,ObservedQuestionMappingRepository mappings,ObservedQuestionMappingHistoryRepository history,CanonicalQuestionKeyService catalog,QuestionResearchService research,QuestionQueueService questionQueue){this.targets=targets;this.snapshots=snapshots;this.observed=observed;this.mappings=mappings;this.history=history;this.catalog=catalog;this.research=research;this.questionQueue=questionQueue;}

    @Transactional(readOnly=true)
    public ReviewResponse review(Long applicationId,JobFamily family,Seniority seniority){
        List<CanonicalKeyOption> keys=catalogOptions();ApplicationFormTarget target=targets.findByApplicationId(applicationId).orElse(null);FormObservationSnapshot snapshot=snapshots.findFirstByFormTargetApplicationIdOrderBySequenceNumberDesc(applicationId).orElse(null);if(target==null||snapshot==null)return new ReviewResponse(applicationId,null,keys,List.of());
        List<LikelyQuestion> likely=research.research(family,seniority,null);Map<String,LikelyQuestion> byKey=new HashMap<>(),byText=new HashMap<>();likely.forEach(q->{byKey.put(normalizeKey(q.canonicalKey()),q);byText.put(normalizeText(q.question()),q);});
        Map<String,ObservedQuestionMapping> current=new HashMap<>();mappings.findByFormTargetApplicationIdOrderByExternalQuestionIdAsc(applicationId).forEach(m->current.put(m.getExternalQuestionId(),m));
        List<ReviewItem> items=new ArrayList<>();
        for(ObservedQuestion q:observed.findBySnapshotIdOrderByDisplayOrderAscExternalQuestionIdAsc(snapshot.getId())){if(!q.isActive())continue;ObservedQuestionMapping mapping=current.get(q.getExternalQuestionId());List<MappingSuggestion> suggestions=suggestions(q,byKey,byText);items.add(new ReviewItem(mapping==null?null:mapping.getId(),q.getId(),q.getExternalQuestionId(),q.getQuestionText(),q.getAnswerType(),q.isRequired(),q.getOptions().stream().map(ObservationDtos.OptionResponse::from).toList(),FormIdentity.from(target),mapping==null?MappingState.UNCONFIRMED:mapping.getMappingState(),mapping==null?null:mapping.getCanonicalQuestionKey(),mapping==null?null:mapping.getMappingSource(),mapping==null?null:mapping.getConfidence(),mapping!=null&&mapping.isUserConfirmed(),mapping==null?null:mapping.getConfirmedAt(),mapping==null?null:mapping.getRevokedAt(),suggestions));}
        return new ReviewResponse(applicationId,snapshot.getId(),keys,List.copyOf(items));
    }

    public MappingResponse confirm(Long applicationId,ConfirmRequest request){
        ApplicationFormTarget target=target(applicationId);ObservedQuestion question=activeQuestion(applicationId,request.externalQuestionId());var key=catalog.require(request.canonicalQuestionKey());List<LikelyQuestion> likely=research.research(request.jobFamily(),request.seniority(),null);
        MappingSource source=likely.stream().anyMatch(q->q.canonicalKey().equals(key.key())&&normalizeText(q.question()).equals(normalizeText(question.getQuestionText())))?MappingSource.EXACT_TEXT:MappingSource.USER;
        BigDecimal confidence=source==MappingSource.EXACT_TEXT?new BigDecimal("0.950"):new BigDecimal("1.000");
        ObservedQuestionMapping mapping=mappings.findByFormTargetApplicationIdAndExternalQuestionId(applicationId,request.externalQuestionId()).orElseGet(()->new ObservedQuestionMapping(target,request.externalQuestionId()));
        if(mapping.getMappingState()==MappingState.CONFIRMED&&Objects.equals(mapping.getCanonicalQuestionKey(),key.key()))throw new BusinessValidationException("Observed question is already mapped to this canonical key");
        String previous=mapping.confirm(key.key(),source,confidence,true);mapping=mappings.saveAndFlush(mapping);history.save(new ObservedQuestionMappingHistory(mapping,previous==null?MappingEventType.CONFIRMED:MappingEventType.CHANGED,previous));
        questionQueue.reconcileObserved(applicationId,question,key.key(),key.classification(),true);return MappingResponse.from(mapping);
    }

    public MappingResponse mapFromAdapter(Long applicationId,String externalQuestionId,String canonicalKey,BigDecimal confidence){
        ApplicationFormTarget target=target(applicationId);ObservedQuestion question=activeQuestion(applicationId,externalQuestionId);var key=catalog.require(canonicalKey);ObservedQuestionMapping mapping=mappings.findByFormTargetApplicationIdAndExternalQuestionId(applicationId,externalQuestionId).orElseGet(()->new ObservedQuestionMapping(target,externalQuestionId));
        if(mapping.getMappingState()==MappingState.CONFIRMED&&mapping.isUserConfirmed())return MappingResponse.from(mapping);
        String previous=mapping.confirm(key.key(),MappingSource.ADAPTER,bounded(confidence),false);mapping=mappings.saveAndFlush(mapping);history.save(new ObservedQuestionMappingHistory(mapping,previous==null?MappingEventType.CONFIRMED:MappingEventType.CHANGED,previous));questionQueue.reconcileObserved(applicationId,question,key.key(),key.classification(),true);return MappingResponse.from(mapping);
    }

    public MappingResponse revoke(Long applicationId,Long mappingId){ObservedQuestionMapping mapping=mappings.findByIdAndFormTargetApplicationId(mappingId,applicationId).orElseThrow(()->new BusinessValidationException("Observed question mapping not found"));if(mapping.getMappingState()==MappingState.REVOKED)throw new BusinessValidationException("Observed question mapping is already revoked");String previous=mapping.revoke();mapping=mappings.saveAndFlush(mapping);history.save(new ObservedQuestionMappingHistory(mapping,MappingEventType.REVOKED,previous));questionQueue.mappingRevoked(applicationId,mapping.getExternalQuestionId());return MappingResponse.from(mapping);}
    @Transactional(readOnly=true) public List<HistoryResponse> history(Long applicationId,Long mappingId){ObservedQuestionMapping mapping=mappings.findByIdAndFormTargetApplicationId(mappingId,applicationId).orElseThrow(()->new BusinessValidationException("Observed question mapping not found"));return history.findByMappingIdOrderByOccurredAtAscIdAsc(mapping.getId()).stream().map(HistoryResponse::from).toList();}
    @Transactional(readOnly=true) Optional<ObservedQuestionMapping> trusted(Long applicationId,String externalId){return mappings.findByFormTargetApplicationIdAndExternalQuestionId(applicationId,externalId).filter(m->m.getMappingState()==MappingState.CONFIRMED).filter(m->m.getMappingSource()==MappingSource.ADAPTER||m.isUserConfirmed());}
    private List<MappingSuggestion> suggestions(ObservedQuestion q,Map<String,LikelyQuestion> byKey,Map<String,LikelyQuestion> byText){List<MappingSuggestion> out=new ArrayList<>();LikelyQuestion external=byKey.get(normalizeKey(q.getExternalQuestionId()));if(external!=null)out.add(new MappingSuggestion(external.canonicalKey(),external.question(),MappingSource.EXACT_EXTERNAL_KEY,new BigDecimal("1.000"),"The normalized ATS field ID exactly equals a researched canonical key; user confirmation is still required unless supplied authoritatively by an adapter."));LikelyQuestion text=byText.get(normalizeText(q.getQuestionText()));if(text!=null&&out.stream().noneMatch(x->x.canonicalKey().equals(text.canonicalKey())))out.add(new MappingSuggestion(text.canonicalKey(),text.question(),MappingSource.EXACT_TEXT,new BigDecimal("0.950"),"The normalized ATS wording exactly equals a researched representative question; review is required."));return List.copyOf(out);}
    private List<CanonicalKeyOption> catalogOptions(){return catalog.all().stream().map(k->new CanonicalKeyOption(k.key(),k.representativeQuestion(),k.answerType(),k.classification(),k.sources().stream().sorted().toList())).toList();}
    private ApplicationFormTarget target(Long id){return targets.findByApplicationId(id).orElseThrow(()->new BusinessValidationException("Application form target not found"));}
    private ObservedQuestion activeQuestion(Long applicationId,String externalId){FormObservationSnapshot snapshot=snapshots.findFirstByFormTargetApplicationIdOrderBySequenceNumberDesc(applicationId).orElseThrow(()->new BusinessValidationException("No observed application questions are available"));return observed.findBySnapshotIdOrderByDisplayOrderAscExternalQuestionIdAsc(snapshot.getId()).stream().filter(ObservedQuestion::isActive).filter(q->q.getExternalQuestionId().equals(externalId)).findFirst().orElseThrow(()->new BusinessValidationException("Active observed question not found"));}
    private BigDecimal bounded(BigDecimal value){if(value==null||value.compareTo(BigDecimal.ZERO)<0||value.compareTo(BigDecimal.ONE)>0)throw new BusinessValidationException("Mapping confidence must be between 0 and 1");return value.setScale(3,java.math.RoundingMode.HALF_UP);}
    private String normalizeKey(String v){return v==null?"":v.trim().toLowerCase(Locale.ROOT).replace('-','_').replace(' ','_');}
    private String normalizeText(String v){return v==null?"":Normalizer.normalize(v,Normalizer.Form.NFKC).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+"," ").trim().replaceAll("\\s+"," ");}
}
