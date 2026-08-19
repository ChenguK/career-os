package com.chengukargbo.careeros.preparation;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import com.chengukargbo.careeros.preparation.QuestionMappingDtos.*;
import com.chengukargbo.careeros.questions.QuestionEnums.*;
import jakarta.validation.Valid;

@RestController @RequestMapping("/api/applications/{applicationId}/preparation/question-mappings")
public class ObservedQuestionMappingController {
    private final ObservedQuestionMappingService service;
    public ObservedQuestionMappingController(ObservedQuestionMappingService service){this.service=service;}
    @GetMapping ReviewResponse review(@PathVariable Long applicationId,@RequestParam JobFamily jobFamily,@RequestParam(required=false) Seniority seniority){return service.review(applicationId,jobFamily,seniority);}
    @PostMapping MappingResponse confirm(@PathVariable Long applicationId,@Valid @RequestBody ConfirmRequest request){return service.confirm(applicationId,request);}
    @PostMapping("/{mappingId}/revoke") MappingResponse revoke(@PathVariable Long applicationId,@PathVariable Long mappingId){return service.revoke(applicationId,mappingId);}
    @GetMapping("/{mappingId}/history") List<HistoryResponse> history(@PathVariable Long applicationId,@PathVariable Long mappingId){return service.history(applicationId,mappingId);}
}
