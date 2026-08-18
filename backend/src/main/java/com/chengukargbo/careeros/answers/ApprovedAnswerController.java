package com.chengukargbo.careeros.answers;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.chengukargbo.careeros.answers.dto.ApprovedAnswerRequest;
import com.chengukargbo.careeros.answers.dto.ApprovedAnswerResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/approved-answers")
public class ApprovedAnswerController {

    private final ApprovedAnswerService answerService;

    public ApprovedAnswerController(ApprovedAnswerService answerService) {
        this.answerService = answerService;
    }

    @GetMapping
    public List<ApprovedAnswerResponse> findAll() {
        return answerService.findAll();
    }

    @PostMapping
    public ResponseEntity<ApprovedAnswerResponse> create(
        @Valid @RequestBody ApprovedAnswerRequest request
    ) {
        ApprovedAnswerResponse response = answerService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.id())
            .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ApprovedAnswerResponse update(
        @PathVariable Long id,
        @Valid @RequestBody ApprovedAnswerRequest request
    ) {
        return answerService.update(id, request);
    }

    @PostMapping("/{id}/approve")
    public ApprovedAnswerResponse approve(@PathVariable Long id) {
        return answerService.approve(id);
    }

    @PostMapping("/{id}/revoke")
    public ApprovedAnswerResponse revoke(@PathVariable Long id) {
        return answerService.revoke(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        answerService.delete(id);
    }
}
