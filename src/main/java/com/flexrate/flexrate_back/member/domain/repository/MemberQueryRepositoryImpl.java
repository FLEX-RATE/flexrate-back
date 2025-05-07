package com.flexrate.flexrate_back.member.domain.repository;

import com.flexrate.flexrate_back.loan.domain.*;
import com.flexrate.flexrate_back.loan.enums.LoanApplicationStatus;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.QMember;
import com.flexrate.flexrate_back.member.dto.MemberSearchRequest;
import com.querydsl.core.types.dsl.Expressions;
import lombok.RequiredArgsConstructor;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberQueryRepositoryImpl implements MemberQueryRepository {
    private final JPAQueryFactory queryFactory;

    /**
     * 회원 목록 조회
     * @param request 검색 조건
     * @param pageable 페이징 정보
     * @return 회원 목록
     * @since 2025.04.26
     * @author 권민지
     */
    @Override
    public Page<Member> searchMembers(MemberSearchRequest request, Pageable pageable) {
        QMember member = QMember.member;

        List<Member> members = queryFactory.selectFrom(member)
                .where(
                        member.name.containsIgnoreCase(request.name() != null ? request.name() : ""),
                        member.email.containsIgnoreCase(request.email() != null ? request.email() : ""),
                        request.sex() != null ? member.sex.eq(request.sex()) : null,
                        request.birthDate() != null ? member.birthDate.eq(request.birthDate()) : null,
                        request.memberStatus() != null ? member.status.eq(request.memberStatus()) : null,
                        (request.startDate() != null ? member.createdAt.goe(request.startDate().atStartOfDay()) : null),
                        (request.endDate() != null ? member.createdAt.loe(request.endDate().atTime(23, 59, 59)) : null),
                        request.hasLoan() != null && request.hasLoan()
                                ? member.loanApplication.status.ne(LoanApplicationStatus.EXECUTED) : null,
                        request.loanTransactionCount() != null
                                ? request.loanTransactionCount() == 0
                                ? member.loanApplication.isNull()
                                : member.loanApplication.isNotNull().and(member.loanApplication.loanTransactions.size().eq(request.loanTransactionCount()))
                                : null
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(pageable.getSort().stream()
                        .map(order -> order.isAscending()
                                ? Expressions.stringPath(order.getProperty()).asc()
                                : Expressions.stringPath(order.getProperty()).desc())
                        .toArray(com.querydsl.core.types.OrderSpecifier[]::new))
                .fetch();

        return new PageImpl<>(members, pageable, members.size());
    }

    /**
     * Id로 특정한 회원 정보 상세 조회
     * @param memberId 회원 ID
     * @return 회원의 상세 정보 제공을 위한 필요 정보들
     * @since 2025.04.29
     * @author 허연규
     */

    @Override
    public LoanApplication findLatestLoanApplication(Long memberId) {
        QLoanApplication a = QLoanApplication.loanApplication;
        return queryFactory
                .selectFrom(a)
                .where(a.member.memberId.eq(memberId))
                .fetchFirst();
    }

    @Override
    public Long countLoanTransactions(Long memberId) {
        QLoanTransaction t = QLoanTransaction.loanTransaction;
        return queryFactory
                .select(t.count())
                .from(t)
                .where(t.member.memberId.eq(memberId))
                .fetchOne();
    }

    @Override
    public Double findLatestInterestRate(Long memberId) {
        QInterest interest = QInterest.interest;
        QLoanProduct loanProduct = QLoanProduct.loanProduct;
        QLoanApplication loanApplication = QLoanApplication.loanApplication;
        QMember member = QMember.member;

        return queryFactory
                .select(interest.interestRate)
                .from(interest)
                .join(interest.loanProduct, loanProduct)
                .join(loanProduct.application, loanApplication)
                .join(loanApplication.member, member)
                .where(member.memberId.eq(memberId))
                .orderBy(interest.interestDate.desc())
                .fetchFirst();
    }
}
