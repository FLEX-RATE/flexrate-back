package com.flexrate.flexrate_back.loan.api;

import com.flexrate.flexrate_back.loan.application.LoanAdminService;
import com.flexrate.flexrate_back.loan.dto.TransactionHistoryResponse;
import com.flexrate.flexrate_back.common.dto.PaginationInfo;
import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class LoanAdminControllerTest {

    @Mock
    private LoanAdminService loanAdminService;

    @InjectMocks
    private LoanAdminController loanAdminController;

    @Test
    @DisplayName("정상적으로 대출 거래 내역 목록을 조회한다")
    void getTransactionHistory_success() {
        // given
        long memberId = 1L;
        TransactionHistoryResponse response = TransactionHistoryResponse.builder()
                .paginationInfo(new PaginationInfo(0, 10, 1, 1))
                .transactionHistories(Collections.emptyList())
                .build();

        given(loanAdminService.getTransactionHistory(eq(memberId), anyInt(), anyInt(), anyString()))
                .willReturn(response);

        // when
        ResponseEntity<TransactionHistoryResponse> result = loanAdminController.getTransactionHistory(
                memberId, 0, 10, "date"
        );

        // then
        assertEquals(200, result.getStatusCodeValue());
        assertNotNull(result.getBody());
        assertEquals(0, result.getBody().paginationInfo().currentPage());
        assertTrue(result.getBody().transactionHistories().isEmpty());
    }

    @Test
    @DisplayName("존재하지 않는 회원일 경우 404를 반환한다")
    void getTransactionHistory_userNotFound() {
        // given
        long memberId = 999L;
        given(loanAdminService.getTransactionHistory(eq(memberId), anyInt(), anyInt(), anyString()))
                .willThrow(new FlexrateException(ErrorCode.USER_NOT_FOUND));

        // when
        FlexrateException ex = assertThrows(
                FlexrateException.class,
                () -> loanAdminController.getTransactionHistory(memberId, 0, 10, "date")
        );

        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("거래 내역이 없는 경우 빈 리스트를 반환한다")
    void getTransactionHistory_noTransactions() {
        // given
        long memberId = 2L;
        TransactionHistoryResponse response = TransactionHistoryResponse.builder()
                .paginationInfo(new PaginationInfo(0, 10, 1, 0))
                .transactionHistories(Collections.emptyList())
                .build();

        given(loanAdminService.getTransactionHistory(eq(memberId), anyInt(), anyInt(), anyString()))
                .willReturn(response);

        // when
        ResponseEntity<TransactionHistoryResponse> result = loanAdminController.getTransactionHistory(
                memberId, 0, 10, "date"
        );

        // then
        assertEquals(200, result.getStatusCodeValue());
        assertTrue(result.getBody().transactionHistories().isEmpty());
    }

    @Test
    @DisplayName("페이징/정렬 파라미터가 정상적으로 동작한다")
    void getTransactionHistory_pagingAndSorting() {
        // given
        long memberId = 1L;
        TransactionHistoryResponse response = TransactionHistoryResponse.builder()
                .paginationInfo(new PaginationInfo(1, 5, 2, 10))
                .transactionHistories(Collections.emptyList())
                .build();

        given(loanAdminService.getTransactionHistory(eq(memberId), eq(1), eq(5), eq("occurredAt")))
                .willReturn(response);

        // when
        ResponseEntity<TransactionHistoryResponse> result = loanAdminController.getTransactionHistory(
                memberId, 1, 5, "occurredAt"
        );

        // then
        assertEquals(200, result.getStatusCodeValue());
        assertEquals(1, result.getBody().paginationInfo().currentPage());
        assertEquals(5, result.getBody().paginationInfo().pageSize());
    }
}
