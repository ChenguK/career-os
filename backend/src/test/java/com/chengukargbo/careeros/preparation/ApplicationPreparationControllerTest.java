package com.chengukargbo.careeros.preparation;

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

import com.chengukargbo.careeros.preparation.PreparationDtos.*;
import com.chengukargbo.careeros.preparation.PreparationEnums.*;

@WebMvcTest(ApplicationPreparationController.class)
class ApplicationPreparationControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private ApplicationPreparationService service;

    @Test
    void exposesCapabilitySessionActionsAndImmutableEvents() throws Exception {
        Response empty = new Response(PreparationCapability.SESSION_ONLY, null);
        when(service.get(12L)).thenReturn(empty);
        when(service.initialize(12L)).thenReturn(empty);
        when(service.cancel(12L)).thenReturn(empty);
        when(service.retry(12L)).thenReturn(empty);
        when(service.resume(12L)).thenReturn(empty);
        when(service.events(12L)).thenReturn(List.of(new Event(
            1L, 5L, EventType.SESSION_INITIALIZED,
            OffsetDateTime.parse("2026-08-18T12:00:00Z"), false,
            "Preparation session initialized", null, null
        )));

        mockMvc.perform(get("/api/applications/12/preparation"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.capability").value("SESSION_ONLY"))
            .andExpect(jsonPath("$.session").doesNotExist());
        mockMvc.perform(post("/api/applications/12/preparation/initialize"))
            .andExpect(status().isOk());
        mockMvc.perform(post("/api/applications/12/preparation/cancel"))
            .andExpect(status().isOk());
        mockMvc.perform(post("/api/applications/12/preparation/retry"))
            .andExpect(status().isOk());
        mockMvc.perform(post("/api/applications/12/preparation/resume"))
            .andExpect(status().isOk());
        mockMvc.perform(get("/api/applications/12/preparation/events"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].eventType")
                .value("SESSION_INITIALIZED"))
            .andExpect(jsonPath("$[0].safeUserMessage")
                .value("Preparation session initialized"));

        verify(service).initialize(12L);
        verify(service).cancel(12L);
        verify(service).retry(12L);
        verify(service).resume(12L);
    }
}
