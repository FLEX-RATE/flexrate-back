package com.flexrate.flexrate_back.loan;

import com.flexrate.flexrate_back.loan.api.LoanProductController;
import com.flexrate.flexrate_back.loan.application.LoanProductService;
import com.flexrate.flexrate_back.loan.dto.LoanProductSummaryDto;
import com.flexrate.flexrate_back.member.application.MemberService;
import com.flexrate.flexrate_back.member.domain.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoanProductController.class)
class LoanProductTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoanProductService loanProductService;

    @MockBean
    private MemberService memberService;

    @MockBean
    private com.flexrate.flexrate_back.common.util.ProfileUtil profileUtil; // 문제 해결 핵심

    private final Principal mockPrincipal = () -> "1";

    @Test
    @DisplayName("대출 상품 목록 조회 API는 상품 리스트를 반환한다")
    void getAllProducts_shouldReturnProductList() throws Exception {
        // given
        List<LoanProductSummaryDto> productList = List.of(
                new LoanProductSummaryDto(1L, "상품 A", "설명 A", 1000.0, 3.5, 6.0, 365),
                new LoanProductSummaryDto(2L, "상품 B", "설명 B", 2000.0, 4.0, 7.0, 730)
        );
        given(loanProductService.getAllProducts()).willReturn(productList);

        // when & then
        mockMvc.perform(get("/api/loans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(productList.size()))
                .andExpect(jsonPath("$[0].name").value("상품 A"))
                .andExpect(jsonPath("$[1].name").value("상품 B"));
    }

    @Test
    @DisplayName("대출 상품 선택 API는 정상적으로 LoanApplication을 생성한다")
    void selectProduct_shouldCreateLoanApplication() throws Exception {
        // given
        Member mockMember = Member.builder().memberId(1L).build();
        given(memberService.findById(1L)).willReturn(mockMember);
        doNothing().when(loanProductService).selectProduct(ArgumentMatchers.anyLong(), ArgumentMatchers.any(Member.class));

        // when & then
        mockMvc.perform(post("/api/loans/1/select")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
