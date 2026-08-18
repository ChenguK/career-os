package com.chengukargbo.careeros.preparation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.time.OffsetDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.chengukargbo.careeros.answers.AnswerType;
import com.chengukargbo.careeros.applications.Application;
import com.chengukargbo.careeros.automation.ApplicationAutomationService;
import com.chengukargbo.careeros.preparation.FieldPreparationResult.Outcome;
import com.chengukargbo.careeros.preparation.PreparationReviewDtos.*;
import com.chengukargbo.careeros.preparation.PreparationEnums.*;
import com.chengukargbo.careeros.questions.*;
import com.chengukargbo.careeros.questions.QuestionEnums.*;

class PreparationReviewServiceTest {
 private final ApplicationPreparationSessionRepository sessions=mock(ApplicationPreparationSessionRepository.class); private final ApprovedFieldPlanRepository plans=mock(ApprovedFieldPlanRepository.class); private final ApprovedFieldPlanItemRepository items=mock(ApprovedFieldPlanItemRepository.class); private final FieldPreparationResultRepository results=mock(FieldPreparationResultRepository.class); private final PreparationReviewRepository reviews=mock(PreparationReviewRepository.class); private final PreparationSessionEventRepository events=mock(PreparationSessionEventRepository.class); private final QuestionReviewSnapshotService questions=mock(QuestionReviewSnapshotService.class); private final ApplicationAutomationService automation=mock(ApplicationAutomationService.class);
 private PreparationReviewService service; private ApplicationPreparationSession session; private ApprovedFieldPlan plan; private ApprovedFieldPlanItem item; private FieldPreparationResult result;
 @BeforeEach void setup(){service=new PreparationReviewService(sessions,plans,items,results,reviews,events,questions,automation);Application app=mock(Application.class);when(app.getId()).thenReturn(12L);session=new ApplicationPreparationSession(app,new ApplicationFormTarget(app,"https://jobs.ashbyhq.com/acme/1"),null);session.beginOpening();session.beginCollectingQuestions();session.waitForUser(null);session.resume();ReflectionTestUtils.setField(session,"id",7L);plan=new ApprovedFieldPlan(session);ReflectionTestUtils.setField(plan,"id",9L);plan.add("first_name",AnswerType.TEXT,"Ada",null,null,ApprovedFieldPlan.ValueSource.APPLICANT_PROFILE,3L,OffsetDateTime.now());item=plan.getItems().getFirst();ReflectionTestUtils.setField(item,"id",20L);result=new FieldPreparationResult(item,Outcome.PREPARED,null,OffsetDateTime.now());when(sessions.findByIdAndApplicationId(7L,12L)).thenReturn(Optional.of(session));when(plans.findBySessionId(7L)).thenReturn(Optional.of(plan));when(items.findByPlanIdOrderByDisplayOrderAscIdAsc(9L)).thenReturn(List.of(item));when(results.findByPlanItemPlanIdOrderByPlanItemDisplayOrderAscIdAsc(9L)).thenReturn(List.of(result));when(reviews.saveAndFlush(any())).thenAnswer(i->{PreparationReview r=i.getArgument(0);ReflectionTestUtils.setField(r,"id",30L);return r;});when(sessions.saveAndFlush(any())).thenAnswer(i->i.getArgument(0));}
 @Test void generatesCompleteImmutableReviewSnapshotAndMarksManualReviewReady(){when(questions.unresolved(12L)).thenReturn(List.of(new QuestionReviewSnapshotService.Item(40L,"why_role","Why this role?",QuestionEnums.AnswerType.TEXT,true,Status.UNANSWERED),new QuestionReviewSnapshotService.Item(41L,"onsite","Can you work onsite?",QuestionEnums.AnswerType.BOOLEAN,true,Status.BLOCKED)));OffsetDateTime captured=OffsetDateTime.now();Response response=service.create(12L,7L,new CreateRequest(List.of(new ScreenshotInput("reviews/7/page-1.png","application",captured))));assertEquals(30L,response.id());assertEquals(1,response.preparedFields().size());assertEquals("Ada",response.preparedFields().getFirst().textValue());assertEquals(1,response.unansweredQuestions().size());assertEquals(1,response.blockedQuestions().size());assertEquals("reviews/7/page-1.png",response.screenshots().getFirst().reference());assertEquals(64,response.snapshotHash().length());assertEquals(SessionState.READY_FOR_REVIEW,session.getState());verify(automation).reviewPackageReady(12L);verify(events).save(argThat(e->e.getEventType()==EventType.REVIEW_GENERATED));}
 @Test void rejectsIncompleteResultsAndUnsafeScreenshotReferences(){when(results.findByPlanItemPlanIdOrderByPlanItemDisplayOrderAscIdAsc(9L)).thenReturn(List.of());assertThrows(com.chengukargbo.careeros.common.exception.BusinessValidationException.class,()->service.create(12L,7L,new CreateRequest(List.of(new ScreenshotInput("reviews/7/a.png",null,OffsetDateTime.now())))));when(results.findByPlanItemPlanIdOrderByPlanItemDisplayOrderAscIdAsc(9L)).thenReturn(List.of(result));assertThrows(com.chengukargbo.careeros.common.exception.BusinessValidationException.class,()->service.create(12L,7L,new CreateRequest(List.of(new ScreenshotInput("file:///tmp/a.png",null,OffsetDateTime.now())))));verifyNoInteractions(automation);}
}
