package com.chengukargbo.careeros.questions;
import static org.assertj.core.api.Assertions.*; import org.junit.jupiter.api.Test; import com.chengukargbo.careeros.questions.QuestionEnums.*;
class ApplicationQuestionTest {
 private ApplicationQuestion question(Classification classification){return new ApplicationQuestion(null,null,"why_this_role","Why this role?",AnswerType.TEXT,true,classification,Source.MANUAL);}
 @Test void beginsUnansweredAndManual(){var q=question(Classification.CONTEXTUAL);assertThat(q.getStatus()).isEqualTo(Status.UNANSWERED);assertThat(q.getSource()).isEqualTo(Source.MANUAL);}
 @Test void answerRequiresApplicationSpecificApproval(){var q=question(Classification.CONTEXTUAL);q.answer("Because it fits");assertThat(q.getStatus()).isEqualTo(Status.ANSWERED);assertThat(q.getApprovedAnswer()).isNull();q.approve();assertThat(q.getStatus()).isEqualTo(Status.APPROVED);}
 @Test void suggestionAlwaysNeedsReview(){var q=question(Classification.VERIFIED_REUSABLE);q.suggest(null,"Yes");assertThat(q.getStatus()).isEqualTo(Status.NEEDS_REVIEW);assertThat(q.getApprovedAnswer()).isNull();q.rejectSuggestion();assertThat(q.getStatus()).isEqualTo(Status.UNANSWERED);}
 @Test void blockerIsQuestionOnlyAndCanBeRemoved(){var q=question(Classification.KNOCKOUT);q.block();assertThat(q.getStatus()).isEqualTo(Status.BLOCKED);q.unblock();assertThat(q.getStatus()).isEqualTo(Status.UNANSWERED);}
}
