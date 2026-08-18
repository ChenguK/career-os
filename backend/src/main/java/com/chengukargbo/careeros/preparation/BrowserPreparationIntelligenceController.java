package com.chengukargbo.careeros.preparation;

import org.springframework.web.bind.annotation.*;
import com.chengukargbo.careeros.questions.QuestionEnums.*;

@RestController
@RequestMapping("/api/applications/{applicationId}/preparation/intelligence")
public class BrowserPreparationIntelligenceController {
 private final BrowserPreparationIntelligenceService service;
 public BrowserPreparationIntelligenceController(BrowserPreparationIntelligenceService service){this.service=service;}
 @GetMapping BrowserPreparationIntelligenceDtos.Response analyze(@PathVariable Long applicationId,@RequestParam JobFamily jobFamily,@RequestParam Seniority seniority,@RequestParam(required=false) String provider){return service.analyze(applicationId,jobFamily,seniority,provider);}
}
