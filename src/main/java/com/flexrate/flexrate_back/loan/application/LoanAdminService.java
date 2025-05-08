package com.flexrate.flexrate_back.loan.application;

import com.flexrate.flexrate_back.common.dto.PaginationInfo;
import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.loan.application.repository.InterestRepository;
import com.flexrate.flexrate_back.loan.application.repository.LoanApplicationRepository;
import com.flexrate.flexrate_back.loan.application.repository.LoanTransactionQueryRepository;
import com.flexrate.flexrate_back.loan.domain.Interest;
import com.flexrate.flexrate_back.loan.domain.LoanApplication;
import com.flexrate.flexrate_back.loan.dto.LoanApplicationStatusUpdateRequest;
import com.flexrate.flexrate_back.loan.dto.LoanApplicationStatusUpdateResponse;
import com.flexrate.flexrate_back.loan.dto.TransactionHistoryResponse;
import com.flexrate.flexrate_back.loan.enums.LoanApplicationStatus;
import com.flexrate.flexrate_back.loan.mapper.LoanTransactionMapper;
import com.flexrate.flexrate_back.member.application.AdminAuthChecker;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoanAdminService {
    private final LoanTransactionQueryRepository loanTransactionQueryRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanTransactionMapper loanTransactionMapper;
    private final MemberRepository memberRepository;
    private final InterestRepository interestRepository;
    /**
     * 대출 거래 내역 목록 조회
     * @param memberId 사용자 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param sortBy 정렬 기준
     * @return 입금 내역 상세 정보(페이지네이션)
     * @since 2025.04.29
     * @author 권민지
     */
    public TransactionHistoryResponse getTransactionHistory(
            Long memberId,
            int page,
            int size,
            String sortBy
    ) {
        // U001 유저 존재 여부 체크
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        // 기본 정렬 occurredAt 내림차순
        Sort sort = sortBy != null
                ? (sortBy.equals("occurredAt") 
                    ? Sort.by(sortBy).descending() 
                    : Sort.by(sortBy).ascending())
                : Sort.by("occurredAt").descending();

        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);

        var transactionHistory = loanTransactionQueryRepository.findByMemberId(memberId, pageable);

        return TransactionHistoryResponse.builder()
                .paginationInfo(new PaginationInfo(
                        transactionHistory.getNumber(),
                        transactionHistory.getSize(),
                        transactionHistory.getTotalPages(),
                        transactionHistory.getTotalElements()
                ))
                .transactionHistories(transactionHistory.getContent().stream()
                        .map(loanTransactionMapper::toSummaryDto)
                        .toList())
                .build();
    }

    /**
     * 대출 상태 변경 updateLoanStatus
     * @param loanApplicationId 대출 신청 ID
     * @param request 변경할 대출 상태(status), 변경 사유(reason)
     * @return 성공 여부
     */
    @Transactional
    public LoanApplicationStatusUpdateResponse patchLoanApplicationStatus(
            Long loanApplicationId,
            LoanApplicationStatusUpdateRequest request) {
        // L002 해당 loanApplicationId 존재 여부 체크
        if (loanApplicationId == null) {
            throw new FlexrateException(ErrorCode.LOAN_NOT_FOUND);
        }

        // L002 loanApplication 데이터 존재여부 체크
        LoanApplication loanApplication = loanApplicationRepository.findById(loanApplicationId)
                .orElseThrow(() -> new FlexrateException(ErrorCode.LOAN_NOT_FOUND));

        // L005 상태 전환 제약조건 체크 & 상태 변경
        loanApplication.patchStatus(request.status());

        // 대출 승인 시 초기 금리 저장 및 loanApplication 상에 승인 반영
        if(request.status() == LoanApplicationStatus.EXECUTED){
            interestRepository.save(Interest.builder()
                    .loanApplication(loanApplication)
                    .interestDate(LocalDateTime.now())
                    .interestRate(loanApplication.getRate())
                    .build());

            loanApplication.patchExecutedAt();
        }


        return LoanApplicationStatusUpdateResponse.builder()
                .loanApplicationId(loanApplication.getApplicationId())
                .success(true)
                .message("대출 상태가 변경되었습니다.")
                .build();
    }

}
