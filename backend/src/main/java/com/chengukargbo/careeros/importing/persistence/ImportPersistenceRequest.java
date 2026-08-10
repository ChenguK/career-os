package com.chengukargbo.careeros.importing.persistence;

import java.util.List;

public record ImportPersistenceRequest(
    String filename,
    int totalRows,
    List<SelectedImportRowRequest> rows
) {
    public ImportPersistenceRequest {
        rows = rows == null ? List.of() : List.copyOf(rows);
    }
}
