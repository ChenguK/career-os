package com.chengukargbo.careeros.answers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

import com.chengukargbo.careeros.answers.dto.ApprovedAnswerRequest;
import com.chengukargbo.careeros.answers.dto.ApprovedAnswerResponse;

@WebMvcTest(ApprovedAnswerController.class)
class ApprovedAnswerControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean ApprovedAnswerService service;

    @Test
    void supportsListCreateUpdateApproveRevokeAndDelete() throws Exception {
        when(service.findAll()).thenReturn(List.of(response(false)));
        when(service.create(any(ApprovedAnswerRequest.class)))
            .thenReturn(response(false));
        when(service.update(any(), any())).thenReturn(response(false));
        when(service.approve(4L)).thenReturn(response(true));
        when(service.revoke(4L)).thenReturn(response(false));

        mockMvc.perform(get("/api/approved-answers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].canonicalKey").value("preferred_pronouns"));
        mockMvc.perform(post("/api/approved-answers")
                .contentType(MediaType.APPLICATION_JSON).content(validJson()))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "http://localhost/api/approved-answers/4"));
        mockMvc.perform(put("/api/approved-answers/4")
                .contentType(MediaType.APPLICATION_JSON).content(validJson()))
            .andExpect(status().isOk());
        mockMvc.perform(post("/api/approved-answers/4/approve"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.userApproved").value(true));
        mockMvc.perform(post("/api/approved-answers/4/revoke"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.userApproved").value(false));
        mockMvc.perform(delete("/api/approved-answers/4"))
            .andExpect(status().isNoContent());
        verify(service).delete(4L);
    }

    @Test
    void validatesCanonicalKeyAndQuestion() throws Exception {
        mockMvc.perform(post("/api/approved-answers")
                .contentType(MediaType.APPLICATION_JSON).content("""
                    {"canonicalKey":"bad key","representativeQuestion":" ",
                    "answerType":"TEXT","textValue":"x",
                    "classification":"UNKNOWN","reusable":false,
                    "answerSource":"MANUAL"}
                    """))
            .andExpect(status().isBadRequest());
    }

    private String validJson() {
        return """
            {"canonicalKey":"preferred_pronouns",
            "representativeQuestion":"What are your pronouns?",
            "answerType":"TEXT","textValue":"they/them",
            "classification":"VERIFIED_REUSABLE","reusable":true,
            "answerSource":"MANUAL"}
            """;
    }

    private ApprovedAnswerResponse response(boolean approved) {
        OffsetDateTime now = OffsetDateTime.parse("2026-08-18T12:00:00Z");
        return new ApprovedAnswerResponse(4L, "preferred_pronouns",
            "What are your pronouns?", AnswerType.TEXT, "they/them", null,
            null, AnswerClassification.VERIFIED_REUSABLE, true, approved,
            approved ? now : null, null, AnswerSource.MANUAL, null, true,
            approved, "they/them", null, null, null, null, now, now);
    }
}
