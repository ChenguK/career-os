package com.chengukargbo.careeros.importing.xlsx;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.chengukargbo.careeros.common.exception.BusinessValidationException;
import com.chengukargbo.careeros.importing.ImportAnalysisService;
import com.chengukargbo.careeros.importing.ImportProposedAction;
import com.chengukargbo.careeros.importing.ImportRowResult;
import com.chengukargbo.careeros.importing.csv.CsvParseResult;
import com.chengukargbo.careeros.importing.csv.ImportPreviewResponse;

@Service
@Transactional(readOnly = true)
public class XlsxImportPreviewService {

    private static final String XLSX_CONTENT_TYPE =
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final XlsxImportParser parser;
    private final ImportAnalysisService analysisService;

    public XlsxImportPreviewService(
        XlsxImportParser parser,
        ImportAnalysisService analysisService
    ) {
        this.parser = parser;
        this.analysisService = analysisService;
    }

    public ImportPreviewResponse preview(MultipartFile file) {
        validate(file);
        CsvParseResult parsed = parser.parse(read(file));
        List<ImportRowResult> rows = analysisService.analyze(parsed.rows());
        return new ImportPreviewResponse(
            file.getOriginalFilename(), rows.size(),
            count(rows, ImportProposedAction.CREATE),
            count(rows, ImportProposedAction.REVIEW_WARNING),
            count(rows, ImportProposedAction.SKIP_DUPLICATE),
            count(rows, ImportProposedAction.INVALID),
            false, !parsed.fileWarnings().isEmpty(), List.of(),
            parsed.fileWarnings(), rows
        );
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessValidationException("An XLSX file is required");
        }
        String filename = file.getOriginalFilename();
        if (filename == null
            || !filename.toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            throw new BusinessValidationException("Only .xlsx files are supported");
        }
        String contentType = file.getContentType();
        if (contentType != null && !contentType.isBlank()
            && !contentType.equalsIgnoreCase(XLSX_CONTENT_TYPE)) {
            throw new BusinessValidationException(
                "The uploaded file does not appear to be an XLSX workbook"
            );
        }
        if (file.getSize() > XlsxImportParser.MAX_FILE_SIZE_BYTES) {
            throw new BusinessValidationException("XLSX file exceeds the 5 MB limit");
        }
    }

    private byte[] read(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw new BusinessValidationException("XLSX file could not be read");
        }
    }

    private long count(List<ImportRowResult> rows, ImportProposedAction action) {
        return rows.stream().filter(row -> row.proposedAction() == action).count();
    }
}
