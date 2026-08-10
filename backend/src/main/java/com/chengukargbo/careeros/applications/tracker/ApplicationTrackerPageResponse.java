package com.chengukargbo.careeros.applications.tracker;

import java.util.List;

import com.chengukargbo.careeros.applications.dto.ApplicationTrackerResponse;

public record ApplicationTrackerPageResponse(
    List<ApplicationTrackerResponse> content,
    int page,
    int size,
    long totalRows,
    int totalPages
) {
}
