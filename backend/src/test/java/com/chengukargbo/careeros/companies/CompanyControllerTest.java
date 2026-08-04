package com.chengukargbo.careeros.companies;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.chengukargbo.careeros.companies.dto.CompanyRequest;
import com.chengukargbo.careeros.companies.dto.CompanyResponse;

@WebMvcTest(CompanyController.class)
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CompanyService companyService;

    @Test
    void createsCompany() throws Exception {
        CompanyResponse response = sampleResponse();

        when(companyService.create(any(CompanyRequest.class)))
            .thenReturn(response);

        mockMvc.perform(
                post("/api/companies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "name": "GitHub",
                          "industry": "Software",
                          "dreamCompany": true
                        }
                        """)
            )
            .andExpect(status().isCreated())
            .andExpect(header().string(
                "Location",
                "http://localhost/api/companies/1"
            ))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("GitHub"))
            .andExpect(jsonPath("$.dreamCompany").value(true));
    }

    @Test
    void returnsAllCompanies() throws Exception {
        when(companyService.findAll())
            .thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/companies"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("GitHub"));
    }

    @Test
    void returnsCompanyById() throws Exception {
        when(companyService.findById(1L))
            .thenReturn(sampleResponse());

        mockMvc.perform(get("/api/companies/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("GitHub"));
    }

    @Test
    void returnsNotFoundForMissingCompany() throws Exception {
        when(companyService.findById(999L))
            .thenThrow(new CompanyNotFoundException(999L));

        mockMvc.perform(get("/api/companies/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message")
                .value("Company not found with id: 999"))
            .andExpect(jsonPath("$.path")
                .value("/api/companies/999"));
    }

    @Test
    void rejectsBlankCompanyName() throws Exception {
        mockMvc.perform(
                post("/api/companies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "name": "   ",
                          "dreamCompany": false
                        }
                        """)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message")
                .value("Company name is required"));
    }

    private CompanyResponse sampleResponse() {
        OffsetDateTime timestamp =
            OffsetDateTime.parse("2026-08-04T11:10:46Z");

        return new CompanyResponse(
            1L,
            "GitHub",
            "https://github.com",
            "https://www.github.careers",
            "Software",
            "SaaS",
            "Build a home for developers",
            "Code hosting and developer collaboration",
            "Java, Ruby, Go, JavaScript",
            "Research required",
            null,
            "Dream company playbook",
            true,
            timestamp,
            timestamp
        );
    }
}