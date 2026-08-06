package com.chengukargbo.careeros.jobs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.chengukargbo.careeros.common.exception.BusinessValidationException;
import com.chengukargbo.careeros.jobs.dto.JobOpportunityRequest;
import com.chengukargbo.careeros.jobs.dto.JobOpportunityResponse;

@WebMvcTest(JobOpportunityController.class)
class JobOpportunityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobOpportunityService jobService;

    @Test
    void createsJobOpportunity() throws Exception {
        when(jobService.create(any(JobOpportunityRequest.class)))
            .thenReturn(sampleResponse());

        mockMvc.perform(
                post("/api/jobs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validRequestJson())
            )
            .andExpect(status().isCreated())
            .andExpect(header().string(
                "Location",
                "http://localhost/api/jobs/1"
            ))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.companyName").value("GitHub"))
            .andExpect(
                jsonPath("$.positionTitle")
                    .value("Software Engineer")
            );
    }

    @Test
    void returnsAllJobs() throws Exception {
        when(jobService.search(null))
            .thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/jobs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(
                jsonPath("$[0].positionTitle")
                    .value("Software Engineer")
            );
    }

    @Test
    void searchesJobs() throws Exception {
        when(jobService.search("engineer"))
            .thenReturn(List.of(sampleResponse()));

        mockMvc.perform(
                get("/api/jobs")
                    .queryParam("search", "engineer")
            )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$[0].positionTitle")
                    .value("Software Engineer")
            );
    }

    @Test
    void returnsJobById() throws Exception {
        when(jobService.findById(1L))
            .thenReturn(sampleResponse());

        mockMvc.perform(get("/api/jobs/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updatesJob() throws Exception {
        when(jobService.update(
            eq(1L),
            any(JobOpportunityRequest.class)
        )).thenReturn(sampleResponse());

        mockMvc.perform(
                put("/api/jobs/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validRequestJson())
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deletesJob() throws Exception {
        doNothing().when(jobService).delete(1L);

        mockMvc.perform(delete("/api/jobs/1"))
            .andExpect(status().isNoContent());

        verify(jobService).delete(1L);
    }

    @Test
    void rejectsBlankPositionTitle() throws Exception {
        mockMvc.perform(
                post("/api/jobs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "positionTitle": "   "
                        }
                        """)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(
                jsonPath("$.message")
                    .value("Position title is required")
            );
    }

    @Test
    void returnsNotFoundForMissingJob() throws Exception {
        when(jobService.findById(999L))
            .thenThrow(
                new JobOpportunityNotFoundException(999L)
            );

        mockMvc.perform(get("/api/jobs/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(
                jsonPath("$.message")
                    .value(
                        "Job opportunity not found with id: 999"
                    )
            );
    }

    @Test
    void returnsBadRequestForInvalidBusinessRule()
        throws Exception {

        when(jobService.create(any(JobOpportunityRequest.class)))
            .thenThrow(
                new BusinessValidationException(
                    "Maximum salary must be greater than or equal to minimum salary"
                )
            );

        mockMvc.perform(
                post("/api/jobs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validRequestJson())
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(
                jsonPath("$.message")
                    .value(
                        "Maximum salary must be greater than or equal to minimum salary"
                    )
            );
    }

    @Test
    void returnsNotFoundWhenDeletingMissingJob()
        throws Exception {

        doThrow(new JobOpportunityNotFoundException(999L))
            .when(jobService)
            .delete(999L);

        mockMvc.perform(delete("/api/jobs/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    private String validRequestJson() {
        return """
            {
              "companyId": 1,
              "positionTitle": "Software Engineer",
              "remoteType": "REMOTE",
              "priority": 1,
              "matchScore": 8.7
            }
            """;
    }

    private JobOpportunityResponse sampleResponse() {
        OffsetDateTime timestamp =
            OffsetDateTime.parse("2026-08-06T18:44:00Z");

        return new JobOpportunityResponse(
            1L,
            1L,
            "GitHub",
            "Software Engineer",
            "Engineering",
            "Remote",
            RemoteType.REMOTE,
            "Full-time",
            new BigDecimal("90000.00"),
            new BigDecimal("120000.00"),
            "USD",
            "Verify range before applying",
            "https://example.com/jobs/software-engineer",
            "Company website",
            LocalDate.parse("2026-08-06"),
            null,
            (short) 1,
            new BigDecimal("8.7"),
            "Build and maintain full-stack software.",
            "Highlight Working Actor OS and DevCommands.",
            timestamp,
            timestamp
        );
    }
}