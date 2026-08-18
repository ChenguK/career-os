package com.chengukargbo.careeros.questions;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.chengukargbo.careeros.questions.QuestionEnums.*;

@Service @Transactional(readOnly=true)
public class QuestionReviewSnapshotService {
 private final ApplicationQuestionRepository questions;
 public QuestionReviewSnapshotService(ApplicationQuestionRepository questions){this.questions=questions;}
 public List<Item> unresolved(Long applicationId){return questions.findByApplicationIdOrderByIdAsc(applicationId).stream().filter(q->q.getStatus()!=Status.APPROVED).map(q->new Item(q.getId(),q.getCanonicalQuestionKey(),q.getQuestionText(),q.getAnswerType(),q.isRequired(),q.getStatus())).toList();}
 public record Item(Long id,String canonicalKey,String questionText,AnswerType answerType,boolean required,Status status){}
}
