package com.chengukargbo.careeros.applications;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.chengukargbo.careeros.applications.dto.ApplicationTrackerResponse;
import com.chengukargbo.careeros.applications.tracker.ApplicationTrackerPageResponse;
import com.chengukargbo.careeros.applications.tracker.ApplicationTrackerQuery;

@WebMvcTest(ApplicationTrackerController.class)
class ApplicationTrackerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApplicationTrackerService trackerService;

    @Test
    void returnsTrackerRows() throws Exception {
        ApplicationTrackerResponse response = response();

        when(trackerService.findAll(any(ApplicationTrackerQuery.class)))
            .thenReturn(new ApplicationTrackerPageResponse(
                List.of(response),
                0,
                25,
                1,
                1
            ));

        mockMvc.perform(get("/api/applications/tracker"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].jobOpportunityId").value(1))
            .andExpect(
                jsonPath("$.content[0].positionTitle")
                    .value("Platform Engineer")
            )
            .andExpect(jsonPath("$.content[0].jobNotes").value("job notes"))
            .andExpect(jsonPath("$.content[0].applicationId").isEmpty())
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(25))
            .andExpect(jsonPath("$.totalRows").value(1))
            .andExpect(jsonPath("$.totalPages").value(1));

        verify(trackerService).findAll(any(ApplicationTrackerQuery.class));
    }

    @Test
    void returnsOneCanonicalTrackerRowByJobOpportunityId() throws Exception {
        when(trackerService.findByJobOpportunityId(41L))
            .thenReturn(response());

        mockMvc.perform(get("/api/applications/tracker/jobs/41"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.jobOpportunityId").value(1))
            .andExpect(jsonPath("$.positionTitle").value("Platform Engineer"));

        verify(trackerService).findByJobOpportunityId(41L);
    }

    private ApplicationTrackerResponse response() {
        return new ApplicationTrackerResponse(
                1L,
                null,
                null,
                "Platform Engineer",
                null,
                null,
                null,
                null,
                null,
                null,
                "USD",
                null,
                null,
                null,
                null,
                null,
                (short) 3,
                null,
                null,
                "job notes",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
            );
    }
}
