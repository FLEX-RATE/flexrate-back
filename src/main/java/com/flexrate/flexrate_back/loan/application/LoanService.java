package com.flexrate.flexrate_back.loan.application;

import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.financialdata.domain.UserFinancialData;
import com.flexrate.flexrate_back.financialdata.domain.repository.UserFinancialDataQueryRepositoryImpl;
import com.flexrate.flexrate_back.financialdata.enums.UserFinancialCategory;
import com.flexrate.flexrate_back.financialdata.enums.UserFinancialDataType;
import com.flexrate.flexrate_back.loan.application.repository.InterestRepository;
import com.flexrate.flexrate_back.loan.application.repository.LoanApplicationRepository;
import com.flexrate.flexrate_back.loan.application.repository.LoanReviewHistoryRepository;
import com.flexrate.flexrate_back.loan.domain.Interest;
import com.flexrate.flexrate_back.loan.domain.LoanApplication;
import com.flexrate.flexrate_back.loan.domain.LoanProduct;
import com.flexrate.flexrate_back.loan.domain.LoanReviewHistory;
import com.flexrate.flexrate_back.loan.dto.LoanApplicationRequest;
import com.flexrate.flexrate_back.loan.dto.LoanApplicationResultResponse;
import com.flexrate.flexrate_back.loan.dto.LoanReviewApplicationRequest;
import com.flexrate.flexrate_back.loan.dto.LoanReviewApplicationResponse;
import com.flexrate.flexrate_back.loan.enums.LoanApplicationStatus;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.repository.MemberQueryRepositoryImpl;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import com.flexrate.flexrate_back.member.enums.ConsumeGoal;
import com.flexrate.flexrate_back.member.enums.Role;
import com.flexrate.flexrate_back.notification.enums.NotificationType;
import com.flexrate.flexrate_back.notification.event.NotificationEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 대출 상품 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 *
 * @since 2025.05.05
 * @author 유승한
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoanService {

    private final RestTemplate restTemplate;

    @Value("${fastapi.host}")
    private String fastApiHost;

    @Value("${fastapi.port}")
    private String fastApiPort;

    private static final float INTEREST_REDUCTION_STEP = 0.01f;

    // 심사 서버 URL (추후 변경 예정)
    private static final String SCREENING_SERVER_URL = "http://external-server/api";
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanReviewHistoryRepository loanReviewHistoryRepository;
    private final MemberRepository memberRepository;
    private final UserFinancialDataQueryRepositoryImpl userFinancialDataQueryRepository;
    private final InterestRepository interestRepository;
    private final NotificationEventPublisher notificationEventPublisher;

    /**
     * 대출 신청 사전 정보를 외부 심사 서버로 전달 후, 심사 결과 저장
     * @param request 대출 신청 기본 정보, Member 대출 신청자
     * @since 2025.04.28
     * @author 서채연
     */
    @Transactional
    public void preApply(LoanReviewApplicationRequest request, Member member) {
        log.info("대출 사전 심사 요청: memberId={}", member.getMemberId());

        LoanApplication loanApplication = loanApplicationRepository.findByMember(member)
                .orElseThrow(() -> {
                    log.warn("대출 사전 심사 실패:\n신청 내역 없음, memberId={}", member.getMemberId());
                    return new FlexrateException(ErrorCode.LOAN_NOT_FOUND);
                });

        if (loanApplication.getStatus() != LoanApplicationStatus.PRE_APPLIED) {
            log.warn("대출 사전 심사 실패:\n상태 불일치, memberId={}, status={}", member.getMemberId(), loanApplication.getStatus());
            throw new FlexrateException(ErrorCode.LOAN_APPLICATION_ALREADY_EXISTS);
        }

        LoanProduct product = member.getLoanApplication().getProduct();

        String fastApiUrl = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(fastApiHost)
                .port(fastApiPort)
                .path("/predict-initial-rate")
                .build()
                .toUriString();

        log.info("외부 심사 서버 호출 준비: memberId={}, fastApiUrl={}", member.getMemberId(), fastApiUrl);

        // FastAPI로 보낼 요청 JSON 생성
        Map<String, Object> fastApiRequest = new HashMap<>();
        fastApiRequest.put("AGE", member.getAge());
        fastApiRequest.put("SEX_CD", member.getSexCode()); // 1: 남성, 2: 여성
        fastApiRequest.put("TOTAL_SPEND", (float) calculateMonthlyTotalSpend(member)); // 소비 총액
        fastApiRequest.put("min_rate", product.getMinRate());
        fastApiRequest.put("max_rate", product.getMaxRate());
        fastApiRequest.put("credit_score", member.getLoanApplication().getCreditScore());

        // FastAPI 호출: 금리 예측
        Map<String, Object> rateResponse = restTemplate.postForObject(
                fastApiUrl,
                fastApiRequest,
                Map.class
        );

        if (rateResponse == null || rateResponse.get("initialRate") == null) {
            log.error("외부 심사 서버 응답 오류: memberId={}, 응답={}", member.getMemberId(), rateResponse);
            throw new FlexrateException(ErrorCode.LOAN_SERVER_ERROR);
        }

        float initialRate = ((Number) rateResponse.get("initialRate")).floatValue();

        // 금리 범위 포함한 응답 생성
        LoanReviewApplicationResponse externalResponse = LoanReviewApplicationResponse.builder()
                .name(member.getName())
                .screeningDate(LocalDate.now().toString())
                .loanLimit(product.getMaxAmount())  // 예시
                .initialRate(initialRate)
                .rateRangeFrom(product.getMinRate())
                .rateRangeTo(product.getMaxRate())
                .creditScore(member.getLoanApplication().getCreditScore())
                .terms(product.getTerms())
                .build();

        // 대출 신청 사전 정보 저장
        LoanReviewHistory loanReviewHistory = loanApplication.getReviewHistory();

        if (loanReviewHistory != null) {
            loanReviewHistory.updateReview(
                    request.employmentType(),
                    request.residenceType(),
                    request.loanPurpose(),
                    request.annualIncome(),
                    request.isBankrupt()
            );
        } else {
            loanReviewHistory = LoanReviewHistory.builder()
                    .employmentType(request.employmentType())
                    .annualIncome(request.annualIncome())
                    .residenceType(request.residenceType())
                    .isBankrupt(request.isBankrupt())
                    .loanPurpose(request.loanPurpose())
                    .application(loanApplication)
                    .build();
        }

        loanReviewHistoryRepository.save(loanReviewHistory);
        loanApplication.applyReviewResult(externalResponse, loanReviewHistory);

        log.info("대출 사전 심사 결과 저장 완료: memberId={}, initialRate={}", member.getMemberId(), initialRate);
    }

    /**
     * 대출 심사 결과 조회
     * @param member 대출 신청자
     * @since 2025.05.06
     * @author 유승한
     */
    public LoanReviewApplicationResponse preApplyResult(Member member) {
        log.info("대출 사전 심사 결과 조회 요청: memberId={}", member.getMemberId());

        LoanApplication loanApplication = loanApplicationRepository.findByMember(member)
                .orElseThrow(() -> {
                    log.warn("대출 사전 심사 결과 조회 실패:\n신청 내역 없음, memberId={}", member.getMemberId());
                    return new FlexrateException(ErrorCode.LOAN_NOT_FOUND);
                });

        if (loanApplication.getStatus() != LoanApplicationStatus.PRE_APPLIED) {
            log.warn("대출 사전 심사 결과 조회 실패:\n상태 불일치, memberId={}, status={}", member.getMemberId(), loanApplication.getStatus());
            throw new FlexrateException(ErrorCode.LOAN_APPLICATION_ALREADY_EXISTS);
        }

        log.info("대출 사전 심사 결과 반환: memberId={}, status={}", member.getMemberId(), loanApplication.getStatus());

        return LoanReviewApplicationResponse.builder()
                .name(member.getName())
                .screeningDate(loanApplication.getAppliedAt() != null ? loanApplication.getAppliedAt().toLocalDate().toString() : null)
                .loanLimit(loanApplication.getTotalAmount())
                .initialRate(loanApplication.getRate())
                .rateRangeFrom(loanApplication.getProduct().getMinRate())
                .rateRangeTo(loanApplication.getProduct().getMaxRate())
                .terms(loanApplication.getProduct().getTerms())
                .build();
    }

    /**
     * 대출 신청
     * @param member 대출 신청자
     * @since 2025.05.06
     * @author 유승한
     */
    @Transactional
    public void applyLoan(Member member, LoanApplicationRequest loanApplicationRequest) {
        log.info("대출 신청 요청: memberId={}, 신청금액={}", member.getMemberId(), loanApplicationRequest.loanAmount());

        LoanApplication loanApplication = loanApplicationRepository.findByMember(member)
                .orElseThrow(() -> {
                    log.warn("대출 신청 실패:\n신청 내역 없음, memberId={}", member.getMemberId());
                    return new FlexrateException(ErrorCode.LOAN_NOT_FOUND);
                });

        if (loanApplication.getStatus() != LoanApplicationStatus.PRE_APPLIED) {
            log.warn("대출 신청 실패:\n상태 불일치, memberId={}, status={}", member.getMemberId(), loanApplication.getStatus());
            throw new FlexrateException(ErrorCode.LOAN_APPLICATION_ALREADY_EXISTS);
        }

        if(loanApplication.getProduct().getMaxAmount() < loanApplicationRequest.loanAmount()){
            log.warn("대출 신청 실패:\n한도 초과, memberId={}, 신청금액={}, 한도={}", member.getMemberId(), loanApplicationRequest.loanAmount(), loanApplication.getProduct().getMaxAmount());
            throw new FlexrateException(ErrorCode.LOAN_REQUEST_CONFLICT);
        }

        if(loanApplication.getProduct().getTerms() < loanApplicationRequest.repaymentMonth()){
            log.warn("대출 신청 실패:\n기한 초과, memberId={}, 요청기한={}, 최대기한={}", member.getMemberId(), loanApplicationRequest.repaymentMonth(), loanApplication.getProduct().getTerms());
            throw new FlexrateException(ErrorCode.LOAN_REQUEST_CONFLICT);
        }

        loanApplication.applyLoan(loanApplicationRequest);

        log.info("대출 신청 완료: memberId={}, 신청금액={}, 기한={}", member.getMemberId(), loanApplicationRequest.loanAmount(), loanApplicationRequest.repaymentMonth());
    }
    /**
     * 대출 결과 조회
     * @param member 대출 신청자
     * @since 2025.05.06
     * @author 유승한
     */
    public LoanApplicationResultResponse getLoanApplicationResult(Member member) {
        log.info("대출 결과 조회 요청: memberId={}", member.getMemberId());

        LoanApplication loanApplication = loanApplicationRepository.findByMember(member)
                .orElseThrow(() -> {
                    log.warn("대출 결과 조회 실패:\n신청 내역 없음, memberId={}", member.getMemberId());
                    return new FlexrateException(ErrorCode.LOAN_NOT_FOUND);
                });

        if(loanApplication.getStatus() == LoanApplicationStatus.PRE_APPLIED){
            log.warn("대출 결과 조회 실패:\n아직 미신청 상태, memberId={}", member.getMemberId());
            throw new FlexrateException(ErrorCode.LOAN_NOT_APPLIED);
        }

        log.info("대출 결과 반환: memberId={}, status={}", member.getMemberId(), loanApplication.getStatus());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return LoanApplicationResultResponse.builder()
                .loanApplicationResult(loanApplication.getStatus().name())
                .loanApplicationAmount(loanApplication.getTotalAmount())
                .loanInterestRate(loanApplication.getRate())
                .loanStartDate(loanApplication.getStartDate().format(formatter))
                .loanEndDate(loanApplication.getEndDate().format(formatter))
                .build();
    }

    private int calculateMonthlyTotalSpend(Member member) {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);

        return member.getFinancialData().stream()
                .filter(data -> data.getDataType() == UserFinancialDataType.EXPENSE)
                .filter(data -> data.getCollectedAt().isAfter(oneMonthAgo))
                .mapToInt(UserFinancialData::getValue)
                .sum();
    }

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정
    public void evaluateConsumeGoalAndCreateInterests() {
        log.info("금리 평가 및 생성 스케줄 시작");

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        List<Member> members = memberRepository.findAllWithLoanApplication();

        for (Member member : members) {
            // 관리자 또는 대출 미신청자는 건너뜀
            if (Role.ADMIN == member.getRole() || member.getLoanApplication() == null) continue;

            Interest yesterdayInterest = interestRepository
                    .findByInterestDateAndLoanApplication(yesterday, member.getLoanApplication())
                    .orElseThrow(() -> {
                        log.error("전일 금리 정보 없음: memberId={}, date={}", member.getMemberId(), yesterday);
                        return new FlexrateException(ErrorCode.NO_INTEREST);
                    });

            float previousRate = yesterdayInterest.getInterestRate();
            float minRate = member.getLoanApplication().getProduct().getMinRate();

            // 전일자 재무 데이터 조회
            List<UserFinancialData> dataList = userFinancialDataQueryRepository
                    .findUserFinancialDataOfMemberForYesterday(member.getMemberId(), yesterday);

            ConsumeGoal goal = member.getConsumeGoal();
            boolean matched = evaluateGoal(goal, dataList);

            float finalRate = matched && (previousRate - INTEREST_REDUCTION_STEP >= minRate)
                    ? previousRate - INTEREST_REDUCTION_STEP
                    : previousRate;

            if(finalRate == previousRate) matched = false;

            Interest interest = Interest.builder()
                    .loanApplication(member.getLoanApplication())
                    .interestRate(finalRate)
                    .interestDate(today)
                    .interestChanged(matched)
                    .build();

            interestRepository.save(interest);

            log.info("금리 평가 결과: memberId={}, 이전금리={}, 변경금리={}, 목표달성={}", member.getMemberId(), previousRate, finalRate, matched);

            // 금리 변동 알림
            if (interest.getInterestChanged()) {
                try {
                    notificationEventPublisher.sendInterestNotification(
                            member,
                            interest.getInterestId(),
                            NotificationType.INTEREST_RATE_CHANGE
                    );
                    log.info("금리 변동 알림 발송 성공:\nmemberId={}, interestId={}", member.getMemberId(), interest.getInterestId());
                } catch (Exception e) {
                    log.error("금리 변동 알림 발송 실패:\nmemberId={}, interestId={}, error={}", member.getMemberId(), interest.getInterestId(), e.getMessage(), e);
                }
            }
        }
    }

    private boolean evaluateGoal(ConsumeGoal goal, List<UserFinancialData> dataList) {
        LocalDate today = LocalDate.now();
        int income = dataList.stream()
                .filter(d -> d.getDataType() == UserFinancialDataType.INCOME)
                .mapToInt(UserFinancialData::getValue)
                .sum();
        int expense = dataList.stream()
                .filter(d -> d.getDataType() == UserFinancialDataType.EXPENSE)
                .mapToInt(UserFinancialData::getValue)
                .sum();

        log.info("소비 목표 평가 시작: goal={}, 수입={}, 지출={}, 데이터 건수={}", goal, income, expense, dataList.size());

        boolean result;

        switch (goal) {
            case NO_SPENDING_TODAY:
                int todaySpend = dataList.stream()
                        .filter(d -> d.getCollectedAt().toLocalDate().equals(today))
                        .mapToInt(UserFinancialData::getValue)
                        .sum();
                result = todaySpend == 0;
                log.debug("NO_SPENDING_TODAY 평가: 오늘 지출={}, 결과={}", todaySpend, result);
                break;

            case LIMIT_DAILY_MEAL:
                int todayMeal = dataList.stream()
                        .filter(d -> d.getCategory() == UserFinancialCategory.FOOD &&
                                d.getCollectedAt().toLocalDate().equals(today))
                        .mapToInt(UserFinancialData::getValue)
                        .sum();
                result = todayMeal <= 10000;
                log.debug("LIMIT_DAILY_MEAL 평가: 오늘 식비={}, 결과={}", todayMeal, result);
                break;

            case SAVE_70_PERCENT:
                result = income > 0 && (income - expense) >= income * 0.7;
                log.debug("SAVE_70_PERCENT 평가: 수입={}, 지출={}, 결과={}", income, expense, result);
                break;

            case INCOME_OVER_EXPENSE:
                result = income > expense;
                log.debug("INCOME_OVER_EXPENSE 평가: 수입={}, 지출={}, 결과={}", income, expense, result);
                break;

            case ONLY_PUBLIC_TRANSPORT:
                long transportCount = dataList.stream()
                        .filter(d -> d.getCategory() == UserFinancialCategory.TRANSPORT &&
                                d.getCollectedAt().toLocalDate().equals(today))
                        .count();
                long allTransport = dataList.stream()
                        .filter(d -> d.getCollectedAt().toLocalDate().equals(today))
                        .count();
                result = transportCount == allTransport;
                log.debug("ONLY_PUBLIC_TRANSPORT 평가: 오늘 교통건수={}, 전체건수={}, 결과={}", transportCount, allTransport, result);
                break;

            case COMPARE_BEFORE_BUYING:
                result = true;
                log.debug("COMPARE_BEFORE_BUYING 평가: 임의 true 반환");
                break;

            case HAS_HOUSING_SAVING:
                result = dataList.stream()
                        .anyMatch(d -> d.getDataType() == UserFinancialDataType.INCOME);
                log.debug("HAS_HOUSING_SAVING 평가: 주택청약저축 여부 결과={}", result);
                break;

            case CLOTHING_UNDER_100K:
                result = dataList.stream()
                        .filter(d -> d.getCategory() == UserFinancialCategory.CLOTHING)
                        .allMatch(d -> d.getValue() <= 100000);
                log.debug("CLOTHING_UNDER_100K 평가: 결과={}", result);
                break;

            case ONE_CATEGORY_SPEND:
                long categoryCount = dataList.stream()
                        .filter(d -> d.getCollectedAt().toLocalDate().equals(today))
                        .map(UserFinancialData::getCategory)
                        .distinct()
                        .count();
                result = categoryCount == 1;
                log.debug("ONE_CATEGORY_SPEND 평가: 오늘 카테고리수={}, 결과={}", categoryCount, result);
                break;

            case SMALL_MONTHLY_SAVE:
                int monthlySaving = dataList.stream()
                        .filter(d -> d.getDataType() == UserFinancialDataType.INCOME)
                        .mapToInt(UserFinancialData::getValue)
                        .sum();
                result = monthlySaving >= 10000;
                log.debug("SMALL_MONTHLY_SAVE 평가: 월저축액={}, 결과={}", monthlySaving, result);
                break;

            case NO_USELESS_ELECTRONICS:
                result = dataList.stream()
                        .filter(d -> d.getCategory() == UserFinancialCategory.ELECTRONICS)
                        .allMatch(d -> d.getValue() <= 100000);
                log.debug("NO_USELESS_ELECTRONICS 평가: 결과={}", result);
                break;

            case OVER_10_PERCENT:
                double avgExpense = dataList.stream()
                        .filter(d -> d.getDataType().name().startsWith("EXPENSE"))
                        .mapToInt(UserFinancialData::getValue)
                        .average()
                        .orElse(0.0);
                int todayExpense = dataList.stream()
                        .filter(d -> d.getCollectedAt().toLocalDate().equals(today))
                        .mapToInt(UserFinancialData::getValue)
                        .sum();
                result = todayExpense > avgExpense * 1.1;
                log.debug("OVER_10_PERCENT 평가: 오늘지출={}, 평균지출={}, 결과={}", todayExpense, avgExpense, result);
                break;

            case NO_EXPENSIVE_DESSERT:
                result = dataList.stream()
                        .filter(d -> d.getCategory() == UserFinancialCategory.CAFE)
                        .noneMatch(d -> d.getValue() > 10000);
                log.debug("NO_EXPENSIVE_DESSERT 평가: 결과={}", result);
                break;

            case NO_OVER_50K_PER_DAY:
                int dailyTotal = dataList.stream()
                        .filter(d -> d.getCollectedAt().toLocalDate().equals(today))
                        .mapToInt(UserFinancialData::getValue)
                        .sum();
                result = dailyTotal <= 50000;
                log.debug("NO_OVER_50K_PER_DAY 평가: 오늘합계={}, 결과={}", dailyTotal, result);
                break;

            case SUBSCRIPTION_UNDER_50K:
                int monthlySubscription = dataList.stream()
                        .filter(d -> d.getCategory() == UserFinancialCategory.SUBSCRIPTION)
                        .mapToInt(UserFinancialData::getValue)
                        .sum();
                result = monthlySubscription <= 50000;
                log.debug("SUBSCRIPTION_UNDER_50K 평가: 월구독료={}, 결과={}", monthlySubscription, result);
                break;

            case MEAL_UNDER_20K:
                result = dataList.stream()
                        .filter(d -> d.getCategory() == UserFinancialCategory.FOOD)
                        .allMatch(d -> d.getValue() <= 20000);
                log.debug("MEAL_UNDER_20K 평가: 결과={}", result);
                break;

            default:
                log.warn("정의되지 않은 목표 평가 요청: goal={}", goal);
                result = false;
        }

        log.info("소비 목표 평가 종료: goal={}, 평가결과={}", goal, result);
        return result;
    }

}
