package com.chengukargbo.careeros.applications.tracker;

import java.time.LocalDate;
import java.util.List;

import com.chengukargbo.careeros.applications.ApplicationStatus;
import com.chengukargbo.careeros.jobs.RemoteType;

public record ApplicationTrackerQuery(
    String search,
    List<ApplicationStatus> statuses,
    List<Short> priorities,
    List<RemoteType> remoteTypes,
    Long companyId,
    LocalDate applicationDateFrom,
    LocalDate applicationDateTo,
    LocalDate datePostedFrom,
    LocalDate datePostedTo,
    LocalDate followUpDateFrom,
    LocalDate followUpDateTo,
    String sort,
    String direction,
    int page,
    int size
) {

    public static final int DEFAULT_SIZE = 25;
    public static final int MAX_SIZE = 100;

    public ApplicationTrackerQuery {
        search = normalize(search);
        statuses = statuses == null ? List.of() : List.copyOf(statuses);
        priorities = priorities == null ? List.of() : List.copyOf(priorities);
        remoteTypes = remoteTypes == null
            ? List.of()
            : List.copyOf(remoteTypes);
        sort = normalize(sort) == null ? "priority" : sort.trim();
        direction = normalize(direction) == null
            ? "asc"
            : direction.trim().toLowerCase();
        page = Math.max(page, 0);
        size = size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
