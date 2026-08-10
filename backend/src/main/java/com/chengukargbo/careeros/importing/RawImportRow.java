package com.chengukargbo.careeros.importing;

import java.util.Map;
import java.util.Collections;
import java.util.LinkedHashMap;

public record RawImportRow(int rowNumber, Map<String, String> fields) {
    public RawImportRow {
        fields = Collections.unmodifiableMap(new LinkedHashMap<>(fields));
    }
}
