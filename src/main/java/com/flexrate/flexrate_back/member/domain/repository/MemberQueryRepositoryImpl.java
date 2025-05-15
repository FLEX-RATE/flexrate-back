package com.flexrate.flexrate_back.member.domain.repository;

import com.flexrate.flexrate_back.loan.domain.*;
import com.flexrate.flexrate_back.loan.enums.LoanApplicationStatus;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.QMember;
import com.flexrate.flexrate_back.member.dto.MemberSearchRequest;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.CaseBuilder;
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
        QLoanApplication loanApplication = QLoanApplication.loanApplication;

        BooleanBuilder builder = new BooleanBuilder();

        // 기본 조건
        if (request.name() != null && !request.name().isEmpty()) {
            builder.and(member.name.containsIgnoreCase(request.name()));
        }
        if (request.email() != null && !request.email().isEmpty()) {
            builder.and(member.email.containsIgnoreCase(request.email()));
        }
        if (request.sex() != null) {
            builder.and(member.sex.eq(request.sex()));
        }
        if (request.birthDateStart() != null) {
            builder.and(member.birthDate.goe(request.birthDateStart()));
        }
        if (request.birthDateEnd() != null) {
            builder.and(member.birthDate.loe(request.birthDateEnd()));
        }

        if (request.memberStatus() != null) {
            builder.and(member.status.eq(request.memberStatus()));
        }
        if (request.startDate() != null) {
            builder.and(member.createdAt.goe(request.startDate().atStartOfDay()));
        }
        if (request.endDate() != null) {
            builder.and(member.createdAt.loe(request.endDate().atTime(23, 59, 59)));
        }

        // hasLoan(대출 중 여부) 조건
        if (request.hasLoan() != null) {
            if (request.hasLoan()) {
                // 대출이 존재하고, 상태가 EXECUTED인 회원만
                builder.and(member.loanApplication.isNotNull()
                        .and(member.loanApplication.status.eq(LoanApplicationStatus.EXECUTED)));
            } else {
                // 대출이 없거나, 상태가 EXECUTED가 아닌 회원만
                builder.and(member.loanApplication.isNull()
                        .or(member.loanApplication.status.ne(LoanApplicationStatus.EXECUTED)));
            }
        }

        // loanTransactionCount(거래 횟수) 조건
        if (request.transactionCountMin() != null) {
            builder.and(
                    new CaseBuilder()
                            .when(member.loanApplication.isNull())
                            .then(0)
                            .otherwise(member.loanApplication.loanTransactions.size())
                            .goe(request.transactionCountMin())
            );
        }
        if (request.transactionCountMax() != null) {
            builder.and(
                    new CaseBuilder()
                            .when(member.loanApplication.isNull())
                            .then(0)
                            .otherwise(member.loanApplication.loanTransactions.size())
                            .loe(request.transactionCountMax())
            );
        }

        List<Member> members = queryFactory.selectFrom(member)
                .leftJoin(member.loanApplication, loanApplication).fetchJoin()
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(pageable.getSort().stream()
                        .map(order -> order.isAscending()
                                ? Expressions.stringPath(order.getProperty()).asc()
                                : Expressions.stringPath(order.getProperty()).desc())
                        .toArray(com.querydsl.core.types.OrderSpecifier[]::new))
                .fetch();

        // 전체 건수 (페이징 total)
        long total = queryFactory
                .selectFrom(member)
                .leftJoin(member.loanApplication, loanApplication)
                .where(builder)
                .fetchCount();

        return new PageImpl<>(members, pageable, total);
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
    public Float findLatestInterestRate(Long memberId) {
        QInterest interest = QInterest.interest;
        QLoanApplication loanApplication = QLoanApplication.loanApplication;
        QMember member = QMember.member;

        return queryFactory
                .select(interest.interestRate)
                .from(interest)
                .join(interest.loanApplication, loanApplication)
                .join(loanApplication.member, member)
                .where(member.memberId.eq(memberId))
                .orderBy(interest.interestDate.desc())
                .fetchFirst();
    }
}
