package com.flexrate.flexrate_back.loan.api;

import com.flexrate.flexrate_back.loan.application.LoanAdminService;
import com.flexrate.flexrate_back.loan.dto.TransactionHistoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;

@RestController
@RequestMapping("/api/admin/loans")
@RequiredArgsConstructor
public class LoanAdminController {
    private final LoanAdminService loanAdminService;

    /**
     * 대출 거래 내역 목록 조회
     * @param memberId 사용자 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param sortBy 정렬 기준
     * @return 대출 거래 내역 목록
     * @since 2025.04.26
     * @author 권민지
     */
    @Operation(summary = "대출 거래 내역 목록 조회", description = "관리자가 특정 사용자의 대출 거래 내역을 조회합니다.",
            parameters = {
                @Parameter(name = "memberId", description = "사용자 ID", required = true),
                @Parameter(name = "page", description = "페이지 번호", required = false),
                @Parameter(name = "size", description = "페이지 크기", required = false),
                @Parameter(name = "sortBy", description = "정렬 기준", required = false)
            },
            responses = {
                @ApiResponse(responseCode = "200", description = "대출 거래 내역 목록 조회 성공"),
                @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\": \"U001\", \"message\": \"사용자를 찾을 수 없습니다.\"}")))
            })
    @GetMapping("/members/{memberId}/transactions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TransactionHistoryResponse> getTransactionHistory(
            @PathVariable("memberId") Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date") String sortBy
    ) {
        return ResponseEntity.ok(loanAdminService.getTransactionHistory(memberId, page, size, sortBy));
    }
}
