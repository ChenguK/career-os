package com.chengukargbo.careeros.applications.history;

import java.time.OffsetDateTime;
import com.chengukargbo.careeros.applications.ApplicationStatus;

public record ApplicationStatusHistoryResponse(Long id, Long applicationId,
    ApplicationStatus previousStatus, ApplicationStatus newStatus,
    OffsetDateTime occurredAt, ApplicationTransitionSource source,
    String note, OffsetDateTime createdAt) {
    static ApplicationStatusHistoryResponse from(ApplicationStatusHistory event) {
        return new ApplicationStatusHistoryResponse(event.getId(),
            event.getApplication().getId(), event.getPreviousStatus(),
            event.getNewStatus(), event.getOccurredAt(), event.getSource(),
            event.getNote(), event.getCreatedAt());
    }
}
