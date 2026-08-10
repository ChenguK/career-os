package com.chengukargbo.careeros.importing.persistence;

public enum ImportRowOutcomeStatus {
    CREATED,
    CREATED_WITH_WARNING,
    SKIPPED_DUPLICATE,
    FAILED_VALIDATION,
    FAILED_PERSISTENCE
}
