package com.chengukargbo.careeros.importing.csv;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import com.chengukargbo.careeros.common.exception.BusinessValidationException;
import com.chengukargbo.careeros.importing.persistence.ImportPersistenceService;
import com.chengukargbo.careeros.importing.persistence.ImportPersistenceRequest;
import com.chengukargbo.careeros.importing.persistence.ImportPersistenceResponse;
import com.chengukargbo.careeros.importing.persistence.ImportRowOutcomeStatus;
import com.chengukargbo.careeros.importing.persistence.ImportRowPersistenceResult;
import com.chengukargbo.careeros.importing.xlsx.XlsxImportPreviewService;

@WebMvcTest(CsvImportController.class)
class CsvImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CsvImportPreviewService previewService;

    @MockitoBean
    private XlsxImportPreviewService xlsxPreviewService;

    @MockitoBean
    private ImportPersistenceService persistenceService;

    @Test
    void returnsReadOnlyCsvPreview() throws Exception {
        when(previewService.preview(any(MultipartFile.class))).thenReturn(
            new ImportPreviewResponse(
                "jobs.csv", 4, 1, 1, 1, 1,
                false, false, List.of(), List.of(), List.of()
            )
        );

        mockMvc.perform(multipart("/api/applications/import/preview")
                .file(new MockMultipartFile(
                    "file", "jobs.csv", "text/csv",
                    "Job Title\nEngineer\n".getBytes()
                )))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.filename").value("jobs.csv"))
            .andExpect(jsonPath("$.totalRows").value(4))
            .andExpect(jsonPath("$.createCount").value(1))
            .andExpect(jsonPath("$.reviewCount").value(1))
            .andExpect(jsonPath("$.duplicateCount").value(1))
            .andExpect(jsonPath("$.invalidCount").value(1));
    }

    @Test
    void routesXlsxThroughTheSameReadOnlyPreviewContract() throws Exception {
        when(xlsxPreviewService.preview(any(MultipartFile.class))).thenReturn(
            new ImportPreviewResponse(
                "jobs.xlsx", 1, 1, 0, 0, 0,
                false, false, List.of(), List.of(), List.of()
            )
        );

        mockMvc.perform(multipart("/api/applications/import/preview")
                .file(new MockMultipartFile(
                    "file", "jobs.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    new byte[] { 1, 2, 3 }
                )))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.filename").value("jobs.xlsx"))
            .andExpect(jsonPath("$.createCount").value(1));
    }

    @Test
    void returnsClearValidationResponse() throws Exception {
        when(previewService.preview(any(MultipartFile.class)))
            .thenThrow(new BusinessValidationException("CSV file is empty"));

        mockMvc.perform(multipart("/api/applications/import/preview")
                .file(new MockMultipartFile(
                    "file", "jobs.csv", "text/csv", new byte[0]
                )))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("CSV file is empty"));
    }

    @Test
    void persistsSelectedRowsThroughTheBulkEndpoint() throws Exception {
        when(persistenceService.persist(any(ImportPersistenceRequest.class)))
            .thenReturn(new ImportPersistenceResponse(
                7L, "jobs.csv", 4, 1, 1, 0, 0, 0,
                List.of(new ImportRowPersistenceResult(
                    2, ImportRowOutcomeStatus.CREATED,
                    10L, 20L, 30L, null, List.of(), List.of()
                ))
            ));

        mockMvc.perform(post("/api/applications/import")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "filename": "jobs.csv",
                      "totalRows": 4,
                      "rows": [{
                        "rowNumber": 2,
                        "fields": {"position_title": "Engineer"}
                      }]
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.batchId").value(7))
            .andExpect(jsonPath("$.created").value(1))
            .andExpect(jsonPath("$.rows[0].jobOpportunityId").value(20));
    }
}
