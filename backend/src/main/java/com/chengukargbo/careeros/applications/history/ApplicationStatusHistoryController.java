package com.chengukargbo.careeros.applications.history;

import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications/{applicationId}/history")
public class ApplicationStatusHistoryController {
    private final ApplicationStatusHistoryService service;
    public ApplicationStatusHistoryController(ApplicationStatusHistoryService service) {
        this.service = service;
    }
    @GetMapping
    public List<ApplicationStatusHistoryResponse> findHistory(@PathVariable Long applicationId) {
        return service.findForApplication(applicationId);
    }
}
