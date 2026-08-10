package com.chengukargbo.careeros.importing.history;

public record ImportBatchDetailResponse(
    ImportBatchSummaryResponse batch,
    ImportHistoryPage<ImportBatchRowResponse> rows
) {
}
