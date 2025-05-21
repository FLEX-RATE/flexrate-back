package com.flexrate.flexrate_back.loan.application;

import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.financialdata.domain.UserFinancialData;
import com.flexrate.flexrate_back.financialdata.enums.UserFinancialDataType;
import com.flexrate.flexrate_back.loan.application.repository.LoanApplicationRepository;
import com.flexrate.flexrate_back.loan.domain.LoanApplication;
import com.flexrate.flexrate_back.loan.domain.LoanProduct;
import com.flexrate.flexrate_back.loan.dto.LoanApplicationRequest;
import com.flexrate.flexrate_back.loan.dto.LoanApplicationResultResponse;
import com.flexrate.flexrate_back.loan.dto.LoanReviewApplicationRequest;
import com.flexrate.flexrate_back.loan.dto.LoanReviewApplicationResponse;
import com.flexrate.flexrate_back.loan.enums.LoanApplicationStatus;
import com.flexrate.flexrate_back.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 대출 상품 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 *
 * @since 2025.05.05
 * @author 유승한
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoanService {

    private final RestTemplate restTemplate;

    @Value("${fastapi.host}")
    private String fastApiHost;

    @Value("${fastapi.port}")
    private String fastApiPort;

    // 심사 서버 URL (추후 변경 예정)
    private static final String SCREENING_SERVER_URL = "http://external-server/api";
    private final LoanApplicationRepository loanApplicationRepository;

    /**
     * 대출 신청 사전 정보를 외부 심사 서버로 전달 후, 심사 결과 저장
     * @param request 대출 신청 기본 정보, Member 대출 신청자
     * @since 2025.04.28
     * @author 서채연
     */
    @Transactional
    public void preApply(LoanReviewApplicationRequest request, Member member) {
        LoanApplication loanApplication = loanApplicationRepository.findByMember(member)
                .orElseThrow(() -> new FlexrateException(ErrorCode.LOAN_NOT_FOUND));

        if (loanApplication.getStatus() != LoanApplicationStatus.PRE_APPLIED) {
            throw new FlexrateException(ErrorCode.LOAN_APPLICATION_ALREADY_EXISTS);
        }

        LoanProduct product = member.getLoanApplication().getProduct();

        String fastApiUrl = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(fastApiHost)
                .port(fastApiPort)
                .path("/predict-initial-rate")
                .build()
                .toUriString();

        // FastAPI로 보낼 요청 JSON 생성
        Map<String, Object> fastApiRequest = new HashMap<>();
        fastApiRequest.put("AGE", member.getAge());
        fastApiRequest.put("SEX_CD", member.getSexCode()); // 1: 남성, 2: 여성
        fastApiRequest.put("TOTAL_SPEND", (float) calculateMonthlyTotalSpend(member)); // 소비 총액
        fastApiRequest.put("min_rate", product.getMinRate());
        fastApiRequest.put("max_rate", product.getMaxRate());
        fastApiRequest.put("credit_score", member.getLoanApplication().getCreditScore());

        // FastAPI 호출: 금리 예측
        Map<String, Object> rateResponse = restTemplate.postForObject(
                fastApiUrl,
                fastApiRequest,
                Map.class
        );

        if (rateResponse == null || rateResponse.get("initialRate") == null) {
            throw new FlexrateException(ErrorCode.LOAN_SERVER_ERROR);
        }

        float initialRate = ((Number) rateResponse.get("initialRate")).floatValue();

        // 금리 범위 포함한 응답 생성
        LoanReviewApplicationResponse externalResponse = LoanReviewApplicationResponse.builder()
                .name(member.getName())
                .screeningDate(LocalDate.now().toString())
                .loanLimit(product.getMaxAmount())  // 예시
                .initialRate(initialRate)
                .rateRangeFrom(product.getMinRate())
                .rateRangeTo(product.getMaxRate())
                .creditScore(member.getLoanApplication().getCreditScore())
                .terms(product.getTerms())
                .build();

        loanApplication.applyReviewResult(externalResponse);
    }

    /**
     * 대출 심사 결과 조회
     * @param member 대출 신청자
     * @since 2025.05.06
     * @author 유승한
     */
    public LoanReviewApplicationResponse preApplyResult(Member member) {
        LoanApplication loanApplication = loanApplicationRepository.findByMember(member)
                .orElseThrow(() -> new FlexrateException(ErrorCode.LOAN_NOT_FOUND));

        if (loanApplication.getStatus() !=  LoanApplicationStatus.PRE_APPLIED){
            throw new FlexrateException(ErrorCode.LOAN_APPLICATION_ALREADY_EXISTS); // 이미 대출 중인 경우 예외처리
        }


        return LoanReviewApplicationResponse.builder()
                .name(member.getName())
                .screeningDate(loanApplication.getAppliedAt().toLocalDate().toString())
                .loanLimit(loanApplication.getTotalAmount())
                .initialRate(loanApplication.getRate())
                .rateRangeFrom(loanApplication.getProduct().getMinRate())
                .rateRangeTo(loanApplication.getProduct().getMaxRate())
                .terms(loanApplication.getProduct().getTerms())
                .build();
    }

    /**
     * 대출 신청
     * @param member 대출 신청자
     * @since 2025.05.06
     * @author 유승한
     */
    @Transactional
    public void applyLoan(Member member, LoanApplicationRequest loanApplicationRequest) {
        LoanApplication loanApplication = loanApplicationRepository.findByMember(member)
                .orElseThrow(() -> new FlexrateException(ErrorCode.LOAN_NOT_FOUND));

        if (loanApplication.getStatus() != LoanApplicationStatus.PRE_APPLIED) {
            throw new FlexrateException(ErrorCode.LOAN_APPLICATION_ALREADY_EXISTS);
        }

        // 한도를 초과한 대출금액 요청 시 예외 처리
        if(loanApplication.getProduct().getMaxAmount() < loanApplicationRequest.loanAmount()){
            throw new FlexrateException(ErrorCode.LOAN_REQUEST_CONFLICT);
        }

        // 최대 대출 기한을 초과한 기한 요청 시 예외 처리
        if(loanApplication.getProduct().getTerms() < loanApplicationRequest.repaymentMonth()){
            throw new FlexrateException(ErrorCode.LOAN_REQUEST_CONFLICT);
        }

        // 신청 정보 반영
        loanApplication.applyLoan(loanApplicationRequest);
    }
    /**
     * 대출 결과 조회
     * @param member 대출 신청자
     * @since 2025.05.06
     * @author 유승한
     */
    public LoanApplicationResultResponse getLoanApplicationResult(Member member) {
        LoanApplication loanApplication = loanApplicationRepository.findByMember(member)
                .orElseThrow(() -> new FlexrateException(ErrorCode.LOAN_NOT_FOUND));

        if(loanApplication.getStatus() == LoanApplicationStatus.PRE_APPLIED){
            throw new FlexrateException(ErrorCode.LOAN_NOT_APPLIED);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        return LoanApplicationResultResponse.builder()
                .loanApplicationResult(loanApplication.getStatus().name())
                .loanApplicationAmount(loanApplication.getTotalAmount())
                .loanInterestRate(loanApplication.getRate())
                .loanStartDate(loanApplication.getStartDate().format(formatter))
                .loanEndDate(loanApplication.getEndDate().format(formatter))
                .build();
    }

    private int calculateMonthlyTotalSpend(Member member) {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);

        return member.getFinancialData().stream()
                .filter(data -> data.getDataType() == UserFinancialDataType.EXPENSE)
                .filter(data -> data.getCollectedAt().isAfter(oneMonthAgo))
                .mapToInt(UserFinancialData::getValue)
                .sum();
    }


}
