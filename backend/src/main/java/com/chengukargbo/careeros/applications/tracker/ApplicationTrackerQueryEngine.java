package com.chengukargbo.careeros.applications.tracker;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.chengukargbo.careeros.applications.ApplicationStatus;
import com.chengukargbo.careeros.applications.dto.ApplicationTrackerResponse;
import com.chengukargbo.careeros.common.exception.BusinessValidationException;

@Component
public class ApplicationTrackerQueryEngine {

    private static final Set<String> SUPPORTED_SORTS = Set.of(
        "company",
        "positionTitle",
        "status",
        "priority",
        "matchScore",
        "location",
        "remoteType",
        "salaryMin",
        "datePosted",
        "applicationDate",
        "followUpDate",
        "source",
        "createdAt"
    );

    public ApplicationTrackerPageResponse execute(
        List<ApplicationTrackerResponse> rows,
        ApplicationTrackerQuery query
    ) {
        List<ApplicationTrackerResponse> filtered = executeAll(rows, query);

        long totalRows = filtered.size();
        int totalPages = totalRows == 0
            ? 0
            : (int) Math.ceil((double) totalRows / query.size());
        int fromIndex = Math.min(query.page() * query.size(), filtered.size());
        int toIndex = Math.min(fromIndex + query.size(), filtered.size());

        return new ApplicationTrackerPageResponse(
            filtered.subList(fromIndex, toIndex),
            query.page(),
            query.size(),
            totalRows,
            totalPages
        );
    }

    public List<ApplicationTrackerResponse> executeAll(
        List<ApplicationTrackerResponse> rows,
        ApplicationTrackerQuery query
    ) {
        validate(query);

        return rows.stream()
            .filter(row -> matches(row, query))
            .sorted(comparator(query))
            .toList();
    }

    private void validate(ApplicationTrackerQuery query) {
        if (!SUPPORTED_SORTS.contains(query.sort())) {
            throw new BusinessValidationException(
                "Unsupported tracker sort field: " + query.sort()
            );
        }

        if (!query.direction().equals("asc")
            && !query.direction().equals("desc")) {
            throw new BusinessValidationException(
                "Tracker sort direction must be asc or desc"
            );
        }
    }

    private boolean matches(
        ApplicationTrackerResponse row,
        ApplicationTrackerQuery query
    ) {
        return matchesSearch(row, query.search())
            && matchesStatuses(row, query.statuses())
            && (query.priorities().isEmpty()
                || query.priorities().contains(row.priority()))
            && (query.remoteTypes().isEmpty()
                || query.remoteTypes().contains(row.remoteType()))
            && (query.companyId() == null
                || query.companyId().equals(row.companyId()))
            && inRange(
                row.applicationDate(),
                query.applicationDateFrom(),
                query.applicationDateTo()
            )
            && inRange(
                row.datePosted(),
                query.datePostedFrom(),
                query.datePostedTo()
            )
            && inRange(
                row.followUpDate(),
                query.followUpDateFrom(),
                query.followUpDateTo()
            );
    }

    private boolean matchesSearch(
        ApplicationTrackerResponse row,
        String search
    ) {
        if (search == null) {
            return true;
        }

        String term = search.toLowerCase(Locale.ROOT);
        return contains(row.companyName(), term)
            || contains(row.positionTitle(), term)
            || contains(row.location(), term)
            || contains(row.source(), term);
    }

    private boolean matchesStatuses(
        ApplicationTrackerResponse row,
        List<ApplicationStatus> statuses
    ) {
        if (statuses.isEmpty()) {
            return true;
        }

        ApplicationStatus effectiveStatus = row.status() == null
            ? ApplicationStatus.SAVED
            : row.status();
        return statuses.contains(effectiveStatus);
    }

    private boolean contains(String value, String term) {
        return value != null
            && value.toLowerCase(Locale.ROOT).contains(term);
    }

    private boolean inRange(
        LocalDate value,
        LocalDate from,
        LocalDate to
    ) {
        if (from == null && to == null) {
            return true;
        }
        if (value == null) {
            return false;
        }
        return (from == null || !value.isBefore(from))
            && (to == null || !value.isAfter(to));
    }

    private Comparator<ApplicationTrackerResponse> comparator(
        ApplicationTrackerQuery query
    ) {
        Comparator<ApplicationTrackerResponse> primary = switch (
            query.sort()
        ) {
            case "company" -> comparingText(
                ApplicationTrackerResponse::companyName,
                query.direction()
            );
            case "positionTitle" -> comparingText(
                ApplicationTrackerResponse::positionTitle,
                query.direction()
            );
            case "status" -> comparingText(
                row -> effectiveStatus(row).name(),
                query.direction()
            );
            case "priority" -> comparing(
                row -> Short.valueOf(row.priority()),
                query.direction()
            );
            case "matchScore" -> comparing(
                ApplicationTrackerResponse::matchScore,
                query.direction()
            );
            case "location" -> comparingText(
                ApplicationTrackerResponse::location,
                query.direction()
            );
            case "remoteType" -> comparingText(
                row -> row.remoteType().name(),
                query.direction()
            );
            case "salaryMin" -> comparing(
                ApplicationTrackerResponse::salaryMin,
                query.direction()
            );
            case "datePosted" -> comparing(
                ApplicationTrackerResponse::datePosted,
                query.direction()
            );
            case "applicationDate" -> comparing(
                ApplicationTrackerResponse::applicationDate,
                query.direction()
            );
            case "followUpDate" -> comparing(
                ApplicationTrackerResponse::followUpDate,
                query.direction()
            );
            case "source" -> comparingText(
                ApplicationTrackerResponse::source,
                query.direction()
            );
            case "createdAt" -> comparing(
                ApplicationTrackerResponse::jobCreatedAt,
                query.direction()
            );
            default -> throw new IllegalStateException();
        };

        if (query.sort().equals("priority")) {
            primary = primary.thenComparing(
                comparing(
                    ApplicationTrackerResponse::jobCreatedAt,
                    "desc"
                )
            );
        }

        return primary.thenComparing(
            ApplicationTrackerResponse::jobOpportunityId,
            Comparator.reverseOrder()
        );
    }

    private ApplicationStatus effectiveStatus(
        ApplicationTrackerResponse row
    ) {
        return row.status() == null ? ApplicationStatus.SAVED : row.status();
    }

    private Comparator<ApplicationTrackerResponse> comparingText(
        Function<ApplicationTrackerResponse, String> extractor,
        String direction
    ) {
        return comparing(
            row -> {
                String value = extractor.apply(row);
                return value == null ? null : value.toLowerCase(Locale.ROOT);
            },
            direction
        );
    }

    private <T extends Comparable<? super T>>
        Comparator<ApplicationTrackerResponse> comparing(
            Function<ApplicationTrackerResponse, T> extractor,
            String direction
        ) {
        Comparator<T> values = direction.equals("desc")
            ? Comparator.reverseOrder()
            : Comparator.naturalOrder();

        return Comparator.comparing(
            extractor,
            Comparator.nullsLast(values)
        );
    }
}
