package com.flexrate.flexrate_back.loan.application.repository;

import com.flexrate.flexrate_back.loan.domain.LoanApplication;
import com.flexrate.flexrate_back.loan.domain.QInterest;
import com.flexrate.flexrate_back.loan.domain.QLoanTransaction;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
@RequiredArgsConstructor
public class InterestQueryRepositoryImpl implements InterestQueryRepository {
    private final JPAQueryFactory queryFactory;
    public int countByConditionsWithInterestChangedTrue(
            LoanApplication app,
            LocalDate startDate,
            LocalDate endDate
    ) {
        QLoanTransaction loanTransaction = QLoanTransaction.loanTransaction;
        QInterest interest = QInterest.interest;

        return queryFactory.selectFrom(interest)
                .where(
                        interest.loanApplication.eq(app),
                        interest.interestDate.between(startDate, endDate),
                        interest.interestChanged.isTrue()
                )
                .fetch().size();
    }

}
