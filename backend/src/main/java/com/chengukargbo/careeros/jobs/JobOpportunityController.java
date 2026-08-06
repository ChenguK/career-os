package com.chengukargbo.careeros.jobs;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.chengukargbo.careeros.jobs.dto.JobOpportunityRequest;
import com.chengukargbo.careeros.jobs.dto.JobOpportunityResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/jobs")
public class JobOpportunityController {

    private final JobOpportunityService jobService;

    public JobOpportunityController(
        JobOpportunityService jobService
    ) {
        this.jobService = jobService;
    }

    @PostMapping
    public ResponseEntity<JobOpportunityResponse> create(
        @Valid @RequestBody JobOpportunityRequest request
    ) {
        JobOpportunityResponse response =
            jobService.create(request);

        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.id())
            .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public List<JobOpportunityResponse> findAll(
        @RequestParam(required = false) String search
    ) {
        return jobService.search(search);
    }

    @GetMapping("/{id}")
    public JobOpportunityResponse findById(
        @PathVariable Long id
    ) {
        return jobService.findById(id);
    }

    @PutMapping("/{id}")
    public JobOpportunityResponse update(
        @PathVariable Long id,
        @Valid @RequestBody JobOpportunityRequest request
    ) {
        return jobService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        jobService.delete(id);
    }
}
