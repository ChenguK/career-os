package com.chengukargbo.careeros.preparation;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.chengukargbo.careeros.preparation.ObservationDtos.SnapshotResponse;
import com.chengukargbo.careeros.preparation.PreparationDtos.Response;
import com.chengukargbo.careeros.preparation.PreparationEnums.PreparationCapability;
import com.chengukargbo.careeros.preparation.FieldPreparationDtos.*;

@WebMvcTest(PreparationWorkerController.class)
class PreparationWorkerControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private PreparationWorkerService service;
    @MockitoBean private ApprovedFieldPlanService fieldPlans;
    @MockitoBean private PreparationReviewService reviews;

    @Test
    void exposesOnlyActionOrientedWorkerCommands() throws Exception {
        Response response = new Response(PreparationCapability.INSPECTION, null);
        when(service.opening(12L, 7L)).thenReturn(response);
        when(service.collectingQuestions(12L, 7L)).thenReturn(response);
        when(service.observations(eq(12L), eq(7L), any())).thenReturn(
            new SnapshotResponse(3L, 7L, null, 1, "a".repeat(64),
                OffsetDateTime.now(), 0, 0)
        );
        when(service.failed(eq(12L), eq(7L), any())).thenReturn(response);
        when(service.pause(eq(12L), eq(7L), any())).thenReturn(response);
        PlanResponse plan = new PlanResponse(9L, 7L, OffsetDateTime.now(), List.of());
        when(fieldPlans.create(12L, 7L)).thenReturn(plan);
        when(fieldPlans.get(12L, 7L)).thenReturn(plan);
        when(fieldPlans.record(eq(12L), eq(7L), any())).thenReturn(
            new ResultsResponse(9L, 0, 0, 0, OffsetDateTime.now()));

        String root = "/api/applications/12/preparation/sessions/7";
        mockMvc.perform(post(root + "/opening"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.capability").value("INSPECTION"));
        mockMvc.perform(post(root + "/collecting-questions"))
            .andExpect(status().isOk());
        mockMvc.perform(post(root + "/observations")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"questions\":[]}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.preparationSessionId").value(7));
        mockMvc.perform(post(root + "/failed")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"safeUserMessage\":\"Inspection failed\",\"retryable\":true}"))
            .andExpect(status().isOk());
        mockMvc.perform(post(root + "/pause")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"checkpoint\":\"field:email\"}"))
            .andExpect(status().isOk());
        mockMvc.perform(post(root + "/field-plan"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(9));
        mockMvc.perform(get(root + "/field-plan"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.fields").isArray());
        mockMvc.perform(post(root + "/field-results")
                .contentType(MediaType.APPLICATION_JSON).content("{\"results\":[]}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.preparedCount").value(0));
        mockMvc.perform(post(root + "/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"screenshots\":[{\"reference\":\"reviews/7/page.png\",\"capturedAt\":\"2026-08-18T20:00:00Z\"}]}"))
            .andExpect(status().isOk());
    }
}
