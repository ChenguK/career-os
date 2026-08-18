package com.chengukargbo.careeros.preparation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.chengukargbo.careeros.answers.*;
import com.chengukargbo.careeros.answers.dto.ApprovedAnswerResponse;
import com.chengukargbo.careeros.applications.Application;
import com.chengukargbo.careeros.jobs.RemoteType;
import com.chengukargbo.careeros.preparation.FieldPreparationDtos.*;
import com.chengukargbo.careeros.preparation.FieldPreparationResult.Outcome;
import com.chengukargbo.careeros.preparation.PreparationEnums.*;
import com.chengukargbo.careeros.profile.*;

class ApprovedFieldPlanServiceTest {
    private final ApplicationPreparationSessionRepository sessions=mock(ApplicationPreparationSessionRepository.class);
    private final ApprovedFieldPlanRepository plans=mock(ApprovedFieldPlanRepository.class);
    private final ApprovedFieldPlanItemRepository items=mock(ApprovedFieldPlanItemRepository.class);
    private final FieldPreparationResultRepository results=mock(FieldPreparationResultRepository.class);
    private final PreparationSessionEventRepository events=mock(PreparationSessionEventRepository.class);
    private final ApplicantProfileRepository profiles=mock(ApplicantProfileRepository.class);
    private final ApprovedAnswerService answers=mock(ApprovedAnswerService.class);
    private ApprovedFieldPlanService service;
    private ApplicationPreparationSession session;

    @BeforeEach void setup() {
        service=new ApprovedFieldPlanService(sessions,plans,items,results,events,profiles,answers);
        Application app=mock(Application.class); when(app.getId()).thenReturn(12L);
        session=new ApplicationPreparationSession(app,new ApplicationFormTarget(app,"https://jobs.ashbyhq.com/acme/1"),null);
        session.beginOpening(); session.beginCollectingQuestions(); session.waitForUser(null); session.resume();
        setId(session,7L);
        when(sessions.findByIdAndApplicationId(7L,12L)).thenReturn(Optional.of(session));
        when(plans.saveAndFlush(any())).thenAnswer(invocation->{ApprovedFieldPlan p=invocation.getArgument(0);setId(p,9L);long id=20;for(var i:p.getItems())setId(i,id++);return p;});
        when(sessions.saveAndFlush(any())).thenAnswer(i->i.getArgument(0));
        when(events.save(any())).thenAnswer(i->i.getArgument(0));
    }

    @Test void createsPlanFromVerifiedProfileAndOnlyEffectiveApprovedAnswers() {
        ApplicantProfile profile=profile(); profile.verify(); setId(profile,3L);
        when(profiles.findByProfileKey(ApplicantProfile.PRIMARY_PROFILE_KEY)).thenReturn(Optional.of(profile));
        when(answers.findAll()).thenReturn(List.of(
            answer(5L,"years_experience",true,"8"),
            answer(6L,"unapproved",false,"unsafe"),
            answer(7L,"github_url",true,"contradictory")
        ));

        PlanResponse plan=service.create(12L,7L);

        assertEquals(SessionState.PREPARING_FIELDS,session.getState());
        assertTrue(plan.fields().stream().anyMatch(f->f.canonicalKey().equals("first_name")&&f.textValue().equals("Ada")&&f.source()==ApprovedFieldPlan.ValueSource.APPLICANT_PROFILE));
        assertTrue(plan.fields().stream().anyMatch(f->f.canonicalKey().equals("willing_to_relocate")&&Boolean.TRUE.equals(f.booleanValue())));
        assertTrue(plan.fields().stream().anyMatch(f->f.canonicalKey().equals("years_experience")&&f.source()==ApprovedFieldPlan.ValueSource.APPROVED_ANSWER));
        assertEquals(1,plan.fields().stream().filter(f->f.canonicalKey().equals("github_url")).count(),"profile authority wins duplicate keys");
        assertFalse(plan.fields().stream().anyMatch(f->f.canonicalKey().equals("unapproved")));
        verify(events).save(argThat(e->e.getEventType()==EventType.FIELD_PLAN_CREATED));
    }

    @Test void unverifiedProfileProducesNoProfileBackedFields() {
        when(profiles.findByProfileKey(ApplicantProfile.PRIMARY_PROFILE_KEY)).thenReturn(Optional.of(profile()));
        when(answers.findAll()).thenReturn(List.of());
        assertTrue(service.create(12L,7L).fields().isEmpty());
    }

    @Test void persistsPreparedSkippedAndFailureAuditAndFailsSession() {
        ApprovedFieldPlan plan=new ApprovedFieldPlan(session); setId(plan,9L);
        plan.add("first_name",AnswerType.TEXT,"Ada",null,null,ApprovedFieldPlan.ValueSource.APPLICANT_PROFILE,3L,OffsetDateTime.now());
        plan.add("email",AnswerType.TEXT,"ada@example.com",null,null,ApprovedFieldPlan.ValueSource.APPLICANT_PROFILE,3L,OffsetDateTime.now());
        plan.add("years_experience",AnswerType.TEXT,"8",null,null,ApprovedFieldPlan.ValueSource.APPROVED_ANSWER,5L,OffsetDateTime.now());
        long id=20;for(var i:plan.getItems())setId(i,id++);
        when(plans.findBySessionId(7L)).thenReturn(Optional.of(plan));
        when(items.findByPlanIdOrderByDisplayOrderAscIdAsc(9L)).thenReturn(plan.getItems());
        OffsetDateTime preparedAt=OffsetDateTime.now();

        ResultsResponse response=service.record(12L,7L,new ResultsRequest(List.of(
            new ResultInput(20L,Outcome.PREPARED,null,preparedAt),
            new ResultInput(21L,Outcome.SKIPPED,"Field was not present",null),
            new ResultInput(22L,Outcome.FAILED,"Field rejected the value",null)
        )));

        assertEquals(1,response.preparedCount()); assertEquals(1,response.skippedCount()); assertEquals(1,response.failedCount());
        assertEquals(SessionState.FAILED,session.getState()); verify(results,times(3)).save(any());
        verify(results).save(argThat(r->r.getOutcome()==Outcome.PREPARED&&preparedAt.equals(r.getPreparedAt())));
        verify(events).save(argThat(e->e.getEventType()==EventType.FIELD_PREPARATION_FAILED));
    }

    private ApplicantProfile profile(){return new ApplicantProfile("Ada","Lovelace",null,"ada@example.com",null,"London",null,"UK",null,null,"https://github.com/ada",null,"resume-v1",RemoteType.REMOTE,new BigDecimal("100000"),"USD",true,null);}
    private ApprovedAnswerResponse answer(Long id,String key,boolean effective,String value){return new ApprovedAnswerResponse(id,key,key,AnswerType.TEXT,value,null,null,AnswerClassification.VERIFIED_REUSABLE,true,effective,OffsetDateTime.now(),null,AnswerSource.MANUAL,null,true,effective,value,null,null,null,null,OffsetDateTime.now(),OffsetDateTime.now());}
    private static void setId(Object target,Long id){try{Field field=target.getClass().getDeclaredField("id");field.setAccessible(true);field.set(target,id);}catch(ReflectiveOperationException e){throw new AssertionError(e);}}
}
