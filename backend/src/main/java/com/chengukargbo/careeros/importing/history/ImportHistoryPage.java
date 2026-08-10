package com.chengukargbo.careeros.importing.history;

import java.util.List;

public record ImportHistoryPage<T>(
    List<T> content,
    int page,
    int size,
    long totalRows,
    int totalPages
) {
    public ImportHistoryPage {
        content = List.copyOf(content);
    }
}
