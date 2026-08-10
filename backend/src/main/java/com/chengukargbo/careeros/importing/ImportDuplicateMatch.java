package com.chengukargbo.careeros.importing;

public record ImportDuplicateMatch(
    Long jobOpportunityId,
    Integer importRowNumber,
    String companyName,
    String positionTitle,
    String applicationUrl
) {
}
