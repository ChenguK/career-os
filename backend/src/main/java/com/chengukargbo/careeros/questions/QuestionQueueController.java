package com.chengukargbo.careeros.questions;
import java.util.List; import org.springframework.web.bind.annotation.*; import com.chengukargbo.careeros.questions.QuestionDtos.*; import com.chengukargbo.careeros.questions.QuestionEnums.*;
import jakarta.validation.Valid;
@RestController @RequestMapping("/api/questions")
public class QuestionQueueController {private final QuestionQueueService service;public QuestionQueueController(QuestionQueueService s){service=s;}
 @GetMapping("/templates") List<TemplateResponse> templates(@RequestParam JobFamily jobFamily,@RequestParam(required=false) Seniority seniority){return service.templates(jobFamily,seniority);}
 @GetMapping List<QuestionResponse> all(@RequestParam(required=false) Long applicationId){return service.all(applicationId);}
 @PostMapping List<QuestionResponse> add(@Valid @RequestBody AddTemplatesRequest r){return service.addTemplates(r);}
 @PostMapping("/manual") QuestionResponse manual(@Valid @RequestBody ManualRequest r){return service.manual(r);}
 @PutMapping("/{id}/answer") QuestionResponse answer(@PathVariable Long id,@Valid @RequestBody AnswerRequest r){return service.answer(id,r.answer());}
 @PostMapping("/{id}/approve") QuestionResponse approve(@PathVariable Long id){return service.approve(id);}
 @PostMapping("/{id}/reject-suggestion") QuestionResponse reject(@PathVariable Long id){return service.reject(id);}
 @PostMapping("/{id}/block") QuestionResponse block(@PathVariable Long id){return service.block(id,true);}
 @PostMapping("/{id}/unblock") QuestionResponse unblock(@PathVariable Long id){return service.block(id,false);}
 @PostMapping("/{id}/link") QuestionResponse link(@PathVariable Long id,@Valid @RequestBody LinkRequest r){return service.link(id,r.approvedAnswerId());}
}
