package com.chengukargbo.careeros.applications.export;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chengukargbo.careeros.applications.ApplicationStatus;
import com.chengukargbo.careeros.applications.tracker.ApplicationTrackerQuery;
import com.chengukargbo.careeros.jobs.RemoteType;

@RestController
@RequestMapping("/api/applications/tracker/export.{format:csv|xlsx}")
public class ApplicationTrackerExportController {

    private static final MediaType CSV = MediaType.parseMediaType(
        "text/csv;charset=UTF-8"
    );

    private final ApplicationTrackerExportService exportService;

    public ApplicationTrackerExportController(
        ApplicationTrackerExportService exportService
    ) {
        this.exportService = exportService;
    }

    @GetMapping
    public ResponseEntity<byte[]> export(
        @PathVariable String format,
        @RequestParam(defaultValue = "CURRENT_VIEW")
        ApplicationTrackerExportMode mode,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) List<ApplicationStatus> statuses,
        @RequestParam(required = false) List<Short> priorities,
        @RequestParam(required = false) List<RemoteType> remoteTypes,
        @RequestParam(required = false) Long companyId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate applicationDateFrom,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate applicationDateTo,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate datePostedFrom,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate datePostedTo,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate followUpDateFrom,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate followUpDateTo,
        @RequestParam(defaultValue = "priority") String sort,
        @RequestParam(defaultValue = "asc") String direction
    ) {
        ApplicationTrackerQuery query = new ApplicationTrackerQuery(
                search, statuses, priorities, remoteTypes, companyId,
                applicationDateFrom, applicationDateTo, datePostedFrom,
                datePostedTo, followUpDateFrom, followUpDateTo, sort,
                direction, 0, ApplicationTrackerQuery.DEFAULT_SIZE
        );

        byte[] content;
        String filename;
        MediaType contentType;
        if (format.equals("xlsx")) {
            ApplicationTrackerXlsxExport export = exportService.exportXlsx(mode, query);
            content = export.content();
            filename = export.filename();
            contentType = MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            );
        } else {
            ApplicationTrackerCsvExport export = exportService.export(mode, query);
            content = export.content();
            filename = export.filename();
            contentType = CSV;
        }

        return ResponseEntity.ok()
            .contentType(contentType)
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + filename + "\""
            )
            .body(content);
    }
}
