package com.chengukargbo.careeros.applications.lock;
import java.util.List;import org.springframework.web.bind.annotation.*;import com.chengukargbo.careeros.applications.lock.ApplicationLockDtos.*;
@RestController @RequestMapping("/api/applications/{applicationId}/lock")
public class ApplicationLockController {private final ApplicationLockService service;public ApplicationLockController(ApplicationLockService service){this.service=service;}
 @GetMapping public Response get(@PathVariable Long applicationId){return service.get(applicationId);}
 @GetMapping("/history") public List<HistoryResponse> history(@PathVariable Long applicationId){return service.history(applicationId);}
 @PostMapping("/mark-submitted") public Response submitted(@PathVariable Long applicationId,@RequestBody(required=false) ReasonRequest request){return service.markSubmitted(applicationId,request==null?null:request.reason());}
 @PostMapping("/archive") public Response archive(@PathVariable Long applicationId,@RequestBody(required=false) ReasonRequest request){return service.archive(applicationId,request==null?null:request.reason());}
 @PostMapping("/restore") public Response restore(@PathVariable Long applicationId,@RequestBody(required=false) ReasonRequest request){return service.restore(applicationId,request==null?null:request.reason());}
 @PostMapping("/mark-testing") public Response testing(@PathVariable Long applicationId,@RequestBody(required=false) ReasonRequest request){return service.markTesting(applicationId,request==null?null:request.reason());}
}
