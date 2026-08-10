package com.chengukargbo.careeros.importing.history;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.chengukargbo.careeros.importing.persistence.ImportRowOutcomeStatus;

@WebMvcTest(ImportHistoryController.class)
class ImportHistoryControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private ImportHistoryService historyService;

    @Test
    void returnsPagedHistory() throws Exception {
        when(historyService.findHistory(0, 25)).thenReturn(
            new ImportHistoryPage<>(List.of(summary()), 0, 25, 1, 1)
        );
        mockMvc.perform(get("/api/applications/imports"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].batchId").value(7))
            .andExpect(jsonPath("$.content[0].filename").value("jobs.csv"))
            .andExpect(jsonPath("$.totalRows").value(1));
        verify(historyService).findHistory(0, 25);
    }

    @Test
    void returnsOneBatchWithoutPerRowRequests() throws Exception {
        ImportBatchRowResponse row = new ImportBatchRowResponse(
            2, ImportRowOutcomeStatus.CREATED,
            1L, 2L, 3L, null, List.of(), List.of()
        );
        when(historyService.findBatch(7L, 0, 25)).thenReturn(
            new ImportBatchDetailResponse(
                summary(),
                new ImportHistoryPage<>(List.of(row), 0, 25, 1, 1)
            )
        );
        mockMvc.perform(get("/api/applications/imports/7"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.batch.batchId").value(7))
            .andExpect(jsonPath("$.rows.content[0].rowNumber").value(2))
            .andExpect(jsonPath("$.rows.content[0].jobOpportunityId").value(2));
        verify(historyService).findBatch(7L, 0, 25);
    }

    @Test
    void returnsStandardNotFoundResponse() throws Exception {
        when(historyService.findBatch(404L, 0, 25))
            .thenThrow(new ImportBatchNotFoundException(404L));
        mockMvc.perform(get("/api/applications/imports/404"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message")
                .value("Import batch not found with id: 404"));
    }

    private ImportBatchSummaryResponse summary() {
        OffsetDateTime now = OffsetDateTime.parse("2026-08-10T12:00:00Z");
        return new ImportBatchSummaryResponse(
            7L, "jobs.csv", "CSV", "careeros_job_import_v1",
            now.minusMinutes(1), now, 4, 3, 1, 1, 1, 0
        );
    }
}
