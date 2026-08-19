package com.chengukargbo.careeros.applications.lock;
import org.springframework.stereotype.Service;import com.chengukargbo.careeros.common.exception.BusinessValidationException;
@Service public class ApplicationLockGuard {private final ApplicationLockRepository locks;public ApplicationLockGuard(ApplicationLockRepository locks){this.locks=locks;}
 public ApplicationLockState state(Long applicationId){return locks.findByApplicationId(applicationId).map(ApplicationLock::getState).orElse(ApplicationLockState.NOT_SUBMITTED);}
 public void requireLiveInteraction(Long applicationId){ApplicationLockState state=state(applicationId);if(!state.allowsLiveInteraction())throw new BusinessValidationException("Application lock "+state+" prevents live preparation interaction");}
 public void requireMaterialChange(Long applicationId){ApplicationLockState state=state(applicationId);if(!state.allowsLiveInteraction())throw new BusinessValidationException("Application lock "+state+" prevents changing the selected resume");}
}
