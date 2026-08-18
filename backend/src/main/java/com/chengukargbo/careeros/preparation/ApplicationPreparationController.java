package com.chengukargbo.careeros.preparation;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.chengukargbo.careeros.preparation.PreparationDtos.*;

@RestController
@RequestMapping("/api/applications/{applicationId}/preparation")
public class ApplicationPreparationController {
    private final ApplicationPreparationService service;

    public ApplicationPreparationController(ApplicationPreparationService service) {
        this.service = service;
    }

    @PostMapping("/initialize")
    Response initialize(@PathVariable Long applicationId) {
        return service.initialize(applicationId);
    }

    @PostMapping("/cancel")
    Response cancel(@PathVariable Long applicationId) {
        return service.cancel(applicationId);
    }

    @PostMapping("/retry")
    Response retry(@PathVariable Long applicationId) {
        return service.retry(applicationId);
    }

    @PostMapping("/resume")
    Response resume(@PathVariable Long applicationId) {
        return service.resume(applicationId);
    }

    @GetMapping
    Response get(@PathVariable Long applicationId) {
        return service.get(applicationId);
    }

    @GetMapping("/events")
    List<Event> events(@PathVariable Long applicationId) {
        return service.events(applicationId);
    }
}
