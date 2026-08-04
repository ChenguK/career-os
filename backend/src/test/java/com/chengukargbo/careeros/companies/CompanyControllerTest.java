package com.chengukargbo.careeros.companies;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
        when(companyService.search(null))
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
    @Test
    void searchesCompaniesByName() throws Exception {
        when(companyService.search("git"))
            .thenReturn(List.of(sampleResponse()));

        mockMvc.perform(
                get("/api/companies")
                    .queryParam("search", "git")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("GitHub"));
    }

    @Test
    void updatesCompany() throws Exception {
        when(companyService.update(
            org.mockito.ArgumentMatchers.eq(1L),
            any(CompanyRequest.class)
        )).thenReturn(sampleResponse());

        mockMvc.perform(
                put("/api/companies/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                        "name": "GitHub",
                        "industry": "Developer Tools",
                        "dreamCompany": true
                        }
                        """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("GitHub"));
    }

    @Test
    void deletesCompany() throws Exception {
        doNothing().when(companyService).delete(1L);

        mockMvc.perform(delete("/api/companies/1"))
            .andExpect(status().isNoContent());

        verify(companyService).delete(1L);
    }

    @Test
    void returnsNotFoundWhenUpdatingMissingCompany() throws Exception {
        when(companyService.update(
            org.mockito.ArgumentMatchers.eq(999L),
            any(CompanyRequest.class)
        )).thenThrow(new CompanyNotFoundException(999L));

        mockMvc.perform(
                put("/api/companies/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                        "name": "Missing Company",
                        "dreamCompany": false
                        }
                        """)
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void returnsNotFoundWhenDeletingMissingCompany() throws Exception {
        org.mockito.Mockito.doThrow(new CompanyNotFoundException(999L))
            .when(companyService)
            .delete(999L);

        mockMvc.perform(delete("/api/companies/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
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