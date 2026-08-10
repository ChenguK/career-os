package com.chengukargbo.careeros.importing.persistence;

import java.util.Map;
import java.util.Collections;
import java.util.LinkedHashMap;

public record SelectedImportRowRequest(
    int rowNumber,
    Map<String, String> fields
) {
    public SelectedImportRowRequest {
        fields = fields == null
            ? Map.of()
            : Collections.unmodifiableMap(new LinkedHashMap<>(fields));
    }
}
