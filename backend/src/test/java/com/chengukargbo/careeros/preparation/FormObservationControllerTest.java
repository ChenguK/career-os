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

@WebMvcTest(FormObservationController.class)
class FormObservationControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private FormObservationService service;

    @Test
    void exposesReadOnlySnapshotAndQuestionRoutes() throws Exception {
        when(service.snapshots(12L)).thenReturn(List.of());
        when(service.latestQuestions(12L)).thenReturn(List.of());
        when(service.snapshotQuestions(12L, 4L)).thenReturn(List.of());

        mockMvc.perform(get("/api/applications/12/preparation/observations/snapshots"))
            .andExpect(status().isOk()).andExpect(content().json("[]"));
        mockMvc.perform(get("/api/applications/12/preparation/observations/questions"))
            .andExpect(status().isOk()).andExpect(content().json("[]"));
        mockMvc.perform(get("/api/applications/12/preparation/observations/snapshots/4/questions"))
            .andExpect(status().isOk()).andExpect(content().json("[]"));
        mockMvc.perform(post("/api/applications/12/preparation/observations/snapshots"))
            .andExpect(status().isMethodNotAllowed());
    }
}
