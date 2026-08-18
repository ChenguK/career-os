package com.chengukargbo.careeros.questions.research;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.chengukargbo.careeros.questions.QuestionEnums.*;

@WebMvcTest(QuestionResearchController.class)
class QuestionResearchControllerTest {
 @Autowired MockMvc mvc; @MockitoBean QuestionResearchService service;
 @Test void exposesResearchWithoutCreatingApplicationQuestions() throws Exception {when(service.research(JobFamily.SOFTWARE_ENGINEER,Seniority.SENIOR,null)).thenReturn(List.of(new LikelyQuestion("email","Email?",AnswerType.TEXT,Classification.VERIFIED_REUSABLE,false,new BigDecimal("0.75"),StaticCareerOSTemplates.ID,JobFamily.SOFTWARE_ENGINEER,Seniority.SENIOR,new BigDecimal("0.85"))));mvc.perform(get("/api/questions/research").param("jobFamily","SOFTWARE_ENGINEER").param("seniority","SENIOR")).andExpect(status().isOk()).andExpect(jsonPath("$[0].canonicalKey").value("email")).andExpect(jsonPath("$[0].source").value(StaticCareerOSTemplates.ID));verify(service).research(JobFamily.SOFTWARE_ENGINEER,Seniority.SENIOR,null);}
}
