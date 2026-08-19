package com.chengukargbo.careeros.preparation;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.chengukargbo.careeros.preparation.ObservationDtos.*;

@RestController
@RequestMapping("/api/applications/{applicationId}/preparation/observations")
public class FormObservationController {
    private final FormObservationService service;

    public FormObservationController(FormObservationService service) {
        this.service = service;
    }

    @GetMapping("/snapshots")
    List<SnapshotResponse> snapshots(@PathVariable Long applicationId) {
        return service.snapshots(applicationId);
    }

    @GetMapping("/questions")
    List<QuestionResponse> questions(@PathVariable Long applicationId) {
        return service.latestQuestions(applicationId);
    }

    @GetMapping("/material-requirements")
    List<MaterialRequirementResponse> materialRequirements(@PathVariable Long applicationId) {
        return service.latestMaterialRequirements(applicationId);
    }

    @GetMapping("/snapshots/{snapshotId}/questions")
    List<QuestionResponse> snapshotQuestions(
        @PathVariable Long applicationId, @PathVariable Long snapshotId
    ) {
        return service.snapshotQuestions(applicationId, snapshotId);
    }

    @GetMapping("/snapshots/{snapshotId}/material-requirements")
    List<MaterialRequirementResponse> snapshotMaterialRequirements(
        @PathVariable Long applicationId, @PathVariable Long snapshotId
    ) {
        return service.snapshotMaterialRequirements(applicationId, snapshotId);
    }
}
