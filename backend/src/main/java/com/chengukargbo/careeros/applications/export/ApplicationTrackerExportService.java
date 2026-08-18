package com.chengukargbo.careeros.applications.export;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chengukargbo.careeros.applications.ApplicationTrackerService;
import com.chengukargbo.careeros.applications.dto.ApplicationTrackerResponse;
import com.chengukargbo.careeros.applications.tracker.ApplicationTrackerQuery;
import com.chengukargbo.careeros.applications.tracker.ApplicationTrackerQueryEngine;
import com.chengukargbo.careeros.common.exception.BusinessValidationException;

@Service
@Transactional(readOnly = true)
public class ApplicationTrackerExportService {

    public static final int MAX_EXPORT_ROWS = 10_000;

    private final ApplicationTrackerService trackerService;
    private final ApplicationTrackerQueryEngine queryEngine;
    private final ApplicationTrackerCsvSerializer serializer;
    private final ApplicationTrackerXlsxSerializer xlsxSerializer;

    public ApplicationTrackerExportService(
        ApplicationTrackerService trackerService,
        ApplicationTrackerQueryEngine queryEngine,
        ApplicationTrackerCsvSerializer serializer,
        ApplicationTrackerXlsxSerializer xlsxSerializer
    ) {
        this.trackerService = trackerService;
        this.queryEngine = queryEngine;
        this.serializer = serializer;
        this.xlsxSerializer = xlsxSerializer;
    }

    public ApplicationTrackerXlsxExport exportXlsx(
        ApplicationTrackerExportMode mode,
        ApplicationTrackerQuery currentViewQuery
    ) {
        List<ApplicationTrackerResponse> rows = exportRows(mode, currentViewQuery);
        String qualifier = mode == ApplicationTrackerExportMode.CURRENT_VIEW
            ? "-current-view"
            : "";
        return new ApplicationTrackerXlsxExport(
            xlsxSerializer.serialize(rows),
            "careeros-applications" + qualifier + "-" + LocalDate.now() + ".xlsx"
        );
    }

    private List<ApplicationTrackerResponse> exportRows(
        ApplicationTrackerExportMode mode,
        ApplicationTrackerQuery currentViewQuery
    ) {
        ApplicationTrackerQuery effectiveQuery = mode == ApplicationTrackerExportMode.ALL
            ? defaultQuery()
            : currentViewQuery;
        List<ApplicationTrackerResponse> rows = queryEngine.executeAll(
            trackerService.findAll(), effectiveQuery
        );
        if (rows.size() > MAX_EXPORT_ROWS) {
            throw new BusinessValidationException(
                "Tracker export exceeds the " + MAX_EXPORT_ROWS + " row limit"
            );
        }
        return rows;
    }

    public ApplicationTrackerCsvExport export(
        ApplicationTrackerExportMode mode,
        ApplicationTrackerQuery currentViewQuery
    ) {
        List<ApplicationTrackerResponse> rows = exportRows(mode, currentViewQuery);

        String qualifier = mode == ApplicationTrackerExportMode.CURRENT_VIEW
            ? "-current-view"
            : "";
        String filename = "careeros-applications" + qualifier + "-"
            + LocalDate.now() + ".csv";
        return new ApplicationTrackerCsvExport(serializer.serialize(rows), filename);
    }

    private ApplicationTrackerQuery defaultQuery() {
        return new ApplicationTrackerQuery(
            null, null, null, null, null, null, null, null, null,
            null, null, "priority", "asc", 0,
            ApplicationTrackerQuery.DEFAULT_SIZE
        );
    }
}
