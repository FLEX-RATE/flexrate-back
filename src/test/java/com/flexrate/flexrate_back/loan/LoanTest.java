package com.flexrate.flexrate_back.loan;

import com.flexrate.flexrate_back.loan.api.LoanController;
import com.flexrate.flexrate_back.loan.application.LoanService;
import com.flexrate.flexrate_back.loan.dto.LoanReviewApplicationResponse;
import com.flexrate.flexrate_back.member.application.MemberService;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.common.util.ProfileUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoanController.class)
class LoanTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoanService loanService;

    @MockBean
    private MemberService memberService;

    @MockBean
    private ProfileUtil profileUtil;

    private final Principal principal = () -> "1";

    @Test
    @WithMockUser(username = "1")
    @DisplayName("preApplyResult 성공 테스트")
    void testPreApplyResultSuccess() throws Exception {
        // given
        Member mockMember = Member.builder()
                .memberId(1L)
                .name("홍길동")
                .build();

        LoanReviewApplicationResponse response = LoanReviewApplicationResponse.builder()
                .name("홍길동")
                .screeningDate("2025-05-05")
                .loanLimit(30000000)
                .initialRate(3.5)
                .rateRangeFrom(2.5f)
                .rateRangeTo(4.5f)
                .creditScore(750)
                .build();

        Mockito.when(memberService.findById(1L)).thenReturn(mockMember);
        Mockito.when(loanService.preApplyResult(eq(mockMember))).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/loans/loan-review-application")
                        .principal(principal)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("홍길동"))
                .andExpect(jsonPath("$.loanLimit").value(30000000))
                .andExpect(jsonPath("$.initialRate").value(3.5));
    }
}
