package com.flexrate.flexrate_back.financialdata.domain.repository;

import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.financialdata.domain.QUserFinancialData;
import com.flexrate.flexrate_back.financialdata.domain.UserFinancialData;
import com.flexrate.flexrate_back.financialdata.enums.UserFinancialDataType;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.QMember;
import com.flexrate.flexrate_back.report.dto.ConsumptionCategoryRatioResponse;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserFinancialDataQueryRepositoryImpl implements UserFinancialDataQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 특정 회원의 특정 월 소비 데이터를 기준으로 카테고리별 소비 금액과 비율 계산
     * @param member 조회 대상 회원
     * @param month 조회할 월 (yyyy-MM)
     * @return 카테고리별 소비 금액과 비율 리스트
     * @throws FlexrateException REPORT_MEMBER_OR_MONTH_NULL member 또는 month가 null일 경우
     * @since 2025.05.08
     * @author 서채연
     */
    @Override
    public List<ConsumptionCategoryRatioResponse> findCategoryStatsWithRatio(Member member, YearMonth month) {
        QUserFinancialData u = QUserFinancialData.userFinancialData;

        if (member == null || month == null) {
            throw new FlexrateException(ErrorCode.REPORT_MEMBER_OR_MONTH_NULL);
        }

        LocalDateTime start = month.atDay(1).atStartOfDay();
        LocalDateTime end = month.plusMonths(1).atDay(1).atStartOfDay();

        BooleanBuilder builder = new BooleanBuilder()
                .and(u.member.memberId.eq(member.getMemberId()))
                .and(u.dataType.eq(UserFinancialDataType.EXPENSE))
                .and(u.collectedAt.goe(start))
                .and(u.collectedAt.lt(end));

        Integer totalAmount = queryFactory
                .select(u.value.sum())
                .from(u)
                .where(builder)
                .fetchOne();

        if (totalAmount == null || totalAmount == 0) {
            return List.of();
        }

        return queryFactory
                .select(
                        u.category.stringValue(),
                        u.value.sum()
                )
                .from(u)
                .where(builder)
                .groupBy(u.category)
                .fetch()
                .stream()
                .map(tuple -> {
                    String category = tuple.get(0, String.class);
                    Integer amount = tuple.get(1, Integer.class);
                    double percentage = Math.round((amount * 1000.0 / totalAmount)) / 10.0;
                    return new ConsumptionCategoryRatioResponse(category, amount, percentage);
                })
                .toList();
    }

    @Override
    public List<UserFinancialData> findUserFinancialDataOfMemberForYesterday(Long memberId, LocalDate date) {
        QUserFinancialData data = QUserFinancialData.userFinancialData;
        QMember member = QMember.member;

        return queryFactory
                .selectFrom(data)
                .join(data.member, member).fetchJoin()
                .where(
                        member.memberId.eq(memberId),
                        data.collectedAt.between(
                                date.atStartOfDay(),
                                date.plusDays(1).atStartOfDay().minusNanos(1)
                        )
                )
                .fetch();
    }


}
