package com.flexrate.flexrate_back.loan.application;

import com.flexrate.flexrate_back.loan.dto.LoanReviewApplicationRequest;
import com.flexrate.flexrate_back.loan.dto.LoanReviewApplicationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class LoanReviewApplicationServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private LoanReviewApplicationService loanPreApplicationService;

    public LoanReviewApplicationServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("LoanPreApplicationService - 정상 응답 테스트")
    void preApplySuccess() {
        LoanReviewApplicationRequest request = LoanReviewApplicationRequest.builder()
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

        LoanReviewApplicationResponse mockResponse = LoanReviewApplicationResponse.builder()
                .name("홍길동")
                .screeningDate("2025-04-28")
                .loanLimit(5000)
                .initialRate(4.9f)
                .rateRangeFrom(4.5f)
                .rateRangeTo(7.2f)
                .build();

        when(restTemplate.postForObject(any(String.class), any(), any()))
                .thenReturn(mockResponse);

        LoanReviewApplicationResponse result = loanPreApplicationService.preApply(request);

        assertThat(result.getName()).isEqualTo("홍길동");
        assertThat(result.getLoanLimit()).isEqualTo(5000);
    }
}
