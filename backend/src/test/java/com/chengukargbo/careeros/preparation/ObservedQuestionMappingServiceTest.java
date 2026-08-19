package com.chengukargbo.careeros.preparation;

import static com.chengukargbo.careeros.preparation.QuestionMappingEnums.*;
import static com.chengukargbo.careeros.questions.QuestionEnums.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import com.chengukargbo.careeros.applications.Application;
import com.chengukargbo.careeros.common.exception.BusinessValidationException;
import com.chengukargbo.careeros.questions.*;
import com.chengukargbo.careeros.questions.research.*;

@ExtendWith(MockitoExtension.class)
class ObservedQuestionMappingServiceTest {
    @Mock ApplicationFormTargetRepository targets; @Mock FormObservationSnapshotRepository snapshots;
    @Mock ObservedQuestionRepository observed; @Mock ObservedQuestionMappingRepository mappings;
    @Mock ObservedQuestionMappingHistoryRepository history; @Mock CanonicalQuestionKeyService catalog;
    @Mock QuestionResearchService research; @Mock QuestionQueueService questionQueue;
    private ObservedQuestionMappingService service; private ApplicationFormTarget target;
    private FormObservationSnapshot snapshot; private ObservedQuestion question;

    @BeforeEach void setUp(){service=new ObservedQuestionMappingService(targets,snapshots,observed,mappings,history,catalog,research,questionQueue);target=mock(ApplicationFormTarget.class);Application application=mock(Application.class);lenient().when(application.getId()).thenReturn(7L);lenient().when(target.getApplication()).thenReturn(application);lenient().when(targets.findByApplicationId(7L)).thenReturn(Optional.of(target));snapshot=mock(FormObservationSnapshot.class);lenient().when(snapshot.getId()).thenReturn(31L);lenient().when(snapshots.findFirstByFormTargetApplicationIdOrderBySequenceNumberDesc(7L)).thenReturn(Optional.of(snapshot));question=new ObservedQuestion(snapshot,"ats-email","What is your email?",AnswerType.TEXT,true,true,0,"fingerprint");ReflectionTestUtils.setField(question,"id",41L);lenient().when(observed.findBySnapshotIdOrderByDisplayOrderAscExternalQuestionIdAsc(31L)).thenReturn(List.of(question));lenient().when(catalog.require("email")).thenReturn(key("email",Classification.VERIFIED_REUSABLE));lenient().when(mappings.saveAndFlush(any())).thenAnswer(invocation->invocation.getArgument(0));lenient().when(research.research(JobFamily.SOFTWARE_ENGINEER,Seniority.MID_LEVEL,null)).thenReturn(List.of(likely("email","What is your email?")));}

    @Test void userConfirmsExactTextAndImmutableObservationRemainsUnchanged(){when(mappings.findByFormTargetApplicationIdAndExternalQuestionId(7L,"ats-email")).thenReturn(Optional.empty());var response=service.confirm(7L,new QuestionMappingDtos.ConfirmRequest("ats-email","email",JobFamily.SOFTWARE_ENGINEER,Seniority.MID_LEVEL));assertThat(response.mappingSource()).isEqualTo(MappingSource.EXACT_TEXT);assertThat(response.mappingState()).isEqualTo(MappingState.CONFIRMED);assertThat(response.userConfirmed()).isTrue();assertThat(response.confidence()).isEqualByComparingTo("0.950");assertThat(question.getQuestionText()).isEqualTo("What is your email?");assertThat(question.getExternalQuestionId()).isEqualTo("ats-email");verify(questionQueue).reconcileObserved(7L,question,"email",Classification.VERIFIED_REUSABLE,true);ArgumentCaptor<ObservedQuestionMappingHistory> audit=ArgumentCaptor.forClass(ObservedQuestionMappingHistory.class);verify(history).save(audit.capture());assertThat(audit.getValue().getEventType()).isEqualTo(MappingEventType.CONFIRMED);}

    @Test void adapterMappingIsAuthoritativeButCannotOverwriteUserConfirmedMapping(){when(mappings.findByFormTargetApplicationIdAndExternalQuestionId(7L,"ats-email")).thenReturn(Optional.empty());var response=service.mapFromAdapter(7L,"ats-email","email",new BigDecimal("0.990"));assertThat(response.mappingSource()).isEqualTo(MappingSource.ADAPTER);assertThat(response.userConfirmed()).isFalse();assertThat(response.mappingState()).isEqualTo(MappingState.CONFIRMED);ObservedQuestionMapping user=new ObservedQuestionMapping(target,"ats-email");user.confirm("email",MappingSource.USER,BigDecimal.ONE,true);when(mappings.findByFormTargetApplicationIdAndExternalQuestionId(7L,"ats-email")).thenReturn(Optional.of(user));assertThat(service.mapFromAdapter(7L,"ats-email","email",BigDecimal.ONE).mappingSource()).isEqualTo(MappingSource.USER);}

