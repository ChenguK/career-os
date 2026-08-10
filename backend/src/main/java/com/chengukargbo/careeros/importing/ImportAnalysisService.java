package com.chengukargbo.careeros.importing;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ImportAnalysisService {

    private final ImportRowNormalizer rowNormalizer;
    private final ImportDuplicateAnalyzer duplicateAnalyzer;

    public ImportAnalysisService(
        ImportRowNormalizer rowNormalizer,
        ImportDuplicateAnalyzer duplicateAnalyzer
    ) {
        this.rowNormalizer = rowNormalizer;
        this.duplicateAnalyzer = duplicateAnalyzer;
    }

    public List<ImportRowResult> analyze(List<RawImportRow> rawRows) {
        List<ImportRowResult> normalized = rawRows.stream()
            .map(row -> rowNormalizer.normalize(
                row.rowNumber(), row.fields()
            ))
            .toList();
        return duplicateAnalyzer.analyze(normalized);
    }
}
