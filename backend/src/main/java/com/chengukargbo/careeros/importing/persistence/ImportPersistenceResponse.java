package com.chengukargbo.careeros.importing.persistence;

import java.util.List;

public record ImportPersistenceResponse(
    Long batchId,
    String filename,
    int totalRows,
    int selectedRows,
    int created,
    int createdWithWarnings,
    int skippedDuplicates,
    int failed,
    List<ImportRowPersistenceResult> rows
) {
    public ImportPersistenceResponse {
        rows = List.copyOf(rows);
    }
}
