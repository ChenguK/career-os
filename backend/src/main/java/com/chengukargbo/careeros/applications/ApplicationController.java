package com.chengukargbo.careeros.applications;

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

import com.chengukargbo.careeros.applications.dto.ApplicationRequest;
import com.chengukargbo.careeros.applications.dto.ApplicationResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(
        ApplicationService applicationService
    ) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> create(
        @Valid @RequestBody ApplicationRequest request
    ) {
        ApplicationResponse response =
            applicationService.create(request);

        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.id())
            .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public List<ApplicationResponse> findAll() {
        return applicationService.findAll();
    }

    @GetMapping("/{id}")
    public ApplicationResponse findById(
        @PathVariable Long id
    ) {
        return applicationService.findById(id);
    }

    @PutMapping("/{id}")
    public ApplicationResponse update(
        @PathVariable Long id,
        @Valid @RequestBody ApplicationRequest request
    ) {
        return applicationService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        applicationService.delete(id);
    }
}