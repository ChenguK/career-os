package com.chengukargbo.careeros.importing.persistence;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.chengukargbo.careeros.common.exception.BusinessValidationException;
import com.chengukargbo.careeros.importing.ImportAnalysisService;
import com.chengukargbo.careeros.importing.ImportProposedAction;
import com.chengukargbo.careeros.importing.ImportRowResult;
import com.chengukargbo.careeros.importing.RawImportRow;

@Service
public class ImportPersistenceService {

    private final ImportAnalysisService analysisService;
    private final ImportRowPersistenceService rowPersistenceService;
    private final ImportBatchMetadataService metadataService;

    public ImportPersistenceService(
        ImportAnalysisService analysisService,
        ImportRowPersistenceService rowPersistenceService,
        ImportBatchMetadataService metadataService
    ) {
        this.analysisService = analysisService;
        this.rowPersistenceService = rowPersistenceService;
        this.metadataService = metadataService;
    }

    public ImportPersistenceResponse persist(ImportPersistenceRequest request) {
        validateRequest(request);
        List<RawImportRow> rawRows = request.rows().stream()
            .map(row -> new RawImportRow(row.rowNumber(), row.fields()))
            .toList();
        List<ImportRowResult> analyzed = analysisService.analyze(rawRows);

        ImportBatch batch = metadataService.create(
            request.filename().trim(), request.totalRows(), analyzed.size()
        );
        List<ImportRowPersistenceResult> outcomes = new ArrayList<>();
        for (ImportRowResult row : analyzed) {
            outcomes.add(persistAnalyzedRow(row));
        }
        ImportBatch completed = metadataService.complete(batch, outcomes);

        return response(completed, outcomes);
    }

    private ImportRowPersistenceResult persistAnalyzedRow(
        ImportRowResult row
    ) {
        if (row.proposedAction() == ImportProposedAction.INVALID) {
            return new ImportRowPersistenceResult(
                row.rowNumber(), ImportRowOutcomeStatus.FAILED_VALIDATION,
                null, null, null, null, List.of(),
                row.errors().stream().map(issue -> issue.message()).toList()
            );
        }
        if (row.proposedAction() == ImportProposedAction.SKIP_DUPLICATE) {
            Long duplicateId = row.exactUrlDuplicate() == null
                ? null : row.exactUrlDuplicate().jobOpportunityId();
            return new ImportRowPersistenceResult(
                row.rowNumber(), ImportRowOutcomeStatus.SKIPPED_DUPLICATE,
                null, null, null, duplicateId,
                List.of("Application URL already exists"), List.of()
            );
        }

        try {
            return rowPersistenceService.persist(row);
        } catch (CompanyResolutionRaceException race) {
            try {
                return rowPersistenceService.persist(row);
            } catch (RuntimeException retryFailure) {
                return failed(row, retryFailure);
            }
        } catch (RuntimeException exception) {
            return failed(row, exception);
        }
    }

    private ImportRowPersistenceResult failed(
        ImportRowResult row,
        RuntimeException exception
    ) {
        String message = exception.getMessage() == null
            ? "Row could not be persisted"
            : exception.getMessage();
        return new ImportRowPersistenceResult(
            row.rowNumber(), ImportRowOutcomeStatus.FAILED_PERSISTENCE,
            null, null, null, null, List.of(), List.of(message)
        );
    }

    private ImportPersistenceResponse response(
        ImportBatch batch,
        List<ImportRowPersistenceResult> outcomes
    ) {
        return new ImportPersistenceResponse(
            batch.getId(),
            batch.getOriginalFilename(),
            batch.getTotalRowCount(),
            batch.getSelectedRowCount(),
            batch.getCreatedRowCount(),
            batch.getWarningCreatedRowCount(),
            batch.getSkippedDuplicateRowCount(),
            batch.getFailedRowCount(),
            outcomes
        );
    }

    private void validateRequest(ImportPersistenceRequest request) {
        if (request == null || request.filename() == null
            || request.filename().isBlank()) {
            throw new BusinessValidationException("Import filename is required");
        }
        String filename = request.filename().trim();
        String lowerFilename = filename.toLowerCase(Locale.ROOT);
        if (filename.length() > 255
            || (!lowerFilename.endsWith(".csv")
                && !lowerFilename.endsWith(".xlsx"))) {
            throw new BusinessValidationException(
                "A valid CSV or XLSX filename is required"
            );
        }
        if (request.rows().isEmpty()) {
            throw new BusinessValidationException(
                "Select at least one row to import"
            );
        }
        if (request.totalRows() < request.rows().size()) {
            throw new BusinessValidationException(
                "Total row count cannot be below selected row count"
            );
        }
        Set<Integer> rowNumbers = new HashSet<>();
        for (SelectedImportRowRequest row : request.rows()) {
            if (row.rowNumber() < 2 || !rowNumbers.add(row.rowNumber())) {
                throw new BusinessValidationException(
                    "Selected import row numbers must be unique and at least 2"
                );
            }
        }
    }
}
