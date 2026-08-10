package com.chengukargbo.careeros.importing.history;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/applications/imports")
public class ImportHistoryController {
    private final ImportHistoryService historyService;

    public ImportHistoryController(ImportHistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping
    public ImportHistoryPage<ImportBatchSummaryResponse> findHistory(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "25") int size
    ) {
        return historyService.findHistory(page, size);
    }

    @GetMapping("/{batchId}")
    public ImportBatchDetailResponse findBatch(
        @PathVariable Long batchId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "25") int size
    ) {
        return historyService.findBatch(batchId, page, size);
    }
}
