package com.chengukargbo.careeros.applications;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chengukargbo.careeros.applications.dto.ApplicationTrackerResponse;
import com.chengukargbo.careeros.applications.tracker.ApplicationTrackerPageResponse;
import com.chengukargbo.careeros.applications.tracker.ApplicationTrackerQuery;
import com.chengukargbo.careeros.jobs.RemoteType;

@RestController
@RequestMapping("/api/applications/tracker")
public class ApplicationTrackerController {

    private final ApplicationTrackerService trackerService;

    public ApplicationTrackerController(
        ApplicationTrackerService trackerService
    ) {
        this.trackerService = trackerService;
    }

    @GetMapping
    public ApplicationTrackerPageResponse findAll(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) List<ApplicationStatus> statuses,
        @RequestParam(required = false) List<Short> priorities,
        @RequestParam(required = false) List<RemoteType> remoteTypes,
        @RequestParam(required = false) Long companyId,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate applicationDateFrom,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate applicationDateTo,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate datePostedFrom,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate datePostedTo,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate followUpDateFrom,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate followUpDateTo,
        @RequestParam(defaultValue = "priority") String sort,
        @RequestParam(defaultValue = "asc") String direction,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "25") int size
    ) {
        return trackerService.findAll(new ApplicationTrackerQuery(
            search,
            statuses,
            priorities,
            remoteTypes,
            companyId,
            applicationDateFrom,
            applicationDateTo,
            datePostedFrom,
            datePostedTo,
            followUpDateFrom,
            followUpDateTo,
            sort,
            direction,
            page,
            size
        ));
    }

    @GetMapping("/jobs/{jobOpportunityId}")
    public ApplicationTrackerResponse findByJobOpportunityId(
        @PathVariable Long jobOpportunityId
    ) {
        return trackerService.findByJobOpportunityId(jobOpportunityId);
    }
}
