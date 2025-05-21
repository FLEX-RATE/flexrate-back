package com.flexrate.flexrate_back.loan.domain;

import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.loan.dto.LoanApplicationRequest;
import com.flexrate.flexrate_back.loan.dto.LoanReviewApplicationResponse;
import com.flexrate.flexrate_back.loan.enums.LoanApplicationStatus;
import com.flexrate.flexrate_back.loan.enums.LoanType;
import com.flexrate.flexrate_back.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "LoanApplication")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long applicationId;

    @OneToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne
    @JoinColumn(name ="product_id")
    private LoanProduct product;

    private LocalDateTime appliedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanApplicationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanType loanType;

    private LocalDateTime executedAt;
    private int totalAmount;
    private int remainAmount;
    private float rate;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LoanTransaction> loanTransactions = new ArrayList<>();

    private int creditScore;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @OneToMany(mappedBy = "loanApplication", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Interest> interests = new ArrayList<>();


    // 대출 상태 변경
    public void patchStatus(LoanApplicationStatus status) {
        // L005 상태 전환 제약조건 체크
        if (!isTransitionValid(this.status, status)) {
            throw new FlexrateException(ErrorCode.LOAN_STATUS_CONFLICT);
        }

        this.status = status;
    }

    /**
     * 대출 상태 전환 유효성 체크
     * 가능한 상태 전환: PENDING -> REJECTED/EXECUTED, EXECUTED -> REJECTED/COMPLETED
     * @param currentStatus 현재 대출 상태
     * @param newStatus 변경할 대출 상태
     * @return true: 유효한 상태 전환, false: 유효하지 않은 상태 전환
     */
    public boolean isTransitionValid(LoanApplicationStatus currentStatus, LoanApplicationStatus newStatus) {
        return (currentStatus == LoanApplicationStatus.PENDING && (newStatus == LoanApplicationStatus.REJECTED || newStatus == LoanApplicationStatus.EXECUTED)) ||
                (currentStatus == LoanApplicationStatus.EXECUTED && newStatus == LoanApplicationStatus.REJECTED || newStatus == LoanApplicationStatus.COMPLETED);
    }
    /**
     * 대출 심사 결과 반영
     *
     * @param response 대출심사결과
     * @return true: 유효한 상태 전환인 경우, false: 유효하지 않은 경우
     */
    public void applyReviewResult(LoanReviewApplicationResponse response) {
        this.totalAmount = response.loanLimit();
        this.remainAmount = response.loanLimit();
        this.rate = response.initialRate();
        this.creditScore = response.creditScore();
        this.appliedAt = LocalDateTime.now();
    }

    /**
     * 대출 승인 시 갱신
     *
     */
    public void patchExecutedAt() {
        this.executedAt = LocalDateTime.now();
    }


    // 신용 점수 변경
    public void patchCreditScore(int score) {
        this.creditScore = score;
    }

    // 대출 신청 정보 갱신
    public void applyLoan(LoanApplicationRequest loanApplicationRequest) {
        this.totalAmount = loanApplicationRequest.loanAmount();
        this.remainAmount = loanApplicationRequest.loanAmount();
        this.startDate = LocalDateTime.now();
        this.endDate = LocalDateTime.now().plusMonths(loanApplicationRequest.repaymentMonth());
        this.status = LoanApplicationStatus.PENDING;
    }

    /**
     * Member와 연관관계 제거
     *
     */
    public void unlinkMember() {
        if (this.member != null) {
            try {
                Field field = Member.class.getDeclaredField("loanApplication");
                field.setAccessible(true);
                field.set(this.member, null);
            } catch (Exception e) {
                throw new RuntimeException("Member에서 LoanApplication 연관 끊기 실패", e);
            }
            this.member = null;
        }
    }
}
