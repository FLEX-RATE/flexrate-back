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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

@Slf4j
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
        log.debug("회원 목록 조회 요청: {}", request);

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
        log.debug("회원 목록 DB 조회 완료 count={}", members.getTotalElements());

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
        log.debug("회원 정보 수정 요청: memberId={}, request={}", memberId, request);

        // U001 유저 존재 여부 체크
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> {
                    log.warn("수정 실패:\n존재하지 않는 회원 memberId={}", memberId);
                    return new FlexrateException(ErrorCode.USER_NOT_FOUND);
                });

        // A000 필수 입력값 누락 체크
        if (request.name() == null && request.sex() == null
                && request.birthDate() == null && request.memberStatus() == null) {
            log.warn("수정 실패:\n필수 입력값 누락 memberId={}", memberId);
            throw new FlexrateException(ErrorCode.AUTH_REQUIRED_FIELD_MISSING);
        }

        if (request.name() != null) {
            log.debug("이름 변경 memberId={} name={}", memberId, request.name());
            member.updateName(request.name());
        }
        if (request.sex() != null) {
            log.debug("성별 변경 memberId={} sex={}", memberId, request.sex());
            member.updateSex(request.sex());
        }
        if (request.birthDate() != null) {
            log.debug("생년월일 변경 memberId={} birthDate={}", memberId, request.birthDate());
            member.updateBirthDate(request.birthDate());
        }
        if (request.memberStatus() != null) {
            log.debug("상태 변경 memberId={} memberStatus={}", memberId, request.memberStatus());
            member.updateMemberStatus(request.memberStatus());
        }

        PatchMemberResponse response = PatchMemberResponse.builder()
                .memberId(member.getMemberId())
                .name(member.getName())
                .sex(member.getSex())
                .birthDate(member.getBirthDate())
                .memberStatus(member.getStatus())
                .updatedAt(LocalDateTime.now())
                .build();

        log.debug("회원 정보 수정 완료 memberId={}", memberId);
        return response;
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
        log.debug("회원 상세조회 요청 memberId={}", memberId);

        // U001 유저 존재 여부 체크
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> {
                    log.warn("회원 상세조회 실패:\n존재하지 않는 회원 memberId={}", memberId);
                    return new FlexrateException(ErrorCode.USER_NOT_FOUND);
                });

        log.debug("회원 정보 조회 성공 memberId={}", memberId);

        LoanApplication app = memberQueryRepository.findLatestLoanApplication(memberId);
        Long tsCount = memberQueryRepository.countLoanTransactions(memberId);
        float interestRate = memberQueryRepository.findLatestInterestRate(memberId);

        log.debug("대출/거래/이율 조회 성공 memberId={} loanAppId={}", memberId, app != null ? app.getApplicationId() : null);

        boolean hasLoan = false;
        LocalDate startDate = null;
        LocalDate endDate = null;
        Integer leftMonths = null;
        Integer monthlyPayment = null;
        Integer repaymentDay = null;
        Float usedInterestRate = null;
        Integer creditScore = null;
        Integer loanAmount = null;
        String loanStartDateStr = null;
        String loanEndDateStr = null;

        // app이 null이 아니고, 실행된 대출일 때만 계산
        if (app != null && LoanApplicationStatus.EXECUTED.equals(app.getStatus())) {
            hasLoan = true;

            // 날짜 처리
            if (app.getStartDate() != null) {
                try {
                    startDate = app.getStartDate().toLocalDate();
                    loanStartDateStr = app.getStartDate().toString();
                    repaymentDay = startDate.getDayOfMonth();
                } catch (Exception e) {
                    log.warn("startDate 파싱 실패 memberId={} error={}", memberId, e.getMessage());
                }
            }
            if (app.getEndDate() != null) {
                try {
                    endDate = app.getEndDate().toLocalDate();
                    loanEndDateStr = app.getEndDate().toString();
                } catch (Exception e) {
                    log.warn("endDate 파싱 실패 memberId={} error={}", memberId, e.getMessage());
                }
            }

            // 남은 대출 기간 및 월 상환액 계산
            if (startDate != null && endDate != null) {
                LocalDate now = LocalDate.now();
                Period period = Period.between(now, endDate);
                leftMonths = period.getYears() * 12 + period.getMonths();

                float monthlyInterestRate = interestRate / 12 / 100;
                double principal = app.getRemainAmount();

                // 월 상환액 계산 (leftMonths가 0이 아니고, 이율이 0이 아닐 때만)
                if (leftMonths > 0 && monthlyInterestRate > 0) {
                    double monthlyPaymentRaw = principal *
                            (monthlyInterestRate * Math.pow(1 + monthlyInterestRate, leftMonths)) /
                            (Math.pow(1 + monthlyInterestRate, leftMonths) - 1);
                    monthlyPayment = (int) Math.round(monthlyPaymentRaw);
                }
            }

            usedInterestRate = interestRate;
            creditScore = app.getCreditScore();
            loanAmount = app.getTotalAmount();
        }

        // 빌더에 null-safe로 값 세팅
        MemberDetailResponse.MemberDetailResponseBuilder responseBuilder = MemberDetailResponse.builder()
                .memberId(member.getMemberId())
                .name(member.getName())
                .sex(member.getSex().name())
                .status(member.getStatus().name())
                .birthDate(member.getBirthDate() != null ? member.getBirthDate().toString() : null)
                .createdAt(member.getCreatedAt() != null ? member.getCreatedAt().toString() : null)
                .hasLoan(hasLoan)
                .loanTransactionCount(tsCount != null ? tsCount.intValue() : 0)
                .consumptionType(member.getConsumptionType() != null ? member.getConsumptionType().name() : null)
                .consumeGoal(member.getConsumeGoal() != null ? member.getConsumeGoal().name() : null);

        // hasLoan이 true일 때만 대출 관련 필드 세팅 (null도 허용)
        if (Boolean.TRUE.equals(hasLoan)) {
            responseBuilder
                    .interestRate(usedInterestRate)
                    .creditScore(creditScore)
                    .loanStartDate(loanStartDateStr)
                    .loanEndDate(loanEndDateStr)
                    .loanAmount(loanAmount)
                    .paymentDue(repaymentDay)
                    .monthlyPayment(monthlyPayment);
            log.debug("대출 정보 포함 memberId={} loanAmount={}", memberId, loanAmount);
        } else {
            log.debug("대출 정보 없음 memberId={}", memberId);
        }

        return responseBuilder.build();
    }
}
