package com.flexrate.flexrate_back.loan.application;

import com.flexrate.flexrate_back.common.dto.PaginationInfo;
import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.loan.application.repository.LoanTransactionQueryRepository;
import com.flexrate.flexrate_back.loan.dto.TransactionHistoryResponse;
import com.flexrate.flexrate_back.loan.mapper.LoanTransactionMapper;
import com.flexrate.flexrate_back.member.application.AdminAuthChecker;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoanAdminService {
    private final LoanTransactionQueryRepository loanTransactionQueryRepository;
    private final LoanTransactionMapper loanTransactionMapper;
    private final MemberRepository memberRepository;
    private final AdminAuthChecker adminAuthChecker;

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
        // A007 관리자 인증 체크
//        if (!adminAuthChecker.isAdmin(adminToken)) {
//            throw new FlexrateException(ErrorCode.ADMIN_AUTH_REQUIRED);
//        }

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
}
