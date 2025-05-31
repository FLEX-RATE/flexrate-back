package com.flexrate.flexrate_back.financialdata.application;

import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.financialdata.domain.UserFinancialData;
import com.flexrate.flexrate_back.loan.application.repository.LoanApplicationRepository;
import com.flexrate.flexrate_back.loan.domain.LoanApplication;
import com.flexrate.flexrate_back.member.domain.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserFinancialDataService {
    private final LoanApplicationRepository loanApplicationRepository;

    /**
     * 신용점수 평가
     * 임의로 만들었으며 추후 조정 필요
     * @param member 대출 신청자
     * @return 계산된 신용점수 (0~1000 범위)
     * @since 2025.05.06
     * @author 유승한
     */
    @Transactional
    public int evaluateCreditScore(Member member) {
        log.debug("신용점수 평가 시작:\nmemberId={}", member.getMemberId());

        List<UserFinancialData> financialDataList = member.getFinancialData();

        // 신용 점수 평가는 대출 상품 선정 이후여야함
        LoanApplication loanApplication = loanApplicationRepository.findByMember(member)
                .orElseThrow(() -> {
                    log.warn("대출 신청 정보 없음:\nmemberId={}", member.getMemberId());
                    return new FlexrateException(ErrorCode.LOAN_NOT_FOUND);
                });

        int income = 0;
        int expense = 0;

        for (UserFinancialData data : financialDataList) {
            switch (data.getDataType()) {
                case INCOME -> income += data.getValue();
                case EXPENSE -> expense += data.getValue();
            }
        }

        // 기본 가중치 기반 점수 계산 (가상의 기준, 필요 시 조정)
        double score = 600.0;

        // 소득이 많을수록 가산
        score += Math.min(income / 100_000, 100); // 소득 10만원당 1점, 최대 100점

        // 지출이 많을수록 감산
        score -= Math.min(expense / 100_000, 100); // 지출 10만원당 -1점, 최대 -100점

        // 점수 제한 범위 조정 (1~1000)
        int finalScore = (int) Math.max(1, Math.min(score, 1000));

        // 신용 점수 적용
        loanApplication.patchCreditScore(finalScore);
        member.updateCreditScoreEvaluated(true);

        log.info("신용점수 평가 완료:\nmemberId={}, 최종점수={}", member.getMemberId(), finalScore);

        return finalScore;
    }

    public int getCreditScorePercentile(int score) {
        log.debug("신용점수 백분위 조회:\nscore={}", score);
        return loanApplicationRepository.findCreditScorePercentile(score);
    }

    @Transactional
    public List<YearMonth> getReportAvailableMonths(Member member) {
        log.debug("리포트 조회 가능 월 리스트 요청:\nmemberId={}", member.getMemberId());

        return member.getFinancialData().stream()
                .map(UserFinancialData::getCollectedAt)
                .map(YearMonth::from)
                .distinct()
                .sorted()
                .toList();
    }
}