    @Test void mappingChangeCreatesHistoryAndInvalidatesPriorAnswerAuthority(){ObservedQuestionMapping mapping=new ObservedQuestionMapping(target,"ats-email");mapping.confirm("phone",MappingSource.USER,BigDecimal.ONE,true);when(mappings.findByFormTargetApplicationIdAndExternalQuestionId(7L,"ats-email")).thenReturn(Optional.of(mapping));service.confirm(7L,new QuestionMappingDtos.ConfirmRequest("ats-email","email",JobFamily.SOFTWARE_ENGINEER,Seniority.MID_LEVEL));ArgumentCaptor<ObservedQuestionMappingHistory> audit=ArgumentCaptor.forClass(ObservedQuestionMappingHistory.class);verify(history).save(audit.capture());assertThat(audit.getValue().getEventType()).isEqualTo(MappingEventType.CHANGED);assertThat(audit.getValue().getPreviousCanonicalKey()).isEqualTo("phone");verify(questionQueue).reconcileObserved(7L,question,"email",Classification.VERIFIED_REUSABLE,true);}

    @Test void revocationIsAuditedAndMarksLinkedQuestionForReview(){ObservedQuestionMapping mapping=new ObservedQuestionMapping(target,"ats-email");mapping.confirm("email",MappingSource.USER,BigDecimal.ONE,true);ReflectionTestUtils.setField(mapping,"id",9L);when(mappings.findByIdAndFormTargetApplicationId(9L,7L)).thenReturn(Optional.of(mapping));var response=service.revoke(7L,9L);assertThat(response.mappingState()).isEqualTo(MappingState.REVOKED);verify(questionQueue).mappingRevoked(7L,"ats-email");ArgumentCaptor<ObservedQuestionMappingHistory> audit=ArgumentCaptor.forClass(ObservedQuestionMappingHistory.class);verify(history).save(audit.capture());assertThat(audit.getValue().getEventType()).isEqualTo(MappingEventType.REVOKED);assertThatThrownBy(()->service.revoke(7L,9L)).isInstanceOf(BusinessValidationException.class).hasMessageContaining("already revoked");}

    @Test void reviewSurfacesUnconfirmedExactCandidateAndDuplicateConfirmationIsRejected(){when(mappings.findByFormTargetApplicationIdOrderByExternalQuestionIdAsc(7L)).thenReturn(List.of());when(catalog.all()).thenReturn(List.of(key("email",Classification.VERIFIED_REUSABLE)));var review=service.review(7L,JobFamily.SOFTWARE_ENGINEER,Seniority.MID_LEVEL);assertThat(review.questions()).singleElement().satisfies(item->{assertThat(item.mappingState()).isEqualTo(MappingState.UNCONFIRMED);assertThat(item.suggestions()).singleElement().satisfies(suggestion->{assertThat(suggestion.source()).isEqualTo(MappingSource.EXACT_TEXT);assertThat(suggestion.rationale()).contains("review is required");});});ObservedQuestionMapping existing=new ObservedQuestionMapping(target,"ats-email");existing.confirm("email",MappingSource.USER,BigDecimal.ONE,true);when(mappings.findByFormTargetApplicationIdAndExternalQuestionId(7L,"ats-email")).thenReturn(Optional.of(existing));assertThatThrownBy(()->service.confirm(7L,new QuestionMappingDtos.ConfirmRequest("ats-email","email",JobFamily.SOFTWARE_ENGINEER,Seniority.MID_LEVEL))).isInstanceOf(BusinessValidationException.class).hasMessageContaining("already mapped");}

    @Test void similarButNonidenticalWordingDoesNotCreateAFuzzySuggestion(){ObservedQuestion similar=new ObservedQuestion(snapshot,"random-id","Please enter an email address",AnswerType.TEXT,true,true,0,"other");when(observed.findBySnapshotIdOrderByDisplayOrderAscExternalQuestionIdAsc(31L)).thenReturn(List.of(similar));when(mappings.findByFormTargetApplicationIdOrderByExternalQuestionIdAsc(7L)).thenReturn(List.of());when(catalog.all()).thenReturn(List.of(key("email",Classification.VERIFIED_REUSABLE)));assertThat(service.review(7L,JobFamily.SOFTWARE_ENGINEER,Seniority.MID_LEVEL).questions().getFirst().suggestions()).isEmpty();}

    private CanonicalQuestionKeyService.CanonicalKey key(String key,Classification classification){return new CanonicalQuestionKeyService.CanonicalKey(key,"Email?",AnswerType.TEXT,classification,Set.of("APPLICANT_PROFILE"));}
    private LikelyQuestion likely(String key,String text){return new LikelyQuestion(key,text,AnswerType.TEXT,Classification.VERIFIED_REUSABLE,true,new BigDecimal("0.90"),StaticCareerOSTemplates.ID,JobFamily.SOFTWARE_ENGINEER,Seniority.MID_LEVEL,new BigDecimal("0.90"));}
}
