package com.chengukargbo.careeros.applications.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.chengukargbo.careeros.applications.ApplicationTrackerService;
import com.chengukargbo.careeros.applications.dto.ApplicationTrackerResponse;
import com.chengukargbo.careeros.applications.tracker.ApplicationTrackerQuery;
import com.chengukargbo.careeros.applications.tracker.ApplicationTrackerQueryEngine;
import com.chengukargbo.careeros.common.exception.BusinessValidationException;
import com.chengukargbo.careeros.jobs.RemoteType;

class ApplicationTrackerExportServiceTest {

    private final ApplicationTrackerService trackerService = mock(
        ApplicationTrackerService.class
    );
    private final ApplicationTrackerExportService service =
        new ApplicationTrackerExportService(
            trackerService,
            new ApplicationTrackerQueryEngine(),
            new ApplicationTrackerCsvSerializer(),
            new ApplicationTrackerXlsxSerializer()
        );

    @Test
    void currentViewUsesCriteriaAndSortButNotPagination() {
        when(trackerService.findAll()).thenReturn(List.of(
            row(1L, "Alpha", (short) 2),
            row(2L, "Beta Engineer", (short) 1),
            row(3L, "Gamma Engineer", (short) 1)
        ));
        ApplicationTrackerQuery query = new ApplicationTrackerQuery(
            "engineer", List.of(), List.of((short) 1),
            List.of(RemoteType.REMOTE), null,
            null, null, null, null, null, null,
            "positionTitle", "desc", 6, 1
        );

        String csv = text(service.export(ApplicationTrackerExportMode.CURRENT_VIEW, query));

        assertThat(csv).contains("Gamma Engineer").contains("Beta Engineer");
        assertThat(csv.indexOf("Gamma Engineer"))
            .isLessThan(csv.indexOf("Beta Engineer"));
    }

    @Test
    void allIgnoresCriteriaAndUsesDefaultDeterministicOrdering() {
        when(trackerService.findAll()).thenReturn(List.of(
            row(1L, "Alpha", (short) 2),
            row(2L, "Beta", (short) 1)
        ));
        ApplicationTrackerQuery filtered = new ApplicationTrackerQuery(
            "missing", null, null, null, null,
            null, null, null, null, null, null,
            "company", "desc", 0, 25
        );

        ApplicationTrackerCsvExport export = service.export(
            ApplicationTrackerExportMode.ALL,
            filtered
        );

        assertThat(text(export)).contains("Alpha").contains("Beta");
        assertThat(text(export).indexOf("Beta"))
            .isLessThan(text(export).indexOf("Alpha"));
        assertThat(export.filename()).doesNotContain("current-view");
    }

    @Test
    void rejectsExportsOverTheV1Limit() {
        when(trackerService.findAll()).thenReturn(Collections.nCopies(
            ApplicationTrackerExportService.MAX_EXPORT_ROWS + 1,
            row(1L, "Repeated", (short) 1)
        ));

        assertThatThrownBy(() -> service.export(
            ApplicationTrackerExportMode.ALL,
            query()
        )).isInstanceOf(BusinessValidationException.class)
            .hasMessageContaining("10000 row limit");
    }

    private String text(ApplicationTrackerCsvExport export) {
        return new String(export.content(), StandardCharsets.UTF_8);
    }

    private ApplicationTrackerQuery query() {
        return new ApplicationTrackerQuery(
            null, null, null, null, null, null, null, null, null,
            null, null, "priority", "asc", 0, 25
        );
    }

    private ApplicationTrackerResponse row(Long id, String title, short priority) {
        OffsetDateTime created = OffsetDateTime.parse("2026-08-01T10:00:00Z");
        return new ApplicationTrackerResponse(
            id, null, null, title, null, null, RemoteType.REMOTE, null,
            null, null, "USD", null, null, null, null, null, priority,
            null, null, null, created, created, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null
        );
    }
}
