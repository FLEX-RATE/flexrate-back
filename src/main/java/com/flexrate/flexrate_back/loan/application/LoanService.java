package com.flexrate.flexrate_back.loan.application;

import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.financialdata.domain.UserFinancialData;
import com.flexrate.flexrate_back.loan.application.repository.LoanApplicationRepository;
import com.flexrate.flexrate_back.loan.domain.LoanApplication;
import com.flexrate.flexrate_back.loan.dto.LoanApplicationRequest;
import com.flexrate.flexrate_back.loan.dto.LoanApplicationResultResponse;
import com.flexrate.flexrate_back.loan.dto.LoanReviewApplicationRequest;
import com.flexrate.flexrate_back.loan.dto.LoanReviewApplicationResponse;
import com.flexrate.flexrate_back.loan.enums.LoanApplicationStatus;
import com.flexrate.flexrate_back.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 대출 상품 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 *
 * @since 2025.05.05
 * @author 유승한
 */
@Service
@RequiredArgsConstructor
public class LoanService {

    private final RestTemplate restTemplate;

    // 심사 서버 URL (추후 변경 예정)
    private static final String SCREENING_SERVER_URL = "http://external-server/api";
    private final LoanApplicationRepository loanApplicationRepository;



    /**
     * 대출 신청 사전 정보를 외부 심사 서버로 전달 후, 심사 결과 저장
     * @param request 대출 신청 기본 정보, Member 대출 신청자
     * @since 2025.04.28
     * @author 서채연
     */
    public void preApply(LoanReviewApplicationRequest request, Member member) {
        LoanApplication loanApplication = loanApplicationRepository.findByMember(member)
                .orElseThrow(() -> new FlexrateException(ErrorCode.LOAN_NOT_FOUND));

        if (loanApplication.getStatus() !=  LoanApplicationStatus.PRE_APPLIED){ // 추후에 분기 조건 조정 필요
            throw new FlexrateException(ErrorCode.LOAN_APPLICATION_ALREADY_EXISTS); // 이미 대출 중인 경우 예외처리
        }

        // 외부 대출 심사 서버 호출
        LoanReviewApplicationResponse externalResponse = restTemplate.postForObject(
                SCREENING_SERVER_URL,
                request,
                LoanReviewApplicationResponse.class
        );

        if (externalResponse == null) {
            throw new FlexrateException(ErrorCode.LOAN_SERVER_ERROR);
        }
        // 기존 loanApplication에 심사결과 적용
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
    public void applyLoan(Member member, LoanApplicationRequest loanApplicationRequest) {
        LoanApplication loanApplication = loanApplicationRepository.findByMember(member)
                .orElseThrow(() -> new FlexrateException(ErrorCode.LOAN_NOT_FOUND));

        if (loanApplication.getStatus() != LoanApplicationStatus.PRE_APPLIED) {
            throw new FlexrateException(ErrorCode.LOAN_APPLICATION_ALREADY_EXISTS);
        }

        // 한도를 초과한 대출금액 요청 시 예외 처리
        if(loanApplication.getTotalAmount() < loanApplicationRequest.loanAmount()){
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

}
