package com.flexrate.flexrate_back.financialdata.application;

import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.financialdata.domain.UserFinancialData;
import com.flexrate.flexrate_back.loan.application.repository.LoanApplicationRepository;
import com.flexrate.flexrate_back.loan.domain.LoanApplication;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.MemberCreditSummary;
import com.flexrate.flexrate_back.member.domain.repository.MemberCreditSummaryRepository;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserFinancialDataService {
    private final LoanApplicationRepository loanApplicationRepository;
    private final MemberCreditSummaryRepository memberCreditSummaryRepository;

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
        if(member == null) {
            throw new FlexrateException(ErrorCode.USER_NOT_FOUND);
        }

        log.info("신용점수 평가 시작:\nmemberId={}", member.getMemberId());
        LoanApplication loanApplication = loanApplicationRepository.findByMember(member)
                .orElseThrow(() -> {
                    log.warn("대출 신청 정보 없음:\nmemberId={}", member.getMemberId());
                    return new FlexrateException(ErrorCode.LOAN_NOT_FOUND);
                });
        log.info("LoanApplication 조회 완료: applicationId={}", loanApplication.getApplicationId());
        log.info("현재 LoanApplication의 신용점수: {}", loanApplication.getCreditScore());

        List<UserFinancialData> financialDataList = member.getFinancialData();

        // 평가 요소별 랜덤 값 생성 (기준 반영)
        int commOverdueCount = getRandomInt(0, 11);         // 통신비 연체 횟수
        int commMaxOverdueDays = getRandomInt(0, 91);      // 통신비 최장 연체일
        int utilityOverdueCount = getRandomInt(0, 11);      // 공과금 연체 횟수
        int utilityMaxOverdueDays = getRandomInt(0, 91);   // 공과금 최장 연체일

        int totalLoanCount = getRandomInt(0, 6);            // 과거 대출 건수
        int loanOverdue30d = getRandomInt(0, 5);            // 30일 연체 횟수
        int loanOverdue90d = getRandomInt(0, 4);            // 90일 연체 횟수
        int activeLoanCount = getRandomInt(0, Math.min(totalLoanCount, 6)); // 현재 보유 대출

        boolean hasCurrentOverdue = getRandomBoolean();     // 현재 연체 여부
        int currentOverdueDays = hasCurrentOverdue ? getRandomInt(1, 91) : 0;

        log.info("=== 생성된 랜덤값들 ===");
        log.info("통신비 관련 - 연체횟수: {}, 최장연체일: {}", commOverdueCount, commMaxOverdueDays);
        log.info("공과금 관련 - 연체횟수: {}, 최장연체일: {}", utilityOverdueCount, utilityMaxOverdueDays);
        log.info("대출 관련 - 총대출건수: {}, 30일연체: {}, 90일연체: {}, 현재보유대출: {}",
                totalLoanCount, loanOverdue30d, loanOverdue90d, activeLoanCount);
        log.info("현재 연체 - 연체여부: {}, 연체일수: {}", hasCurrentOverdue, currentOverdueDays);

        // 소득/지출 계산
        int income = 0;
        int expense = 0;
        for (UserFinancialData data : financialDataList) {
            switch (data.getDataType()) {
                case INCOME -> income += data.getValue();
                case EXPENSE -> expense += data.getValue();
            }
        }
        log.info("재정 상태 - 소득: {}, 지출: {}, 지출비율: {}",
                income, expense, income == 0 ? "무한대" : String.format("%.2f%%", (double) expense / income * 100));

        int totalScore = 0;

        // 1. 통신비 및 공과금 납부이력 (25% - 250점 만점)
        int maxCommUtilityDays = Math.max(commMaxOverdueDays, utilityMaxOverdueDays);
        int totalCommUtilityCount = commOverdueCount + utilityOverdueCount;

        if (commMaxOverdueDays > 180 || utilityMaxOverdueDays > 180) {
            totalScore += 0;
        } else if (commOverdueCount == 0 && utilityOverdueCount == 0 &&
                commMaxOverdueDays == 0 && utilityMaxOverdueDays == 0) {
            totalScore += 250;
        } else if (totalCommUtilityCount <= 2 && maxCommUtilityDays <= 30) {
            totalScore += 200;
        } else if (totalCommUtilityCount <= 5 && maxCommUtilityDays <= 60) {
            totalScore += 150;
        } else if (totalCommUtilityCount <= 10 || maxCommUtilityDays <= 90) {
            totalScore += 100;
        } else {
            totalScore += 50;
        }

        // 2. 과거 대출 상환 이력 (15% - 150점 만점)
        if (totalLoanCount >= 2 && loanOverdue30d == 0 && loanOverdue90d == 0) {
            totalScore += 150;
        } else if (totalLoanCount >= 1 && loanOverdue30d <= 1) {
            totalScore += 120;
        } else if (totalLoanCount >= 1 && loanOverdue30d <= 3) {
            totalScore += 80;
        } else if ((loanOverdue30d + loanOverdue90d) > 0) {
            totalScore += 50;
        } else {
            totalScore += 75;
        }

        // 3. 보유 대출 건수 (10% - 100점 만점)
        if (activeLoanCount == 0) {
            totalScore += 100;
        } else if (activeLoanCount == 1) {
            totalScore += 80;
        } else if (activeLoanCount <= 3) {
            totalScore += 60;
        } else if (activeLoanCount <= 5) {
            totalScore += 30;
        } else {
            totalScore += 0;
        }

        // 4. 연체 이력 (15% - 150점 만점)
        int totalOverdueCount = loanOverdue30d + loanOverdue90d;
        if (totalOverdueCount == 0) {
            totalScore += 150;
        } else if (loanOverdue30d <= 2 && loanOverdue90d == 0) {
            totalScore += 120;
        } else if (loanOverdue30d <= 4 && loanOverdue90d <= 1) {
            totalScore += 80;
        } else if (loanOverdue90d >= 2) {
            totalScore += 40;
        } else if (loanOverdue90d >= 3 || totalOverdueCount >= 10) {
            totalScore += 0;
        }

        // 5. 현재 연체 여부 (15% - 150점 만점)
        if (!hasCurrentOverdue) {
            totalScore += 150;
        } else if (currentOverdueDays <= 30) {
            totalScore += 100;
        } else if (currentOverdueDays <= 90) {
            totalScore += 50;
        } else {
            totalScore += 0;
        }

        // 6. 소득 대비 지출 관리 (20% - 200점 만점)
        int incomeScore = 0;
        if (income >= 200) incomeScore = 100;
        else if (income >= 150) incomeScore = 80;
        else if (income >= 100) incomeScore = 60;
        else if (income >= 50) incomeScore = 30;
        else incomeScore = 0;

        double spendRatio = income == 0 ? 1.0 : (double) expense / income;
        int expenseScore = 0;
        if (spendRatio <= 0.5) expenseScore = 100;
        else if (spendRatio <= 0.7) expenseScore = 70;
        else if (spendRatio <= 0.9) expenseScore = 40;
        else expenseScore = 0;

        totalScore += (incomeScore + expenseScore);

        // 점수 제한 범위 조정 (1~1000, 기본값 500 적용)
        int finalScore = Math.max(500, Math.min(totalScore, 1000));

        log.info("=== 신용점수 계산 완료 ===");
        log.info("계산된 점수: {}", finalScore);

        MemberCreditSummary creditSummary = MemberCreditSummary.builder()
                .loanApplication(loanApplication)
                .totalLoanCount(totalLoanCount)
                .activeLoanCount(activeLoanCount)
                .totalLoanBalance(getRandomInt(0, 50000000))
                .totalLoanOverdue30d(loanOverdue30d)
                .totalLoanOverdue90d(loanOverdue90d)
                .hasCurrentOverdue(hasCurrentOverdue)
                .lastOverdueDate(hasCurrentOverdue ?
                        LocalDate.now().minusDays(getRandomInt(1, 365)) : null)
                .commOverdueCount(commOverdueCount)
                .commOverdueMaxDays(commMaxOverdueDays)
                .utilityOverdueCount(utilityOverdueCount)
                .utilityOverdueMaxDays(utilityMaxOverdueDays)
                .creditScore(finalScore)
                .build();

        log.info("=== MemberCreditSummary 저장 시작 ===");
        MemberCreditSummary savedSummary = memberCreditSummaryRepository.save(creditSummary);
        log.info("MemberCreditSummary 저장 완료: summaryId={}, creditScore={}",
                savedSummary.getSummaryId(), savedSummary.getCreditScore());

        // 점수 적용
        log.info("=== LoanApplication 신용점수 업데이트 시작 ===");
        log.info("업데이트 전 LoanApplication 신용점수: {}", loanApplication.getCreditScore());

        loanApplication.patchCreditScore(finalScore);

        log.info("patchCreditScore 호출 후 LoanApplication 신용점수: {}", loanApplication.getCreditScore());

        // Member 업데이트
        log.info("=== Member 신용점수 평가 상태 업데이트 ===");
        log.info("업데이트 전 Member creditScoreEvaluated: {}", member.getCreditScoreEvaluated());

        member.updateCreditScoreEvaluated(true);

        log.info("업데이트 후 Member creditScoreEvaluated: {}", member.getCreditScoreEvaluated());

        log.info("=== 신용점수 평가 완료 ===");
        log.info("memberId: {}, applicationId: {}, 최종점수: {}",
                member.getMemberId(), loanApplication.getApplicationId(), finalScore);

        return finalScore;
    }

    // 랜덤값 메서드
    private int getRandomInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    private boolean getRandomBoolean() {
        return ThreadLocalRandom.current().nextBoolean();
    }


    public int getCreditScorePercentile(int score) {
        log.info("신용점수 백분위 조회:\nscore={}", score);
        return loanApplicationRepository.findCreditScorePercentile(score);
    }

    @Transactional
    public List<YearMonth> getReportAvailableMonths(Member member) {
        log.info("리포트 조회 가능 월 리스트 요청:\nmemberId={}", member.getMemberId());

        return member.getFinancialData().stream()
                .map(UserFinancialData::getCollectedAt)
                .map(YearMonth::from)
                .distinct()
                .sorted()
                .toList();
    }
}
