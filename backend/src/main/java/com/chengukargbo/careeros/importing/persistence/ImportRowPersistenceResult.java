package com.chengukargbo.careeros.importing.persistence;

import java.util.List;

public record ImportRowPersistenceResult(
    int rowNumber,
    ImportRowOutcomeStatus status,
    Long companyId,
    Long jobOpportunityId,
    Long applicationId,
    Long duplicateJobOpportunityId,
    List<String> warnings,
    List<String> errors
) {
    public ImportRowPersistenceResult {
        warnings = List.copyOf(warnings);
        errors = List.copyOf(errors);
    }
}
