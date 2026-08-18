package com.chengukargbo.careeros.preparation;

import org.springframework.web.bind.annotation.*;

import com.chengukargbo.careeros.preparation.ObservationDtos.*;
import com.chengukargbo.careeros.preparation.PreparationDtos.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/applications/{applicationId}/preparation/sessions/{sessionId}")
public class PreparationWorkerController {
    private final PreparationWorkerService service;
    private final ApprovedFieldPlanService fieldPlans;
    private final PreparationReviewService reviews;

    public PreparationWorkerController(PreparationWorkerService service,
        ApprovedFieldPlanService fieldPlans, PreparationReviewService reviews) {
        this.service = service;
        this.fieldPlans = fieldPlans;
        this.reviews = reviews;
    }

    @PostMapping("/opening")
    Response opening(@PathVariable Long applicationId,
        @PathVariable Long sessionId) {
        return service.opening(applicationId, sessionId);
    }

    @PostMapping("/collecting-questions")
    Response collectingQuestions(@PathVariable Long applicationId,
        @PathVariable Long sessionId) {
        return service.collectingQuestions(applicationId, sessionId);
    }

    @PostMapping("/observations")
    SnapshotResponse observations(@PathVariable Long applicationId,
        @PathVariable Long sessionId, @RequestBody SnapshotInput input) {
        return service.observations(applicationId, sessionId, input);
    }

    @PostMapping("/failed")
    Response failed(@PathVariable Long applicationId,
        @PathVariable Long sessionId,
        @Valid @RequestBody WorkerFailureRequest request) {
        return service.failed(applicationId, sessionId, request);
    }

    @PostMapping("/pause")
    Response pause(@PathVariable Long applicationId, @PathVariable Long sessionId,
        @Valid @RequestBody PauseRequest request) {
        return service.pause(applicationId, sessionId, request);
    }

    @PostMapping("/field-plan")
    FieldPreparationDtos.PlanResponse createFieldPlan(
        @PathVariable Long applicationId, @PathVariable Long sessionId) {
        return fieldPlans.create(applicationId, sessionId);
    }

    @GetMapping("/field-plan")
    FieldPreparationDtos.PlanResponse fieldPlan(
        @PathVariable Long applicationId, @PathVariable Long sessionId) {
        return fieldPlans.get(applicationId, sessionId);
    }

    @PostMapping("/field-results")
    FieldPreparationDtos.ResultsResponse fieldResults(
        @PathVariable Long applicationId, @PathVariable Long sessionId,
        @Valid @RequestBody FieldPreparationDtos.ResultsRequest request) {
        return fieldPlans.record(applicationId, sessionId, request);
    }

    @PostMapping("/review")
    PreparationReviewDtos.Response createReview(@PathVariable Long applicationId,
        @PathVariable Long sessionId,
        @Valid @RequestBody PreparationReviewDtos.CreateRequest request) {
        return reviews.create(applicationId, sessionId, request);
    }

    @GetMapping("/review")
    PreparationReviewDtos.Response review(@PathVariable Long applicationId,
        @PathVariable Long sessionId) {
        return reviews.get(applicationId, sessionId);
    }
}
