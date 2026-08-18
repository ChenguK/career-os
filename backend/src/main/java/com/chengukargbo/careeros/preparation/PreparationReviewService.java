package com.chengukargbo.careeros.preparation;

import static com.chengukargbo.careeros.preparation.PreparationReviewDtos.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.chengukargbo.careeros.automation.ApplicationAutomationService;
import com.chengukargbo.careeros.common.exception.BusinessValidationException;
import com.chengukargbo.careeros.preparation.PreparationEnums.*;
import com.chengukargbo.careeros.questions.QuestionEnums.Status;
import com.chengukargbo.careeros.questions.QuestionReviewSnapshotService;

@Service @Transactional
public class PreparationReviewService {
 private final ApplicationPreparationSessionRepository sessions; private final ApprovedFieldPlanRepository plans;
 private final ApprovedFieldPlanItemRepository items; private final FieldPreparationResultRepository results;
 private final PreparationReviewRepository reviews; private final PreparationSessionEventRepository events;
 private final QuestionReviewSnapshotService questions; private final ApplicationAutomationService automation;
 public PreparationReviewService(ApplicationPreparationSessionRepository s,ApprovedFieldPlanRepository p,ApprovedFieldPlanItemRepository i,FieldPreparationResultRepository r,PreparationReviewRepository reviews,PreparationSessionEventRepository e,QuestionReviewSnapshotService q,ApplicationAutomationService a){sessions=s;plans=p;items=i;results=r;this.reviews=reviews;events=e;questions=q;automation=a;}

 public Response create(Long applicationId,Long sessionId,CreateRequest request){
  ApplicationPreparationSession session=session(applicationId,sessionId);
  if(session.getState()!=SessionState.PREPARING_FIELDS)throw new BusinessValidationException("Field preparation must complete before review generation");
  if(reviews.findBySessionId(sessionId).isPresent())throw new BusinessValidationException("A review already exists for this session");
  ApprovedFieldPlan plan=plans.findBySessionId(sessionId).orElseThrow(()->new BusinessValidationException("Approved field plan not found"));
  List<ApprovedFieldPlanItem> planItems=items.findByPlanIdOrderByDisplayOrderAscIdAsc(plan.getId());
  List<FieldPreparationResult> fieldResults=results.findByPlanItemPlanIdOrderByPlanItemDisplayOrderAscIdAsc(plan.getId());
  if(fieldResults.size()!=planItems.size())throw new BusinessValidationException("Field preparation results are incomplete");
  if(fieldResults.stream().anyMatch(r->r.getOutcome()==FieldPreparationResult.Outcome.FAILED))throw new BusinessValidationException("Failed fields must be resolved before review generation");
  List<QuestionReviewSnapshotService.Item> unresolved=questions.unresolved(applicationId);
  validateScreenshots(request.screenshots());
  String hash=hash(planItems,fieldResults,unresolved,request.screenshots());
  PreparationReview review=new PreparationReview(session,hash);
  Map<Long,FieldPreparationResult> byItem=new HashMap<>();for(var r:fieldResults)byItem.put(r.getPlanItem().getId(),r);
  for(var item:planItems)review.addField(item,byItem.get(item.getId()));
  for(var q:unresolved)review.addQuestion(q.id(),q.canonicalKey(),q.questionText(),q.answerType(),q.required(),q.status(),q.status()==Status.BLOCKED?PreparationReviewQuestion.Category.BLOCKED:PreparationReviewQuestion.Category.UNANSWERED);
  for(var screenshot:request.screenshots())review.addScreenshot(screenshot.reference().trim(),trim(screenshot.pageKey()),screenshot.capturedAt());
  PreparationReview saved=reviews.saveAndFlush(review); session.readyForReview(); sessions.saveAndFlush(session);
  automation.reviewPackageReady(applicationId);
  events.save(new PreparationSessionEvent(session,EventType.REVIEW_GENERATED,false,"Preparation review package generated for manual review",session.getCurrentPage(),session.getCurrentQuestion()));
  return response(saved);
 }
 @Transactional(readOnly=true) public Response get(Long applicationId,Long sessionId){session(applicationId,sessionId);return response(reviews.findBySessionId(sessionId).orElseThrow(()->new BusinessValidationException("Preparation review not found")));}
 private void validateScreenshots(List<ScreenshotInput> values){Set<String> refs=new HashSet<>();for(var value:values){String ref=value.reference().trim();if(!refs.add(ref))throw new BusinessValidationException("Duplicate screenshot reference");String lower=ref.toLowerCase(Locale.ROOT);if(lower.startsWith("file:")||ref.startsWith("/")||ref.contains(".."))throw new BusinessValidationException("Screenshot reference must be an application-safe reference");}}
 private String hash(List<ApprovedFieldPlanItem> fields,List<FieldPreparationResult> results,List<QuestionReviewSnapshotService.Item> questions,List<ScreenshotInput> shots){StringBuilder b=new StringBuilder();fields.forEach(f->b.append("F|").append(f.getId()).append('|').append(f.getCanonicalKey()).append('|').append(f.getAnswerType()).append('|').append(f.getTextValue()).append('|').append(f.getBooleanValue()).append('|').append(f.getNumberValue()).append('|').append(f.getValueSource()).append('|'));results.forEach(r->b.append("R|").append(r.getPlanItem().getId()).append('|').append(r.getOutcome()).append('|').append(r.getSafeMessage()).append('|').append(r.getPreparedAt()).append('|'));questions.forEach(q->b.append("Q|").append(q.id()).append('|').append(q.canonicalKey()).append('|').append(q.questionText()).append('|').append(q.answerType()).append('|').append(q.required()).append('|').append(q.status()).append('|'));shots.forEach(s->b.append("S|").append(s.reference().trim()).append('|').append(trim(s.pageKey())).append('|').append(s.capturedAt()).append('|'));try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(b.toString().getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException("SHA-256 unavailable",e);}}
 private ApplicationPreparationSession session(Long app,Long id){return sessions.findByIdAndApplicationId(id,app).orElseThrow(()->new BusinessValidationException("Preparation session not found for application"));}
 private Response response(PreparationReview r){List<Question> unanswered=r.getQuestions().stream().filter(q->q.getCategory()==PreparationReviewQuestion.Category.UNANSWERED).map(this::question).toList();List<Question> blocked=r.getQuestions().stream().filter(q->q.getCategory()==PreparationReviewQuestion.Category.BLOCKED).map(this::question).toList();return new Response(r.getId(),r.getSession().getId(),r.getSnapshotHash(),r.getGeneratedAt(),r.getFields().stream().map(f->new Field(f.getCanonicalKey(),f.getAnswerType(),f.getTextValue(),f.getBooleanValue(),f.getNumberValue(),f.getValueSource(),f.getOutcome(),f.getSafeMessage(),f.getPreparedAt())).toList(),unanswered,blocked,r.getScreenshots().stream().map(s->new Screenshot(s.getReference(),s.getPageKey(),s.getCapturedAt())).toList());}
 private Question question(PreparationReviewQuestion q){return new Question(q.getApplicationQuestionId(),q.getCanonicalKey(),q.getQuestionText(),q.getAnswerType(),q.isRequired(),q.getQuestionStatus(),q.getCategory());}
 private String trim(String s){return s==null||s.isBlank()?null:s.trim();}
}
