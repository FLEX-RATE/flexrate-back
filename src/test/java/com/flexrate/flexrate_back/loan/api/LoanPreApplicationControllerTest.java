package com.flexrate.flexrate_back.loan.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flexrate.flexrate_back.loan.dto.LoanPreApplicationRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LoanPreApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("대출 사전 신청 - 정상 요청")
    void preApplyLoan_success() throws Exception {
        LoanPreApplicationRequest request = LoanPreApplicationRequest.builder()
                .businessType("IT")
                .employmentType("FULL_TIME")
                .hireDate("2022-01")
                .schoolName("서울대학교")
                .educationStatus("GRADUATED")
                .annualIncome(3500)
                .creditGrade("2")
                .residenceType("OWNED")
                .isBankrupt(false)
                .loanPurpose("BUSINESS")
                .build();

        mockMvc.perform(post("/api/loans/pre-applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.loanLimit").exists());
    }

    @Test
    @DisplayName("대출 사전 신청 - 필수값 누락 시 400 반환")
    void preApplyLoan_validationFail() throws Exception {
        LoanPreApplicationRequest request = LoanPreApplicationRequest.builder()
                .employmentType("FULL_TIME")
                .hireDate("2022-01")
                .schoolName("서울대학교")
                .educationStatus("GRADUATED")
                .annualIncome(3500)
                .creditGrade("2")
                .residenceType("OWNED")
                .isBankrupt(false)
                .loanPurpose("BUSINESS")
                .build();

        mockMvc.perform(post("/api/loans/pre-applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
