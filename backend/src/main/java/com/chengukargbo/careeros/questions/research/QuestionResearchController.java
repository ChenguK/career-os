package com.chengukargbo.careeros.questions.research;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import com.chengukargbo.careeros.questions.QuestionEnums.*;

@RestController
@RequestMapping("/api/questions/research")
public class QuestionResearchController {
    private final QuestionResearchService service;
    public QuestionResearchController(QuestionResearchService service) { this.service = service; }
    @GetMapping
    List<LikelyQuestion> research(@RequestParam JobFamily jobFamily,
        @RequestParam Seniority seniority,
        @RequestParam(required=false) String provider) {
        return service.research(jobFamily, seniority, provider);
    }
}
