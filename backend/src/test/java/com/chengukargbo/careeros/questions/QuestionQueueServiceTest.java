package com.chengukargbo.careeros.questions;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.time.OffsetDateTime; import java.util.*;
import org.junit.jupiter.api.BeforeEach; import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith; import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension; import org.springframework.test.util.ReflectionTestUtils;
import com.chengukargbo.careeros.applications.*; import com.chengukargbo.careeros.answers.*;
import com.chengukargbo.careeros.answers.dto.ApprovedAnswerResponse; import com.chengukargbo.careeros.jobs.*;
import com.chengukargbo.careeros.questions.QuestionDtos.*; import com.chengukargbo.careeros.questions.QuestionEnums.*;
import com.chengukargbo.careeros.automation.ApplicationAutomationService;

@ExtendWith(MockitoExtension.class)
class QuestionQueueServiceTest {
 @Mock QuestionTemplateRepository templates; @Mock ApplicationQuestionRepository questions;
 @Mock ApplicationRepository applications; @Mock ApprovedAnswerRepository answers;
 @Mock ApprovedAnswerService answerService; QuestionQueueService service;
 @Mock ApplicationAutomationService automationService;
 @BeforeEach void setup(){service=new QuestionQueueService(templates,questions,applications,answers,answerService,automationService);lenient().when(questions.saveAndFlush(any())).thenAnswer(i->i.getArgument(0));}

 @Test void matchesBroadAndSelectedSeniorityTemplatesDeterministically(){QuestionTemplate broad=template(1L,null,"a_key");QuestionTemplate senior=template(2L,Seniority.SENIOR,"b_key");when(templates.findByJobFamilyAndActiveTrueOrderByCanonicalQuestionKeyAsc(JobFamily.SOFTWARE_ENGINEER)).thenReturn(List.of(broad,senior));assertThat(service.templates(JobFamily.SOFTWARE_ENGINEER,Seniority.MID_LEVEL)).extracting(TemplateResponse::canonicalQuestionKey).containsExactly("a_key");assertThat(service.templates(JobFamily.SOFTWARE_ENGINEER,Seniority.SENIOR)).extracting(TemplateResponse::canonicalQuestionKey).containsExactly("a_key","b_key");}

 @Test void selectedTemplateUsesExactEffectiveAnswerAsReviewSuggestion(){Application app=application();QuestionTemplate template=template(1L,null,"willing_to_relocate");ApprovedAnswer entity=approvedEntity("willing_to_relocate");when(applications.findById(7L)).thenReturn(Optional.of(app));when(templates.findById(1L)).thenReturn(Optional.of(template));when(answerService.findAll()).thenReturn(List.of(answerResponse(4L,"willing_to_relocate",true,true)));when(answers.findById(4L)).thenReturn(Optional.of(entity));List<QuestionResponse> result=service.addTemplates(new AddTemplatesRequest(7L,List.of(1L)));assertThat(result.getFirst().status()).isEqualTo(Status.NEEDS_REVIEW);assertThat(result.getFirst().proposedAnswer()).isEqualTo("Yes");}

 @Test void noFuzzyOrIneffectiveAnswerIsSuggested(){Application app=application();QuestionTemplate template=template(1L,null,"willing_to_relocate");when(applications.findById(7L)).thenReturn(Optional.of(app));when(templates.findById(1L)).thenReturn(Optional.of(template));when(answerService.findAll()).thenReturn(List.of(answerResponse(4L,"relocation_willingness",true,true),answerResponse(5L,"willing_to_relocate",false,true)));assertThat(service.addTemplates(new AddTemplatesRequest(7L,List.of(1L))).getFirst().status()).isEqualTo(Status.UNANSWERED);verifyNoInteractions(answers);}

 @Test void duplicateTemplateSelectionIsSkipped(){Application app=application();QuestionTemplate template=template(1L,null,"x_key");when(applications.findById(7L)).thenReturn(Optional.of(app));when(templates.findById(1L)).thenReturn(Optional.of(template));when(questions.existsByApplicationIdAndTemplateId(7L,1L)).thenReturn(true);assertThat(service.addTemplates(new AddTemplatesRequest(7L,List.of(1L)))).isEmpty();verify(questions,never()).saveAndFlush(any());}

 @Test void explicitLinkRequiresExactCanonicalIdentityButAllowsContextualReview(){ApplicationQuestion question=new ApplicationQuestion(application(),null,"why_this_role","Why?",QuestionEnums.AnswerType.TEXT,false,Classification.CONTEXTUAL,Source.MANUAL);ReflectionTestUtils.setField(question,"id",9L);ApprovedAnswer entity=approvedEntity("why_this_role");when(questions.findById(9L)).thenReturn(Optional.of(question));when(answers.findById(4L)).thenReturn(Optional.of(entity));when(answerService.findAll()).thenReturn(List.of(answerResponse(4L,"why_this_role",false,true)));QuestionResponse response=service.link(9L,4L);assertThat(response.status()).isEqualTo(Status.NEEDS_REVIEW);assertThat(response.proposedAnswer()).isEqualTo("Yes");}

 @Test void questionActionsDoNotDependOnLifecycleHistory(){ApplicationQuestion question=new ApplicationQuestion(application(),null,null,"Question?",QuestionEnums.AnswerType.TEXT,false,Classification.UNKNOWN,Source.MANUAL);ReflectionTestUtils.setField(question,"id",9L);when(questions.findById(9L)).thenReturn(Optional.of(question));service.answer(9L,"User answer");service.approve(9L);service.block(9L,true);service.block(9L,false);assertThat(question.getStatus()).isEqualTo(Status.NEEDS_REVIEW);}

 private QuestionTemplate template(Long id,Seniority seniority,String key){QuestionTemplate t=new QuestionTemplate();ReflectionTestUtils.setField(t,"id",id);ReflectionTestUtils.setField(t,"jobFamily",JobFamily.SOFTWARE_ENGINEER);ReflectionTestUtils.setField(t,"seniority",seniority);ReflectionTestUtils.setField(t,"canonicalQuestionKey",key);ReflectionTestUtils.setField(t,"representativeQuestion","Question "+key);ReflectionTestUtils.setField(t,"answerType",QuestionEnums.AnswerType.BOOLEAN);ReflectionTestUtils.setField(t,"classification",Classification.VERIFIED_REUSABLE);ReflectionTestUtils.setField(t,"active",true);return t;}
 private Application application(){JobOpportunity job=new JobOpportunity(null,"Engineer",null,null,RemoteType.UNKNOWN,null,null,null,"USD",null,null,null,null,null,(short)3,null,null,null);ReflectionTestUtils.setField(job,"id",12L);Application app=new Application(job,ApplicationStatus.SAVED,null,false,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null);ReflectionTestUtils.setField(app,"id",7L);return app;}
 private ApprovedAnswer approvedEntity(String key){ApprovedAnswer a=new ApprovedAnswer(key,"Question?",com.chengukargbo.careeros.answers.AnswerType.BOOLEAN,null,true,null,AnswerClassification.CONTEXTUAL,false,AnswerSource.MANUAL,null,null);ReflectionTestUtils.setField(a,"id",4L);a.approve();return a;}
 private ApprovedAnswerResponse answerResponse(Long id,String key,boolean effective,boolean approved){OffsetDateTime now=OffsetDateTime.now();return new ApprovedAnswerResponse(id,key,"Question?",com.chengukargbo.careeros.answers.AnswerType.BOOLEAN,null,true,null,AnswerClassification.CONTEXTUAL,false,approved,approved?now:null,null,AnswerSource.MANUAL,null,true,effective,null,true,null,null,null,now,now);}
}
