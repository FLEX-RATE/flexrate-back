package com.flexrate.flexrate_back.member.domain.repository;

import com.flexrate.flexrate_back.loan.domain.LoanApplication;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.dto.MemberSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 회원 조회 쿼리
 * - 읽기/검색 전용
 */
public interface MemberQueryRepository {
    Page<Member> searchMembers(MemberSearchRequest request, Pageable pageable);

    LoanApplication findLatestLoanApplication(Long memberId);
    Long countLoanTransactions(Long memberId);
    Double findLatestInterestRate(Long memberId);
}
