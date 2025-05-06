package com.flexrate.flexrate_back.financialdata.application;

import com.flexrate.flexrate_back.financialdata.domain.UserFinancialData;
import com.flexrate.flexrate_back.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserFinantialDataService {
    /**
     * 신용점수 평가
     * 임의로 만들었으며 추후 조정 필요
     * @param member 대출 신청자
     * @return 계산된 신용점수 (0~1000 범위)
     * @since 2025.05.06
     * @author 유승한
     */
    public int evaluateCreditScore(Member member) {
        List<UserFinancialData> financialDataList = member.getFinancialData();

        int income = 0;
        int expense = 0;
        int loanBalance = 0;

        for (UserFinancialData data : financialDataList) {
            switch (data.getDataType()) {
                case INCOME -> income += data.getValue();
                case EXPENSE -> expense += data.getValue();
                case LOAN_BALANCE -> loanBalance += data.getValue();
            }
        }

        // 기본 가중치 기반 점수 계산 (가상의 기준, 필요 시 조정)
        double score = 600.0;

        // 소득이 많을수록 가산
        score += Math.min(income / 100_000, 100); // 소득 10만원당 1점, 최대 100점

        // 지출이 많을수록 감산
        score -= Math.min(expense / 100_000, 100); // 지출 10만원당 -1점, 최대 -100점

        // 대출 잔액이 많을수록 감산
        score -= Math.min(loanBalance / 500_000, 200); // 500만원당 -1점, 최대 -200점

        // 점수 제한 범위 조정 (0~1000)
        return (int) Math.max(0, Math.min(score, 1000));
    }

}
