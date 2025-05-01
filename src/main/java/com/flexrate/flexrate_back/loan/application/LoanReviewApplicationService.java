package com.flexrate.flexrate_back.loan.application;

import com.flexrate.flexrate_back.loan.dto.LoanReviewApplicationRequest;
import com.flexrate.flexrate_back.loan.dto.LoanReviewApplicationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class LoanReviewApplicationService {

    private final RestTemplate restTemplate;

    // 심사 서버 URL (추후 변경 예정)
    private static final String SCREENING_SERVER_URL = "http://external-server/api";

    /**
     * 대출 신청 사전 정보를 외부 심사 서버로 전달 후, 심사 결과 반환
     * @param request 대출 신청 기본 정보
     * @return LoanPreApplicationResponse 대출 심사 결과
     * @since 2025.04.28
     * @author 서채연
     */
    public LoanReviewApplicationResponse preApply(LoanReviewApplicationRequest request) {
        LoanReviewApplicationResponse externalResponse = restTemplate.postForObject(
                SCREENING_SERVER_URL,
                request,
                LoanReviewApplicationResponse.class
        );

        if (externalResponse == null) {
            throw new IllegalStateException("심사 서버 응답이 없습니다.");
        }

        return LoanReviewApplicationResponse.builder()
                .name(externalResponse.getName())
                .screeningDate(externalResponse.getScreeningDate())
                .loanLimit(externalResponse.getLoanLimit())
                .initialRate(externalResponse.getInitialRate())
                .rateRangeFrom(externalResponse.getRateRangeFrom())
                .rateRangeTo(externalResponse.getRateRangeTo())
                .build();

        // 테스트용 더미 데이터
//        return LoanReviewApplicationResponse.builder()
//                .name("홍길동")
//                .screeningDate("2025-04-28")
//                .loanLimit(5000)
//                .initialRate(4.9f)
//                .rateRangeFrom(4.5f)
//                .rateRangeTo(7.2f)
//                .build();
    }
}
