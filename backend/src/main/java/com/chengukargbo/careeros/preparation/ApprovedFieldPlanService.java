package com.chengukargbo.careeros.preparation;

import static com.chengukargbo.careeros.preparation.FieldPreparationDtos.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chengukargbo.careeros.answers.*;
import com.chengukargbo.careeros.answers.dto.ApprovedAnswerResponse;
import com.chengukargbo.careeros.common.exception.BusinessValidationException;
import com.chengukargbo.careeros.preparation.ApprovedFieldPlan.ValueSource;
import com.chengukargbo.careeros.preparation.FieldPreparationResult.Outcome;
import com.chengukargbo.careeros.preparation.PreparationEnums.*;
import com.chengukargbo.careeros.profile.*;

@Service
@Transactional
public class ApprovedFieldPlanService {
    private final ApplicationPreparationSessionRepository sessions;
    private final ApprovedFieldPlanRepository plans;
    private final ApprovedFieldPlanItemRepository items;
    private final FieldPreparationResultRepository results;
    private final PreparationSessionEventRepository events;
    private final ApplicantProfileRepository profiles;
    private final ApprovedAnswerService answerService;

    public ApprovedFieldPlanService(ApplicationPreparationSessionRepository sessions,
        ApprovedFieldPlanRepository plans, ApprovedFieldPlanItemRepository items,
        FieldPreparationResultRepository results, PreparationSessionEventRepository events,
        ApplicantProfileRepository profiles, ApprovedAnswerService answerService) {
        this.sessions=sessions; this.plans=plans; this.items=items; this.results=results;
        this.events=events; this.profiles=profiles; this.answerService=answerService;
    }

    public PlanResponse create(Long applicationId, Long sessionId) {
        ApplicationPreparationSession session=session(applicationId, sessionId);
        ApprovedFieldPlan existing = plans.findBySessionId(sessionId).orElse(null);
        if (existing != null) {
            if (session.getState() != SessionState.PREPARING_FIELDS)
                throw new BusinessValidationException("A field plan already exists for this session");
            return response(existing);
        }
        if (session.getState() != SessionState.PREPARING_FIELDS) {
            throw new BusinessValidationException("Preparation session must be resumed before creating a field plan");
        }
        ApprovedFieldPlan plan=new ApprovedFieldPlan(session);
        Set<String> keys=new HashSet<>();
        ApplicantProfile profile=profiles.findByProfileKey(ApplicantProfile.PRIMARY_PROFILE_KEY).orElse(null);
        if (profile!=null && profile.isVerified()) addProfile(plan, profile, keys);
        for (ApprovedAnswerResponse answer : answerService.findAll()) {
            if (!answer.effectiveReusable() || !keys.add(answer.canonicalKey())) continue;
            plan.add(answer.canonicalKey(), answer.answerType(), answer.resolvedTextValue(),
                answer.resolvedBooleanValue(), answer.resolvedNumberValue(), ValueSource.APPROVED_ANSWER,
                answer.id(), answer.approvedAt());
        }
        ApprovedFieldPlan saved=plans.saveAndFlush(plan);
        sessions.saveAndFlush(session);
        events.save(new PreparationSessionEvent(session, EventType.FIELD_PLAN_CREATED, false,
            "Approved field plan created", null, null));
        return response(saved);
    }

    @Transactional(readOnly=true)
    public PlanResponse get(Long applicationId, Long sessionId) {
        session(applicationId, sessionId);
        return response(plans.findBySessionId(sessionId).orElseThrow(() ->
            new BusinessValidationException("Field plan not found for preparation session")));
    }

