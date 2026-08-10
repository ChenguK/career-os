package com.chengukargbo.careeros.applications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.chengukargbo.careeros.applications.dto.ApplicationTrackerResponse;
import com.chengukargbo.careeros.applications.tracker.ApplicationTrackerPageResponse;
import com.chengukargbo.careeros.applications.tracker.ApplicationTrackerQuery;
import com.chengukargbo.careeros.applications.tracker.ApplicationTrackerQueryEngine;
import com.chengukargbo.careeros.common.exception.BusinessValidationException;
import com.chengukargbo.careeros.jobs.RemoteType;

class ApplicationTrackerQueryEngineTest {

    private ApplicationTrackerQueryEngine engine;
    private List<ApplicationTrackerResponse> rows;

    @BeforeEach
    void setUp() {
        engine = new ApplicationTrackerQueryEngine();
        rows = List.of(
            row(
                1L,
                10L,
                "GitHub",
                "Platform Engineer",
                "New York, NY",
                "Referral",
                RemoteType.HYBRID,
                (short) 1,
                ApplicationStatus.APPLIED,
                "2026-08-01",
                "2026-08-05",
                "2026-08-10",
                "2026-08-01T10:00:00Z",
                new BigDecimal("9.0")
            ),
            row(
                2L,
                20L,
                "Acme Health",
                "Support Specialist",
                "Boston, MA",
                "LinkedIn",
                RemoteType.REMOTE,
                (short) 2,
                ApplicationStatus.PHONE_SCREEN,
                "2026-08-02",
                "2026-08-06",
                "2026-08-11",
                "2026-08-02T10:00:00Z",
                new BigDecimal("7.5")
            ),
            row(
                3L,
                null,
                null,
                "Operations Engineer",
                null,
                null,
                RemoteType.UNKNOWN,
                (short) 1,
                null,
                null,
                null,
                null,
                "2026-08-01T10:00:00Z",
                null
            )
        );
    }

    @Test
    void searchesSupportedFieldsCaseInsensitivelyAndIgnoresBlankSearch() {
        assertIds(execute(search("github")), 1L);
        assertIds(execute(search("PLATFORM")), 1L);
        assertIds(execute(search("boston")), 2L);
        assertIds(execute(search("linkedIN")), 2L);
        assertThat(execute(search("   ")).totalRows()).isEqualTo(3);
    }

    @Test
    void filtersSingleAndMultipleStatusesIncludingJobOnlyRowsAsSaved() {
        assertIds(execute(statuses(ApplicationStatus.APPLIED)), 1L);
        assertIds(
            execute(statuses(
                ApplicationStatus.APPLIED,
                ApplicationStatus.PHONE_SCREEN
            )),
            1L,
            2L
        );
        assertIds(execute(statuses(ApplicationStatus.SAVED)), 3L);
    }

    @Test
    void filtersPriorityRemoteTypeAndCompanyAndCombinesFilters() {
        assertIds(execute(priorities((short) 1)), 3L, 1L);
        assertIds(execute(remoteTypes(RemoteType.REMOTE)), 2L);
        assertIds(execute(company(10L)), 1L);

        ApplicationTrackerQuery combined = new ApplicationTrackerQuery(
            "engineer",
            List.of(ApplicationStatus.APPLIED),
            List.of((short) 1),
            List.of(RemoteType.HYBRID),
            10L,
            null,
            null,
            null,
            null,
            null,
            null,
            "priority",
            "asc",
            0,
            25
        );
        assertIds(execute(combined), 1L);
    }

    @Test
    void appliesInclusiveApplicationPostedAndFollowUpDateRanges() {
        assertIds(
            execute(dates(
                "2026-08-05", "2026-08-05",
                null, null,
                null, null
            )),
            1L
        );
        assertIds(
            execute(dates(
                null, null,
                "2026-08-02", "2026-08-02",
                null, null
            )),
            2L
        );
        assertIds(
            execute(dates(
                null, null,
                null, null,
                "2026-08-10", "2026-08-10"
            )),
            1L
        );
    }

    @Test
    void sortsSupportedFieldsInBothDirectionsWithDeterministicTies() {
        assertIds(execute(sort("company", "asc")), 2L, 1L, 3L);
        assertIds(execute(sort("positionTitle", "desc")), 2L, 1L, 3L);
        assertIds(execute(sort("status", "asc")), 1L, 2L, 3L);
        assertIds(execute(sort("matchScore", "desc")), 1L, 2L, 3L);
        assertIds(execute(sort("location", "asc")), 2L, 1L, 3L);
        assertIds(execute(sort("remoteType", "asc")), 1L, 2L, 3L);
        assertIds(execute(sort("salaryMin", "desc")), 2L, 1L, 3L);
        assertIds(execute(sort("datePosted", "asc")), 1L, 2L, 3L);
        assertIds(execute(sort("applicationDate", "desc")), 2L, 1L, 3L);
        assertIds(execute(sort("followUpDate", "asc")), 1L, 2L, 3L);
        assertIds(execute(sort("source", "desc")), 1L, 2L, 3L);
        assertIds(execute(sort("createdAt", "desc")), 2L, 3L, 1L);

        assertIds(execute(sort("priority", "asc")), 3L, 1L, 2L);
        assertIds(execute(sort("priority", "desc")), 2L, 3L, 1L);
    }

