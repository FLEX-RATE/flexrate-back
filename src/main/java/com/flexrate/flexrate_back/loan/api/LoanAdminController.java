package com.flexrate.flexrate_back.loan.api;

import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.loan.application.LoanAdminService;
import com.flexrate.flexrate_back.loan.dto.LoanApplicationStatusUpdateRequest;
import com.flexrate.flexrate_back.loan.dto.LoanApplicationStatusUpdateResponse;
import com.flexrate.flexrate_back.loan.dto.TransactionHistoryResponse;
import com.flexrate.flexrate_back.member.application.AdminAuthChecker;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;

import java.security.Principal;

@RestController
@RequestMapping("/api/admin/loans")
@RequiredArgsConstructor
public class LoanAdminController {
    private final LoanAdminService loanAdminService;
    private final AdminAuthChecker adminAuthChecker;

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
    public ResponseEntity<TransactionHistoryResponse> getTransactionHistory(
            @PathVariable("memberId") Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            Principal principal
    ) {
        // A007 관리자 인증 체크
        if (!adminAuthChecker.isAdmin(principal)) {
            throw new FlexrateException(ErrorCode.ADMIN_AUTH_REQUIRED);
        }

        return ResponseEntity.ok(loanAdminService.getTransactionHistory(memberId, page, size, sortBy));
    }

    /**
     * 대출 상태 변경
     * @param loanApplicationId 대출 신청 ID
     * @param request 변경할 대출 상태(status), 변경 사유(reason)
     * @return 성공 여부
     * @since 2025.05.02
     * @author 권민지
     */
    @Operation(
            summary = "대출 상태 변경", description = "관리자가 대출 신청의 상태를 변경합니다.",
            parameters = {
                    @Parameter(name = "loanApplicationId", description = "대출 신청 ID", required = true),
                    @Parameter(name = "request", description = "변경할 대출 상태 및 사유", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "대출 상태 변경 성공"),
                    @ApiResponse(responseCode = "404", description = "대출 신청을 찾을 수 없음", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\": \"L002\", \"message\": \"대출 정보를 찾을 수 없습니다.\"}"))),
                    @ApiResponse(responseCode = "400", description = "대출 상태 변경 실패", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\": \"L005\", \"message\": \"변경을 요청한 상태가 제약 조건에 위배됩니다.\"}")))
            })
    @PatchMapping("/{loanApplicationId}/status")
    public ResponseEntity<LoanApplicationStatusUpdateResponse> patchLoanApplicationStatus(
            @PathVariable("loanApplicationId") Long loanApplicationId,
            @Valid @RequestBody LoanApplicationStatusUpdateRequest request,
            Principal principal
    ) {
        // A007 관리자 인증 체크
        if (!adminAuthChecker.isAdmin(principal)) {
            throw new FlexrateException(ErrorCode.ADMIN_AUTH_REQUIRED);
        }

        return ResponseEntity.ok(loanAdminService.patchLoanApplicationStatus(loanApplicationId, request));
    }
}
