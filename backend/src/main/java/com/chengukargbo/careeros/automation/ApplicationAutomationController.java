package com.chengukargbo.careeros.automation;
import java.util.List; import org.springframework.web.bind.annotation.*; import com.chengukargbo.careeros.automation.AutomationDtos.*;
@RestController @RequestMapping("/api/applications/{applicationId}/automation")
public class ApplicationAutomationController {private final ApplicationAutomationService service;public ApplicationAutomationController(ApplicationAutomationService s){service=s;}
 @GetMapping Response get(@PathVariable Long applicationId){return service.get(applicationId);}
 @PostMapping("/approve-prep") Response approvePrep(@PathVariable Long applicationId){return service.approvePrep(applicationId);}
 @PostMapping("/mark-ready") Response markReady(@PathVariable Long applicationId){return service.markReady(applicationId);}
 @PostMapping("/approve-submit") Response approveSubmit(@PathVariable Long applicationId){return service.approveSubmit(applicationId);}
 @PostMapping("/revoke") Response revoke(@PathVariable Long applicationId){return service.revoke(applicationId);}
 @PutMapping("/ats-type") Response ats(@PathVariable Long applicationId,@RequestBody AtsRequest request){return service.setAts(applicationId,request.atsType());}
 @GetMapping("/history") List<HistoryResponse> history(@PathVariable Long applicationId){return service.history(applicationId);}
}