    @Test
    void rejectsUnsupportedSorting() {
        assertThatThrownBy(() -> execute(sort("notes", "asc")))
            .isInstanceOf(BusinessValidationException.class);
        assertThatThrownBy(() -> execute(sort("priority", "sideways")))
            .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void returnsStablePaginationMetadataWithoutDuplicateRows() {
        ApplicationTrackerPageResponse first = execute(page(0, 2));
        ApplicationTrackerPageResponse second = execute(page(1, 2));

        assertThat(first.page()).isZero();
        assertThat(first.size()).isEqualTo(2);
        assertThat(first.totalRows()).isEqualTo(3);
        assertThat(first.totalPages()).isEqualTo(2);
        assertIds(first, 3L, 1L);
        assertIds(second, 2L);
        assertThat(first.content())
            .extracting(ApplicationTrackerResponse::jobOpportunityId)
            .doesNotContainAnyElementsOf(
                second.content().stream()
                    .map(ApplicationTrackerResponse::jobOpportunityId)
                    .toList()
            );
    }

    @Test
    void normalizesInvalidPageAndEnforcesMaximumPageSize() {
        ApplicationTrackerQuery query = page(-2, 500);
        ApplicationTrackerPageResponse result = execute(query);

        assertThat(result.page()).isZero();
        assertThat(result.size()).isEqualTo(100);
    }

    private ApplicationTrackerPageResponse execute(
        ApplicationTrackerQuery query
    ) {
        return engine.execute(rows, query);
    }

    private ApplicationTrackerQuery search(String value) {
        return query(value, List.of(), List.of(), List.of(), null);
    }

    private ApplicationTrackerQuery statuses(
        ApplicationStatus... values
    ) {
        return query(null, List.of(values), List.of(), List.of(), null);
    }

    private ApplicationTrackerQuery priorities(Short... values) {
        return query(null, List.of(), List.of(values), List.of(), null);
    }

    private ApplicationTrackerQuery remoteTypes(RemoteType... values) {
        return query(null, List.of(), List.of(), List.of(values), null);
    }

    private ApplicationTrackerQuery company(Long companyId) {
        return query(null, List.of(), List.of(), List.of(), companyId);
    }

    private ApplicationTrackerQuery query(
        String search,
        List<ApplicationStatus> statuses,
        List<Short> priorities,
        List<RemoteType> remoteTypes,
        Long companyId
    ) {
        return new ApplicationTrackerQuery(
            search,
            statuses,
            priorities,
            remoteTypes,
            companyId,
            null, null, null, null, null, null,
            "priority", "asc", 0, 25
        );
    }

    private ApplicationTrackerQuery dates(
        String applicationFrom,
        String applicationTo,
        String postedFrom,
        String postedTo,
        String followUpFrom,
        String followUpTo
    ) {
        return new ApplicationTrackerQuery(
            null,
            List.of(),
            List.of(),
            List.of(),
            null,
            date(applicationFrom),
            date(applicationTo),
            date(postedFrom),
            date(postedTo),
            date(followUpFrom),
            date(followUpTo),
            "priority",
            "asc",
            0,
            25
        );
    }

    private ApplicationTrackerQuery sort(String field, String direction) {
        return new ApplicationTrackerQuery(
            null, List.of(), List.of(), List.of(), null,
            null, null, null, null, null, null,
            field, direction, 0, 25
        );
    }

    private ApplicationTrackerQuery page(int page, int size) {
        return new ApplicationTrackerQuery(
            null, List.of(), List.of(), List.of(), null,
            null, null, null, null, null, null,
            "priority", "asc", page, size
        );
    }

    private LocalDate date(String value) {
        return value == null ? null : LocalDate.parse(value);
    }

    private void assertIds(
        ApplicationTrackerPageResponse response,
        Long... ids
    ) {
        assertThat(response.content())
            .extracting(ApplicationTrackerResponse::jobOpportunityId)
            .containsExactly(ids);
    }

    private ApplicationTrackerResponse row(
        Long jobId,
        Long companyId,
        String companyName,
        String title,
        String location,
        String source,
        RemoteType remoteType,
        short priority,
        ApplicationStatus status,
        String datePosted,
        String applicationDate,
        String followUpDate,
        String createdAt,
        BigDecimal matchScore
    ) {
        return new ApplicationTrackerResponse(
            jobId,
            companyId,
            companyName,
            title,
            null,
            location,
            remoteType,
            null,
            matchScore == null ? null : new BigDecimal("90000"),
            null,
            "USD",
            null,
            null,
            source,
            date(datePosted),
            null,
            priority,
            matchScore,
            null,
            null,
            OffsetDateTime.parse(createdAt),
            OffsetDateTime.parse(createdAt),
            status == null ? null : jobId + 100,
            status,
            null,
            status == null ? null : false,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            date(applicationDate),
            date(followUpDate),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );
    }
}
