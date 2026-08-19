package com.chengukargbo.careeros.applications;

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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.chengukargbo.careeros.applications.dto.ApplicationRequest;
import com.chengukargbo.careeros.applications.dto.ApplicationResponse;
import com.chengukargbo.careeros.common.exception.BusinessValidationException;
import com.chengukargbo.careeros.applications.lock.ApplicationLockDtos;
import com.chengukargbo.careeros.applications.lock.ApplicationLockState;

@WebMvcTest(ApplicationController.class)
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApplicationService applicationService;

    @MockitoBean
    private ManualSubmissionService manualSubmissionService;

    @Test
    void createsApplication() throws Exception {
        when(
            applicationService.create(
                any(ApplicationRequest.class)
            )
        ).thenReturn(sampleResponse());

        mockMvc.perform(
                post("/api/applications")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validRequestJson())
            )
            .andExpect(status().isCreated())
            .andExpect(
                header().string(
                    "Location",
                    "http://localhost/api/applications/1"
                )
            )
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(
                jsonPath("$.jobOpportunityId").value(1)
            )
            .andExpect(
                jsonPath("$.positionTitle")
                    .value("Junior Software Engineer")
            )
            .andExpect(
                jsonPath("$.companyName").value("GitHub")
            )
            .andExpect(
                jsonPath("$.status").value("APPLIED")
            );
    }

    @Test
    void recordsManualSubmission() throws Exception {
        when(manualSubmissionService.markApplied(eq(1L), eq(LocalDate.of(2026, 8, 10))))
            .thenReturn(new ManualSubmissionDtos.Response(sampleResponse(),
                new ApplicationLockDtos.Response(4L, 1L,
                    ApplicationLockState.SUBMITTED, OffsetDateTime.now(),
                    "User recorded manual application submission",
                    OffsetDateTime.now(), OffsetDateTime.now())));

        mockMvc.perform(post("/api/applications/1/mark-applied")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"applicationDate\":\"2026-08-10\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lock.lockState").value("SUBMITTED"));
        verify(manualSubmissionService).markApplied(1L, LocalDate.of(2026, 8, 10));
    }

    @Test
    void returnsAllApplications() throws Exception {
        when(applicationService.findAll())
            .thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/applications"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(
                jsonPath("$[0].status").value("APPLIED")
            );
    }

    @Test
    void returnsApplicationById() throws Exception {
        when(applicationService.findById(1L))
            .thenReturn(sampleResponse());

        mockMvc.perform(get("/api/applications/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(
                jsonPath("$.resumeVersion")
                    .value("Software Engineering")
            );
    }

    @Test
    void updatesApplication() throws Exception {
        when(
            applicationService.update(
                eq(1L),
                any(ApplicationRequest.class)
            )
        ).thenReturn(sampleResponse());

        mockMvc.perform(
                put("/api/applications/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validRequestJson())
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(
                jsonPath("$.status").value("APPLIED")
            );
    }

    @Test
    void deletesApplication() throws Exception {
        doNothing().when(applicationService).delete(1L);

        mockMvc.perform(delete("/api/applications/1"))
            .andExpect(status().isNoContent());

        verify(applicationService).delete(1L);
    }

    @Test
    void rejectsMissingJobOpportunity() throws Exception {
        mockMvc.perform(
                post("/api/applications")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "status": "SAVED",
                          "coverLetterNeeded": false
                        }
                        """)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(
                jsonPath("$.message")
                    .value("Job opportunity is required")
            );
    }

    @Test
    void returnsNotFoundForMissingApplication()
        throws Exception {

        when(applicationService.findById(999L))
            .thenThrow(
                new ApplicationNotFoundException(999L)
            );

        mockMvc.perform(get("/api/applications/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(
                jsonPath("$.message")
                    .value(
                        "Application not found with id: 999"
                    )
            );
    }

    @Test
    void rejectsDuplicateApplication() throws Exception {
        when(
            applicationService.create(
                any(ApplicationRequest.class)
            )
        ).thenThrow(
            new BusinessValidationException(
                "An application already exists for this job opportunity"
            )
        );

        mockMvc.perform(
                post("/api/applications")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validRequestJson())
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(
                jsonPath("$.message")
                    .value(
                        "An application already exists for this job opportunity"
                    )
            );
    }

    @Test
    void rejectsChangingJobOpportunityDuringUpdate()
        throws Exception {

        when(
            applicationService.update(
                eq(1L),
                any(ApplicationRequest.class)
            )
        ).thenThrow(
            new BusinessValidationException(
                "The job opportunity cannot be changed after an application is created"
            )
        );

        mockMvc.perform(
                put("/api/applications/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validRequestJson())
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void returnsNotFoundWhenDeletingMissingApplication()
        throws Exception {

        doThrow(new ApplicationNotFoundException(999L))
            .when(applicationService)
            .delete(999L);

        mockMvc.perform(delete("/api/applications/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    private String validRequestJson() {
        return """
            {
              "jobOpportunityId": 1,
              "status": "APPLIED",
              "resumeVersion": "Software Engineering",
              "coverLetterNeeded": true,
              "portfolioLink": "https://chengucodes.dev",
              "githubLink": "https://github.com/ChenguK",
              "projectsToHighlight": "Career OS, Working Actor OS",
              "skillsToEmphasize": "Java, Spring Boot, React, PostgreSQL",
              "interviewTopics": "REST APIs, architecture, testing",
              "recruiterName": "Test Recruiter",
              "recruiterEmail": "recruiter@example.com",
              "applicationDate": "2026-08-07",
              "followUpDate": "2026-08-14",
              "notes": "Test application"
            }
            """;
    }

    private ApplicationResponse sampleResponse() {
        OffsetDateTime timestamp =
            OffsetDateTime.parse("2026-08-07T21:30:00Z");

        return new ApplicationResponse(
            1L,
            1L,
            "Junior Software Engineer",
            1L,
            "GitHub",
            ApplicationStatus.APPLIED,
            "Software Engineering",
            true,
            "https://chengucodes.dev",
            "https://github.com/ChenguK",
            "Career OS, Working Actor OS",
            "Java, Spring Boot, React, PostgreSQL",
            "REST APIs, architecture, testing",
            "Test Recruiter",
            "recruiter@example.com",
            LocalDate.parse("2026-08-07"),
            LocalDate.parse("2026-08-14"),
            null,
            null,
            null,
            null,
            null,
            "Test application",
            timestamp,
            timestamp
        );
    }
}
