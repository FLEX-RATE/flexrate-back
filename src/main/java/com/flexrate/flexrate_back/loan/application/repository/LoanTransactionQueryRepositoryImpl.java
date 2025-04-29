package com.flexrate.flexrate_back.loan.application.repository;

import com.flexrate.flexrate_back.loan.domain.LoanTransaction;
import com.flexrate.flexrate_back.loan.domain.QLoanTransaction;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class LoanTransactionQueryRepositoryImpl implements LoanTransactionQueryRepository {
    private final JPAQueryFactory queryFactory;

    /**
     * 대출 거래 내역 목록 조회
     * @param memberId 사용자 ID
     * @param pageable 페이징 정보
     * @return 대출 거래 내역 목록
     * @since 2025.04.29
     * @author 권민지
     */
    @Override
    public Page<LoanTransaction> findByMemberId(Long memberId, Pageable pageable) {
        QLoanTransaction loanTransaction = QLoanTransaction.loanTransaction;

        List<LoanTransaction> content = queryFactory.selectFrom(loanTransaction)
                .where(loanTransaction.member.memberId.eq(memberId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                // createdAt으로 내림차순
                .orderBy(loanTransaction.occurredAt.desc())
                .fetch();

        Long total = queryFactory
                .select(loanTransaction.count())
                .from(loanTransaction)
                .where(loanTransaction.member.memberId.eq(memberId))
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }
}
