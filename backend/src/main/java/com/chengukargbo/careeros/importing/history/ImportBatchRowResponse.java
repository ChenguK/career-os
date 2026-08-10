package com.chengukargbo.careeros.importing.history;

import java.util.Arrays;
import java.util.List;

import com.chengukargbo.careeros.importing.persistence.ImportBatchRow;
import com.chengukargbo.careeros.importing.persistence.ImportRowOutcomeStatus;

public record ImportBatchRowResponse(
    int rowNumber,
    ImportRowOutcomeStatus outcome,
    Long companyId,
    Long jobOpportunityId,
    Long applicationId,
    Long duplicateJobOpportunityId,
    List<String> warnings,
    List<String> errors
) {
    public static ImportBatchRowResponse from(ImportBatchRow row) {
        return new ImportBatchRowResponse(
            row.getSourceRowNumber(), row.getOutcome(), row.getCompanyId(),
            row.getJobOpportunityId(), row.getApplicationId(),
            row.getDuplicateJobOpportunityId(), split(row.getWarnings()),
            split(row.getErrors())
        );
    }

    private static List<String> split(String value) {
        return value == null || value.isBlank()
            ? List.of()
            : Arrays.stream(value.split("\\R"))
                .filter(part -> !part.isBlank())
                .toList();
    }
}
