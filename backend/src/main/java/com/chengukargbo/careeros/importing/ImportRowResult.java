package com.chengukargbo.careeros.importing;

import java.util.List;

public record ImportRowResult(
    int rowNumber,
    CanonicalImportRow values,
    List<ImportIssue> errors,
    List<ImportIssue> warnings,
    String normalizedApplicationUrl,
    ImportDuplicateMatch exactUrlDuplicate,
    List<ImportDuplicateMatch> companyTitleDuplicateCandidates,
    ImportProposedAction proposedAction,
    boolean selectable
) {
    public ImportRowResult {
        errors = List.copyOf(errors);
        warnings = List.copyOf(warnings);
        companyTitleDuplicateCandidates = List.copyOf(
            companyTitleDuplicateCandidates
        );
    }
}
