package com.chengukargbo.careeros.applications.lock;
import java.time.OffsetDateTime;
public final class ApplicationLockDtos {private ApplicationLockDtos(){}
 public record Response(Long id,Long applicationId,ApplicationLockState lockState,OffsetDateTime changedAt,String reason,OffsetDateTime createdAt,OffsetDateTime updatedAt){static Response from(ApplicationLock lock){return new Response(lock.getId(),lock.getApplication().getId(),lock.getState(),lock.getChangedAt(),lock.getReason(),lock.getCreatedAt(),lock.getUpdatedAt());}}
 public record HistoryResponse(Long id,Long applicationId,ApplicationLockState previousLock,ApplicationLockState newLock,ApplicationLockSource source,OffsetDateTime occurredAt,String reason){static HistoryResponse from(ApplicationLockHistory h){return new HistoryResponse(h.getId(),h.getApplicationId(),h.getPreviousLock(),h.getNewLock(),h.getSource(),h.getOccurredAt(),h.getReason());}}
 public record ReasonRequest(String reason){}
}
