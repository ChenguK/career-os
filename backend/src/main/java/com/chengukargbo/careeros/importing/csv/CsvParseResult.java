package com.chengukargbo.careeros.importing.csv;

import java.util.List;

import com.chengukargbo.careeros.importing.ImportIssue;
import com.chengukargbo.careeros.importing.RawImportRow;

public record CsvParseResult(
    List<RawImportRow> rows,
    List<ImportIssue> fileWarnings
) {
    public CsvParseResult {
        rows = List.copyOf(rows);
        fileWarnings = List.copyOf(fileWarnings);
    }
}
