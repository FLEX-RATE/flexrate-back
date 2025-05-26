package com.flexrate.flexrate_back.loan.application;

import com.flexrate.flexrate_back.common.dto.PaginationInfo;
import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.loan.application.repository.InterestRepository;
import com.flexrate.flexrate_back.loan.application.repository.LoanAdminQueryRepository;
import com.flexrate.flexrate_back.loan.application.repository.LoanApplicationRepository;
import com.flexrate.flexrate_back.loan.application.repository.LoanTransactionQueryRepository;
import com.flexrate.flexrate_back.loan.domain.Interest;
import com.flexrate.flexrate_back.loan.domain.LoanApplication;
import com.flexrate.flexrate_back.loan.dto.*;
import com.flexrate.flexrate_back.loan.enums.LoanApplicationStatus;
import com.flexrate.flexrate_back.loan.mapper.LoanApplicationMapper;
import com.flexrate.flexrate_back.loan.mapper.LoanTransactionMapper;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import com.flexrate.flexrate_back.notification.enums.NotificationType;
import com.flexrate.flexrate_back.notification.event.NotificationEvent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoanAdminService {
    private final LoanTransactionQueryRepository loanTransactionQueryRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanTransactionMapper loanTransactionMapper;
    private final MemberRepository memberRepository;
    private final InterestRepository interestRepository;
    private final LoanAdminQueryRepository loanAdminQueryRepository;
    private final LoanApplicationMapper loanApplicationMapper;
    private final ApplicationEventPublisher eventPublisher;
    /**
     * 대출 거래 내역 목록 조회
     * @param memberId 사용자 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param sortBy 정렬 기준
     * @return 입금 내역 상세 정보(페이지네이션)
     * @since 2025.04.29
     * @author 권민지
     */
    public TransactionHistoryResponse getTransactionHistory(
            Long memberId,
            int page,
            int size,
            String sortBy
    ) {
        // U001 유저 존재 여부 체크
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        // 기본 정렬 occurredAt 내림차순
        Sort sort = sortBy != null
                ? (sortBy.equals("occurredAt") 
                    ? Sort.by(sortBy).descending() 
                    : Sort.by(sortBy).ascending())
                : Sort.by("occurredAt").descending();

        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);

        var transactionHistory = loanTransactionQueryRepository.findByMemberId(memberId, pageable);

        return TransactionHistoryResponse.builder()
                .paginationInfo(new PaginationInfo(
                        transactionHistory.getNumber(),
                        transactionHistory.getSize(),
                        transactionHistory.getTotalPages(),
                        transactionHistory.getTotalElements()
                ))
                .transactionHistories(transactionHistory.getContent().stream()
                        .map(loanTransactionMapper::toSummaryDto)
                        .toList())
                .build();
    }

    /**
     * 대출 상태 변경 updateLoanStatus
     * @param loanApplicationId 대출 신청 ID
     * @param request 변경할 대출 상태(status), 변경 사유(reason)
     * @return 성공 여부
     */
    @Transactional
    public LoanApplicationStatusUpdateResponse patchLoanApplicationStatus(
            Long loanApplicationId,
            LoanApplicationStatusUpdateRequest request) {
        log.info("대출 상태 변경 시작 - loanApplicationId={}, 요청 상태={}", loanApplicationId, request.status());
        // L002 해당 loanApplicationId 존재 여부 체크
        if (loanApplicationId == null) {
            log.warn("대출 상태 변경 실패 - loanApplicationId가 null입니다.");
            throw new FlexrateException(ErrorCode.LOAN_NOT_FOUND);
        }

        LoanApplication loanApplication = loanApplicationRepository.findById(loanApplicationId)
                .orElseThrow(() ->{
                    log.warn("대출 상태 변경 실패 - 해당 loanApplicationId({})가 존재하지 않음", loanApplicationId);
                    return new FlexrateException(ErrorCode.LOAN_NOT_FOUND);
                });

        log.info("기존 상태: {}, 변경할 상태: {}", loanApplication.getStatus(), request.status());

        // 상태 변경 전후 로그
        loanApplication.patchStatus(request.status());
        log.info("대출 상태 변경 완료 - loanApplicationId={}, 변경된 상태={}", loanApplicationId, loanApplication.getStatus());

        // 대출 승인 시 초기 금리 저장 및 loanApplication 상에 승인 반영
        if(request.status() == LoanApplicationStatus.EXECUTED){
            interestRepository.save(Interest.builder()
                    .loanApplication(loanApplication)
                    .interestDate(LocalDate.now())
                    .interestRate(loanApplication.getRate())
                    .build());

            loanApplication.patchExecutedAt();
    // 알림은 실패해도 메인 로직에 영향 안 주도록
            try {
                String content = loanApplication.getMember().getName() + NotificationType.LOAN_APPROVAL.getMessageTemplate();
                NotificationEvent notificationEvent = new NotificationEvent(
                        this,
                        loanApplication.getMember(),
                        NotificationType.LOAN_APPROVAL,
                        content
                );
                eventPublisher.publishEvent(notificationEvent);
                log.info("알림 이벤트 발행 완료 - loanApplicationId={}", loanApplicationId);
            } catch (Exception e) {
                log.error("알림 이벤트 발행 실패 - 대출 승인은 완료됨, loanApplicationId={}", loanApplicationId, e);
            }
        }


        return LoanApplicationStatusUpdateResponse.builder()
                .loanApplicationId(loanApplication.getApplicationId())
                .success(true)
                .message("대출 상태가 변경되었습니다.")
                .build();
    }

    /**
     * 대출 현황 목록 조회
     * @param request 조회할 때 원하는 필터값
     * @return 조회된 대출 목록
     * @since 2025.05.02
     * @author 허연규
     */

    public LoanAdminSearchResponse searchLoans(@Valid LoanAdminSearchRequest request) {

        Sort sort = Sort.by("appliedAt").descending();

        Pageable pageable = PageRequest.of(
                request.page() != null ? request.page() : 0,
                request.size() != null ? request.size() : 20,
                sort
        );

        Page<LoanApplication> loans = loanAdminQueryRepository.searchLoans(request, pageable);

        if (loans.isEmpty()) {
            throw new FlexrateException(ErrorCode.LOAN_NOT_FOUND);
        }

        return LoanAdminSearchResponse.builder()
                .paginationInfo(new PaginationInfo(
                        loans.getNumber(),
                        loans.getContent().size(),
                        loans.getTotalPages(),
                        loans.getTotalElements()
                ))
                .loans(loans.getContent().stream()
                        .map(loanApplicationMapper::toSummaryDto)
                        .toList())
                .build();
    }
}
