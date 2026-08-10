package com.chengukargbo.careeros.importing.csv;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.chengukargbo.careeros.common.exception.BusinessValidationException;
import com.chengukargbo.careeros.importing.ImportAnalysisService;
import com.chengukargbo.careeros.importing.ImportProposedAction;
import com.chengukargbo.careeros.importing.ImportRowResult;

@Service
@Transactional(readOnly = true)
public class CsvImportPreviewService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
        "text/csv",
        "application/csv",
        "text/plain",
        "application/vnd.ms-excel"
    );

    private final CsvImportParser csvParser;
    private final ImportAnalysisService analysisService;

    public CsvImportPreviewService(
        CsvImportParser csvParser,
        ImportAnalysisService analysisService
    ) {
        this.csvParser = csvParser;
        this.analysisService = analysisService;
    }

    public ImportPreviewResponse preview(MultipartFile file) {
        validateFile(file);
        CsvParseResult parsed = csvParser.parse(readBytes(file));
        List<ImportRowResult> rows = analysisService.analyze(parsed.rows());

        return new ImportPreviewResponse(
            file.getOriginalFilename(),
            rows.size(),
            count(rows, ImportProposedAction.CREATE),
            count(rows, ImportProposedAction.REVIEW_WARNING),
            count(rows, ImportProposedAction.SKIP_DUPLICATE),
            count(rows, ImportProposedAction.INVALID),
            false,
            !parsed.fileWarnings().isEmpty(),
            List.of(),
            parsed.fileWarnings(),
            rows
        );
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessValidationException("A CSV file is required");
        }
        String filename = file.getOriginalFilename();
        if (filename == null
            || !filename.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            throw new BusinessValidationException(
                "Only .csv files are supported"
            );
        }
        String contentType = file.getContentType();
        if (contentType != null && !contentType.isBlank()
            && !ALLOWED_CONTENT_TYPES.contains(
                contentType.toLowerCase(Locale.ROOT)
            )) {
            throw new BusinessValidationException(
                "The uploaded file does not appear to be CSV text"
            );
        }
        if (file.getSize() > CsvImportParser.MAX_FILE_SIZE_BYTES) {
            throw new BusinessValidationException(
                "CSV file exceeds the 2 MB limit"
            );
        }
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw new BusinessValidationException(
                "CSV file could not be read"
            );
        }
    }

    private long count(
        List<ImportRowResult> rows,
        ImportProposedAction action
    ) {
        return rows.stream()
            .filter(row -> row.proposedAction() == action)
            .count();
    }
}
