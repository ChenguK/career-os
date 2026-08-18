package com.chengukargbo.careeros.applications.export;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.chengukargbo.careeros.applications.tracker.ApplicationTrackerQuery;

@WebMvcTest(ApplicationTrackerExportController.class)
class ApplicationTrackerExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApplicationTrackerExportService exportService;

    @Test
    void returnsUtf8CsvAttachmentAndAcceptsCanonicalQueryParameters() throws Exception {
        when(exportService.export(eq(ApplicationTrackerExportMode.CURRENT_VIEW), any()))
            .thenReturn(new ApplicationTrackerCsvExport(
                "job_id\r\n1\r\n".getBytes(),
                "careeros-applications-current-view-2026-08-10.csv"
            ));

        mockMvc.perform(get("/api/applications/tracker/export.csv")
                .param("search", "platform")
                .param("statuses", "APPLIED")
                .param("priorities", "1")
                .param("remoteTypes", "HYBRID")
                .param("companyId", "9")
                .param("applicationDateFrom", "2026-08-01")
                .param("datePostedTo", "2026-08-31")
                .param("followUpDateFrom", "2026-09-01")
                .param("sort", "company")
                .param("direction", "desc")
                .param("page", "99")
                .param("size", "1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/csv;charset=UTF-8"))
            .andExpect(header().string(
                "Content-Disposition",
                "attachment; filename=\"careeros-applications-current-view-2026-08-10.csv\""
            ));

        verify(exportService).export(
            eq(ApplicationTrackerExportMode.CURRENT_VIEW),
            any(ApplicationTrackerQuery.class)
        );
    }

    @Test
    void returnsXlsxAttachmentThroughTheSameCanonicalQueryRoute() throws Exception {
        when(exportService.exportXlsx(eq(ApplicationTrackerExportMode.ALL), any()))
            .thenReturn(new ApplicationTrackerXlsxExport(
                new byte[] { 1, 2, 3 },
                "careeros-applications-2026-08-13.xlsx"
            ));

        mockMvc.perform(get("/api/applications/tracker/export.xlsx")
                .param("mode", "ALL"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            ))
            .andExpect(header().string(
                "Content-Disposition",
                "attachment; filename=\"careeros-applications-2026-08-13.xlsx\""
            ));

        verify(exportService).exportXlsx(
            eq(ApplicationTrackerExportMode.ALL),
            any(ApplicationTrackerQuery.class)
        );
    }
}
