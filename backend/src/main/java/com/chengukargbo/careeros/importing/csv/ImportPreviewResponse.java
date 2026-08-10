package com.chengukargbo.careeros.importing.csv;

import java.util.List;

import com.chengukargbo.careeros.importing.ImportIssue;
import com.chengukargbo.careeros.importing.ImportRowResult;

public record ImportPreviewResponse(
    String filename,
    int totalRows,
    long createCount,
    long reviewCount,
    long duplicateCount,
    long invalidCount,
    boolean hasFileErrors,
    boolean hasFileWarnings,
    List<ImportIssue> fileErrors,
    List<ImportIssue> fileWarnings,
    List<ImportRowResult> rows
) {
    public ImportPreviewResponse {
        fileErrors = List.copyOf(fileErrors);
        fileWarnings = List.copyOf(fileWarnings);
        rows = List.copyOf(rows);
    }
}
