package com.chengukargbo.careeros.preparation;

import org.springframework.web.bind.annotation.*;

import com.chengukargbo.careeros.preparation.ObservationDtos.*;
import com.chengukargbo.careeros.preparation.PreparationDtos.*;

import jakarta.validation.Valid;
import com.chengukargbo.careeros.applications.lock.ApplicationLockGuard;
import com.chengukargbo.careeros.automation.ApplicationAutomationService;
import com.chengukargbo.careeros.automation.AutomationEnums.State;
import com.chengukargbo.careeros.common.exception.BusinessValidationException;

@RestController
@RequestMapping("/api/applications/{applicationId}/preparation/sessions/{sessionId}")
public class PreparationWorkerController {
    private final PreparationWorkerService service;
    private final ApprovedFieldPlanService fieldPlans;
    private final PreparationReviewService reviews;
    private final ApprovedMaterialPlanService materialPlans;
    private final ApplicationLockGuard lockGuard;
    private final ApplicationAutomationService automation;

    public PreparationWorkerController(PreparationWorkerService service,
        ApprovedFieldPlanService fieldPlans, PreparationReviewService reviews,
        ApprovedMaterialPlanService materialPlans, ApplicationLockGuard lockGuard,
        ApplicationAutomationService automation) {
        this.service = service;
        this.fieldPlans = fieldPlans;
        this.reviews = reviews;
        this.materialPlans = materialPlans;
        this.lockGuard = lockGuard;
        this.automation = automation;
    }

    @PostMapping("/opening")
    Response opening(@PathVariable Long applicationId,
        @PathVariable Long sessionId) {
        lockGuard.requireLiveInteraction(applicationId);
        requirePreparationPermission(applicationId);
        return service.opening(applicationId, sessionId);
    }

    @PostMapping("/collecting-questions")
    Response collectingQuestions(@PathVariable Long applicationId,
        @PathVariable Long sessionId) {
        lockGuard.requireLiveInteraction(applicationId);
        requirePreparationPermission(applicationId);
        return service.collectingQuestions(applicationId, sessionId);
    }

    @PostMapping("/observations")
    SnapshotResponse observations(@PathVariable Long applicationId,
        @PathVariable Long sessionId, @RequestBody SnapshotInput input) {
        lockGuard.requireLiveInteraction(applicationId);
        requirePreparationPermission(applicationId);
        return service.observations(applicationId, sessionId, input);
    }

    @PostMapping("/failed")
    Response failed(@PathVariable Long applicationId,
        @PathVariable Long sessionId,
        @Valid @RequestBody WorkerFailureRequest request) {
        lockGuard.requireLiveInteraction(applicationId);
        return service.failed(applicationId, sessionId, request);
    }

    private void requirePreparationPermission(Long applicationId) {
        State state = automation.get(applicationId).state();
        if (state == State.NOT_APPROVED || state == State.BLOCKED) {
            throw new BusinessValidationException(
                "Application is not approved for preparation"
            );
        }
    }

    @PostMapping("/pause")
    Response pause(@PathVariable Long applicationId, @PathVariable Long sessionId,
        @Valid @RequestBody PauseRequest request) {
        lockGuard.requireLiveInteraction(applicationId);
        return service.pause(applicationId, sessionId, request);
    }

    @PostMapping("/field-plan")
    FieldPreparationDtos.PlanResponse createFieldPlan(
        @PathVariable Long applicationId, @PathVariable Long sessionId) {
        lockGuard.requireLiveInteraction(applicationId);
        return fieldPlans.create(applicationId, sessionId);
    }

    @GetMapping("/field-plan")
    FieldPreparationDtos.PlanResponse fieldPlan(
        @PathVariable Long applicationId, @PathVariable Long sessionId) {
        return fieldPlans.get(applicationId, sessionId);
    }

    @PostMapping("/material-plan")
    ApprovedMaterialPlanService.Response createMaterialPlan(@PathVariable Long applicationId,
        @PathVariable Long sessionId){lockGuard.requireLiveInteraction(applicationId);return materialPlans.create(applicationId,sessionId);}

    @GetMapping("/material-plan")
    ApprovedMaterialPlanService.Response materialPlan(@PathVariable Long applicationId,
        @PathVariable Long sessionId){return materialPlans.get(applicationId,sessionId);}

    @PostMapping("/field-results")
    FieldPreparationDtos.ResultsResponse fieldResults(
        @PathVariable Long applicationId, @PathVariable Long sessionId,
        @Valid @RequestBody FieldPreparationDtos.ResultsRequest request) {
        lockGuard.requireLiveInteraction(applicationId);
        return fieldPlans.record(applicationId, sessionId, request);
    }

    @PostMapping("/review")
    PreparationReviewDtos.Response createReview(@PathVariable Long applicationId,
        @PathVariable Long sessionId,
        @Valid @RequestBody PreparationReviewDtos.CreateRequest request) {
        lockGuard.requireLiveInteraction(applicationId);
        return reviews.create(applicationId, sessionId, request);
    }

    @GetMapping("/review")
    PreparationReviewDtos.Response review(@PathVariable Long applicationId,
        @PathVariable Long sessionId) {
        return reviews.get(applicationId, sessionId);
    }
}