    public ResultsResponse record(Long applicationId, Long sessionId, ResultsRequest request) {
        ApplicationPreparationSession session=session(applicationId, sessionId);
        if (session.getState()!=SessionState.PREPARING_FIELDS)
            throw new BusinessValidationException("Preparation session must be preparing fields");
        ApprovedFieldPlan plan=plans.findBySessionId(sessionId).orElseThrow(() ->
            new BusinessValidationException("Field plan not found for preparation session"));
        List<ApprovedFieldPlanItem> expected=items.findByPlanIdOrderByDisplayOrderAscIdAsc(plan.getId());
        Map<Long,ResultInput> supplied=new HashMap<>();
        for (ResultInput input:request.results()) {
            if (supplied.put(input.planItemId(), input)!=null)
                throw new BusinessValidationException("Duplicate field preparation result");
        }
        if (expected.size()!=supplied.size() || expected.stream().anyMatch(i->!supplied.containsKey(i.getId())))
            throw new BusinessValidationException("Report exactly one result for every planned field");
        if (expected.stream().anyMatch(i->results.existsByPlanItemId(i.getId())))
            throw new BusinessValidationException("Field preparation results were already recorded");
        long prepared=0, skipped=0, failed=0;
        for (ApprovedFieldPlanItem item:expected) {
            ResultInput input=supplied.get(item.getId());
            if (input.outcome()==Outcome.PREPARED && input.preparedAt()==null)
                throw new BusinessValidationException("Prepared fields require a timestamp");
            results.save(new FieldPreparationResult(item,input.outcome(),normalize(input.safeMessage()),input.preparedAt()));
            switch(input.outcome()){case PREPARED->prepared++;case SKIPPED->skipped++;case FAILED->failed++;}
        }
        EventType eventType;
        if (failed>0) { session.fail(); eventType=EventType.FIELD_PREPARATION_FAILED; }
        else { eventType=EventType.FIELD_PREPARATION_COMPLETED; }
        sessions.saveAndFlush(session);
        events.save(new PreparationSessionEvent(session,eventType,false,
            failed>0?"Field preparation failed":"Field preparation completed",null,null));
        return new ResultsResponse(plan.getId(),prepared,skipped,failed,OffsetDateTime.now());
    }

    private void addProfile(ApprovedFieldPlan p, ApplicantProfile x, Set<String> keys) {
        addText(p,keys,"first_name",x.getFirstName(),x); addText(p,keys,"last_name",x.getLastName(),x);
        addText(p,keys,"preferred_name",x.getPreferredName(),x); addText(p,keys,"email",x.getEmail(),x);
        addText(p,keys,"phone",x.getPhone(),x); addText(p,keys,"city",x.getCity(),x);
        addText(p,keys,"state_region",x.getStateRegion(),x); addText(p,keys,"country",x.getCountry(),x);
        addText(p,keys,"postal_code",x.getPostalCode(),x); addText(p,keys,"portfolio_url",x.getPortfolioUrl(),x);
        addText(p,keys,"github_url",x.getGithubUrl(),x); addText(p,keys,"linkedin_url",x.getLinkedinUrl(),x);
        addText(p,keys,"preferred_work_arrangement",x.getPreferredWorkArrangement()==null?null:x.getPreferredWorkArrangement().name(),x);
        addNumber(p,keys,"minimum_salary",x.getMinimumSalary(),x);
        addText(p,keys,"salary_currency",x.getSalaryCurrency(),x);
        addBoolean(p,keys,"willing_to_relocate",x.getWillingToRelocate(),x);
        addBoolean(p,keys,"willing_to_travel",x.getWillingToTravel(),x);
    }
    private void addText(ApprovedFieldPlan p,Set<String> k,String key,String v,ApplicantProfile x){if(v!=null&&!v.isBlank()&&k.add(key))p.add(key,AnswerType.TEXT,v,null,null,ValueSource.APPLICANT_PROFILE,x.getId(),x.getLastVerifiedAt());}
    private void addBoolean(ApprovedFieldPlan p,Set<String> k,String key,Boolean v,ApplicantProfile x){if(v!=null&&k.add(key))p.add(key,AnswerType.BOOLEAN,null,v,null,ValueSource.APPLICANT_PROFILE,x.getId(),x.getLastVerifiedAt());}
    private void addNumber(ApprovedFieldPlan p,Set<String> k,String key,BigDecimal v,ApplicantProfile x){if(v!=null&&k.add(key))p.add(key,AnswerType.NUMBER,null,null,v,ValueSource.APPLICANT_PROFILE,x.getId(),x.getLastVerifiedAt());}
    private ApplicationPreparationSession session(Long app,Long id){return sessions.findByIdAndApplicationId(id,app).orElseThrow(()->new BusinessValidationException("Preparation session not found for application"));}
    private PlanResponse response(ApprovedFieldPlan p){return new PlanResponse(p.getId(),p.getSession().getId(),p.getGeneratedAt(),p.getItems().stream().map(i->new PlanItemResponse(i.getId(),i.getCanonicalKey(),i.getAnswerType(),i.getTextValue(),i.getBooleanValue(),i.getNumberValue(),i.getValueSource(),i.getSourceRecordId(),i.getSourceVerifiedAt())).toList());}
    private String normalize(String s){return s==null||s.isBlank()?null:s.trim();}
}
