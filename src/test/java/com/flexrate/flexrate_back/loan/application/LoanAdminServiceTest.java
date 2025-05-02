package com.flexrate.flexrate_back.loan.application;

import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.loan.application.repository.LoanApplicationRepository;
import com.flexrate.flexrate_back.loan.domain.LoanApplication;
import com.flexrate.flexrate_back.loan.dto.LoanApplicationStatusUpdateRequest;
import com.flexrate.flexrate_back.loan.enums.LoanApplicationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class LoanAdminServiceTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @InjectMocks
    private LoanAdminService loanAdminService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Given 대기중 대출, When EXECUTED로 변경, Then 상태 변경 성공")
    void changeStatus_success() {
        // Given
        Long loanId = 100L;
        LoanApplication loanApp = LoanApplication.builder()
                .applicationId(loanId)
                .status(LoanApplicationStatus.PENDING)
                .build();
        when(loanApplicationRepository.findById(loanId)).thenReturn(Optional.of(loanApp));
        LoanApplicationStatusUpdateRequest request = new LoanApplicationStatusUpdateRequest(LoanApplicationStatus.EXECUTED, null);

        // When
        var response = loanAdminService.patchLoanApplicationStatus(loanId, request);

        // Then
        assertThat(response.success()).isTrue();
        assertThat(loanApp.getStatus()).isEqualTo(LoanApplicationStatus.EXECUTED);
        assertThat(response.message()).isEqualTo("대출 상태가 변경되었습니다.");
    }

    @Test
    @DisplayName("Given 없는 대출, When 상태 변경 요청, Then L002 예외 발생")
    void changeStatus_notFound() {
        // Given
        Long loanId = 999L;
        when(loanApplicationRepository.findById(loanId)).thenReturn(Optional.empty());
        LoanApplicationStatusUpdateRequest request = new LoanApplicationStatusUpdateRequest(LoanApplicationStatus.EXECUTED, null);

        // When & Then
        assertThatThrownBy(() -> loanAdminService.patchLoanApplicationStatus(loanId, request))
                .isInstanceOf(FlexrateException.class)
                .hasMessageContaining(ErrorCode.LOAN_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("Given 이미 EXECUTED, When EXECUTED로 변경, Then L005 예외 발생")
    void changeStatus_duplicate() {
        // Given
        Long loanId = 101L;
        LoanApplication loanApp = LoanApplication.builder()
                .applicationId(loanId)
                .status(LoanApplicationStatus.EXECUTED)
                .build();
        when(loanApplicationRepository.findById(loanId)).thenReturn(Optional.of(loanApp));
        LoanApplicationStatusUpdateRequest request = new LoanApplicationStatusUpdateRequest(LoanApplicationStatus.EXECUTED, null);

        // When & Then
        assertThatThrownBy(() -> loanAdminService.patchLoanApplicationStatus(loanId, request))
                .isInstanceOf(FlexrateException.class)
                .hasMessageContaining(ErrorCode.LOAN_STATUS_CONFLICT.getMessage());
    }

    @Test
    @DisplayName("Given EXECUTED, When PENDING로 변경, Then L005 예외 발생")
    void changeStatus_invalidTransition() {
        // Given
        Long loanId = 102L;
        LoanApplication loanApp = LoanApplication.builder()
                .applicationId(loanId)
                .status(LoanApplicationStatus.EXECUTED)
                .build();
        when(loanApplicationRepository.findById(loanId)).thenReturn(Optional.of(loanApp));
        LoanApplicationStatusUpdateRequest request = new LoanApplicationStatusUpdateRequest(LoanApplicationStatus.PENDING, "사유");

        // When & Then
        assertThatThrownBy(() -> loanAdminService.patchLoanApplicationStatus(loanId, request))
                .isInstanceOf(FlexrateException.class)
                .hasMessageContaining(ErrorCode.LOAN_STATUS_CONFLICT.getMessage());
    }

}
