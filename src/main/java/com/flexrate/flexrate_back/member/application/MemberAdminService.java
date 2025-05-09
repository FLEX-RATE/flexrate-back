package com.flexrate.flexrate_back.member.application;

import com.flexrate.flexrate_back.common.dto.PaginationInfo;
import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.loan.domain.LoanApplication;
import com.flexrate.flexrate_back.loan.enums.LoanApplicationStatus;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import com.flexrate.flexrate_back.member.domain.repository.MemberQueryRepository;
import com.flexrate.flexrate_back.member.dto.*;
import com.flexrate.flexrate_back.member.mapper.MemberMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberAdminService {
    private final MemberRepository memberRepository;
    private final MemberQueryRepository memberQueryRepository;
    private final MemberMapper memberMapper;

    /**
     * 관리자 권한으로 회원 목록 조회
     * @param request 검색 조건
     * @return MemberSearchResponse 회원 목록, 페이징 정보
     * @throws FlexrateException ErrorCode ADMIN_AUTH_REQUIRED 관리자 인증 필요
     * @since 2025.04.26
     * @author 권민지
     */
    public MemberSearchResponse searchMembers(@Valid MemberSearchRequest request) {
        // 기본 정렬 createdAt 내림차순
        Sort sort = request.sortBy() != null
                ? Sort.by(request.sortBy().name()).ascending()
                : Sort.by("createdAt").descending();

        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                request.page() != null ? request.page() : 0,
                request.size() != null ? request.size() : 10,
                sort
        );

        var members = memberQueryRepository.searchMembers(request, pageable);

        return MemberSearchResponse.builder()
                .paginationInfo(new PaginationInfo(
                        members.getNumber(),
                        members.getSize(),
                        members.getTotalPages(),
                        members.getTotalElements()
                ))
                .members(members.getContent().stream()
                        .map(memberMapper::toSummaryDto)
                        .toList())
                .build();
    }

    /**
     * 관리자 권한으로 회원 정보 수정
     * @param request 수정할 회원 정보
     * @return MemberSearchResponse 수정된 회원 정보
     * @throws FlexrateException ErrorCode ADMIN_AUTH_REQUIRED 관리자 인증 필요, USER_NOT_FOUND 사용자를 찾지 못함,
     * AUTH_REQUIRED_FIELD_MISSING 필수 입력값 누락
     * @since 2025.04.26
     * @author 허연규
     */
    @Transactional
    public PatchMemberResponse patchMember(Long memberId, @Valid PatchMemberRequest request) {
        // U001 유저 존재 여부 체크
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        // A000 필수 입력값 누락 체크
        if (request.name() == null && request.sex() == null
                && request.birthDate() == null && request.memberStatus() == null) {
            throw new FlexrateException(ErrorCode.AUTH_REQUIRED_FIELD_MISSING);
        }

        if (request.name() != null) {
            member.updateName(request.name());
        }
        if (request.sex() != null) {
            member.updateSex(request.sex());
        }
        if (request.birthDate() != null) {
            member.updateBirthDate(request.birthDate());
        }
        if (request.memberStatus() != null) {
            member.updateMemberStatus(request.memberStatus());
        }

        return PatchMemberResponse.builder()
                .memberId(member.getMemberId())
                .name(member.getName())
                .sex(member.getSex())
                .birthDate(member.getBirthDate())
                .memberStatus(member.getStatus())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 관리자 권한으로 회원 정보 상세 조회
     * @param memberId 조회할 회원 Id
     * @return MemberDetailSearchResponse
     * @throws FlexrateException ErrorCode ADMIN_AUTH_REQUIRED 관리자 인증 필요, USER_NOT_FOUND 사용자를 찾지 못함
     * @since 2025.04.29
     * @author 허연규
     */

    public MemberDetailResponse searchMemberDetail(Long memberId) {
        // U001 유저 존재 여부 체크
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        LoanApplication app = memberQueryRepository.findLatestLoanApplication(memberId);
        Long tsCount = memberQueryRepository.countLoanTransactions(memberId);
        float interestRate = memberQueryRepository.findLatestInterestRate(memberId);

        // 남은 대출 기간 & 전체 대출 기간 조회
        LocalDate now = LocalDate.now();
        LocalDate startDate = app.getStartDate().toLocalDate();
        LocalDate endDate = app.getEndDate().toLocalDate();

        Period period = Period.between(now, endDate);
        int leftMonths = period.getYears() * 12 + period.getMonths();
        Period loanPeriod = Period.between(startDate, endDate);
        int loanMonths = loanPeriod.getYears() * 12 + loanPeriod.getMonths();

        // 월 상환액
        float monthlyInterestRate = interestRate / 12 / 100;
        double principal = app.getRemainAmount();
        double monthlyPaymentRaw = principal *
                (monthlyInterestRate * Math.pow(1 + monthlyInterestRate, leftMonths)) /
                (Math.pow(1 + monthlyInterestRate, leftMonths) - 1);
        int monthlyPayment = (int) Math.round(monthlyPaymentRaw);

        // 총 상환 금액(월 납입금 * 전체 대출 기간)
        // <- 현재 금리가 처음부터 끝까지 적용될 때 예상 금액
        int totalPayment = monthlyPayment * loanMonths;

        // 납입일
        int repaymentDay = startDate.getDayOfMonth();

        return MemberDetailResponse.builder()
                .memberId(member.getMemberId())
                .name(member.getName())
                .sex(member.getSex().name())
                .status(member.getStatus().name())
                .birthDate(member.getBirthDate().toString())
                .createdAt(member.getCreatedAt().toString())
                .hasLoan(LoanApplicationStatus.EXECUTED.equals(app.getStatus()))
                .loanTransactionCount(tsCount != null ? tsCount.intValue() : 0)
                .consumptionType(member.getConsumptionType().name())
                .consumeGoal(member.getConsumeGoal().name())
                .interestRate(interestRate)
                .creditScore(app.getCreditScore())
                .loanStartDate(app.getStartDate().toString())
                .loanEndDate(app.getEndDate().toString())
                .loanAmount(app.getTotalAmount())
                .totalPayment(totalPayment)
                .repaymentDay(repaymentDay)
                .monthlyPayment(monthlyPayment)
                .build();
    }
}
