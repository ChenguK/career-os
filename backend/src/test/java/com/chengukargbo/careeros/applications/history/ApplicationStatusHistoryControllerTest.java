package com.chengukargbo.careeros.applications.history;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.chengukargbo.careeros.applications.*;

@WebMvcTest(ApplicationStatusHistoryController.class)
class ApplicationStatusHistoryControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean ApplicationStatusHistoryService service;

    @Test void returnsDeterministicallyOrderedHistory() throws Exception {
        OffsetDateTime first = OffsetDateTime.parse("2026-08-18T10:00:00Z");
        when(service.findForApplication(7L)).thenReturn(List.of(
            new ApplicationStatusHistoryResponse(1L, 7L, null,
                ApplicationStatus.SAVED, first, ApplicationTransitionSource.USER,
                null, first),
            new ApplicationStatusHistoryResponse(2L, 7L, ApplicationStatus.SAVED,
                ApplicationStatus.APPLIED, first.plusHours(1),
                ApplicationTransitionSource.USER, "Submitted", first.plusHours(1))));
        mockMvc.perform(get("/api/applications/7/history"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].previousStatus").doesNotExist())
            .andExpect(jsonPath("$[0].newStatus").value("SAVED"))
            .andExpect(jsonPath("$[1].note").value("Submitted"));
    }

    @Test void hasNoClientAuthoredWriteEndpoint() throws Exception {
        mockMvc.perform(post("/api/applications/7/history"))
            .andExpect(status().isMethodNotAllowed());
    }

    @Test void missingApplicationUsesStandardNotFound() throws Exception {
        when(service.findForApplication(99L))
            .thenThrow(new ApplicationNotFoundException(99L));
        mockMvc.perform(get("/api/applications/99/history"))
            .andExpect(status().isNotFound());
    }
}
