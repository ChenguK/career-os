package com.chengukargbo.careeros.preparation;

import java.time.OffsetDateTime;

import com.chengukargbo.careeros.preparation.PreparationEnums.IdentitySource;

public record FormIdentity(
    Long formTargetId,
    Long applicationId,
    String normalizedFormUrl,
    String externalRequisitionId,
    String externalFormKey,
    IdentitySource identitySource,
    OffsetDateTime lastConfirmedAt
) {
    static FormIdentity from(ApplicationFormTarget target) {
        return new FormIdentity(
            target.getId(), target.getApplication().getId(),
            target.getNormalizedFormUrl(), target.getExternalRequisitionId(),
            target.getExternalFormKey(), target.getIdentitySource(),
            target.getLastConfirmedAt()
        );
    }
}
