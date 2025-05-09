package com.flexrate.flexrate_back.loan.application.repository;

import com.flexrate.flexrate_back.loan.domain.LoanApplication;
import com.flexrate.flexrate_back.loan.domain.QLoanApplication;
import com.flexrate.flexrate_back.loan.dto.LoanAdminSearchRequest;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class LoanAdminQueryRepositoryImpl implements LoanAdminQueryRepository {

    private final JPAQueryFactory queryFactory;

    QLoanApplication loan = QLoanApplication.loanApplication;

    @Override
    public Page<LoanApplication> searchLoans(LoanAdminSearchRequest request, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        if (request.status() != null && !request.status().isEmpty()) {
            builder.and(loan.status.in(request.status()));
        }
        if (request.applicantId() != null) {
            builder.and(loan.member.memberId.eq(request.applicantId()));
        }
        if (request.applicant() != null && !request.applicant().isBlank()) {
            builder.and(loan.member.name.containsIgnoreCase(request.applicant()));
        }
        if (request.appliedFrom() != null) {
            builder.and(loan.appliedAt.goe(request.appliedFrom().atStartOfDay()));
        }
        if (request.appliedTo() != null) {
            builder.and(loan.appliedAt.loe(request.appliedTo().atTime(23, 59, 59)));
        }
        if (request.limitFrom() != null) {
            builder.and(loan.remainAmount.goe(request.limitFrom()));
        }
        if (request.limitTo() != null) {
            builder.and(loan.remainAmount.loe(request.limitTo()));
        }
        if (request.rateFrom() != null && request.rateFrom() > 0) {
            builder.and(loan.rate.goe(request.rateFrom()));
        }
        if (request.rateTo() != null && request.rateTo() > 0) {
            builder.and(loan.rate.loe(request.rateTo()));
        }
        if (request.prevLoanCountFrom() != null) {
            builder.and(loan.loanTransactions.size().goe(request.prevLoanCountFrom()));
        }
        if (request.prevLoanCountTo() != null) {
            builder.and(loan.loanTransactions.size().loe(request.prevLoanCountTo()));
        }
        if (request.type() != null) {
            builder.and(loan.loanType.eq(request.type()));
        }

        List<LoanApplication> content = queryFactory
                .selectFrom(loan)
                .where(builder)
                .orderBy(getOrderSpecifiers(pageable, loan))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(loan.count())
                .from(loan)
                .where(builder)
                .fetchOne();

        return PageableExecutionUtils.getPage(content, pageable, () -> total != null ? total : 0);
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(Pageable pageable, QLoanApplication loan) {
        if (pageable.getSort().isEmpty()) {
            return new OrderSpecifier[]{new OrderSpecifier<LocalDateTime>(Order.DESC, loan.appliedAt)};
        }
        return pageable.getSort().stream()
                .map(order -> {
                    PathBuilder<LoanApplication> pathBuilder = new PathBuilder<>(LoanApplication.class, loan.getMetadata().getName());
                    return new OrderSpecifier<Comparable>(
                            order.isAscending() ? Order.ASC : Order.DESC,
                            pathBuilder.get(order.getProperty(), Comparable.class)
                    );
                })
                .toArray(OrderSpecifier[]::new);
    }
}