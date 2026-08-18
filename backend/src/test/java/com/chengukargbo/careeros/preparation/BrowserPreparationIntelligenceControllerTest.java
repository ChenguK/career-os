package com.chengukargbo.careeros.preparation;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.chengukargbo.careeros.questions.QuestionEnums.JobFamily;
import com.chengukargbo.careeros.questions.QuestionEnums.Seniority;

@WebMvcTest(BrowserPreparationIntelligenceController.class)
class BrowserPreparationIntelligenceControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean BrowserPreparationIntelligenceService service;

    @Test
    void exposesReadOnlyPreparationIntelligence() throws Exception {
        when(service.analyze(7L, JobFamily.SOFTWARE_ENGINEER, Seniority.MID_LEVEL, null))
            .thenReturn(new BrowserPreparationIntelligenceDtos.Response(
                7L, 31L, "static-careeros-templates",
                List.of(), List.of(), List.of(), List.of()));

        mvc.perform(get("/api/applications/7/preparation/intelligence")
                .param("jobFamily", "SOFTWARE_ENGINEER")
                .param("seniority", "MID_LEVEL"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.applicationId").value(7))
            .andExpect(jsonPath("$.observationSnapshotId").value(31))
            .andExpect(jsonPath("$.suggestedAnswers").isArray())
            .andExpect(jsonPath("$.preparationGaps").isArray());
        mvc.perform(post("/api/applications/7/preparation/intelligence"))
            .andExpect(status().isMethodNotAllowed());
    }
}
