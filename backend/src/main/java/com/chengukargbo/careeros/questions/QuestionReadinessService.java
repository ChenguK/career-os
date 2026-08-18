package com.chengukargbo.careeros.questions;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import com.chengukargbo.careeros.questions.QuestionEnums.Status;
@Service @Transactional(readOnly=true)
public class QuestionReadinessService {private final ApplicationQuestionRepository repository;public QuestionReadinessService(ApplicationQuestionRepository repository){this.repository=repository;}
 public Readiness assess(Long applicationId){var values=repository.findByApplicationIdOrderByIdAsc(applicationId);long requiredUnresolved=values.stream().filter(q->q.isRequired()&&q.getStatus()!=Status.APPROVED).count();long review=values.stream().filter(q->q.isRequired()&&q.getStatus()==Status.NEEDS_REVIEW).count();long blockers=values.stream().filter(q->q.getStatus()==Status.BLOCKED).count();return new Readiness(requiredUnresolved,review,blockers,requiredUnresolved==0&&blockers==0);}
 public record Readiness(long requiredUnresolved,long needsReview,long blockers,boolean ready){}
}
