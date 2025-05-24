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
import lombok.RequiredArgsConstructor;
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
    /**
     * 대출 신청 사전 정보를 외부 심사 서버로 전달 후, 심사 결과 저장
     * @param request 대출 신청 기본 정보, Member 대출 신청자
     * @since 2025.04.28
     * @author 서채연
     */
    @Transactional
    public void preApply(LoanReviewApplicationRequest request, Member member) {
        LoanApplication loanApplication = loanApplicationRepository.findByMember(member)
                .orElseThrow(() -> new FlexrateException(ErrorCode.LOAN_NOT_FOUND));

        if (loanApplication.getStatus() != LoanApplicationStatus.PRE_APPLIED) {
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
        LoanReviewHistory loanReviewHistory = LoanReviewHistory.builder()
                .employmentType(request.employmentType())
                .annualIncome(request.annualIncome())
                .residenceType(request.residenceType())
                .isBankrupt(request.isBankrupt())
                .loanPurpose(request.loanPurpose())
                .application(loanApplication)
                .build();

        loanReviewHistoryRepository.save(loanReviewHistory);
        loanApplication.applyReviewResult(externalResponse, loanReviewHistory);
    }

    /**
     * 대출 심사 결과 조회
     * @param member 대출 신청자
     * @since 2025.05.06
     * @author 유승한
     */
    public LoanReviewApplicationResponse preApplyResult(Member member) {
        LoanApplication loanApplication = loanApplicationRepository.findByMember(member)
                .orElseThrow(() -> new FlexrateException(ErrorCode.LOAN_NOT_FOUND));

        if (loanApplication.getStatus() !=  LoanApplicationStatus.PRE_APPLIED){
            throw new FlexrateException(ErrorCode.LOAN_APPLICATION_ALREADY_EXISTS); // 이미 대출 중인 경우 예외처리
        }


        return LoanReviewApplicationResponse.builder()
                .name(member.getName())
                .screeningDate(loanApplication.getAppliedAt().toLocalDate().toString())
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
        LoanApplication loanApplication = loanApplicationRepository.findByMember(member)
                .orElseThrow(() -> new FlexrateException(ErrorCode.LOAN_NOT_FOUND));

        if (loanApplication.getStatus() != LoanApplicationStatus.PRE_APPLIED) {
            throw new FlexrateException(ErrorCode.LOAN_APPLICATION_ALREADY_EXISTS);
        }

        // 한도를 초과한 대출금액 요청 시 예외 처리
        if(loanApplication.getProduct().getMaxAmount() < loanApplicationRequest.loanAmount()){
            throw new FlexrateException(ErrorCode.LOAN_REQUEST_CONFLICT);
        }

        // 최대 대출 기한을 초과한 기한 요청 시 예외 처리
        if(loanApplication.getProduct().getTerms() < loanApplicationRequest.repaymentMonth()){
            throw new FlexrateException(ErrorCode.LOAN_REQUEST_CONFLICT);
        }

        // 신청 정보 반영
        loanApplication.applyLoan(loanApplicationRequest);
    }
    /**
     * 대출 결과 조회
     * @param member 대출 신청자
     * @since 2025.05.06
     * @author 유승한
     */
    public LoanApplicationResultResponse getLoanApplicationResult(Member member) {
        LoanApplication loanApplication = loanApplicationRepository.findByMember(member)
                .orElseThrow(() -> new FlexrateException(ErrorCode.LOAN_NOT_FOUND));

        if(loanApplication.getStatus() == LoanApplicationStatus.PRE_APPLIED){
            throw new FlexrateException(ErrorCode.LOAN_NOT_APPLIED);
        }

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
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        List<Member> members = memberRepository.findAllWithLoanApplication();

        for (Member member : members) {
            // 관리자 또는 대출 미신청자는 건너뜀
            if (Role.ADMIN == member.getRole() || member.getLoanApplication() == null) continue;

            Interest yesterdayInterest = interestRepository
                    .findByInterestDateAndLoanApplication(yesterday, member.getLoanApplication())
                    .orElseThrow(() -> new FlexrateException(ErrorCode.NO_INTEREST));

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

        switch (goal) {
            case NO_SPENDING_TODAY:
                return dataList.stream()
                        .filter(d -> d.getCollectedAt().toLocalDate().equals(today))
                        .mapToInt(UserFinancialData::getValue)
                        .sum() == 0;

            case LIMIT_DAILY_MEAL:
                return dataList.stream()
                        .filter(d -> d.getCategory() == UserFinancialCategory.FOOD &&
                                d.getCollectedAt().toLocalDate().equals(today))
                        .mapToInt(UserFinancialData::getValue)
                        .sum() <= 10000;

            case SAVE_70_PERCENT:
                return income > 0 && (income - expense) >= income * 0.7;

            case INCOME_OVER_EXPENSE:
                return income > expense;

            case ONLY_PUBLIC_TRANSPORT:
                return dataList.stream()
                        .filter(d -> d.getCategory() == UserFinancialCategory.TRANSPORT &&
                                d.getCollectedAt().toLocalDate().equals(today))
                        .allMatch(d -> d.getCategory() == UserFinancialCategory.TRANSPORT);

            case COMPARE_BEFORE_BUYING:
                // 사용자 행동을 추적할 수 있는 로그나 추가 데이터가 있어야 평가 가능. 임의로 true로 가정
                return true;

            case HAS_HOUSING_SAVING:
                return dataList.stream()
                        .anyMatch(d -> d.getDataType() == UserFinancialDataType.INCOME);

            case CLOTHING_UNDER_100K:
                return dataList.stream()
                        .filter(d -> d.getCategory() == UserFinancialCategory.CLOTHING)
                        .allMatch(d -> d.getValue() <= 100000);

            case ONE_CATEGORY_SPEND:
                return dataList.stream()
                        .filter(d -> d.getCollectedAt().toLocalDate().equals(today))
                        .map(UserFinancialData::getCategory)
                        .distinct()
                        .count() == 1;

            case SMALL_MONTHLY_SAVE:
                int monthlySaving = dataList.stream()
                        .filter(d -> d.getDataType() == UserFinancialDataType.INCOME)
                        .mapToInt(UserFinancialData::getValue)
                        .sum();
                return monthlySaving >= 10000;

            case NO_USELESS_ELECTRONICS:
                return dataList.stream()
                        .filter(d -> d.getCategory() == UserFinancialCategory.ELECTRONICS)
                        .allMatch(d -> d.getValue() <= 100000);

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
                return todayExpense > avgExpense * 1.1;

            case NO_EXPENSIVE_DESSERT:
                return dataList.stream()
                        .filter(d -> d.getCategory() == UserFinancialCategory.CAFE)
                        .noneMatch(d -> d.getValue() > 10000);

            case NO_OVER_50K_PER_DAY:
                int dailyTotal = dataList.stream()
                        .filter(d -> d.getCollectedAt().toLocalDate().equals(today))
                        .mapToInt(UserFinancialData::getValue)
                        .sum();
                return dailyTotal <= 50000;

            case SUBSCRIPTION_UNDER_50K:
                int monthlySubscription = dataList.stream()
                        .filter(d -> d.getCategory() == UserFinancialCategory.SUBSCRIPTION)
                        .mapToInt(UserFinancialData::getValue)
                        .sum();
                return monthlySubscription <= 50000;

            case MEAL_UNDER_20K:
                return dataList.stream()
                        .filter(d -> d.getCategory() == UserFinancialCategory.FOOD)
                        .allMatch(d -> d.getValue() <= 20000);

            default:
                return false;
        }
    }


}
