package com.chengukargbo.careeros.preparation;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.chengukargbo.careeros.preparation.QuestionMappingDtos.*;
import com.chengukargbo.careeros.questions.QuestionEnums.*;

@WebMvcTest(ObservedQuestionMappingController.class)
class ObservedQuestionMappingControllerTest {
    @Autowired MockMvc mvc; @MockitoBean ObservedQuestionMappingService service;
    @Test void exposesReviewConfirmRevokeAndHistoryRoutes() throws Exception {
        when(service.review(7L,JobFamily.SOFTWARE_ENGINEER,Seniority.MID_LEVEL)).thenReturn(new ReviewResponse(7L,31L,List.of(),List.of()));
        mvc.perform(get("/api/applications/7/preparation/question-mappings").param("jobFamily","SOFTWARE_ENGINEER").param("seniority","MID_LEVEL")).andExpect(status().isOk()).andExpect(jsonPath("$.snapshotId").value(31));
        mvc.perform(post("/api/applications/7/preparation/question-mappings").contentType(MediaType.APPLICATION_JSON).content("""
            {"externalQuestionId":"ats-email","canonicalQuestionKey":"email","jobFamily":"SOFTWARE_ENGINEER","seniority":"MID_LEVEL"}
            """)).andExpect(status().isOk());
        mvc.perform(post("/api/applications/7/preparation/question-mappings/9/revoke")).andExpect(status().isOk());
        when(service.history(7L,9L)).thenReturn(List.of());
        mvc.perform(get("/api/applications/7/preparation/question-mappings/9/history")).andExpect(status().isOk()).andExpect(content().json("[]"));
        verify(service).confirm(eq(7L),any(ConfirmRequest.class)); verify(service).revoke(7L,9L);
    }
}
