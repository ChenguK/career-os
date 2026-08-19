package com.chengukargbo.careeros.applications.lock;

import java.util.List;
import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;
import com.chengukargbo.careeros.applications.*;import com.chengukargbo.careeros.common.exception.BusinessValidationException;import com.chengukargbo.careeros.preparation.ApplicationPreparationService;
import com.chengukargbo.careeros.applications.lock.ApplicationLockDtos.*;

@Service @Transactional
public class ApplicationLockService {
 private final ApplicationLockRepository locks;private final ApplicationLockHistoryRepository history;private final ApplicationRepository applications;private final ApplicationPreparationService preparation;
 public ApplicationLockService(ApplicationLockRepository locks,ApplicationLockHistoryRepository history,ApplicationRepository applications,ApplicationPreparationService preparation){this.locks=locks;this.history=history;this.applications=applications;this.preparation=preparation;}
 public void initialize(Application application){if(locks.findByApplicationId(application.getId()).isPresent())return;ApplicationLockState state=submissionEstablished(application.getStatus())?ApplicationLockState.SUBMITTED:ApplicationLockState.NOT_SUBMITTED;ApplicationLock lock=locks.save(new ApplicationLock(application,state,"Deterministic lifecycle bootstrap"));history.save(new ApplicationLockHistory(application.getId(),null,state,ApplicationLockSource.SYSTEM,"Initialized from application lifecycle"));}
 @Transactional(readOnly=true) public Response get(Long id){return Response.from(find(id));}
 @Transactional(readOnly=true) public List<HistoryResponse> history(Long id){application(id);return history.findByApplicationIdOrderByOccurredAtAscIdAsc(id).stream().map(HistoryResponse::from).toList();}
 public void requireManualSubmission(Long id){ApplicationLock lock=find(id);if(lock.getState()!=ApplicationLockState.NOT_SUBMITTED)throw new BusinessValidationException("Mark as Applied requires an application lock of NOT_SUBMITTED; current lock is "+lock.getState());}
 public Response recordManualSubmission(Long id){requireManualSubmission(id);return move(id,ApplicationLockState.SUBMITTED,"User recorded manual application submission",true);}
 public Response markSubmitted(Long id,String reason){Application app=application(id);if(!submissionEstablished(app.getStatus()))throw new BusinessValidationException("Update the application lifecycle to Applied or a later submitted status before marking it submitted");return move(id,ApplicationLockState.SUBMITTED,reason("User confirmed this application was submitted",reason),true);}
 public Response archive(Long id,String reason){return move(id,ApplicationLockState.ARCHIVED,reason("User archived this application",reason),true);}
 public Response restore(Long id,String reason){Application app=application(id);ApplicationLock lock=find(id);if(lock.getState()!=ApplicationLockState.ARCHIVED&&lock.getState()!=ApplicationLockState.TESTING)throw invalid(lock,"restore");ApplicationLockState target=submissionEstablished(app.getStatus())?ApplicationLockState.SUBMITTED:ApplicationLockState.NOT_SUBMITTED;return move(id,target,reason("User restored this application",reason),false);}
 public Response markTesting(Long id,String reason){return move(id,ApplicationLockState.TESTING,reason("User marked this application as a testing record",reason),false);}
 private Response move(Long id,ApplicationLockState next,String reason,boolean cancel){ApplicationLock lock=find(id);ApplicationLockState previous=lock.getState();if(previous==next)throw new BusinessValidationException("Application lock is already "+next);if(next==ApplicationLockState.TESTING&&previous!=ApplicationLockState.NOT_SUBMITTED)throw invalid(lock,"mark testing");if(next==ApplicationLockState.SUBMITTED&&previous!=ApplicationLockState.NOT_SUBMITTED&&previous!=ApplicationLockState.TESTING)throw invalid(lock,"mark submitted");if(next==ApplicationLockState.ARCHIVED&&previous==ApplicationLockState.ARCHIVED)throw invalid(lock,"archive");if(cancel)preparation.cancelActiveForLock(id,next.name());lock.transition(next,reason);locks.save(lock);history.save(new ApplicationLockHistory(id,previous,next,ApplicationLockSource.USER,reason));return Response.from(lock);}
 private ApplicationLock find(Long id){Application app=application(id);return locks.findByApplicationId(id).orElseGet(()->{initialize(app);return locks.findByApplicationId(id).orElseThrow();});}
 private Application application(Long id){return applications.findById(id).orElseThrow(()->new ApplicationNotFoundException(id));}
 private boolean submissionEstablished(ApplicationStatus status){return switch(status){case APPLIED,PHONE_SCREEN,INTERVIEW_ONE,INTERVIEW_TWO,OFFER,REJECTED->true;default->false;};}
 private String reason(String fallback,String supplied){return supplied==null||supplied.isBlank()?fallback:supplied.trim();}
 private BusinessValidationException invalid(ApplicationLock lock,String action){return new BusinessValidationException("Cannot "+action+" while application lock is "+lock.getState());}
}
