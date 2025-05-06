package com.flexrate.flexrate_back.member.application;

import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.loan.domain.LoanApplication;
import com.flexrate.flexrate_back.loan.enums.LoanApplicationStatus;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.repository.MemberQueryRepository;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import com.flexrate.flexrate_back.member.dto.MemberDetailResponse;
import com.flexrate.flexrate_back.member.dto.PatchMemberRequest;
import com.flexrate.flexrate_back.member.dto.PatchMemberResponse;
import com.flexrate.flexrate_back.member.enums.ConsumeGoal;
import com.flexrate.flexrate_back.member.enums.ConsumptionType;
import com.flexrate.flexrate_back.member.enums.MemberStatus;
import com.flexrate.flexrate_back.member.enums.Sex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class MemberAdminServiceTest {
    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberQueryRepository memberQueryRepository;

    @Mock
    private AdminAuthChecker adminAuthChecker;

    @InjectMocks
    private MemberAdminService memberAdminService;

    public MemberAdminServiceTest() {
        MockitoAnnotations.openMocks(this);
    }


    /**
     * 관리자 권한으로 회원 정보 수정 테스트
     * @since 2025.04.28
     * 허연규
     * */
/*
    private Member member;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .memberId(1L)
                .name("김영희")
                .birthDate(LocalDate.of(1980, 1, 1))
                .sex(Sex.FEMALE)
                .status(MemberStatus.SUSPENDED)
                .build();
    }

    @Test
    @DisplayName("MemberAdminService - 회원 수정 정상 응답 테스트")
    void patchMemberSuccess() {
        // given
        PatchMemberRequest request = PatchMemberRequest.builder()
                .name("홍길동")
                .birthDate(LocalDate.of(1990, 10, 10))
                .sex(Sex.MALE)
                .memberStatus(MemberStatus.ACTIVE)
                .build();

        when(adminAuthChecker.isAdmin(any())).thenReturn(true);
        when(memberRepository.findById(any(Long.class))).thenReturn(Optional.of(member));

        // when
        PatchMemberResponse response = memberAdminService.patchMember(1L, request, "adminToken");

        // then
        assertThat(response.getName()).isEqualTo("홍길동");
        assertThat(response.getBirthDate()).isEqualTo(LocalDate.of(1990, 10, 10));
        assertThat(response.getSex()).isEqualTo(Sex.MALE);
        assertThat(response.getMemberStatus()).isEqualTo(MemberStatus.ACTIVE);
    }

    @Test
    @DisplayName("MemberAdminService - 관리자 인증 실패")
    public void patchMemberFail_AdminTokenNotFound() {
        // given
        PatchMemberRequest request = PatchMemberRequest.builder()
                .name("홍길동")
                .birthDate(LocalDate.of(1990, 10, 10))
                .sex(Sex.MALE)
                .memberStatus(MemberStatus.ACTIVE)
                .build();

        when(adminAuthChecker.isAdmin(any())).thenReturn(false);

        // when & then
        FlexrateException exception = assertThrows(FlexrateException.class, () -> {
            memberAdminService.patchMember(1L, request, "NoToken");
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ADMIN_AUTH_REQUIRED);
    }

    @Test
    @DisplayName("MemberAdminService - 유저가 존재하지 않음")
    public void patchMemberFail_UserNotFound() {
        // given
        PatchMemberRequest request = PatchMemberRequest.builder()
                .name("홍길동")
                .birthDate(LocalDate.of(1990, 10, 10))
                .sex(Sex.MALE)
                .memberStatus(MemberStatus.ACTIVE)
                .build();

        when(adminAuthChecker.isAdmin(any())).thenReturn(true);

        // when & then
        FlexrateException exception = assertThrows(FlexrateException.class, () -> {
            memberAdminService.patchMember(2L, request, "adminToken");
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("MemberAdminService - 필수 입력값 누락")
    public void patchMemberFail_NullMember() {
        // given
        PatchMemberRequest request = PatchMemberRequest.builder().build();

        when(adminAuthChecker.isAdmin(any())).thenReturn(true);
        when(memberRepository.findById(any(Long.class))).thenReturn(Optional.of(member));

        // when & then
        FlexrateException exception = assertThrows(FlexrateException.class, () -> {
            memberAdminService.patchMember(1L, request, "adminToken");
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AUTH_REQUIRED_FIELD_MISSING);
    }*/

    /**
     * 관리자 권한으로 회원 정보 상세 조회 테스트
     * @since 2025.04.29
     * 허연규
     * */
/*
    @Test
    @DisplayName("MemberAdminService - 사용자 정보 상세 조회 성공")
    public void searchMemberDetailSuccess(){
        // given
        Long memberId = 1L;

        Member member = Member.builder()
                .memberId(memberId)
                .name("김영희")
                .sex(Sex.FEMALE)
                .status(MemberStatus.SUSPENDED)
                .birthDate(LocalDate.of(1980, 1, 1))
                .createdAt(LocalDateTime.now())
                .consumptionType(ConsumptionType.CONSERVATIVE)
                .consumeGoal(ConsumeGoal.INCOME_OVER_EXPENSE)
                .build();

        LocalDateTime now = LocalDateTime.now();
        LoanApplication app = LoanApplication.builder()
                .status(LoanApplicationStatus.EXECUTED)
                .remainAmount(1000000)
                .creditScore(750)
                .startDate(now.minusMonths(1))
                .endDate(now.plusMonths(12))
                .build();

        Double interestRate = 15.0;
        Long transactionCount = 3L;

        when(adminAuthChecker.isAdmin(anyString())).thenReturn(true);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(memberQueryRepository.findLatestLoanApplication(memberId)).thenReturn(app);
        when(memberQueryRepository.countLoanTransactions(memberId)).thenReturn(transactionCount);
        when(memberQueryRepository.findLatestInterestRate(memberId)).thenReturn(interestRate);

        // when
        MemberDetailResponse response = memberAdminService.searchMemberDetail(memberId, "adminToken");

        // then
        assertThat(response).isNotNull();

        assertThat(response.getMemberId()).isEqualTo(memberId);
        assertThat(response.getName()).isEqualTo("김영희");
        assertThat(response.getSex()).isEqualTo("FEMALE");
        assertThat(response.getStatus()).isEqualTo("SUSPENDED");
        assertThat(response.getBirthDate()).isEqualTo("1980-01-01");
        assertThat(response.getCreatedAt()).isNotNull();

        assertThat(response.getHasLoan()).isTrue();
        assertThat(response.getCreditScore()).isEqualTo(750);
        assertThat(response.getLoanTransactionCount()).isEqualTo(3);
        assertThat(response.getConsumptionType()).isEqualTo("CONSERVATIVE");
        assertThat(response.getConsumeGoal()).isEqualTo("INCOME_OVER_EXPENSE");
        assertThat(response.getInterestRate()).isEqualTo(15.0);

        assertThat(response.getMonthlyPayment()).isGreaterThan(0);
        assertThat(response.getTotalPayment()).isGreaterThan(response.getMonthlyPayment());

        LocalDate paymentDueDate = LocalDate.parse(response.getPaymentDue());
        assertThat(paymentDueDate).isNotNull();
        assertThat(paymentDueDate).isAfterOrEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("MemberAdminService - 상세 조회 관리자 인증 실패")
    public void searchMemberDetailFail_AdminTokenNotFound() {
        // given
        Long memberId = 1L;

        MemberDetailResponse mockResponse = MemberDetailResponse.builder()
                .memberId(memberId)
                .name("김영희")
                .sex("FEMALE")
                .status("SUSPENDED")
                .birthDate("1980-01-01")
                .createdAt(LocalDateTime.now().toString())
                .hasLoan(true)
                .loanTransactionCount(1)
                .consumptionType(ConsumptionType.CONSERVATIVE.name())
                .consumeGoal(ConsumeGoal.INCOME_OVER_EXPENSE.name())
                .interestRate(10.5)
                .creditScore(720)
                .totalPayment(2830000)
                .paymentDue("2025-06-01")
                .monthlyPayment(282310)
                .build();

        when(adminAuthChecker.isAdmin(any())).thenReturn(false);
        when(memberRepository.findById(any(Long.class))).thenReturn(Optional.of(member));

        // when & then
        FlexrateException exception = assertThrows(FlexrateException.class, () -> {
            memberAdminService.searchMemberDetail(1L, "NoToken");
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ADMIN_AUTH_REQUIRED);
    }

    @Test
    @DisplayName("MemberAdminService - 상세 조회 유저가 존재하지 않음")
    public void searchMemberDetailFail_UserNotFound() {
        // given
        Long nonExistentMemberId = 2L;

        when(adminAuthChecker.isAdmin(any())).thenReturn(true);
        when(memberRepository.findById(nonExistentMemberId)).thenReturn(Optional.empty());

        // when & then
        FlexrateException exception = assertThrows(FlexrateException.class, () -> {
            memberAdminService.searchMemberDetail(nonExistentMemberId, "adminToken");
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    }*/
}
