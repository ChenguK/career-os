package com.chengukargbo.careeros.importing.history;

import java.time.OffsetDateTime;

import com.chengukargbo.careeros.importing.persistence.ImportBatch;

public record ImportBatchSummaryResponse(
    Long batchId,
    String filename,
    String format,
    String schemaVersion,
    OffsetDateTime createdAt,
    OffsetDateTime completedAt,
    int totalRows,
    int selectedRows,
    int created,
    int createdWithWarnings,
    int skippedDuplicates,
    int failed
) {
    public static ImportBatchSummaryResponse from(ImportBatch batch) {
        return new ImportBatchSummaryResponse(
            batch.getId(), batch.getOriginalFilename(), batch.getFormat(),
            batch.getSchemaVersion(), batch.getCreatedAt(), batch.getCompletedAt(),
            batch.getTotalRowCount(), batch.getSelectedRowCount(),
            batch.getCreatedRowCount(), batch.getWarningCreatedRowCount(),
            batch.getSkippedDuplicateRowCount(), batch.getFailedRowCount()
        );
    }
}
