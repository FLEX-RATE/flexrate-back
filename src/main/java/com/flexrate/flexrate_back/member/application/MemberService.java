package com.flexrate.flexrate_back.member.application;

import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.loan.application.repository.InterestQueryRepository;
import com.flexrate.flexrate_back.loan.application.repository.LoanTransactionRepository;
import com.flexrate.flexrate_back.loan.domain.LoanApplication;
import com.flexrate.flexrate_back.loan.domain.LoanTransaction;
import com.flexrate.flexrate_back.loan.dto.MainPageResponse;
import com.flexrate.flexrate_back.loan.enums.TransactionType;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.repository.MemberQueryRepository;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import com.flexrate.flexrate_back.member.dto.ConsumeGoalResponse;
import com.flexrate.flexrate_back.member.dto.MypageResponse;
import com.flexrate.flexrate_back.member.dto.MypageUpdateRequest;
import com.flexrate.flexrate_back.member.enums.ConsumeGoal;
import com.flexrate.flexrate_back.member.enums.ConsumptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberQueryRepository memberQueryRepository;
    private final LoanTransactionRepository loanTransactionRepository;
    private final InterestQueryRepository interestQueryRepositoryImpl;
    /**
     * 마이페이지 조회
     * @param memberId 회원 ID
     * @return 회원 정보(MypageResponse) - 이름, 이메일, 소비 목표, 소비 유형
     * @since 2025.05.07
     * @author 권민지
     */
    public MypageResponse getMyPage(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        return MypageResponse.builder()
                .name(member.getName())
                .email(member.getEmail())
                .consumeGoal(member.getConsumeGoal())
                .consumptionType(member.getConsumptionType())
                .build();
    }

    /**
     * 마이페이지 정보 수정
     * @param memberId 회원 ID
     * @param request MypageUpdateRequest 요청 DTO (이메일, 소비 목표)
     * @return 수정된 회원 정보(MypageResponse) - 이름, 이메일, 소비 목표, 소비 유형
     * @since 2025.05.07
     * @author 권민지
     */
    @Transactional
    public MypageResponse updateMyPage(Long memberId, MypageUpdateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        if (request.email() != null) member.updateEmail(request.email());
        if (request.consumeGoal() != null) {
            // L011 - 소비 목표가 소비 타입과 다르다면, 소비 목표를 변경할 수 없음
            if (request.consumeGoal().getType() != member.getConsumptionType()) {
                throw new FlexrateException(ErrorCode.LOAN_CONSUMPTION_TYPE_MISMATCH);
            }

            member.updateConsumeGoal(request.consumeGoal());
        }

        return MypageResponse.builder()
                .name(member.getName())
                .email(member.getEmail())
                .consumeGoal(member.getConsumeGoal())
                .consumptionType(member.getConsumptionType())
                .build();
    }

    /**
     * 소비 유형별 소비 목표 반환
     * @param consumptionType 소비 유형
     * @return 소비 목표 list
     * @since 2025.05.07
     * @author 권민지
     */
    public ConsumeGoalResponse getConsumeGoal(ConsumptionType consumptionType) {
        return ConsumeGoalResponse.builder()
                .consumeGoals(ConsumeGoal.getConsumeGoalsByType(consumptionType))
                .build();

    }

    /**
     * 사용자의 대출 상태 조회
     * @param memberId 회원 ID
     * @return 대출 상태 (PRE_APPLIED, PENDING, REJECTED, EXECUTED, COMPLETED, NONE)
     * @since 2025.05.23
     * @author 권민지
     */
    public String getLoanStatus(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        // member의 LoanApplication이 존재하지 않으면 NONE 반환
        if (member.getLoanApplication() == null) {
            return "NONE";
        }

        return member.getLoanApplication().getStatus().name();
    }

    /**
     * 메인페이지 조회
     * @param memberId 조회할 회원id
     * @return MainPageResponse
     * @throws FlexrateException ErrorCode ADMIN_AUTH_REQUIRED 관리자 인증 필요, USER_NOT_FOUND 사용자를 찾지 못함
     * @since 2025.05.24
     * @author 유승한
     */
    public MainPageResponse getMainPage(Long memberId) {
        LoanApplication app = memberQueryRepository.findLatestLoanApplication(memberId);
        float interestRate = Optional.ofNullable(memberQueryRepository.findLatestInterestRate(memberId))
                .orElse(0.0f); // 기본값

        // 날짜 관련 계산
        LocalDate now = LocalDate.now();
        LocalDate startDate = app.getStartDate().toLocalDate();
        LocalDate endDate = app.getEndDate().toLocalDate();

        int totalMonths = Period.between(startDate, endDate).getYears() * 12 +
                Period.between(startDate, endDate).getMonths();
        int leftMonths = Period.between(now, endDate).getYears() * 12 +
                Period.between(now, endDate).getMonths();

        // 경과된 달 계산 (1부터 시작)
        int targetMonth = totalMonths - leftMonths + 1;

        // 대출 정보
        double principal = app.getRemainAmount();
        float monthlyInterestRate = interestRate / 12 / 100;

        // 월 납부액 (원리금 균등 상환 방식)
        double monthlyPaymentRaw = principal *
                (monthlyInterestRate * Math.pow(1 + monthlyInterestRate, totalMonths)) /
                (Math.pow(1 + monthlyInterestRate, totalMonths) - 1);
        int monthlyPayment = (int) Math.round(monthlyPaymentRaw);

        // 결과 변수 초기화
        double targetInterest = 0;
        double targetPrincipal = 0;
        double remainingPrincipalAfterTarget = principal;

        // 반복 계산
        for (int month = 1; month <= totalMonths; month++) {
            double interest = remainingPrincipalAfterTarget * monthlyInterestRate;
            double principalPayment = monthlyPaymentRaw - interest;
            remainingPrincipalAfterTarget -= principalPayment;

            if (month == targetMonth) {
                targetInterest = interest;
                targetPrincipal = principalPayment;
                break;
            }
        }
        // 월 이자 상환액
        int monthlyInterest = (int) Math.round(targetInterest);
        // 월 원금 상환액
        int monthlyPrincipal = (int) Math.round(targetPrincipal);
        // 상환 비율
        int repaymentRate = (int) Math.round((1f - (float) leftMonths / (float) totalMonths) * 100.0);

        // 다음 대출금 상환 예정일 : nextPaymentDate
        int repaymentDay = startDate.getDayOfMonth();
        LocalDate thisMonthPaymentDate = LocalDate.of(now.getYear(), now.getMonth(), 1)
                .withDayOfMonth(Math.min(repaymentDay, now.lengthOfMonth()));
        LocalDate nextPaymentDate;
        if (now.isAfter(thisMonthPaymentDate)) {
            // 이번 달 상환일이 지났으므로 다음 달로 계산
            LocalDate nextMonth = now.plusMonths(1);
            nextPaymentDate = LocalDate.of(nextMonth.getYear(), nextMonth.getMonth(), 1)
                    .withDayOfMonth(Math.min(repaymentDay, nextMonth.lengthOfMonth()));
        } else {
            // 이번 달 상환일이 아직 안 지남
            nextPaymentDate = thisMonthPaymentDate;
        }
        LocalDateTime recentRepaymentDate = loanTransactionRepository
                .findFirstByMember_MemberIdAndTypeOrderByOccurredAtDesc(memberId, TransactionType.REPAYMENT)
                .map(LoanTransaction::getOccurredAt)
                .orElse(null);
        // 대출금 납부 회차
        int loanRepaymentTransactionNum = loanTransactionRepository.findByMember_MemberIdAndTypeAndOccurredAtBetween(memberId, TransactionType.REPAYMENT, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX)).size();

        // 금리 변경 횟수
        int interestChangedNum = interestQueryRepositoryImpl.countByConditionsWithInterestChangedTrue(app, startDate, endDate);

        return MainPageResponse.builder()
                .monthlyPayment(monthlyPayment)
                .repaymentRate(repaymentRate)
                .monthlyPrincipal(monthlyPrincipal)
                .monthlyInterest(monthlyInterest)
                .nextPaymentDate(nextPaymentDate)
                .loanRepaymentTransactionNum(loanRepaymentTransactionNum)
                .interestChangedNum(interestChangedNum)
                .startDate(startDate)
                .recentRepaymentDate(recentRepaymentDate)
                .build();
    }


    // 회원 ID로 회원 조회
    public Member findById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));
    }

}