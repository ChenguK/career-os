package com.chengukargbo.careeros.profile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.chengukargbo.careeros.jobs.RemoteType;
import com.chengukargbo.careeros.profile.dto.ApplicantProfileRequest;
import com.chengukargbo.careeros.profile.dto.ApplicantProfileResponse;

@WebMvcTest(ApplicantProfileController.class)
class ApplicantProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApplicantProfileService profileService;

    @Test
    void readsTheCurrentProfile() throws Exception {
        when(profileService.findCurrent()).thenReturn(response(false));

        mockMvc.perform(get("/api/applicant-profile"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exists").value(true))
            .andExpect(jsonPath("$.id").value(8))
            .andExpect(jsonPath("$.firstName").value("Chengu"))
            .andExpect(jsonPath("$.verified").value(false));
    }

    @Test
    void createsOrUpdatesThroughTheSingletonPutEndpoint() throws Exception {
        when(profileService.save(any(ApplicantProfileRequest.class)))
            .thenReturn(response(false));

        mockMvc.perform(put("/api/applicant-profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJson()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("chengu@example.com"));

        verify(profileService).save(any(ApplicantProfileRequest.class));
    }

    @Test
    void rejectsInvalidIdentityAndPreferenceValues() throws Exception {
        mockMvc.perform(put("/api/applicant-profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "firstName": " ",
                      "lastName": "Kargbo",
                      "email": "not-an-email",
                      "portfolioUrl": "javascript:bad",
                      "minimumSalary": -1,
                      "salaryCurrency": "US"
                    }
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void verifiesOnlyThroughTheExplicitUserIntentEndpoint() throws Exception {
        when(profileService.verifyCurrent()).thenReturn(response(true));

        mockMvc.perform(post("/api/applicant-profile/verify"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.verified").value(true))
            .andExpect(jsonPath("$.lastVerifiedAt").isNotEmpty());

        verify(profileService).verifyCurrent();
    }

    private ApplicantProfileResponse response(boolean verified) {
        OffsetDateTime now = OffsetDateTime.parse("2026-08-18T12:00:00Z");
        return new ApplicantProfileResponse(
            true, 8L, "Chengu", "Kargbo", null, "chengu@example.com",
            null, "New York", "NY", "United States", "10001",
            "https://portfolio.example", "https://github.com/chengu",
            "https://linkedin.com/in/chengu", "Software Engineering",
            RemoteType.REMOTE, new BigDecimal("120000"), "USD", true,
            false, verified, verified ? now : null, now, now
        );
    }

    private String validJson() {
        return """
            {
              "firstName": "Chengu",
              "lastName": "Kargbo",
              "email": "chengu@example.com",
              "preferredWorkArrangement": "REMOTE",
              "minimumSalary": 120000,
              "salaryCurrency": "USD",
              "willingToRelocate": true,
              "willingToTravel": false
            }
            """;
    }
}
