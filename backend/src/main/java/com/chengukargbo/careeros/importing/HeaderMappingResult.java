package com.chengukargbo.careeros.importing;

import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.LinkedHashMap;

public record HeaderMappingResult(
    Map<String, String> fields,
    List<ImportIssue> errors,
    List<ImportIssue> warnings
) {
    public HeaderMappingResult {
        fields = Collections.unmodifiableMap(new LinkedHashMap<>(fields));
        errors = List.copyOf(errors);
        warnings = List.copyOf(warnings);
    }
}
